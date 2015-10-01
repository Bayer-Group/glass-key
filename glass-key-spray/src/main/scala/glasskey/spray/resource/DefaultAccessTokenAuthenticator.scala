package glasskey.spray.resource

import glasskey.RuntimeEnvironment
import glasskey.model.{ValidatedToken, ValidatedData, ValidatedAccessToken, OAuthAccessToken}
import glasskey.resource.OIDCTokenData

trait DefaultAccessTokenAuthenticator extends AccessTokenAuthenticator {

  import glasskey.model.ValidatedData

  import scala.concurrent.{ExecutionContext, Future}
  import scala.language.implicitConversions

  def attemptAuth(accessToken: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[Option[ValidatedData]]
}

object DefaultAccessTokenAuthenticator {

  import glasskey.resource.ProtectedResourceHandler

  import scala.concurrent.ExecutionContext

  class Default(handler: ProtectedResourceHandler[ValidatedData, ValidatedToken], env: RuntimeEnvironment)(implicit val ec: ExecutionContext)
    extends AccessTokenAuthenticator with DefaultAccessTokenAuthenticator {

    import glasskey.resource.ProtectedResource

    import scala.concurrent.Future

    val resource = new ProtectedResource.Default(env)

    override def apply(accessToken: (OAuthAccessToken, Option[OIDCTokenData])): Future[Option[ValidatedData]] =
      attemptAuth(accessToken) flatMap Future.successful

    def attemptAuth(accessToken: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[Option[ValidatedData]] =
      resource.handleRequest[(OAuthAccessToken, Option[OIDCTokenData])](accessToken, handler).flatMap { authInfo: ValidatedData =>
        Future.successful(Some(authInfo))
      }
  }

}
