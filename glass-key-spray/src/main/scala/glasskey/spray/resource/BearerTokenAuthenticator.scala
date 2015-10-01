package glasskey.spray.resource

import glasskey.model.fetchers.{IDTokenAuthHeader, IDTokenRequestParameter}
import glasskey.model.{OAuthAccessToken, OAuthTerms}
import spray.http.HttpHeaders.Cookie

import scala.concurrent.ExecutionContext

trait BearerTokenAuthenticator[R] extends AbstractTokenAuthenticator {

  import glasskey.model.OAuthException
  import glasskey.resource.OIDCTokenData
  import glasskey.resource.validation.PingIdentityProtectedResourceHandler
  import spray.http.HttpHeaders.Authorization
  import spray.http.{HttpHeader, OAuth2BearerToken}
  import spray.routing.RequestContext
  import spray.util._

  implicit val env: SprayResourceRuntimeEnvironment[R]
  def handler: PingIdentityProtectedResourceHandler[R]

  def idTokenHdr : IDTokenAuthHeader
  def idTokenParam : IDTokenRequestParameter

  override def extractAccessToken(ctx: RequestContext): Either[OAuthException, (OAuthAccessToken, Option[OIDCTokenData])] =
    List(fromCookie(ctx), fromHeader(ctx), fromQueryParameter(ctx)).partition(_.isDefined) match {
      case (Some(token) :: Nil, _) => Right(token)
      case (Nil, _) => Left(new glasskey.model.InvalidRequest(description = "Access token missing."))
      case _ => Left(new glasskey.model.InvalidToken())
    }

  private def fromCookie(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    (getCookieByName(env.config.providerConfig.authCookieName, ctx), getCookieByName(env.config.providerConfig.authCookieName + "_OIDC", ctx)) match {
      case (Some(token), Some(oidc)) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), getIdTokenFromHeader(ctx)))
      case (Some(token), None) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), None))
      case _ => None
    }

  private def getCookieByName(name: String, ctx: RequestContext): Option[String] =
    ctx.request.headers.find(h => h.name == "Cookie") match {
      case Some(Cookie(cookies)) =>
        cookies.filter(c => c.name == name) match {
          case Nil => None
          case cookies => Some(cookies.head.content)
        }
      case _ => None
    }

  private def fromHeader(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    ctx.request.headers.findByType[Authorization].flatMap {
      case Authorization(OAuth2BearerToken(token)) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), getIdTokenFromHeader(ctx)))
      case _ => None
    }

  def getIdTokenFromHeader(ctx: RequestContext): Option[OIDCTokenData] =
    ctx.request.headers.find(_.name.toLowerCase == env.config.providerConfig.idHeaderName.toLowerCase) match {
      case Some(header: HttpHeader) => idTokenHdr.fetch(header.value)
      case None => None
    }

  def getIdTokenFromQueryParameter(ctx: RequestContext): Option[OIDCTokenData] =
    ctx.request.uri.query.get(env.config.providerConfig.idHeaderPrefix) match {
      case Some(tokenStr) => idTokenParam.fetch(tokenStr)
      case None => None
    }

  private def fromQueryParameter(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    ctx.request.uri.query.get("access_token") match {
      case Some(tokenStr) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, tokenStr, None), getIdTokenFromQueryParameter(ctx)))
      case None => None
    }
}

object BearerTokenAuthenticator {

  import glasskey.model.ValidatedData

  class Default[R](realm: String = "Secured Resource")(implicit ec: ExecutionContext,
                                                    implicit override val env: SprayResourceRuntimeEnvironment[R])
    extends AbstractTokenAuthenticator.Default[ValidatedData](env.config.providerConfig.authHeaderPrefix, realm) with BearerTokenAuthenticator[R] {

    import glasskey.model.fetchers.IDTokenAuthHeader
    import glasskey.resource.validation.PingIdentityProtectedResourceHandler

    override val idTokenHdr = new IDTokenAuthHeader.Default(env.config.providerConfig.jwksUri,
      env.config.providerConfig.idHeaderName,
      env.config.providerConfig.idHeaderPrefix)
    override val idTokenParam = new IDTokenRequestParameter.Default(env.config.providerConfig.jwksUri)
    override val handler = new PingIdentityProtectedResourceHandler[R](env.tokenValidators)

    override def authenticator: AccessTokenAuthenticator = new DefaultAccessTokenAuthenticator.Default(handler, env)

  }

}
