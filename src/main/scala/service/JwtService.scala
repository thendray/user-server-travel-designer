package service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.{JWTVerificationException, TokenExpiredException}
import zio._
import java.time.{temporal, Instant}
import java.util.Date

object JwtService {
  private val SECRET = "your-secret-key"
  private val ISSUER = "your-application"
  private val algorithm = Algorithm.HMAC256(SECRET)

  private val DEFAULT_TOKEN_EXPIRATION = 12.hour

  def createToken(userId: String, expiration: Duration = DEFAULT_TOKEN_EXPIRATION): Task[TokenInfo] = ZIO.attempt {
    val now = Instant.now()
    val expiresAt = now.plusSeconds(expiration.toSeconds)
    val claim: Double = expiresAt.getEpochSecond

    val token = JWT
      .create()
      .withIssuer(ISSUER)
      .withSubject(userId)
      .withIssuedAt(Date.from(now))
      .withExpiresAt(Date.from(expiresAt))
      .withClaim("exp_time", claim)
      .sign(algorithm)

    TokenInfo(
      token = token,
      expiresAt = expiresAt.getEpochSecond,
      tokenType = "Bearer"
    )
  }

  def verifyToken(token: String): Task[TokenPayload] = {
    ZIO
      .attempt {
        val verifier = JWT
          .require(algorithm)
          .withIssuer(ISSUER)
          .build()

        val decoded = verifier.verify(token)
        val userId = decoded.getSubject
        val expiresAt = decoded.getClaim("exp_time").asLong()

        TokenPayload(
          userId = userId,
          expiresAt = expiresAt
        )
      }
      .mapError {
        case e: TokenExpiredException => new JWTVerificationException("Token expired", e)
        case e => new JWTVerificationException("Invalid token", e)
      }
  }

  // Проверка валидности токена (не истек ли срок)
  def isTokenValid(token: String): Task[Boolean] = {
    verifyToken(token)
      .map(_ => true)
      .catchAll(_ => ZIO.succeed(false))
  }

  // Получение оставшегося времени жизни токена (в секундах)
  def getRemainingTime(token: String): Task[Long] = {
    verifyToken(token).map { payload =>
      val now = Instant.now().getEpochSecond
      val remaining = payload.expiresAt - now
      if (remaining < 0) 0L else remaining
    }
  }
}

// Модели для работы с токенами
case class TokenInfo(
    token: String,
    expiresAt: Long,
    tokenType: String)

case class TokenPayload(
    userId: String,
    expiresAt: Long)
