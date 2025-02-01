package controller

import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import zio.{ULayer, ZIO, ZLayer}

class MainRouter {

  private val defaultRoute = "api" / "user" / "v1"

  private val get =
    endpoint
      .get
      .in(defaultRoute / path[Int](name= "id"))
      .out(statusCode)
      .out(jsonBody[String])
      .errorOut(statusCode)
      .errorOut(stringBody)


  def getUser: ZServerEndpoint[Any, Any] =
    get.zServerLogic(id =>
      ZIO.succeed(StatusCode.Ok, s"Ok, $id")
    )

}

object MainRouter {

  val live: ULayer[MainRouter] = ZLayer.fromFunction(() => new MainRouter())

}