package repositories

import com.sun.org.apache.xpath.internal.operations.Bool
import database.{Db, UserTable, WishlistTable}
import models.WishList
import org.h2.tools.SimpleResultSet.SimpleArray
import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, JdbcProfile}
import slick.lifted
import slick.lifted.{QueryBase, TableQuery}

import scala.concurrent.Future

class WishListRepository (val config: DatabaseConfig[H2Profile])
  extends Db with WishlistTable {

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

  //Por que no devuelve un Long? TODO me devuelve el id de la fila, no el del producto
  def deleteProduct(userId: Long, productId: Long): Future[Int] = db.run {
    wishList.filter( w => w.productId === productId && w.userId === userId).delete
  }
}
