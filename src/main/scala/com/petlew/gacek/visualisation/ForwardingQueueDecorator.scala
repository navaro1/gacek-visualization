package com.petlew.gacek.visualisation

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch._
import akka.event.EventStream
import com.typesafe.config.Config


class UnboundedForwardingMailboxType() extends MailboxType with ProducesMessageQueue[ForwardingQueueDecorator] {

  def this(settings: ActorSystem.Settings, config: Config) = this()

  override def create(owner: Option[ActorRef], system: Option[ActorSystem]): MessageQueue =
    new ForwardingQueueDecorator(
      UnboundedMailbox().create(owner, system),
      () => System.nanoTime(),
      system.get.eventStream
    ) with UnboundedMessageQueueSemantics
}


class ForwardingQueueDecorator(
                                queue: MessageQueue,
                                timestamp: () => Long,
                                events: EventStream
                              ) extends MessageQueue {

  override def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
    val message = EnqueueEvent(timestamp(), receiver, handle.sender, handle.message)
    events.publish(message)
    queue.enqueue(receiver, handle)
  }

  override def dequeue(): Envelope = {
    val envelope = queue.dequeue()
    if (envelope != null) {
      val message = DequeueEvent(timestamp(), envelope.sender, envelope.message)
      events.publish(message)
    }
    envelope
  }

  override def numberOfMessages: Int =
    queue.numberOfMessages

  override def hasMessages: Boolean =
    queue.hasMessages

  override def cleanUp(owner: ActorRef, deadLetters: MessageQueue): Unit =
    queue.cleanUp(owner, deadLetters)

}

sealed trait QueueEvent {
  val timestamp: Long
}

case class EnqueueEvent(timestamp: Long, receiver: ActorRef, sender: ActorRef, message: Any) extends QueueEvent

case class DequeueEvent(timestamp: Long, sender: ActorRef, message: Any) extends QueueEvent
