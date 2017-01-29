package com.thoughtworks

import redis.RedisClient

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object RedisPingPongApp extends App {

  implicit val akkaSystem = akka.actor.ActorSystem()

  val redis = RedisClient()

  val futurePong: Future[String] = redis.ping()

  println("Ping sent!!")

  futurePong.map(pong => {
    println(s"Redis replied with a $pong")
  })

  Await.result(futurePong, 5 seconds)

  akkaSystem.terminate()
}
