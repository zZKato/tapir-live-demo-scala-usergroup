package eu.firstbird.customers.tapircommons

import java.util.UUID

import eu.firstbird.customers.CustomerModule.DomainModel.{Attribute, ErrorInfo, Person}
import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

object Endpoints {

  type ErrorOutput = (StatusCode, ErrorInfo)

  private val baseEndpoint: Endpoint[Unit, ErrorOutput, Unit, Nothing] = endpoint
    .in("directory" / "customers")
    .errorOut(statusCode.and(jsonBody[ErrorInfo]))

  val getCustomer: Endpoint[UUID, ErrorOutput, Person, Nothing] = baseEndpoint.get
    .in(path[UUID]("customerId").description("the customerId"))
    .out(jsonBody[Person].description("the requested customer"))
    .description("this is an endpoint to fetch a customer")

  val getAllCustomers: Endpoint[(Option[Int], Option[String]), (StatusCode, ErrorInfo), List[Person], Nothing] = baseEndpoint.get
    .in(query[Option[Int]]("limit").description("Limit the result size"))
    .in(query[Option[String]]("attribute").description("filter by attribute"))
    .out(jsonBody[List[Person]].description("list of customers"))
    .description("This is an endpoint to fetch all customers")

  val postCustomer: Endpoint[(String, Person), ErrorOutput, Unit, Nothing] = baseEndpoint.post
    .in(auth.bearer)
    .in(jsonBody[Person].description("the customer to create"))
    .description("This is an endpoint to create a customer")

  val postAttribute: Endpoint[(String, UUID, Attribute), ErrorOutput, Unit, Nothing] = baseEndpoint.post
    .in(auth.bearer)
    .in(path[UUID]("customerId"))
    .in("attributes")
    .in(jsonBody[Attribute].description("the attribute to add"))
    .description("This is an endpoint to create attributes")

  val deleteAttribute: Endpoint[(String, UUID, String), ErrorOutput, Unit, Nothing] = baseEndpoint.delete
    .in(auth.bearer).description("Bearer authentication")
    .in(path[UUID]("customerId"))
    .in("attributes")
    .in(path[String]("attributeType").description("the type of attribute to remove"))
    .description("This is an endpoint to delete attributes")
}
