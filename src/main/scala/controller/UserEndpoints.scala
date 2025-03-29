package controller

import io.circe.generic.auto._
import service.UserServiceImpl.{AuthResponse, LoginRequest, RegisterRequest, UserError, UserResponse, UserUpdateRequest}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

class UserEndpoints() {

  private val baseEndpoint = endpoint.in("api" / "users")

  val registerEndpoint =
    baseEndpoint
      .in("sign-up")
      .post
      .in(jsonBody[RegisterRequest])
      .out(jsonBody[AuthResponse])
      .errorOut(jsonBody[UserError])

  val loginEndpoint =
    baseEndpoint.post
      .in("login")
      .in(jsonBody[LoginRequest])
      .out(jsonBody[AuthResponse])
      .errorOut(jsonBody[UserError])

  val getUserEndpoint =
    baseEndpoint.get
      .in(path[Int]("userId"))
      .out(jsonBody[UserResponse])
      .errorOut(jsonBody[UserError])

  val updateUserEndpoint =
    baseEndpoint.put
      .in(jsonBody[UserUpdateRequest])
      .errorOut(jsonBody[UserError])

  val deleteUserEndpoint =
    baseEndpoint.delete
      .in(path[Int]("userId"))
      .errorOut(jsonBody[UserError])
}