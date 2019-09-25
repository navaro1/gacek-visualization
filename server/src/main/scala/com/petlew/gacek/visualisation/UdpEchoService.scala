package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}

object UdpEchoService {

  def props(address: InetSocketAddress): Props = Props(new UdpEchoService(address))
}

class UdpEchoService(address: InetSocketAddress) extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, address)

  def receive: PartialFunction[Any, Unit] = {
    case Udp.Bound(_) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, _) =>
      val event = QueueEventDeserializer.read(data)
      println(event)
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}