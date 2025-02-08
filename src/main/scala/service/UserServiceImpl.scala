package service

import model.User
import repository.UserDaoImpl
import service.UserServiceImpl._
import zio._

class UserServiceImpl(userDao: UserDaoImpl) {

  def register(request: RegisterRequest): ZIO[Any, UserError, AuthResponse] = {
    for {
      existingUser <- userDao.getUserByEmail(request.email).orElseFail(EmailAlreadyExists(request.email))
      _ <- ZIO.fail(EmailAlreadyExists(request.email)).when(existingUser.isDefined)

      newUser = User(0, request.email, request.username, request.password, request.profilePhoto)
      userId <- userDao.addUser(newUser).orElseFail(EmailAlreadyExists(request.email))

      response = AuthResponse(userId, newUser.email, newUser.username, newUser.profilePhoto)
    } yield response
  }

  def login(request: LoginRequest): ZIO[Any, UserError, AuthResponse] = {
    for {
      userOpt <- userDao.getUserByEmail(request.email).orElseFail(InvalidCredentials(request.email))
      user <- ZIO.fromOption(userOpt).orElseFail(InvalidCredentials(request.email))

      _ <- ZIO.fail(InvalidCredentials(request.email)).when(user.password != request.password)

      response = AuthResponse(user.id, user.email, user.username, user.profilePhoto)
    } yield response
  }

  def getUserById(userId: Int): ZIO[Any, UserError, AuthResponse] = {
    for {
      userOpt <- userDao.getUser(userId).orElseFail(UserNotFound(userId))
      user <- ZIO.fromOption(userOpt).orElseFail(UserNotFound(userId))

      response = AuthResponse(user.id, user.email, user.username, user.profilePhoto)
    } yield response
  }

  def updateUser(request: UserUpdateRequest): ZIO[Any, UserError, Unit] = {
    for {
      userOpt <- userDao.getUser(request.userId).orElseFail(UserNotFound(request.userId))
      user <- ZIO.fromOption(userOpt).orElseFail(UserNotFound(request.userId))

      updatedUser = User(
        user.id,
        email = request.email.getOrElse(user.email),
        username = request.username.getOrElse(user.username),
        password = user.password,
        profilePhoto = request.profilePhoto.getOrElse(user.profilePhoto)
      )
      _ <- userDao.updateUser(updatedUser).orElseFail(UserNotFound(request.userId))
    } yield ()
  }
}

object UserServiceImpl {

  val live: URLayer[UserDaoImpl, UserServiceImpl] = ZLayer.fromFunction(new UserServiceImpl(_))

  sealed trait UserError extends Exception
  case class EmailAlreadyExists(email: String) extends UserError
  case class UserNotFound(userId: Int) extends UserError
  case class InvalidCredentials(email: String) extends UserError

  case class RegisterRequest(
      email: String,
      username: String,
      password: String,
      profilePhoto: Array[Byte])

  case class LoginRequest(
      email: String,
      password: String)

  case class AuthResponse(
      userId: Int,
      email: String,
      username: String,
      profilePhoto: Array[Byte])

  case class UserUpdateRequest(
      userId: Int,
      email: Option[String],
      username: Option[String],
      profilePhoto: Option[Array[Byte]])
}
