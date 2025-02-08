package repository

import doobie.implicits._
import doobie.util.transactor.Transactor
import model.User
import utils.CustomTransactor
import zio.{Task, ULayer, ZLayer}
import zio.interop.catz._

class UserDaoImpl(transactor: Transactor[Task]) {

  def addUser(user: User): Task[Int] = {
    sql"""
      INSERT INTO users (email, username, password, profile_photo)
      VALUES (${user.email}, ${user.username}, ${user.password}, ${user.profilePhoto})
    """.update.run.transact(transactor)
  }

  def deleteUser(id: Int): Task[Int] = {
    sql"""
      DELETE FROM users WHERE id = $id
    """.update.run.transact(transactor)
  }

  def updateUser(user: User): Task[Int] = {
    sql"""
      UPDATE users
      SET email = ${user.email}, username = ${user.username}, password = ${user.password}, profile_photo = ${user.profilePhoto}
      WHERE id = ${user.id}
    """.update.run.transact(transactor)
  }

  def getUser(id: Int): Task[Option[User]] = {
    sql"""
      SELECT id, email, username, password, profile_photo FROM users WHERE id = $id
    """.query[User].option.transact(transactor)
  }

  def getUserByEmail(email: String): Task[Option[User]] = {
    sql"""
      SELECT id, email, username, password, profile_photo FROM users WHERE email = $email
    """.query[User].option.transact(transactor)
  }
}

object UserDaoImpl {
  val live: ULayer[UserDaoImpl] = ZLayer.succeed(new UserDaoImpl(CustomTransactor.transactor))
}
