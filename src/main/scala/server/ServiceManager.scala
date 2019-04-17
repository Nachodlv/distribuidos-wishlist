package server


import com.google.gson.Gson
import org.etcd4s.pb.mvccpb.KeyValue
import org.etcd4s.{Etcd4sClient, Etcd4sClientConfig}

import scala.concurrent.Future
import scala.util.Random

class ServiceManager {
  import scala.concurrent.ExecutionContext.Implicits.global

  // Addres and port of the etcd server
  val addressClient = "127.0.0.1"
  val addressPort = 2379

  def startConnection(address: String, port: Int, url: String): Future[Option[KeyValue]] = {
    val client = getClient
    val future = client.kvService.setKey(url, new Gson().toJson(AddressWithPort(address, port)))
    future.onComplete(_ => client.shutdown())
    future
  }

  def getAddress(url: String): Future[Option[AddressWithPort]] = {
    val client = getClient
    val future = client.kvService.getRange(url).map(res => {
      val quantity = res.count
      if(quantity > 0)
        Option(new Gson()
          .fromJson(res.kvs(Random.nextInt(res.count.toInt)).value.toStringUtf8, classOf[AddressWithPort]))
      else None
    })
    future.onComplete(_ => client.shutdown())
    future
  }

  private def getClient = {
    val config = Etcd4sClientConfig(
      address = addressClient,
      port = addressPort
    )
    Etcd4sClient.newClient(config)
  }
}

case class AddressWithPort(address: String, port: Int)
