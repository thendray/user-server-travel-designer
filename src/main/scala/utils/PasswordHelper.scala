package utils

import com.github.t3hnar.bcrypt._

object PasswordHelper {

  def hashString(password: String): String =
    password.boundedBcrypt

  def checkData(data: String, hash: String): Boolean =
    data.isBcryptedSafeBounded(hash).toOption.nonEmpty
}