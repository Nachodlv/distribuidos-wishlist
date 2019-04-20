package server

import io.grpc.{ManagedChannelBuilder, ServerBuilder}
import proto.wishlist.{AddProductRequest, WishListServiceGrpc}
import repositories.{UserRepository, WishListRepository}
import service.WishListService
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

object WishListServer extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  // setup stub manager
  val stubManager = new ServiceManager
  stubManager.startConnection("0.0.0.0", 50001, "wishlist")

  val config = DatabaseConfig.forConfig[H2Profile]("db")

  // setup repositories
  val wishListRepository = new WishListRepository(config)
  val userRepository = new UserRepository(config)

  // setup server
  val server = ServerBuilder.forPort(50001)
    .addService(WishListServiceGrpc.bindService(
      new WishListService(
        wishListRepository,
        stubManager), ExecutionContext.global))
    .build()

  server.start()
  println("Running...")

  server.awaitTermination()
}

object ClientDemo extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val channel = ManagedChannelBuilder.forAddress("localhost", 50001).build()

  val stub = WishListServiceGrpc.stub(channel)

  val wishList = stub.addProduct(AddProductRequest(1, 2))

  wishList.onComplete { res => println(res) }

  val stubManager = new ServiceManager()

  stubManager.getAddress("wishlist").onComplete {
    case Success(value) => println(value.get.port)
    case Failure(exception) => println(exception)
  }

  System.in.read()
}
