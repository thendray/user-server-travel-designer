package service

import model.User
import repository.UserDaoImpl
import service.UserServiceImpl._
import utils.{PasswordHelper, S3Uploader}
import zio._

class UserServiceImpl(userDao: UserDaoImpl) {

  def register(request: RegisterRequest): ZIO[Any, UserError, AuthResponse] = {
    for {
      existingUser <- userDao.getUserByEmail(request.email).mapError(e => DbProblem(e.getMessage))
      _ <- ZIO.fail(EmailAlreadyExists(request.email)).when(existingUser.isDefined)
      url = S3Uploader.uploadToYandexCloudObjectStorage(request.profilePhoto)

      newUser = User(
        0,
        request.email,
        request.username,
        PasswordHelper.hashString(request.password),
        url
      )
      _ <- userDao.addUser(newUser).mapError(e => DbProblem(e.getMessage))
      userOpt <- userDao.getUserByEmail(newUser.email).orElseFail(EmailAlreadyExists(""))
      user = userOpt.get
      jwt <- JwtService.createToken(user.id.toString).mapError(e => DbProblem(e.getMessage))
      response = AuthResponse(user.id, jwt.token)
    } yield response
  }

  def login(request: LoginRequest): ZIO[Any, UserError, AuthResponse] = {
    for {
      userOpt <- userDao.getUserByEmail(request.email).orElseFail(InvalidCredentials(request.email))
      user <- ZIO.fromOption(userOpt).orElseFail(InvalidCredentials(request.email))

      _ <- ZIO
        .fail(InvalidCredentials(request.email))
        .when(PasswordHelper.checkData(request.password, user.password) && request.password == user.password)
      jwt <- JwtService.createToken(user.id.toString).mapError(e => DbProblem(e.getMessage))
      response = AuthResponse(user.id, jwt.token)
    } yield response
  }

  def getUserById(userId: Int): ZIO[Any, UserError, UserResponse] = {
    for {
      userOpt <- userDao.getUser(userId).orElseFail(UserNotFound(userId))
      user <- ZIO.fromOption(userOpt).orElseFail(UserNotFound(userId))

      response = UserResponse(user.id, user.email, user.username, user.profilePhoto)
    } yield response
  }

  def updateUser(request: UserUpdateRequest): ZIO[Any, UserError, Unit] = {
    for {
      userOpt <- userDao.getUser(request.userId).orElseFail(UserNotFound(request.userId))
      user <- ZIO.fromOption(userOpt).orElseFail(UserNotFound(request.userId))
      url = request.profilePhoto.map(S3Uploader.uploadToYandexCloudObjectStorage(_))
      updatedUser = User(
        user.id,
        email = request.email.getOrElse(user.email),
        username = request.username.getOrElse(user.username),
        password = user.password,
        profilePhoto = url.getOrElse(user.profilePhoto)
      )
      _ <- userDao.updateUser(updatedUser).orElseFail(UserNotFound(request.userId))
    } yield ()
  }

  def deleteUserId(userId: Int): ZIO[Any, UserError, Unit] = {
    for {
      _ <- userDao.deleteUser(userId).orElseFail(UserNotFound(userId))
    } yield ()
  }
}

object UserServiceImpl {

  val live: URLayer[UserDaoImpl, UserServiceImpl] = ZLayer.fromFunction(new UserServiceImpl(_))

  sealed trait UserError extends Exception
  case class DbProblem(error: String) extends UserError
  case class EmailAlreadyExists(email: String) extends UserError
  case class UserNotFound(userId: Int) extends UserError
  case class InvalidCredentials(email: String) extends UserError

  case class RegisterRequest(
      email: String,
      username: String,
      password: String,
      profilePhoto: String)

  case class LoginRequest(
      email: String,
      password: String)

  case class AuthResponse(
      userId: Int,
      jwt: String)

  case class UserResponse(
      userId: Int,
      email: String,
      username: String,
      profilePhoto: String)

  case class UserUpdateRequest(
      userId: Int,
      email: Option[String],
      username: Option[String],
      profilePhoto: Option[String])
}
