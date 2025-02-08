package utils

import doobie.util.transactor.Transactor
import zio.Task
import zio.interop.catz._


object CustomTransactor {

  val transactor: Transactor[Task] = Transactor.fromDriverManager[Task](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/mydatabase",
    user = "user",
    password = "password",
    logHandler = None
  )

}
