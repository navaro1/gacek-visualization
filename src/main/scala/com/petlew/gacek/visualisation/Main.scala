package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}

class HelloActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case "hello" => println("hello back at you")
    case _       => println("huh?")
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
//  private val address = new InetSocketAddress("localhost", 2313)
//  val server = system.actorOf(UdpEchoService.props(address).withMailbox("mailbox"))
  Thread.sleep(5000)
  println("TEST")

  val helloActor = system.actorOf(Props[HelloActor], name = "helloactor")

  helloActor ! "hello"
  helloActor ! "buenos dias"
  helloActor ! "hello"
  helloActor ! "buenos dias"
  helloActor ! "hello"
  helloActor ! "buenos dias"
}