package glasskey.resource

import glasskey.model.OAuthAccessToken

import scala.concurrent.ExecutionContext

/**
 *
 * Provide access to <b>Protected Resource</b> phase support for using OAuth 2.0.
 *
 * <h3>[Access to Protected Resource phase]</h3>
 * <ul>
 * <li>findAccessToken(token)</li>
 * <li>findAuthInfoByAccessToken(token)</li>
 * </ul>
 */
trait ProtectedResourceHandler[+U, T] {

  import scala.concurrent.Future

  /**
   * Find authorized information by access token.
   *
   * @param accessToken This value is AccessToken.
   * @return Return authorized information if the parameter is available.
   */
  def findValidatedData(accessToken: T): Future[Option[U]]

  /**
   * Find AccessToken object by access token code.
   *
   * @param token Client sends access token which is created by system.
   * @return Return access token that matched the token.
   */
  def validateToken(token: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[T]
}
