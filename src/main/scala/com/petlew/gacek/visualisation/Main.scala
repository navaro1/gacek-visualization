package com.petlew.gacek.visualisation

import akka.actor.{Actor, ActorSystem, Props}

class HelloActor extends Actor {
  def receive = {
    case "hello" => println("hello back at you")
    case _       => println("huh?")
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")
  val server = system.actorOf(Props[UdpEchoService].withMailbox("mailbox"))
  val listener = system.actorOf(Props[QueueEventListener].withMailbox("mailbox"))
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