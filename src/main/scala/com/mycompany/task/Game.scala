package com.mycompany.task

import akka.actor.ActorRef
import com.mycompany.task.Game.Player

import scala.annotation.tailrec
import scala.util.Random

object Game {
  case class Player(link: ActorRef, balance: Int, cards: Option[Seq[(String, Int)]], move: Option[String]){
    def editMove(move: String) = copy(move = Some(move))
    def editBank(money: Int) = copy(balance = money)
  }
  case class Players(players: Seq[Player] = Seq.empty)
}

class Game(game: String, players: Seq[Player] = Seq.empty) {
  import Game._

  var online = Players(players)

  val suites = List("Spade", "Heart", "Club", "Diamond")
  val ranks = List("Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace")
  val cards = for (r <- ranks.zip(1 to 13); s <- suites) yield (s"${r._1} of $s", r._2)

  def addPlayer(user: ActorRef) = {
    online = Players(online.players :+ Player(user, 1000, None, None))
  }

  @tailrec
  private def genCard(cards: Seq[(String,Int)], result: Seq[(String,Int)] = Seq.empty, i: Int): Seq[(String,Int)] = {
    if (result.size == i) result
    else {
      genCard(cards.tail, result :+ Random.shuffle(cards).head, i)
    }
  }

  private def getCard(game: String): Option[Seq[(String,Int)]] = {
    game match {
      case "single-card-game" => Some(genCard(cards, i = 1))
      case "double-card-game" => Some(genCard(cards, i = 2))
    }
  }

  @tailrec
  final def giveCards(acc: Int = 0, result: Players = Players()): Players = {
    if (acc == online.players.size) result
    else {
      val a = online.players(acc).link
      val b = online.players(acc).balance
      val c = getCard(game)

      giveCards(acc + 1, Players(result.players :+ Player(a, b, c, None)))
    }
  }

  def newGame(game: String, players: Seq[Player] = Seq.empty) ={
    new Game(game, players)
  }

}
