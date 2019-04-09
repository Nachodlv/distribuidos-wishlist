package routers

import product.user.{AddProductRequest, AddProductResponse, AddUserRequest, AddUserResponse, DeleteProductRequest, DeleteProductResponse, GetProductsRequest, GetProductsResponse, UserServiceGrpc}
import repositories.{UserRepository, WishListRepository}

import scala.concurrent.{ExecutionContext, Future}

class UserService(wishListRepo: WishListRepository, userRepo: UserRepository)(implicit ec: ExecutionContext) extends UserServiceGrpc.UserService  {


  override def addProduct(in: AddProductRequest): Future[AddProductResponse] = {
    wishListRepo.addProduct(in.userId, in.productId) map {
      w => AddProductResponse(w.productId)
    }
    //how can I manage a failure? use option?
  }

  //TODO managed in controller
  override def getProducts(in: GetProductsRequest): Future[GetProductsResponse] = {
    /*repo.getProducts(in.userId) map {
      w => AddProductResponse(w)
    }*/
    return null
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
}

case object UserNotFoundException extends RuntimeException
