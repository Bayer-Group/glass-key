package glasskey.resource.validation

import glasskey.model.{ValidatedToken, OAuthAccessToken}
import glasskey.resource.OIDCTokenData

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by loande on 8/13/15.
 */
trait Validator[R] {
  def getValidationResponse(tokenData: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[R]
  def validate(response: Future[R])(implicit ec: ExecutionContext): Future[ValidatedToken]
}
