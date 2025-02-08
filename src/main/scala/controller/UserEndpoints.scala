package controller

import io.circe.generic.auto._
import service.UserServiceImpl.{AuthResponse, LoginRequest, RegisterRequest, UserError, UserUpdateRequest}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

class UserEndpoints() {

  private val baseEndpoint = endpoint.in("api" / "v1" / "users")

  val registerEndpoint =
    baseEndpoint.post
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
      .out(jsonBody[AuthResponse])
      .errorOut(jsonBody[UserError])

  val updateUserEndpoint =
    baseEndpoint.put
      .in(jsonBody[UserUpdateRequest])
      .errorOut(jsonBody[UserError])
}