package server

import com.google.gson.Gson
import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import org.etcd4s.pb.etcdserverpb.{LeaseGrantRequest, LeaseKeepAliveRequest, LeaseKeepAliveResponse, PutRequest, PutResponse}
import org.etcd4s.{Etcd4sClient, Etcd4sClientConfig}

import scala.concurrent.Future
import scala.util.Random

class ServiceManager {
  import scala.concurrent.ExecutionContext.Implicits.global

  /*The address client is the address where etcd is running*/
  val addressClient = "127.0.0.1"
  val addressPort = 2379

  /*The ttl is the time to live for the key in seconds*/
  val ttl = 1
  private val client = getClient

  /** Adds the service to etcd
    *
    * Adds a key/value to etcd where the key is the url and value is the address with the port. The service keeps alive
    * the key so when it dies the key is removed.
    */
  def startConnection(address: String, port: Int, url: String): Future[PutResponse] = {
    val id = Random.nextLong()
//    The minimum ttl a lease can receive is 2 seconds. When a minor ttl is granted, it is automatically set to 2.
    val response = client.rpcClient.leaseRpc.leaseGrant(LeaseGrantRequest(ttl, id))
    val future: Future[PutResponse] = response.flatMap(v => {
      println(v.tTL)
      client.rpcClient.kvRpc
        .put(PutRequest(
          stringToByteString(url + "/" + id),
          stringToByteString(new Gson().toJson(AddressWithPort(address, port))),
          v.iD,
          prevKv = false,
          ignoreValue = false,
          ignoreLease = false))
    })
    val request: StreamObserver[LeaseKeepAliveRequest] = client.rpcClient.leaseRpc.leaseKeepAlive(new KeepAliveObserver)
    keepAlive(id, request)
    future
  }

  /** Gets the address and port for the specific url.
    *
    * Gets all the keys matching the url and choose a random value to return.
    */
  def getAddress(url: String): Future[Option[AddressWithPort]] = {
    val future = client.kvService.getRange(url).map(res => {
      val quantity = res.count
      if(quantity > 0)
        Option(new Gson()
          .fromJson(res.kvs(Random.nextInt(res.count.toInt)).value.toStringUtf8, classOf[AddressWithPort]))
      else None
    })
    future
  }

  private def getClient = {
    val config = Etcd4sClientConfig(
      address = addressClient,
      port = addressPort
    )
    Etcd4sClient.newClient(config)
  }

  private def stringToByteString(string: String): ByteString = {
    import com.google.protobuf.ByteString
    ByteString.copyFrom(string.getBytes())
  }

  /** Keeps alive the key
    *
    * Keeps alive the key with recursion. The method calls himself every ttl so the key is not removed.
    * */
  private def keepAlive(id: Long, request: StreamObserver[LeaseKeepAliveRequest]): Unit = {
    request.onNext(LeaseKeepAliveRequest(id))
    Future {
      Thread.sleep(ttl * 1000)
      keepAlive(id, request)
    }

  }
}

case class AddressWithPort(address: String, port: Int)

class KeepAliveObserver extends StreamObserver[LeaseKeepAliveResponse] {
  override def onNext(value: LeaseKeepAliveResponse): Unit = Unit

  override def onError(t: Throwable): Unit = throw t

  override def onCompleted(): Unit = println("Completed")
}
