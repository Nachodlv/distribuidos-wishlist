package service

import io.grpc.{ManagedChannel, ManagedChannelBuilder, Status, StatusRuntimeException}
import proto.product.{ProductRequest, ProductServiceGrpc}
import proto.wishlist._
import repositories.{WishListRepository, WishListUserRepository}
import server.ServiceManager

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class WishListService(wishListRepository: WishListRepository,
                      wishListUserRepository: WishListUserRepository,
                      serviceManager: ServiceManager)
                     (implicit ec: ExecutionContext) extends WishListServiceGrpc.WishListService  {

  override def addProduct(request: AddProductRequest): Future[AddProductResponse] = {
    //ignore rows affected returned
    wishListUserRepository.refreshUser(request.userId)

    wishListRepository.addProduct(request.userId, request.productId) map {
      wishList => AddProductResponse(wishList.productId)
    }
  }

  override def getProducts(request: GetProductsRequest): Future[GetProductsResponse] = {
    getProductStub.flatMap(stub => {
      val result = wishListRepository
        .getProducts(request.userId)
        .map(ids => ids.map(id => stub.getProduct(ProductRequest(id))))
        .flatMap(r => Future.sequence(r))

      /* TODO no blocking */
      val future = Await.ready(result, Duration.apply(5, "second")).value.get

      future match {
        case Success(value) => Future.successful(GetProductsResponse(value))
        case Failure(exception: StatusRuntimeException) =>
          if(exception.getStatus.getCode == Status.Code.UNAVAILABLE) {
            println("Get another stub")
            getProducts(request)
          } else throw exception
      }
    })
  }

  override def getRecentUsers(request: GetRecentUsersRequest): Future[GetRecentUsersResponse] = {
    wishListUserRepository.getRecentUsers().map(ids => GetRecentUsersResponse(ids))
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

