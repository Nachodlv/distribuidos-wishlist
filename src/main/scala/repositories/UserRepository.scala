package repositories

import database.{Db, UserTable}
import models.User
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile
import slick.lifted

import scala.concurrent.Future

class UserRepository (val config: DatabaseConfig[H2Profile])
  extends Db with UserTable {

  import config.profile.api._

  private val users = lifted.TableQuery[Users]
  db.run(DBIO.seq(users.schema.create))

  def create(firstName: String, lastName: String): Future[User] = db.run (
    (users.map(u => (u.firstName, u.lastName))
      returning users.map(_.id)
      into ((data, id) => User(id, data._1, data._2))
      ) += (firstName, lastName)
  )

  def list(): Future[Seq[User]] = db.run (
    users.result
  )

  def getById(id: Long): Future[Option[User]] = db.run (
    users.filter{_.id === id}.result.headOption
  )
}
