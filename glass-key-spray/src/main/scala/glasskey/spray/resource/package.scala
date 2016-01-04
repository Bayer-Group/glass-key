package glasskey.spray

import glasskey.config.OAuthConfig
import glasskey.model.fetchers.{IDTokenRequestParameter, IDTokenAuthHeader}
import glasskey.model.{OAuthException, OAuthTerms, OAuthAccessToken}
import glasskey.resource.OIDCTokenData
import spray.http.HttpHeaders.{Authorization, Cookie}
import spray.http.{HttpHeader, OAuth2BearerToken}
import spray.routing.{Rejection, RequestContext}

package object resource {
  import glasskey.model.ValidatedData
  import scala.concurrent.Future
  import spray.util._

  type AccessTokenAuthenticator = ((OAuthAccessToken, Option[OIDCTokenData])) => Future[Option[ValidatedData]]

  def idTokenHdr: IDTokenAuthHeader = new IDTokenAuthHeader.Default(OAuthConfig.providerConfig.jwksUri,
    OAuthConfig.providerConfig.idHeaderName,
    OAuthConfig.providerConfig.idHeaderPrefix)

  def idTokenParam: IDTokenRequestParameter = new IDTokenRequestParameter.Default(OAuthConfig.providerConfig.jwksUri)

  def extractAccessToken(ctx: RequestContext): (OAuthAccessToken, Option[OIDCTokenData]) =
    List(fromHeader(ctx), fromQueryParameter(ctx), fromCookie(ctx)).flatten.headOption match {
      case Some(tokenData) => tokenData
      case _ => throw new glasskey.model.InvalidRequest(description = "Access token missing.")
    }

  private def fromCookie(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    (getCookieByName(OAuthConfig.providerConfig.authCookieName, ctx), getCookieByName(OAuthConfig.providerConfig.authCookieName + "_OIDC", ctx)) match {
      case (Some(token), Some(oidc)) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), idTokenHdr.fetch(oidc)))
      case (Some(token), None) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), getIdTokenFromHeader(ctx)))
      case _ => None
    }

  private def getCookieByName(name: String, ctx: RequestContext): Option[String] =
    ctx.request.headers.find(h => h.name == "Cookie") match {
      case Some(Cookie(cookies)) =>
        cookies.filter(c => c.name == name) match {
          case Nil => None
          case filteredCookies => Some(filteredCookies.head.content)
        }
      case _ => None
    }

  private def fromHeader(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    ctx.request.headers.findByType[Authorization].flatMap {
      case Authorization(OAuth2BearerToken(token)) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), getIdTokenFromHeader(ctx)))
      case _ => None
    }

  def getIdTokenFromHeader(ctx: RequestContext): Option[OIDCTokenData] =
    ctx.request.headers.find(_.name.toLowerCase == OAuthConfig.providerConfig.idHeaderName.toLowerCase) match {
      case Some(header: HttpHeader) => idTokenHdr.fetch(header.value)
      case None => None
    }

  def getIdTokenFromQueryParameter(ctx: RequestContext): Option[OIDCTokenData] =
    ctx.request.uri.query.get(OAuthConfig.providerConfig.idHeaderPrefix) match {
      case Some(tokenStr) => idTokenParam.fetch(tokenStr)
      case None => None
    }

  def rejection(wrappedEx: OAuthException, scheme: String, realm: String): Rejection = {
    import glasskey.spray.model.OAuthRejection
    import spray.http.HttpChallenge
    import spray.http.HttpHeaders.`WWW-Authenticate`
    val header = `WWW-Authenticate`(HttpChallenge(scheme, realm, Map.empty)) :: Nil
    new OAuthRejection(wrappedEx, header)
  }

  private def fromQueryParameter(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    ctx.request.uri.query.get("access_token") match {
      case Some(tokenStr) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, tokenStr, None), getIdTokenFromQueryParameter(ctx)))
      case None => None
    }
}
