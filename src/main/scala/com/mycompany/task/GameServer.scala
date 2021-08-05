package com.mycompany.task

import akka.actor.{Actor, ActorRef, Props}

object GameServer {
  case class Registration(user: ActorRef, game: String)

  def props = Props(new GameServer())
}

class GameServer extends Actor {
  import GameServer._

  def createGameRoom(game: String) = {
    context.actorOf(GameRoom.props(game), game)
  }

  def receive = {
    case Registration(user, game) =>
      context.child(game).fold(createGameRoom(game) ! user)(_ ! user)

  }
}
