package service

import product.product.ProductServiceGrpc.ProductServiceStub
import product.product.{ProductReply, ProductRequest}
import product.user.{AddProductRequest, AddProductResponse, AddUserRequest, AddUserResponse, DeleteProductRequest, DeleteProductResponse, GetProductsRequest, GetProductsResponse, UserServiceGrpc}
import repositories.{UserRepository, WishListRepository}

import scala.concurrent.{ExecutionContext, Future}

class UserService(wishListRepo: WishListRepository, userRepo: UserRepository, stub: ProductServiceStub)(implicit ec: ExecutionContext) extends UserServiceGrpc.UserService  {

  override def addProduct(in: AddProductRequest): Future[AddProductResponse] = {
    wishListRepo.addProduct(in.userId, in.productId) map {
      w => AddProductResponse(w.productId)
    }
    //how can I manage a failure? use option?
  }

  override def getProducts(in: GetProductsRequest): Future[GetProductsResponse] = {
    val result: Future[Seq[Future[ProductReply]]] =
      wishListRepo.getProducts(in.userId).map(ids => ids.map(id => stub.getProduct(ProductRequest(id))))
    val results2: Future[Seq[ProductReply]] = result.flatMap(r => Future.sequence(r))
    results2.map(products =>  GetProductsResponse(products))
  }

  override def addUser(request: AddUserRequest): Future[AddUserResponse] = {
    userRepo.create(request.firstName, request.lastName).map(u => AddUserResponse(u.id))
  }

  override def deleteProduct(request: DeleteProductRequest): Future[DeleteProductResponse] = ???
}

case object UserNotFoundException extends RuntimeException
