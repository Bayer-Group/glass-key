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


trait BearerTokenAuthenticator[R] extends ContextAuthenticator[ValidatedData]  {
  import glasskey.resource.validation.PingIdentityProtectedResourceHandler

    def tokenValidators : Iterable[Validator[R]] = Iterable.empty
    def scheme: String
    def realm: String
    def handler: PingIdentityProtectedResourceHandler[R]
    def authenticator: AccessTokenAuthenticator
}

object BearerTokenAuthenticator {

    import scala.concurrent.{Future, ExecutionContext}
    def apply[R](validators : Iterable[Validator[R]] = Iterable.empty)(implicit ec: ExecutionContext): BearerTokenAuthenticator[R] = new BearerTokenAuthenticator[R] {

      import glasskey.resource.validation.PingIdentityProtectedResourceHandler
      override val tokenValidators : Iterable[Validator[R]] = validators
      override val handler = new PingIdentityProtectedResourceHandler[R](tokenValidators)
      override val scheme: String = OAuthConfig.providerConfig.authHeaderPrefix
      override val realm: String = "Secured Resource"
      override val authenticator: AccessTokenAuthenticator = DefaultAccessTokenAuthenticator(handler).validate
      override def apply(ctx: RequestContext): Future[Authentication[ValidatedData]] =
          authenticator(extractAccessToken(ctx)).map {
            case Some(restrictedUser) => Right(restrictedUser)
            case None =>
              import glasskey.model.AccessDenied
              Left(rejection(new AccessDenied(description = "User not found."), scheme, realm))
          }
    }
}