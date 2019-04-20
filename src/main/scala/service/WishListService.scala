package service

import io.grpc.{ManagedChannel, ManagedChannelBuilder, Status, StatusRuntimeException}
import proto.product.{ProductReply, ProductRequest, ProductServiceGrpc}
import proto.wishlist.{AddProductRequest, AddProductResponse, DeleteProductRequest, DeleteProductResponse, GetProductsRequest, GetProductsResponse, WishListServiceGrpc}
import repositories.WishListRepository
import server.ServiceManager

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class WishListService(wishListRepository: WishListRepository,
                      serviceManager: ServiceManager)
                     (implicit ec: ExecutionContext) extends WishListServiceGrpc.WishListService  {

  override def addProduct(request: AddProductRequest): Future[AddProductResponse] = {
    wishListRepository.addProduct(request.userId, request.productId) map {
      w => AddProductResponse(w.productId)
    }
  }

  override def getProducts(request: GetProductsRequest): Future[GetProductsResponse] = {
    getProductStub.flatMap(stub => {
      val result = wishListRepository.getProducts(request.userId).map(ids => ids.map(id => stub.getProduct(ProductRequest(id))))

      val results2: Future[Seq[ProductReply]] = result.flatMap(r => Future.sequence(r))

      /* TODO ver como hacerlo no blocking */
      val future = Await.ready(results2, Duration.apply(5, "second")).value.get

      future  match {
        case Success(value) => Future.successful(GetProductsResponse(value))
        case Failure(exception: StatusRuntimeException) =>
          if(exception.getStatus.getCode == Status.Code.UNAVAILABLE) {
            println("Get another stub")
            getProducts(request)
          } else throw exception
      }
    })
  }

  override def deleteProduct(request: DeleteProductRequest): Future[DeleteProductResponse] = {
    wishListRepository.deleteProduct(request.userId, request.productId).map{
      id => DeleteProductResponse(id)
    }
  }

  private def getProductStub: Future[ProductServiceGrpc.ProductServiceStub] = {
    serviceManager.getAddress("product").map{
      case Some(value) =>
        val channel: ManagedChannel = ManagedChannelBuilder.forAddress(value.address, value.port).build()
        ProductServiceGrpc.stub(channel)
      case None => throw new RuntimeException("No product services running")
    }
  }
}

case object UserNotFoundException extends RuntimeException

