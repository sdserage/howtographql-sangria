package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime
import sangria.execution.deferred.HasId
import sangria.validation.Violation

package object models {
  case class Link(id: Int, url: String, description: String, postedBy: Int, createdAt: DateTime = DateTime.now) extends Identifiable

  case object DateTimeCoerceViolation extends Violation {
    override def errorMessage: String = "Error during parsing DateTime"
  }

  case class User(id: Int, name: String, email: String, password: String, createdAt: DateTime = DateTime.now) extends Identifiable

  case class Vote(id: Int, createdAt: DateTime = DateTime.now, userId: Int, linkId: Int) extends Identifiable

  trait Identifiable {
    val id: Int
  }

  object Identifiable {
    implicit def hasId[T <: Identifiable]: HasId[T, Int] = HasId(_.id)
  }

  case class AuthProviderEmail(email: String, password: String)

  case class AuthProviderSignupData(email: AuthProviderEmail)
}
