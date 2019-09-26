package com.petlew.gacek.visualisation

import akka.actor.Actor

object Store {

  sealed trait StoreMessages
  case class StoreEvent(queueEvent: QueueEvent)
  case class GetEvents()
  case class Events(events: Set[QueueEvent])

}

class Store extends Actor {
  import Store._

  private var events: Set[QueueEvent] = Set.empty[QueueEvent] + EnqueueEvent(1, 1, "a", "b", "c")

  override def receive: Receive = {
    case StoreEvent(event: QueueEvent) =>
      events += event
    case GetEvents =>
      sender ! Events(events)

  }
}
