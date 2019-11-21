package eu.firstbird.customers.tapircommons

object Documentation {
  import Endpoints._
  import sttp.tapir.docs.openapi._
  import sttp.tapir.openapi.circe.yaml._

  private val documentedEndpoints = List(
    getCustomer,
    getAllCustomers,
    postCustomer,
    postAttribute,
    deleteAttribute
  )

  val yml: String = documentedEndpoints.toOpenAPI("Customer Endpoints", version = "0.0.1").toYaml
}
