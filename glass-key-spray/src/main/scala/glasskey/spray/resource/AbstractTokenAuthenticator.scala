package glasskey.spray.resource

import glasskey.model.{ExpiredToken, OAuthException, OAuthAccessToken, ValidatedData}
import glasskey.resource.OIDCTokenData
import spray.routing.authentication.ContextAuthenticator

trait AbstractTokenAuthenticator extends ContextAuthenticator[ValidatedData] {

  import spray.routing.RequestContext
  import spray.routing.authentication.Authentication

  import scala.concurrent.Future

  def authenticator: AccessTokenAuthenticator

  override def apply(ctx: RequestContext): Future[Authentication[ValidatedData]]

  def extractAccessToken(ctx: RequestContext): Either[OAuthException, (OAuthAccessToken, Option[OIDCTokenData])]
}

object AbstractTokenAuthenticator {

  import scala.concurrent.{ExecutionContext, Future}

  abstract class Default[U](scheme: String, realm: String)(implicit ec: ExecutionContext)
    extends AbstractTokenAuthenticator {

    import glasskey.model.OAuthException
    import spray.routing.authentication.Authentication
    import spray.routing.{Rejection, RequestContext}

    def authenticator: AccessTokenAuthenticator

    override def apply(ctx: RequestContext): Future[Authentication[ValidatedData]] =
      extractAccessToken(ctx) match {
        case Left(e: OAuthException) => Future.successful(Left(rejection(e)))
        case Right(accessToken) => authenticator(accessToken).map {
          case Some(restrictedUser) => Right(restrictedUser)
          case None => import glasskey.model.AccessDenied
            Left(rejection(new AccessDenied(description = "User not found.")))
        } recoverWith {
          case e: ExpiredToken => Future.successful(Left(rejection(e)))
        }
      }

    def extractAccessToken(ctx: RequestContext): Either[OAuthException, (OAuthAccessToken, Option[OIDCTokenData])]

    private def rejection(wrappedEx: OAuthException): Rejection = {
      import glasskey.spray.model.OAuthRejection
      import spray.http.HttpChallenge
      import spray.http.HttpHeaders.`WWW-Authenticate`

      val header = `WWW-Authenticate`(HttpChallenge(scheme, realm, Map.empty)) :: Nil
      new OAuthRejection(wrappedEx, header)
    }
  }

}
