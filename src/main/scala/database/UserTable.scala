package database

import models.{Product, User}

trait UserTable {
  this: Db =>

  import config.profile.api._

  class Users(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def * = (id, firstName, lastName) <> ((User.apply _).tupled, User.unapply)
  }

}
