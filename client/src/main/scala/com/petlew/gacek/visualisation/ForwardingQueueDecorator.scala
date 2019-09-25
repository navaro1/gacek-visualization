package com.petlew.gacek.visualisation

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch._
import akka.event.EventStream
import com.typesafe.config.Config

class UnboundedForwardingMailboxType()
    extends MailboxType
    with ProducesMessageQueue[ForwardingQueueDecorator] {

  def this(settings: ActorSystem.Settings, config: Config) = this()

  override def create(owner: Option[ActorRef],
                      system: Option[ActorSystem]): MessageQueue = {
    require(system isDefined, "Actor System has to be present.")
    new ForwardingQueueDecorator(
      UnboundedMailbox().create(owner, system),
      System.nanoTime(),
      system.get.eventStream
    ) with UnboundedMessageQueueSemantics
  }
}

class ForwardingQueueDecorator(queue: MessageQueue,
                               timestamp: => Long,
                               events: EventStream)
    extends MessageQueue {

  override def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
    val message = EnqueueEvent(
      timestamp(),
      numberOfMessages,
      receiver.path.toSerializationFormat,
      handle.sender.path.toSerializationFormat,
      handle.message.getClass.toString
    )
    events.publish(message)
    queue.enqueue(receiver, handle)
  }

  override def dequeue(): Envelope =
    queue.dequeue()

  override def numberOfMessages: Int =
    queue.numberOfMessages

  override def hasMessages: Boolean =
    queue.hasMessages

  override def cleanUp(owner: ActorRef, deadLetters: MessageQueue): Unit =
    queue.cleanUp(owner, deadLetters)

}
