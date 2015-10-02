package glasskey.resource.validation

import java.util.concurrent.atomic.AtomicInteger

import glasskey.model._
import glasskey.resource.{OIDCTokenData, ProtectedResourceHandler}

import scala.concurrent._
import scala.util.{Failure, Success}

/**
 * Created by loande on 12/1/2014.
 */

class PingIdentityProtectedResourceHandler[R](tokenValidators : Iterable[Validator[R]]) extends ProtectedResourceHandler[ValidatedData, ValidatedToken] {

  /**
   * Find authorized information by access token.
   *
   * @param accessToken This value is AccessToken.
   * @return Return authorized information if the parameter is available.
   */

  override def findValidatedData(accessToken: ValidatedToken): Future[Option[ValidatedData]] = Future.successful(Some(
    accessToken.access_token
  ))

  /**
   * Find AccessToken object by access token code.
   *
   * @param token Client sends access token which is created by system.
   * @return Return access token that matched the token.
   */

  // ERROR RESPONSE looks like : {"error":"invalid_grant","error_description":"token expired"}
  override def validateToken(token: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[ValidatedToken] =
    allSucceed[ValidatedToken](tokenValidators.map(validator =>
      validator.validate(validator.getValidationResponse(token))
    ))

  // Nondeterministic.
  // If any failure, return it immediately, else return the final success.
  private def allSucceed[T](fs: Iterable[Future[T]])(implicit ec: ExecutionContext): Future[T] = {
    val remaining = new AtomicInteger(fs.size)
    val p = Promise[T]()

    fs foreach {_ onComplete {
        case s@Success(_) => if (remaining.decrementAndGet() == 0) p tryComplete s // Arbitrarily return the final success
        case f@Failure(_) => p tryComplete f
      }
    }
    p.future
  }
}
