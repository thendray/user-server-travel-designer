package controller

import service.UserServiceImpl
import service.UserServiceImpl.{AuthResponse, LoginRequest, RegisterRequest, UserError, UserUpdateRequest}
import sttp.tapir.ztapir.{RichZEndpoint, ZServerEndpoint}
import zio._

class UserRoutes(userService: UserServiceImpl, userEndpoints: UserEndpoints) {

  private val registerHandler: RegisterRequest => ZIO[Any, UserError, AuthResponse] =
    request => userService.register(request)

  private val loginHandler: LoginRequest => ZIO[Any, UserError, AuthResponse] =
    request => userService.login(request)

  private val getUserHandler: Int => ZIO[Any, UserError, AuthResponse] =
    userId => userService.getUserById(userId)

  private val updateUserHandler: UserUpdateRequest => ZIO[Any, UserError, Unit] =
    request => userService.updateUser(request)

  def reg(): ZServerEndpoint[Any, Any] =
    userEndpoints.registerEndpoint.zServerLogic(registerHandler)

  def log(): ZServerEndpoint[Any, Any] =
    userEndpoints.loginEndpoint.zServerLogic(loginHandler)

  def get(): ZServerEndpoint[Any, Any] =
    userEndpoints.getUserEndpoint.zServerLogic(getUserHandler)

  def update(): ZServerEndpoint[Any, Any] =
    userEndpoints.updateUserEndpoint.zServerLogic(updateUserHandler)

}

object UserRoutes {
  val live: URLayer[UserServiceImpl with UserEndpoints, UserRoutes] = ZLayer.fromFunction(new UserRoutes(_, _))
}
