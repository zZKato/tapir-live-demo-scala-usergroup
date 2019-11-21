package eu.firstbird.customers

import java.util.UUID

import eu.firstbird.customers.CustomerModule.DomainModel.{Attribute, Person}

import scala.concurrent.Future

class CustomerRepository {
  private var customers: List[Person] = {
    List(
      Person(UUID.randomUUID(), List(Attribute("weight", 56), Attribute("height", 160), Attribute("age", 18))),
      Person(UUID.randomUUID(), List(Attribute("weight", 80), Attribute("height", 175), Attribute("age", 25))),
      Person(UUID.randomUUID(), List(Attribute("weight", 23), Attribute("height", 60), Attribute("age", 5))),
      Person(UUID.randomUUID(), List(Attribute("weight", 100), Attribute("height", 190), Attribute("age", 58))),
      Person(UUID.randomUUID(), List(Attribute("weight", 90), Attribute("height", 185), Attribute("age", 32)))
    )
  }

  def findCustomer(id: UUID): Future[Option[Person]] = Future.successful(customers.find(_.id == id))

  def getAllCustomers: Future[List[Person]] = Future.successful(customers)

  def saveCustomer(customer: Person): Future[Unit] = {
    customers = customers.filter(_.id != customer.id) :+ customer

    Future.successful(())
  }

  def deleteCustomer(id: UUID): Future[Unit] = {
    customers = customers.filter(_.id != id)

    Future.successful(())
  }
}
