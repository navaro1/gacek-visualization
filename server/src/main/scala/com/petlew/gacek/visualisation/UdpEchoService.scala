package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}
import com.petlew.gacek.visualisation.Store.StoreEvent

object UdpEchoService {

  def props(address: InetSocketAddress, store: ActorRef): Props = Props(new UdpEchoService(address, store))
}

class UdpEchoService(address: InetSocketAddress, store: ActorRef) extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, address)

  def receive: PartialFunction[Any, Unit] = {
    case Udp.Bound(_) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, _) =>
      val event = QueueEventDeserializer.read(data)
      store ! StoreEvent(event)
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}