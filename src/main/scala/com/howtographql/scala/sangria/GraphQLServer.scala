package com.howtographql.scala.sangria

import akka.http.scaladsl.server.Route
import sangria.parser.QueryParser
import spray.json.{JsObject, JsString, JsValue}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import akka.http.scaladsl.server._
import sangria.ast.Document
import sangria.execution._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.marshalling.sprayJson._

object GraphQLServer {

  private val dao = DBSchema.createDatabase // Get access to DB

  def endpoint(requestJSON: JsValue)(implicit ec: ExecutionContext): Route = { // endpoint that returns a Route, expects a JSON object

    val JsObject(fields) = requestJSON // Main JSON object extracted from the root object, has three children: query, variables, and operationName

    val JsString(query) = fields("query") // Extracting the query

    QueryParser.parse(query) match { // Parsing the query
      case Success(queryAst) => // Successfully parsed
        val operation = fields.get("operationName") collect { // extract operationName
          case JsString(op) => op
        }
        val variables = fields.get("variables") match { // extract variables
          case Some(obj: JsObject) => obj
          case _ => JsObject.empty
        }
        complete(executeGraphQLQuery(queryAst, operation, variables))

      case Failure(error) =>
        complete(BadRequest, JsObject("error" -> JsString(error.getMessage))) // Respond with 400 and error message
    }
  }

  private def executeGraphQLQuery(query: Document, operation: Option[String], vars: JsObject)(implicit ec: ExecutionContext) = {
    Executor.execute( // Execute the query
      GraphQLSchema.SchemaDefinition, // Our Schema
      query,
      MyContext(dao), // Context object
      variables = vars,
      operationName = operation,
      deferredResolver = GraphQLSchema.Resolver
    ).map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      }
  }
}