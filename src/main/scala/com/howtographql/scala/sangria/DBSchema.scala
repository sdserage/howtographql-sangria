package com.howtographql.scala.sangria

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps
import com.howtographql.scala.sangria.models._
import java.sql.Timestamp
import akka.http.scaladsl.model.DateTime


object DBSchema {

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )

  class LinksTable(tag: Tag) extends Table[Link](tag, "LINKS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def url = column[String]("URL")
    def description = column[String]("DESCRIPTION")
    def createdAt = column[DateTime]("CREATED_AT")

    def * = (id, url, description, createdAt).mapTo[Link]
  }

  class UsersTable(tag: Tag) extends Table[User](tag, _tableName = "USERS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def email = column[String]("EMAIL", O.Unique)
    def password = column[String]("PASSWORD")
    def createdAt = column[DateTime]("CREATED_AT")

    def * = (id, name, email, password, createdAt).mapTo[User]
  }

  class VotesTable(tag: Tag) extends Table[Vote](tag, _tableName = "VOTES") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt = column[DateTime]("CREATED_AT")
    def userId = column[Int]("USER_ID")
    def linkId = column[Int]("LINK_ID")

    def * = (id, createdAt, userId, linkId).mapTo[Vote]
  }

  val Links = TableQuery[LinksTable]
  val Users = TableQuery[UsersTable]
  val Votes = TableQuery[VotesTable]

  /**
    * Load schema and populate sample data withing this Sequence od DBActions
    */
  val databaseSetup = DBIO.seq(
    Links.schema.create,
    Users.schema.create,
    Votes.schema.create,

    Links forceInsertAll Seq(
      Link(1, "http://howtographql.com", "Awesome community driven GraphQL tutorial", DateTime(2017,9,12)),
      Link(2, "http://graphql.org", "Official GraphQL web page", DateTime(2017,10,1)),
      Link(3, "https://facebook.github.io/graphql/", "GraphQL specification", DateTime(2017,10,2))
    ),

    Users forceInsertAll Seq(
      User(1, "mario", "mario@example.com", "s3cr3t", DateTime(2017,10,1)),
      User(2, "Fred", "fred@flinstones.com", "wilmalove", DateTime(2017,10,1))
    ),

    Votes forceInsertAll Seq(
      Vote(id = 1, userId = 1, linkId = 1, createdAt = DateTime(2017,10,2)),
      Vote(id = 2, userId = 1, linkId = 2, createdAt = DateTime(2017,10,2)),
      Vote(id = 3, userId = 1, linkId = 3, createdAt = DateTime(2017,10,2)),
      Vote(id = 4, userId = 2, linkId = 2, createdAt = DateTime(2017,10,2)),
    ),
  )


  def createDatabase: DAO = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(databaseSetup), 10 seconds)

    new DAO(db)

  }

}
