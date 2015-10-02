package glasskey.resource

import glasskey.config.OAuthConfig
import glasskey.model.fetchers.Cookie
import glasskey.model.{ValidatedToken, OAuthAccessToken}

trait ProtectedResource {

  import glasskey.model.fetchers.{AuthHeader, RequestParameter}
  import glasskey.model.{InvalidRequest, InvalidToken, ValidatedData, ProtectedResourceRequest}

  import scala.concurrent.{ExecutionContext, Future}

  val fetchers = Seq(new AuthHeader.Default, new RequestParameter.Default(OAuthConfig.providerConfig.jwksUri), new Cookie.Default(OAuthConfig.providerConfig.authCookieName))

  def handleRequest[R](request: R, handler: ProtectedResourceHandler[ValidatedData, ValidatedToken])(implicit ec: ExecutionContext): Future[ValidatedData] =
    request match {
      case r: ProtectedResourceRequest => handleProtectedResourceRequest(r, handler)
      case t: (OAuthAccessToken, Option[OIDCTokenData]) => handleToken(t, handler)
    }

  private def handleToken(token: (OAuthAccessToken, Option[OIDCTokenData]), handler: ProtectedResourceHandler[ValidatedData, ValidatedToken])(implicit ec: ExecutionContext): Future[ValidatedData] = {
    handler.validateToken(token).flatMap { maybeToken =>
      handler
        .findValidatedData(maybeToken)
        .map(_.getOrElse(throw new InvalidToken("The access token is invalid")))
    }
  }

  private def handleProtectedResourceRequest(request: ProtectedResourceRequest,
                                             handler: ProtectedResourceHandler[ValidatedData, ValidatedToken])(implicit ec: ExecutionContext): Future[ValidatedData] =
    fetchers
      .find(fetcher => fetcher.matches(request))
      .map(fetcher => handleToken(fetcher.fetch(request), handler))
      .getOrElse(throw new InvalidRequest("Access token is not found"))
}

object ProtectedResource {

  class Default extends ProtectedResource

}
