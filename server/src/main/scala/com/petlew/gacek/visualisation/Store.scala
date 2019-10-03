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

  private var events: Set[QueueEvent] = Set.empty[QueueEvent]

  override def receive: Receive = {
    case StoreEvent(event: QueueEvent) =>
      events += event
    case GetEvents =>
      val evnts = Events(events)
      sender() ! evnts

  }
}
