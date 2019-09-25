package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import akka.actor.{
  Actor,
  ActorRef,
  ExtendedActorSystem,
  Extension,
  ExtensionId,
  ExtensionIdProvider,
  Props
}
import akka.io.{IO, Udp}
import com.typesafe.config.Config

object QueueEventExtension
    extends ExtensionId[QueueEventExtension]
    with ExtensionIdProvider {

  override def createExtension(
    system: ExtendedActorSystem
  ): QueueEventExtension = new QueueEventExtension(
    system,
    QueueEventExtensionConfig.fromConfig(system.settings.config)
  )

  override def lookup(): ExtensionId[_ <: Extension] = QueueEventExtension
}

class QueueEventExtension(system: ExtendedActorSystem,
                          config: QueueEventExtensionConfig)
    extends Extension {
  private val listener =
    system.actorOf(
      QueueEventListener.props(config.address).withMailbox("unbounded-mailbox")
    )
}

object QueueEventExtensionConfig {

  def fromConfig(config: Config) = QueueEventExtensionConfig(
    address = new InetSocketAddress(
      config.getString("com.petlew.gacek.visualization.server.address.host"),
      config.getInt("com.petlew.gacek.visualization.server.address.port")
    )
  )
}

case class QueueEventExtensionConfig(address: InetSocketAddress)

object QueueEventListener {

  def props(address: InetSocketAddress): Props =
    Props(new QueueEventListener(address))
}

class QueueEventListener(address: InetSocketAddress) extends Actor {

  import context.system

  this.context.system.eventStream
    .subscribe(this.context.self, classOf[QueueEvent])

  IO(Udp) ! Udp.SimpleSender

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case msg: QueueEvent =>
      send ! Udp.Send(QueueEventSerializer.write(msg), address)
  }

}
