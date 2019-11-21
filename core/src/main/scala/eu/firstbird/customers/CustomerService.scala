package eu.firstbird.customers

import java.util.UUID

import eu.firstbird.customers.CustomerModule.DomainModel._
import sttp.model.StatusCode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomerService(repository: CustomerRepository) {

  def findCustomer(id: UUID): Future[Either[(StatusCode, ErrorInfo), Person]] = {
    repository.findCustomer(id).map {
      case Some(customer) => Right(customer)
      case None           => Left((StatusCode.unsafeApply(404), ErrorInfo(s"Customer $id not found")))
    }
  }

  def listCustomers(limit: Option[Int], attribute: Option[String]): Future[Either[(StatusCode, ErrorInfo), List[Person]]] = {
    val customers = limit match {
      case Some(l) if l <= 0 => Future.successful(Left((StatusCode.unsafeApply(404), ErrorInfo(s"limit must be positive"))))
      case Some(l)           => repository.getAllCustomers.map(c => Right(c.take(l)))
      case None              => repository.getAllCustomers.map(Right(_))
    }

    attribute match {
      case Some(attr) => customers.map(_.map(_.filter(_.attributes.map(_.tpe).contains(attr))))
      case None       => customers
    }
  }

  def createCustomer(customer: Person): Future[Either[(StatusCode, ErrorInfo), Unit]] = {
    findCustomer(customer.id).flatMap {
      case Right(c) => Future.successful(Left((StatusCode.unsafeApply(400), ErrorInfo(s"Customer ${c.id} exists!"))))
      case Left(_)  => repository.saveCustomer(customer).map(Right(_))
    }
  }

  def addAttribute(id: UUID, attribute: Attribute): Future[Either[(StatusCode, ErrorInfo), Unit]] = {
    findCustomer(id).flatMap {
      case Right(c) =>
        val updated = c.copy(attributes = c.attributes :+ attribute)
        repository.saveCustomer(updated).map(Right(_))
      case left @ Left(_) => Future.successful(left.map(_ => ()))
    }
  }

  def removeAttribute(id: UUID, attributeType: String): Future[Either[(StatusCode, ErrorInfo), Unit]] = {
    findCustomer(id).flatMap {
      case Right(c) =>
        val updated = c.copy(attributes = c.attributes.filter(_.tpe != attributeType))
        repository.saveCustomer(updated).map(Right(_))
      case left @ Left(_) => Future.successful(left.map(_ => ()))
    }
  }

}
