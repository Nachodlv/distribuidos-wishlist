package server

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import io.grpc.ServerBuilder
import proto.wishlist.WishListServiceGrpc
import repositories.{WishListRepository, WishListUserRepository}
import service.WishListService
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object WishListServer extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  // root is the default value for user and password
  val user: String = args.lift(0).getOrElse("root")
  val password: String = args.lift(1).getOrElse("root")

  // setup stub manager
  /*val stubManager = new ServiceManager
  stubManager.startConnection("0.0.0.0", 50001, "wishlist")*/

  val config: Config = ConfigFactory.load("db")
  val url: String = s"jdbc:mysql://localhost:3306/test?user=$user&password=$password"
  val newConfig = config.withValue("db.db.url", ConfigValueFactory.fromAnyRef(url))

  val databaseConfig = DatabaseConfig.forConfig[MySQLProfile]("db", newConfig)

  // setup repositories
  val wishListRepository = new WishListRepository(databaseConfig)
  val wishListUserRepository = new WishListUserRepository(databaseConfig)

  // setup server
  val server = ServerBuilder.forPort(50001)
    .addService(WishListServiceGrpc.bindService(
      new WishListService(
        wishListRepository,
        wishListUserRepository,
        new StubManager), ExecutionContext.global))
    .build()

  server.start()

  println("Running...")

  server.awaitTermination()
}

/*object ClientDemo extends App {

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
}*/
