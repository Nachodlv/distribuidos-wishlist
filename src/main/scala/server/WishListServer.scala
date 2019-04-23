package server

import io.grpc.{ManagedChannelBuilder, ServerBuilder}
import proto.wishlist.{AddProductRequest, WishListServiceGrpc}
import repositories.{WishListRepository, WishListUserRepository}
import service.WishListService
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

object WishListServer extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  // setup stub manager
  val stubManager = new ServiceManager
  stubManager.startConnection("0.0.0.0", 50001, "wishlist")

  val config = DatabaseConfig.forConfig[H2Profile]("db")

  // setup repositories
  val wishListRepository = new WishListRepository(config)
  val wishListUserRepository = new WishListUserRepository(config)

  // setup server
  val server = ServerBuilder.forPort(50001)
    .addService(WishListServiceGrpc.bindService(
      new WishListService(
        wishListRepository,
        wishListUserRepository,
        stubManager), ExecutionContext.global))
    .build()

  server.start()

  println("Running...")

  server.awaitTermination()
}

object ClientDemo extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  val serviceManager = new ServiceManager()
  val address = Await.ready(serviceManager.getAddress("wishlist"), Duration(5, "second")).value.get.get

  val channel = ManagedChannelBuilder.forAddress(address.get.address, address.get.port)
    .usePlaintext(true)
    .build()

  val stub = WishListServiceGrpc.stub(channel)

  println("\nAdding product...")

  val wishList = stub.addProduct(AddProductRequest(1, 2))

  print("Product added successfully")

  wishList.onComplete {
    res => println("\nProduct id in the response: " + res.get.productId)
  }

  System.in.read()
}
