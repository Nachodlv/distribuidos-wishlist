package repositories

import database.{Db, WishListTable}
import models.WishList
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile
import slick.lifted

import scala.concurrent.Future

class WishListRepository (val config: DatabaseConfig[MySQLProfile])
  extends Db with WishListTable {

  import config.profile.api._

  private val wishList = lifted.TableQuery[WishLists]
  db.run(DBIO.seq(wishList.schema.create))

  def addProduct(userId: Long, productId: Long): Future[WishList] = db.run {
    (wishList.map(u => (u.userId, u.productId))
      returning wishList.map(_.id)
      into ((data, id) => WishList(id, data._1, data._2))
      ) += (userId, productId)
  }

  def getProducts(userId: Long): Future[Seq[Long]] = db.run {
    wishList.filter{
      _.userId === userId
    }.map{_.productId}.result
  }

  def deleteProduct(userId: Long, productId: Long): Future[Int] = db.run {
    wishList.filter(w => w.productId === productId && w.userId === userId).delete
  }
}
