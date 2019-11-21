package eu.firstbird.customers

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import eu.firstbird.customers.CustomerModule.DomainModel.ErrorInfo
import eu.firstbird.customers.tapircommons.{Documentation, Endpoints}
import sttp.model.StatusCode
import sttp.tapir.server.akkahttp._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object CustomerModule extends App {

  object DomainModel {
    case class Attribute(tpe: String, value: Int)
    case class Person(id: UUID, attributes: List[Attribute])
    case class ErrorInfo(error: String)
  }

  def startServer(): Unit = {

    val repository = new CustomerRepository
    val service = new CustomerService(repository)

    //Route definitions
    val getAllCustomersRoute: Route = Endpoints.getAllCustomers.toRoute {
      case (limit, attribute) => service.listCustomers(limit, attribute)
    }

    val findCustomerRoute: Route = Endpoints.getCustomer.toRoute(id => service.findCustomer(id))

    val postCustomerRoute: Route = Endpoints.postCustomer.toRoute {
      case (auth, person) =>
        if (auth == "secret")
          service.createCustomer(person)
        else
          Future.successful(Left((StatusCode.unsafeApply(401), ErrorInfo(s"Incorrect authentication!"))))
    }

    val postAttributeRoute: Route = Endpoints.postAttribute.toRoute {
      case (auth, customerId, attribute) =>
        if (auth == "secret")
          service.addAttribute(customerId, attribute)
        else
          Future.successful(Left((StatusCode.unsafeApply(401), ErrorInfo(s"Incorrect authentication!"))))
    }

    val deleteAttributeRoute: Route = Endpoints.deleteAttribute.toRoute {
      case (auth, customerId, attributeType) =>
        if (auth == "secret")
          service.removeAttribute(customerId, attributeType)
        else
          Future.successful(Left((StatusCode.unsafeApply(401), ErrorInfo(s"Incorrect authentication!"))))
    }

    //AkkaHttp
    val routes =
      getAllCustomersRoute ~
        findCustomerRoute ~
        postCustomerRoute ~
        postAttributeRoute ~
        deleteAttributeRoute ~
        new SwaggerUI(Documentation.yml).routes

    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(actorSystem)
    Await.result(Http().bindAndHandle(routes, "localhost", 8080), 1.minute)
    println("Server is now ready")
  }

  startServer()
}
