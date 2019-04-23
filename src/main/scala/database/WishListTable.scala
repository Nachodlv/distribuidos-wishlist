package database

import models.{WishList, WishListUser}

trait WishListTable {
  this: Db =>

  import config.profile.api._

  class WishLists(tag: Tag) extends Table[WishList](tag, "wish_list") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def productId = column[Long]("product_id")
    def * = (id, userId, productId) <> ((WishList.apply _).tupled, WishList.unapply)

  }

  class WishListUsers(tag: Tag) extends Table[WishListUser](tag, "wish_list_user") {

    def userId = column[Long]("user_id", O.PrimaryKey)
    def lastUpdate = column[Int]("last_update")
    def * = (userId, lastUpdate) <> ((WishListUser.apply _).tupled, WishListUser.unapply)

  }

}
