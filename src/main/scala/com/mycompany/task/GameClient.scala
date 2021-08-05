package com.mycompany.task

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import com.mycompany.task.Game.Player
import com.mycompany.task.GameServer.Registration

import scala.concurrent.duration._
import scala.io.StdIn._

object GameClient {
  case class Game(name: String)
  case class Move(player: Player)

  def props(gameServer: ActorRef) = Props(new GameClient(gameServer))
}

class GameClient(gameServer: ActorRef) extends Actor {
  import GameClient._
  implicit val timeout = Timeout(5 seconds)

  override def preStart() = {
    println("select game 'single-card-game' or 'double-card-game'")

    readLine() match {
      case "single-card-game" => self ! Game("single-card-game")
      case "double-card-game" => self ! Game("double-card-game")
      case _ => println("Game not found"); preStart()
    }
  }

  def receive = {
    case Game(name) =>
      gameServer ! Registration(self, name)

    case Move(player) =>
      println(s"You cards: ${player.cards}, Balance: ${player.balance}. Select 'play' or 'fold'")
      readLine() match {
        case "play" => sender ! player.editMove("play")
        case "fold" => sender ! player.editMove("fold")
      }

    case message: String =>
      println(message)
  }
}
