package com.mycompany.task

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._

object GameClientApplication1 extends App {
  val actorSystem = ActorSystem("GameServer")
  implicit val dispatcher = actorSystem.dispatcher

  val gameServerAddress = "akka.tcp://GameServer@127.0.0.1:2552/user/gameServer"
  actorSystem.actorSelection(gameServerAddress).resolveOne(3 seconds).onSuccess {
    case gameServer : ActorRef =>
      actorSystem.actorOf(GameClient.props(gameServer), "gameClient")
  }
}

object GameClientApplication2 extends App {
  val actorSystem = ActorSystem("GameServer")
  implicit val dispatcher = actorSystem.dispatcher

  val gameServerAddress = "akka.tcp://GameServer@127.0.0.1:2552/user/gameServer"
  actorSystem.actorSelection(gameServerAddress).resolveOne(3 seconds).onSuccess {
    case gameServer : ActorRef =>
      actorSystem.actorOf(GameClient.props(gameServer), "gameClient")
  }
}

object GameServerApplication extends App {
  val actorSystem = ActorSystem("GameServer")
  actorSystem.actorOf(GameServer.props, "gameServer")
}
