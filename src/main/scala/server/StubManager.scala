package server

import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import proto.product.ProductServiceGrpc
import proto.wishlist.WishListServiceGrpc

class StubManager {

  private def productChannel: ManagedChannel = ManagedChannelBuilder.forAddress("product", 50000)
    .usePlaintext(true)
    .build()

  def productStub: ProductServiceGrpc.ProductServiceStub = ProductServiceGrpc.stub(productChannel)

}
