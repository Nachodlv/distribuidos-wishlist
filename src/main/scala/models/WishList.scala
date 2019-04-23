package models


case class WishList(id: Long, userId: Long, productId: Long)

case class WishListUser(userId: Long, lastUpdate: Int)

