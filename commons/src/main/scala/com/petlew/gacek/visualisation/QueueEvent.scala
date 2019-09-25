package com.petlew.gacek.visualisation

import akka.util.ByteString

sealed trait QueueEvent {
  val timestamp: Long
  val mailboxSize: Int
}

case class EnqueueEvent(timestamp: Long,
                        mailboxSize: Int,
                        receiver: String,
                        sender: String,
                        messageClassName: String)
    extends QueueEvent

object QueueEventDeserializer {

  // currently only for enqueue event
  def read(byteString: ByteString): QueueEvent = {
    val strings = byteString.utf8String.split(SEP)
    EnqueueEvent(
      strings(0) toLong,
      strings(1) toInt,
      strings(2),
      strings(3),
      strings(4)
    )
  }
}

object QueueEventSerializer {

  def write(event: QueueEvent): ByteString = event match {
    case EnqueueEvent(
        timestamp,
        mailboxSize,
        receiver,
        sender,
        messageClassName
        ) =>
      val string =
        s"""$timestamp$SEP$mailboxSize$SEP$receiver$SEP$sender$SEP$messageClassName\n""".stripMargin
      ByteString(string)
  }

}
