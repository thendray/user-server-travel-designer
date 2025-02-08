package utils

import service.UserServiceImpl.{EmailAlreadyExists, InvalidCredentials, UserError, UserNotFound}
import sttp.model.StatusCode
import sttp.tapir._

object ErrorMapping {

  // Mapping UserError to (StatusCode, String)
  val userErrorMapping: UserError => (StatusCode, String) = {
    case EmailAlreadyExists(email) => (StatusCode.Conflict, s"Email ${email} already exists")
    case UserNotFound(userId) => (StatusCode.NotFound, s"User with id ${userId} not found")
    case InvalidCredentials(email) => (StatusCode.Unauthorized, s"Invalid credentials for email ${email}")
  }

  // Implicit class to add error mapping to endpoints
  implicit class RichEndpoint[I, O](endpoint: Endpoint[Unit, I, UserError, O, Any]) {
    def withErrorMapping: Endpoint[Unit, I, (StatusCode, String), O, Any] =
      endpoint
        .mapErrorOut(userErrorMapping)(a => UserNotFound(1))
  }
}