package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}

class UdpEchoService(address: InetSocketAddress) extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, address)

  def receive: PartialFunction[Any, Unit] = {
    case Udp.Bound(_) =>
      context.become(ready(sender))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      QueueEventDeserializer.read(data)
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}