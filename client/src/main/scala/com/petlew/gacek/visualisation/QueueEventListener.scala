package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString

class QueueEventListener(address: InetSocketAddress) extends Actor {

  import context.system

  this.context.system.eventStream.subscribe(this.context.self, classOf[QueueEvent])

  IO(Udp) ! Udp.SimpleSender

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case msg: QueueEvent =>
      send ! Udp.Send(ByteString(msg.toString), address)
  }

}
