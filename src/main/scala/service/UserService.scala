package service

import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import product.product.{ProductReply, ProductRequest, ProductServiceGrpc}
import product.user.{AddProductRequest, AddProductResponse, AddUserRequest, AddUserResponse, DeleteProductRequest, DeleteProductResponse, GetProductsRequest, GetProductsResponse, PingReply, PingRequest, UserServiceGrpc}
import repositories.{UserRepository, WishListRepository}
import server.ServiceManager

import scala.concurrent.{ExecutionContext, Future}

class UserService(wishListRepo: WishListRepository, userRepo: UserRepository, serviceManager: ServiceManager)(implicit ec: ExecutionContext) extends UserServiceGrpc.UserService  {

  override def addProduct(in: AddProductRequest): Future[AddProductResponse] = {
    wishListRepo.addProduct(in.userId, in.productId) map {
      w => AddProductResponse(w.productId)
    }
    //how can I manage a failure? use option?
  }

  override def getProducts(in: GetProductsRequest): Future[GetProductsResponse] = {
    getProductStub.flatMap(stub => {
      val result: Future[Seq[Future[ProductReply]]] =
        wishListRepo.getProducts(in.userId).map(ids => ids.map(id => stub.getProduct(ProductRequest(id))))
      val results2: Future[Seq[ProductReply]] = result.flatMap(r => Future.sequence(r))
      results2.map(products =>  GetProductsResponse(products))
    })
  }

  override def deleteProduct(in: DeleteProductRequest): Future[DeleteProductResponse] = {
    wishListRepo.deleteProduct(in.userId, in.productId).map{
      id => DeleteProductResponse(id)
    }
    //how can I manage a failure? use option?
  }

  override def addUser(request: AddUserRequest): Future[AddUserResponse] = {
    userRepo.create(request.firstName, request.lastName).map(u => AddUserResponse(u.id))
  }

  //implementar isActive (solo recibe el request y devuelve un reply con un string)
  @Deprecated
  override def isActive(request: PingRequest): Future[PingReply] = {
    Future.successful(PingReply("active"))
  }

  private def getProductStub = {
    serviceManager.getAddress("product").map{
        case Some(value) =>
          val channel: ManagedChannel = ManagedChannelBuilder.forAddress(value.address, value.port)
            .usePlaintext(true)
            .build()
          ProductServiceGrpc.stub(channel)
        case None => throw new RuntimeException("No product services running")
      }
  }
}

case object UserNotFoundException extends RuntimeException
