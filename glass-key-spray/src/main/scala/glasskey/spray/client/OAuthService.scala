package glasskey.spray.client

import glasskey.model.InvalidToken
import glasskey.spray.resource.validation.PingValidationJsonProtocol
import spray.http.HttpCookie
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait OAuthService extends HttpService with SprayJsonSupport with OAuthRejectionExceptionHandler {

  import java.nio.channels.UnresolvedAddressException

  import PingValidationJsonProtocol._
  import _root_.spray.http.StatusCodes.InternalServerError
  import _root_.spray.httpx.marshalling.marshalUnsafe
  import _root_.spray.routing.directives.CachingDirectives.routeCache
  import glasskey.model.{OAuthAccessToken, OAuthAccessTokenHelper, ValidationError}
  import shapeless.HList
  import spray.can.Http.ConnectionAttemptFailedException
  import spray.httpx.UnsuccessfulResponseException
  import spray.httpx.unmarshalling.Unmarshaller
  import spray.routing._
  import spray.util.LoggingContext
  import scala.concurrent.duration.Duration
  import scala.language.implicitConversions

  val logctx: LoggingContext = LoggingContext.fromActorRefFactory

  implicit def executionContext = actorRefFactory.dispatcher

  def getPath[L <: HList](pm: PathMatcher[L]): Directive[L] = get & path(pm)

  def postPath[L <: HList](pm: PathMatcher[L]): Directive[L] = post & path(pm)

  val cache14min = routeCache(maxCapacity = 1000, timeToLive = Duration("14 min")) // Probably don't need to keep this

  implicit def exHandler(implicit log: LoggingContext): ExceptionHandler = ExceptionHandler {
    case e: UnsuccessfulResponseException =>
      log.error("Unsuccessful Response received")
      val error: ValidationError = Unmarshaller.unmarshalUnsafe[ValidationError](e.response.entity)
      complete((InternalServerError, error.copy(status_code = Some(e.response.status.intValue))))

    case e: UnresolvedAddressException =>
      log.error("Unresolved Address exception received")
      complete((InternalServerError, marshalUnsafe(
        new ValidationError("server_error", "Unresolved address blah", Some(InternalServerError.intValue)))))

    case e: ConnectionAttemptFailedException =>
      log.error("Connection attempt exception received")
      complete((InternalServerError, marshalUnsafe(
        new ValidationError("connection_error", e.getLocalizedMessage, Some(InternalServerError.intValue)))))
  }

  def useNewToken(callback: OAuthAccessToken => Route, uri: String)(implicit env: SprayClientRuntimeEnvironment): Route =
    getNewToken(env.tokenHelper, uri, callback)

  def getNewToken(tokenHelper: OAuthAccessTokenHelper, uri: String, callback: OAuthAccessToken => Route)(implicit env: SprayClientRuntimeEnvironment): Route = {
    import glasskey.model.{AuthorizationCode, ClientCredentials, GrantType, ResourceOwner}

    def useCallback(token: OAuthAccessToken) = {
      val sessId = java.util.UUID.randomUUID().toString
      env.daoService.set(sessId, token, Some(4)).map { _ => token } //TODO: Change this value - 4
      setCookie(HttpCookie("oauthSessionID", content = sessId)) {
        callback(token)
      }
    }

    GrantType.withName(tokenHelper.grantType) match {
      case AuthorizationCode =>
        logctx.debug("Request has no token, redirecting now..")
        setCookie(HttpCookie("oauth-state", content = tokenHelper.apiAuthState), HttpCookie("orig-url", content = uri)) {
          // This is because spray does not have session support built-in yet
          redirect(tokenHelper.authRequestUri, TemporaryRedirect)
        }

      case ClientCredentials =>
        onComplete(tokenHelper.generateClientCredentialsAccessToken) {
          case Success(token) => useCallback(token.get)
        }

      case ResourceOwner =>
        onComplete(tokenHelper.generateResourceOwnerAccessToken) {
          case Success(token) => useCallback(token.get)
        }

    }
  }

  def useExistingToken(successCallBack: OAuthAccessToken => Route)(implicit env: SprayClientRuntimeEnvironment): Route = { ctx =>
    optionalCookie("oauthSessionID") {
      case None => useNewToken(successCallBack, ctx.request.uri.toString())
      case Some(sessIdCookie) => onComplete(env.daoService.getAs(sessIdCookie.content)) {
        case Success(Some(token: OAuthAccessToken)) => successCallBack(token)
        case Success(None) => useNewToken(successCallBack, ctx.request.uri.toString())
        case Failure(t) => useNewToken(successCallBack, ctx.request.uri.toString())
      }
    }.apply(ctx)
  }

  def oauth2Redirect(implicit env: SprayClientRuntimeEnvironment): Route = { ctx =>
    parameters('code, 'state) { (code, state) =>
      val oldState = findCookieValue(ctx, "oauth-state")
      val origUrl = findCookieValue(ctx, "orig-url")

      if (state == oldState.get) {
        onComplete(env.tokenHelper.generateAuthCodeAccessToken(code, state)) {
          case Failure(ex) => complete((InternalServerError, s"Route Error Getting New Token Error: ${ex.getMessage}"))
          case Success(None) => complete((InternalServerError, "Could not generate token."))
          case Success(Some(token)) =>
            val sessId = java.util.UUID.randomUUID().toString
            env.daoService.set(sessId, token, token.expires_in).map { _ => token }
            setCookie(HttpCookie("oauthSessionID", content = sessId)) {
              redirect(origUrl.get, TemporaryRedirect)
            }
        }
      } else complete((InternalServerError, "Route Error Getting New Token Error: Auth States did not match. Possibly due to CSRF."))
    }.apply(ctx)
  }

  //Because we use multiple cookies (spray's directive assumes 1)
  private def findCookieValue(ctx: RequestContext, cookieName: String): Option[String] =
    ctx.request.cookies.find(_.name == cookieName).map(_.content)

  def refreshToken(refreshToken: String)(implicit env: SprayClientRuntimeEnvironment): Route = {
    import glasskey.spray.resource.validation.PingValidationJsonProtocol._
    onComplete(env.tokenHelper.refreshToken(refreshToken)) {
      case Failure(error) => complete(error)
      case Success(None) => complete(new InvalidToken("Refreshing token operation succeeded, but no token returned."))
      case Success(Some(refreshedToken)) => refreshedToken.refresh_token match {
          case Some(newRefreshToken) => complete(refreshedToken)
          case None => complete(refreshedToken.copy(refresh_token = Some(refreshToken)))
        }
      }
  }

  def oauthCall(implicit callToMake: (OAuthAccessToken) => Route, env: SprayClientRuntimeEnvironment): Route = {
    requestUri { uri =>
      handleOAuthRejections(oauthClientRejectionHandler(getNewToken(env.tokenHelper, uri.toString(), callToMake))) {
        useExistingToken(callToMake)
      }
    }
  }

  //This is a hack/copy of ExecutionDirective's handleRejections method, but allows for
  //a third retry, given that the first 2 cause Rejections. Otherwise, a rejection cannot
  //occur within another RejectionHandler
  def handleOAuthRejections(handler: RejectionHandler): Directive0 =
    mapRequestContext { ctx ⇒
      ctx withRejectionHandling { rejections ⇒
        val filteredRejections = RejectionHandler.applyTransformations(rejections)
        def handleRejection: PartialFunction[List[Rejection], Unit] = handler andThen (_(ctx.withContentNegotiationDisabled))

        if (handler isDefinedAt filteredRejections)
          handler(filteredRejections) {
            ctx.withContentNegotiationDisabled withRejectionHandling { r ⇒
              handleRejection(r) //Third retry
            }
          }
        else ctx.reject(filteredRejections: _*)
      }
    }

}