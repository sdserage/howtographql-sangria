package com.howtographql.scala.sangria

import sangria.schema.{Field, ListType, ObjectType}
import models._
import sangria.execution.deferred.{DeferredResolver, Fetcher, HasId}
import sangria.schema._
import sangria.macros.derive._

object GraphQLSchema {
//  val LinkType = ObjectType[Unit, Link]( // Define a 'LinkType' for Link objects in the GQL schema
//    "Link",
//    fields[Unit, Link](
//      Field("id", IntType, resolve = _.value.id),
//      Field("url", StringType, resolve = _.value.url),
//      Field("description", StringType, resolve = _.value.description)
//    )
//  )
  implicit val LinkType = deriveObjectType[Unit, Link]() // same as above but using the macros.derive
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