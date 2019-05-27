package database

import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile

trait Db {
  val config: DatabaseConfig[MySQLProfile]
  val db: MySQLProfile#Backend#Database = config.db
}
