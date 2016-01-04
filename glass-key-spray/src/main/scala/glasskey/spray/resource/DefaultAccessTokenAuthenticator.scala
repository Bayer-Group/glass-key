package glasskey.spray.resource

import glasskey.model.{ValidatedToken, ValidatedData, OAuthAccessToken}
import glasskey.resource.{ProtectedResourceHandler, ProtectedResource, OIDCTokenData}

trait DefaultAccessTokenAuthenticator {

  import glasskey.model.ValidatedData

  import scala.concurrent.{ExecutionContext, Future}
  import scala.language.implicitConversions

  val resource = ProtectedResource.apply
  val handler: ProtectedResourceHandler[ValidatedData, ValidatedToken]

  def attemptAuth(accessToken: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[Option[ValidatedData]] =
    resource.handleRequest[(OAuthAccessToken, Option[OIDCTokenData])](accessToken, handler).flatMap { authInfo: ValidatedData =>
      Future.successful(Some(authInfo))
    }

  def validate(implicit ec: ExecutionContext): AccessTokenAuthenticator =
    (accessToken) => attemptAuth(accessToken) flatMap Future.successful
}

object DefaultAccessTokenAuthenticator {

  import glasskey.resource.ProtectedResourceHandler

  def apply(resourceHandler: ProtectedResourceHandler[ValidatedData, ValidatedToken]): DefaultAccessTokenAuthenticator = new DefaultAccessTokenAuthenticator {
    override val handler: ProtectedResourceHandler[ValidatedData, ValidatedToken] = resourceHandler
  }
}
