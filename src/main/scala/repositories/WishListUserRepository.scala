package repositories

import com.google.protobuf.timestamp.Timestamp
import database.{Db, WishListTable}
import models.{WishList, WishListUser}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile
import slick.lifted

import scala.concurrent.Future

class WishListUserRepository(val config: DatabaseConfig[H2Profile])
  extends Db with WishListTable {

  import config.profile.api._

  private final val RECENT_THRESHOLD = 86400 //se considera reciente si se updateo hace menos de un dia (86400 seg)

  private val wishList = lifted.TableQuery[WishListUsers]
  db.run(DBIO.seq(wishList.schema.create))

  // TODO test if timestamp works
  def refreshUser(userId: Long): Future[Int] = db.run {
    wishList.insertOrUpdate(WishListUser(userId, Timestamp.SECONDS_FIELD_NUMBER))
  }

  def getRecentUsers():Future[Seq[Long]] = db.run {
    wishList.filter( _.lastUpdate - Timestamp.SECONDS_FIELD_NUMBER >= RECENT_THRESHOLD).map(_.userId).result
  }
}
