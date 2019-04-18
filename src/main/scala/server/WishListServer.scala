package server

import io.grpc.{ManagedChannel, ManagedChannelBuilder, ServerBuilder}
import product.product.ProductServiceGrpc
import product.user.{AddProductRequest, AddUserRequest, DeleteProductRequest, GetProductsRequest, UserServiceGrpc}
import repositories.{UserRepository, WishListRepository}
import service.UserService
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object WishListServer extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val config = DatabaseConfig.forConfig[H2Profile]("db")
  val wishListRepo = new WishListRepository(config)
  val userRepo = new UserRepository(config)

  val channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 50000)
    .usePlaintext(true)
    .build()
  val stub: ProductServiceGrpc.ProductServiceStub = ProductServiceGrpc.stub(channel)

  val server = ServerBuilder.forPort(50001)
    .addService(UserServiceGrpc.bindService(new UserService(wishListRepo, userRepo, stub), ExecutionContext.global))
    .build()

  server.start()
  println("Running...")

  server.awaitTermination()
}

//object ClientDemo extends App {
//
//  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
//
//  val channel = ManagedChannelBuilder.forAddress("localhost", 50001)
//    .usePlaintext(true)
//    .build()
//
//  val stub = UserServiceGrpc.stub(channel)
//
//  val user = stub.addUser(AddUserRequest("eduardo", "scolaro"))
//  user.onComplete { r =>
//    stub.addProduct(AddProductRequest(1, r.get.userId)).onComplete(r2 => {
//      println(r2.get.productId)
//      stub.deleteProduct(DeleteProductRequest(r.get.userId, r2.get.productId)).onComplete(r4 => {
//        println(r4)
//        println("completed")
//      })
//    })
//  }
//
//  System.in.read()
//}
