package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime
import sangria.schema.{Field, ListType, ObjectType}
import models._
import sangria.execution.deferred.{DeferredResolver, Fetcher, HasId}
import sangria.schema._
import sangria.macros.derive._
import sangria.ast.StringValue

object GraphQLSchema {
  implicit val GraphQLDateTime = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _) => DateTime.fromIsoDateTimeString(dt).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String => DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    }
  )
  val LinkType = deriveObjectType[Unit, Link](
    ReplaceField("createdAt", Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt))
  )

  implicit val linkHasId = HasId[Link, Int](_.id)

  val linksFetcher = Fetcher(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getLinks(ids)
  )

  val Resolver = DeferredResolver.fetchers(linksFetcher)

  // "drying" up the code
  val Id = Argument("id", IntType)
  val Ids = Argument("ids", ListInputType(IntType))

  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field(
        "link",
        OptionType(LinkType),
        arguments = Id :: Nil, //List(Argument("id", IntType)),
        resolve = c => linksFetcher.deferOpt(c.arg(Id))
      ),
      Field(
        "links",
        ListType(LinkType),
        arguments = Ids :: Nil, //List(Argument("ids", ListInputType(IntType))),
        resolve = c => linksFetcher.deferSeq(c.arg(Ids))
      ),
    )
  )

  val SchemaDefinition = Schema(QueryType)
}