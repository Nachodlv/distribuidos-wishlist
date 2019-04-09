package server

import io.grpc.{ManagedChannelBuilder, ServerBuilder}
import product.user.{AddProductRequest, AddUserRequest, UserServiceGrpc}
import repositories.{UserRepository, WishListRepository}
import routers.UserService
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object WishListServer extends App{

  implicit val ec = ExecutionContext.global

  val config = DatabaseConfig.forConfig[H2Profile]("db")
  val wishListRepo = new WishListRepository(config)
  val userRepo = new UserRepository(config);

  val server = ServerBuilder.forPort(50000)
    .addService(UserServiceGrpc.bindService(new UserService(wishListRepo, userRepo), ExecutionContext.global))
    .build()

  server.start()
  println("Running...")

  server.awaitTermination()
}

object ClientDemo extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val channel = ManagedChannelBuilder.forAddress("localhost", 50000)
    .usePlaintext(true)
    .build()

  val stub = UserServiceGrpc.stub(channel)

  val result = stub.addUser(AddUserRequest("eduardo", "scolaro"))

  result.onComplete { r =>
    stub.addProduct(AddProductRequest(1, r.get.userId)).onComplete(r2 => {
      println(r2.get.productId)
      println(r.get.userId)
      println("completed")
    })
  }

  System.in.read()
}
