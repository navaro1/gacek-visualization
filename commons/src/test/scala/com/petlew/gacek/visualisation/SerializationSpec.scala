package com.petlew.gacek.visualisation

import akka.util.ByteString
import org.scalatest.{FlatSpec, Matchers}

class SerializationSpec extends FlatSpec with Matchers {

  val event = EnqueueEvent(1, 2, "i am receiver", "i am sender", "klazz")
  val expected = ByteString("1%@%2%@%i am receiver%@%i am sender%@%klazz\n")

  "QueueEvent serializer" should "serialize EnqueueEvent" in {
    QueueEventSerializer.write(event) shouldEqual expected
  }

  "QueueEvent deserializer" should "deserialize EnqueueEvent" in {
    QueueEventDeserializer.read(expected) shouldEqual event
  }

  "Serialization and deserialization" should "not change value of EnqueueEvent" in {
    QueueEventDeserializer.read(QueueEventSerializer.write(event)) shouldEqual event
  }

}
