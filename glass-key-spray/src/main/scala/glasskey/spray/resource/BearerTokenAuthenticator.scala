package glasskey.spray.resource

import glasskey.config.OAuthConfig
import glasskey.model.fetchers.{IDTokenAuthHeader, IDTokenRequestParameter}
import glasskey.model._
import glasskey.model.validation.UnauthorizedException
import glasskey.resource.OIDCTokenData
import glasskey.resource.validation.Validator
import spray.http.HttpHeaders.{Authorization, Cookie}
import spray.http.{HttpHeader, OAuth2BearerToken}
import spray.routing.{RequestContext, Rejection}
import spray.routing.authentication._

import scala.concurrent.ExecutionContext

trait BearerTokenExtractor {

  import spray.util._

  def idTokenHdr = new IDTokenAuthHeader.Default(OAuthConfig.providerConfig.jwksUri,
        OAuthConfig.providerConfig.idHeaderName,
        OAuthConfig.providerConfig.idHeaderPrefix)
  def idTokenParam = new IDTokenRequestParameter.Default(OAuthConfig.providerConfig.jwksUri)

  def extractAccessToken(ctx: RequestContext): Either[OAuthException, (OAuthAccessToken, Option[OIDCTokenData])] =
    List(fromCookie(ctx), fromHeader(ctx), fromQueryParameter(ctx)).partition(_.isDefined) match {
      case (Some(token) :: Nil, _) => Right(token)
      case (Nil, _) => Left(new glasskey.model.InvalidRequest(description = "Access token missing."))
      case _ => Left(new glasskey.model.InvalidToken())
    }

  def extractOrThrowEx(ctx: RequestContext): (OAuthAccessToken, Option[OIDCTokenData]) =
    extractAccessToken(ctx) match {
      case Left(ex) => throw new UnauthorizedException("Could not extract OAuth data from request context.")
      case Right((token, option)) => (token, option)
    }

  private def fromCookie(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    (getCookieByName(OAuthConfig.providerConfig.authCookieName, ctx), getCookieByName(OAuthConfig.providerConfig.authCookieName + "_OIDC", ctx)) match {
      case (Some(token), Some(oidc)) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), getIdTokenFromHeader(ctx)))
      case (Some(token), None) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, token, None), None))
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

  private def fromQueryParameter(ctx: RequestContext): Option[(OAuthAccessToken, Option[OIDCTokenData])] =
    ctx.request.uri.query.get("access_token") match {
      case Some(tokenStr) => Some((OAuthAccessToken(None, OAuthTerms.Bearer, None, tokenStr, None), getIdTokenFromQueryParameter(ctx)))
      case None => None
    }
}

trait BearerTokenAuthenticator[R] extends ContextAuthenticator[ValidatedData] with BearerTokenExtractor {


    import glasskey.model.OAuthException
    import glasskey.resource.validation.PingIdentityProtectedResourceHandler

    def tokenValidators : Iterable[Validator[R]] = Iterable.empty
    def scheme: String
    def realm: String
    def handler: PingIdentityProtectedResourceHandler[R]

    def authenticator: AccessTokenAuthenticator

    def rejection(wrappedEx: OAuthException, scheme: String, realm: String): Rejection = {
      import glasskey.spray.model.OAuthRejection
      import spray.http.HttpChallenge
      import spray.http.HttpHeaders.`WWW-Authenticate`
      val header = `WWW-Authenticate`(HttpChallenge(scheme, realm, Map.empty)) :: Nil
      new OAuthRejection(wrappedEx, header)
    }
}

object BearerTokenAuthenticator {

    import scala.concurrent.{Future, ExecutionContext}
    def apply[R](validators : Iterable[Validator[R]] = Iterable.empty)(implicit ec: ExecutionContext): BearerTokenAuthenticator[R] = new BearerTokenAuthenticator[R] {

      import glasskey.resource.validation.PingIdentityProtectedResourceHandler
      override val tokenValidators : Iterable[Validator[R]] = validators
      override val handler = new PingIdentityProtectedResourceHandler[R](tokenValidators)
      override val scheme: String = OAuthConfig.providerConfig.authHeaderPrefix
      override val realm: String = "Secured Resource"
      override val authenticator: AccessTokenAuthenticator = new DefaultAccessTokenAuthenticator.Default(handler)
      override def apply(ctx: RequestContext): Future[Authentication[ValidatedData]] =
        extractAccessToken(ctx) match {
          case Left(e: OAuthException) => Future.successful(Left(rejection(e, scheme, realm)))
          case Right(accessToken) => authenticator(accessToken).map {
            case Some(restrictedUser) => Right(restrictedUser)
            case None =>
              import glasskey.model.AccessDenied
              Left(rejection(new AccessDenied(description = "User not found."), scheme, realm))
          } recoverWith {
            case e: ExpiredToken => Future.successful(Left(rejection(e, scheme, realm)))
          }
        }
    }
}