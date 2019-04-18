package database

import models.WishList

trait WishlistTable {
  this: Db =>

  import config.profile.api._

  class WishLists(tag: Tag) extends Table[WishList](tag, "wish_list") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def productId = column[Long]("product_id")
    def * = (id, userId, productId) <> ((WishList.apply _).tupled, WishList.unapply)

  }

}
