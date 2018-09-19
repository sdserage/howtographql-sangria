package com.howtographql.scala.sangria

import sangria.schema.{Field, ListType, ObjectType}
import models._
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

  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks)
    )
  )

  val SchemaDefinition = Schema(QueryType)
}