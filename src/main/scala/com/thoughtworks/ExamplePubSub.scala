package com.thoughtworks

import akka.actor.Props
import java.net.InetSocketAddress

import akka.util.ByteString
import redis.actors.RedisSubscriberActor
import redis.api.pubsub.{Message, PMessage}
import redis.RedisClient

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ExamplePubSub extends App {
  implicit val akkaSystem = akka.actor.ActorSystem()

  val redis = RedisClient()

  akkaSystem.scheduler.schedule(2 seconds, 2 seconds)(redis.publish("time", System.currentTimeMillis()))
  akkaSystem.scheduler.schedule(2 seconds, 5 seconds)(redis.publish("pattern.match", "pattern value"))
  akkaSystem.scheduler.scheduleOnce(20 seconds)(akkaSystem.terminate())

  val channels = Seq("time")
  val patterns = Seq("pattern.*")
  akkaSystem.actorOf(Props(classOf[SubscribeActor], channels, patterns).withDispatcher("rediscala.rediscala-client-worker-dispatcher"))
}

class SubscribeActor(channels: Seq[String] = Nil, patterns: Seq[String] = Nil)
  extends RedisSubscriberActor(
    new InetSocketAddress("localhost", 6379),
    channels,
    patterns,
    onConnectStatus = connected => { println(s"connected: $connected")}
  ) {

  def onMessage(message: Message) {
    println(s" message received: $message")
  }

  def onPMessage(pmessage: PMessage) {
    println(s"pattern message received: ${pmessage.data.decodeString(ByteString.UTF_8)}")
  }
}