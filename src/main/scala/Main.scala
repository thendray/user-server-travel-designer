import controller.{MainRouter, UserEndpoints, UserRoutes}
import org.http4s._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import zio.interop.catz._
import cats.syntax.all._
import pureconfig.generic.auto._
import repository.UserDaoImpl
import service.UserServiceImpl
import zio.{Scope, Task, ULayer, URLayer, ZIO, ZLayer}

object Main extends zio.ZIOAppDefault {

  type EnvIn = MainRouter with UserRoutes

  def swaggerRoutes(routes: List[ZServerEndpoint[Any, Any]]): HttpRoutes[Task] =
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter()
          .fromServerEndpoints(routes, "Travel Designer", "1.1")
      )
      .toRoutes

  def makeLayer: ULayer[EnvIn] =
    ZLayer.make[EnvIn](
      MainRouter.live,
      UserRoutes.live,
      UserServiceImpl.live,
      UserDaoImpl.live,
      ZLayer.succeed(new UserEndpoints())
    )

  def getEndpoints(router: MainRouter, userRoutes: UserRoutes):  List[ZServerEndpoint[Any, Any]] =
    List(
      router.getUser,
      userRoutes.reg(),
      userRoutes.get(),
      userRoutes.update(),
      userRoutes.log(),
      userRoutes.delete()
    )
      .map(_.tag("Users"))

  def run: ZIO[Environment with Scope, Any, Any] =
    (for {
      mainRouter <- ZIO.service[MainRouter]
      userRoutes <- ZIO.service[UserRoutes]
      endpoints = getEndpoints(mainRouter, userRoutes)
      routes: HttpRoutes[Task] = ZHttp4sServerInterpreter()
        .from(endpoints)
        .toRoutes
      _ <-
        ZIO.executor.flatMap(executor =>
          BlazeServerBuilder[Task]
            .withExecutionContext(executor.asExecutionContext)
            .bindHttp(8081, "0.0.0.0")
            .withHttpApp(Router("/" -> (routes <+> swaggerRoutes(endpoints))).orNotFound)
            .serve
            .compile
            .drain
        )
    } yield ())
      .provideLayer(makeLayer)
}