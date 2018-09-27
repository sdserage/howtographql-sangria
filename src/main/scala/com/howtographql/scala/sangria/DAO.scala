package com.howtographql.scala.sangria

import DBSchema._
import slick.jdbc.H2Profile.api._
import com.howtographql.scala.sangria.models._
import scala.concurrent.Future

class DAO(db: Database) {
  def allLinks = db.run(Links.result)
  def allUsers = db.run(Users.result)
  def allVotes = db.run(Votes.result)

//  def getLink(id: Int): Future[Option[Link]] = db.run(
//    Links.filter(_.id === id).result.headOption
//  )

  def getLinks(ids: Seq[Int]) = db.run(
    Links.filter(_.id inSet ids).result
  )

  def getUsers(ids: Seq[Int]) = db.run(
    Users.filter(_.id inSet ids).result
  )

  def getVotes(ids: Seq[Int]) = db.run(
    Votes.filter(_.id inSet ids).result
  )
}
