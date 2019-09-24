package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString

class QueueEventListener extends Actor {
  import context.system
  this.context.system.eventStream.subscribe(this.context.self, classOf[QueueEvent])

  IO(Udp) ! Udp.SimpleSender

  def receive = {
    case Udp.SimpleSenderReady =>
      println("I AM ALIVE!")
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case msg: QueueEvent =>
      println("SENDING")
      send ! Udp.Send(ByteString(msg.toString), new InetSocketAddress("localhost", 2313))
  }
}
