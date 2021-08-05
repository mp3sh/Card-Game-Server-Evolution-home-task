package com.mycompany.task

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.mycompany.task.Game.{Player, Players}
import com.mycompany.task.GameClient.Move

import scala.concurrent.Future
import scala.concurrent.duration._

object GameRoom {
  case class Start(game: Game)
  case class Result(players: Players)

  def props(game: String) = Props(new GameRoom(game))
}

class GameRoom(game: String) extends Actor {
  import GameRoom._
  import context.dispatcher

  implicit val timeout = Timeout(5 minutes)
  val playGame = new Game(game)

  def receive = {
    case user: ActorRef =>
      playGame.addPlayer(user)
      user ! s"You login to room. Count clients: '${playGame.online.players.size}'"

      if (playGame.online.players.size < 2) {
        user ! "Waiting users"
      } else {
        playGame.online.players.foreach(_.link ! "User connect to room")
        self ! Start(playGame)
      }

    case Start(gameObject) =>
      val request = gameObject.giveCards()
        .players
        .foldLeft(Seq.empty[Future[Player]])((acc, pl) => acc :+ (pl.link ? Move(pl)).mapTo[Player])

      Future.sequence(request).pipeTo(self)

    case players: Seq[Player] =>

      val reward = game match {
        case "single-card-game" => (1, 3, 10)
        case "double-card-game" => (2, 5, 20)
      }

      val update_players = players.filter(_.move.get == "play") match {

        case _ :: Nil => players.collect{
          case x if x.move.get == "play" => x.editBank(x.balance + reward._2)
          case x => x.editBank(x.balance - reward._2)
        }

        case first :: second :: Nil => {
          (first.cards.get.foldLeft(0)(_+_._2), second.cards.get.foldLeft(0)(_+_._2)) match {
            case (a, b) if a > b => Seq(first.editBank(first.balance + reward._3), second.editBank(second.balance - reward._3))
            case (a, b) if a < b => Seq(first.editBank(first.balance - reward._3), second.editBank(second.balance + reward._3))
            case (a, b) if a == b => Seq(first, second)
          }
        }

        case _ => players.map{x => x.editBank(x.balance - reward._1)}
      }

      update_players.foreach(x => x.link ! s"Balance update: ${x.balance}")

      self ! Start(playGame.newGame(game, update_players))
  }
}
