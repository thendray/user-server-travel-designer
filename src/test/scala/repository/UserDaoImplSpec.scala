package repository

import test.InitSchema
import model.User
import zio._
import zio.test._
import zio.test.Assertion._
import zio.interop.catz._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.scottweaver.models.JdbcInfo
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import zio.test.TestAspect.{after, before, sequential}

object UserDaoImplSpec extends ZIOSpecDefault {

  val postgresTestContainer = ZPostgreSQLContainer.live
  val defaultSettings = ZPostgreSQLContainer.Settings.default

  val xa = ZLayer.fromZIO {
    ZIO.serviceWithZIO[JdbcInfo] { jdbcInfo =>
      ZIO.attempt(
        Transactor.fromDriverManager[Task](
          driver = jdbcInfo.driverClassName,
          url = jdbcInfo.jdbcUrl,
          user = jdbcInfo.username,
          password = jdbcInfo.password,
          logHandler = None
        )
      )
    }
  }

  private def cleanTable =
    for {
      xa <- ZIO.service[Transactor[Task]]
      _ <- sql"TRUNCATE TABLE users RESTART IDENTITY CASCADE ".update.run.transact(xa)
      _ = println("clean table")
    } yield ()

  def initTable =
    ZIO.serviceWithZIO[Transactor[Task]] { xa =>
      for {
        _ <- InitSchema.apply("/users.sql", xa)
        _ = println("схема готова!")
      } yield ()
    }

  def spec =
    (suite("UserDaoImpl")(
//    test("addUser should add new user to the database") {
//      val testUser = User(
//        id = 0,
//        email = "test1@example.com",
//        username = "testuser",
//        password = "password123",
//        profilePhoto = "http://example.com/photo.jpg"
//      )
//
//      for {
//        dao <- ZIO.service[UserDaoImpl]
//        insertCount <- dao.addUser(testUser)
//        retrievedUser <- dao.getUserByEmail("test@example.com")
//        _ <- cleanTable
//      } yield assert(insertCount)(equalTo(1)) &&
//        assert(retrievedUser.map(_.email))(isSome(equalTo("test@example.com"))) &&
//        assert(retrievedUser.map(_.username))(isSome(equalTo("testuser")))
//    },
//
//    test("getUser should return the user with the specified id") {
//      val testUser = User(
//        id = 0,
//        email = "get2@example.com",
//        username = "getuser",
//        password = "password123",
//        profilePhoto = "None"
//      )
//
//      for {
//        dao <- ZIO.service[UserDaoImpl]
//        insertCount <- dao.addUser(testUser)
//        userByEmail <- dao.getUserByEmail("get2@example.com")
//        userId = userByEmail.map(_.id).get
//        retrievedUser <- dao.getUser(userId)
//        _ <- cleanTable
//      } yield assert(retrievedUser.map(_.email))(isSome(equalTo("get2@example.com"))) &&
//        assert(retrievedUser.map(_.username))(isSome(equalTo("getuser")))
//    },
//
//    test("updateUser should update existing user in database") {
//      val initialUser = User(
//        id = 0,
//        email = "update@example.com",
//        username = "updateuser",
//        password = "password123",
//        profilePhoto = "None"
//      )
//
//      for {
//        dao <- ZIO.service[UserDaoImpl]
//        _ <- dao.addUser(initialUser)
//        userByEmail <- dao.getUserByEmail("update@example.com")
//        userId = userByEmail.map(_.id).get
//        updatedUser = User(
//          id = userId,
//          email = "updated@example.com",
//          username = "updateduser",
//          password = "newpassword",
//          profilePhoto = "http://example.com/newphoto.jpg"
//        )
//        updateCount <- dao.updateUser(updatedUser)
//        retrievedUser <- dao.getUser(userId)
//        _ <- cleanTable
//      } yield assert(updateCount)(equalTo(1)) &&
//        assert(retrievedUser.map(_.email))(isSome(equalTo("updated@example.com"))) &&
//        assert(retrievedUser.map(_.username))(isSome(equalTo("updateduser")))
//    },
//
//    test("deleteUser should mark user as deleted") {
//      val testUser = User(
//        id = 0,
//        email = "delete@example.com",
//        username = "deleteuser",
//        password = "password123",
//        profilePhoto = "None"
//      )
//
//      for {
//        dao <- ZIO.service[UserDaoImpl]
//        _ <- dao.addUser(testUser)
//        userByEmail <- dao.getUserByEmail("delete@example.com")
//        userId = userByEmail.map(_.id).get
//        deleteCount <- dao.deleteUser(userId)
//        retrievedUser <- dao.getUser(userId)
//        _ <- cleanTable
//      } yield assert(deleteCount)(equalTo(1)) &&
//        assert(retrievedUser.map(_.email))(isSome(equalTo("deleted")))
//    }
  )
      @@ before(initTable) @@ sequential)
      .provide(xa, UserDaoImpl.live, postgresTestContainer, defaultSettings)
}
