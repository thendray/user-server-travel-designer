package utils

import service.UserServiceImpl.{EmailAlreadyExists, InvalidCredentials, UserError, UserNotFound}
import sttp.model.StatusCode
import sttp.tapir._

object ErrorMapping {

  val userErrorMapping: UserError => (StatusCode, String) = {
    case EmailAlreadyExists(email) => (StatusCode.Conflict, s"Email ${email} already exists")
    case UserNotFound(userId) => (StatusCode.NotFound, s"User with id ${userId} not found")
    case InvalidCredentials(email) => (StatusCode.Unauthorized, s"Invalid credentials for email ${email}")
  }

  implicit class RichEndpoint[I, O](endpoint: Endpoint[Unit, I, UserError, O, Any]) {
    def withErrorMapping: Endpoint[Unit, I, (StatusCode, String), O, Any] =
      endpoint
        .mapErrorOut(userErrorMapping)(a => UserNotFound(1))
  }
}