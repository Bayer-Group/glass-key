package glasskey.play.client

import _root_.play.api.Logger
import _root_.play.api.Play.current
import _root_.play.api.http.HeaderNames
import _root_.play.api.libs.ws.{WS, WSRequestHolder, WSResponse}
import _root_.play.api.mvc._
import glasskey.config.OAuthConfig
import glasskey.play.resource.validation.PingIdentityAccessTokenValidatorFormats._
import play.api.libs.json.{JsError, JsSuccess}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

trait OAuthController extends Controller {

  import glasskey.model.OAuthAccessToken

  def doAction(request: Request[AnyContent],
                            origRequestURL: Option[String],
                            resourceUri: Option[String],
                            httpMethod: (WSRequestHolder) => Future[WSResponse],
                            finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result]

  def getHeaderedWSRequestHolder(token: OAuthAccessToken, resourceUri: Option[String])(implicit ec: ExecutionContext): Option[WSRequestHolder]

  def oauth2Redirect(codeOpt: Option[String] = None, stateOpt: Option[String] = None): Action[AnyContent]
}

object OAuthController {

  abstract class Default(val env: PlayClientRuntimeEnvironment) extends OAuthController {

    import glasskey.model._

    private def getExistingToken(request: Request[AnyContent])(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
      request.session.get("oauthSessionID") match {
        case Some(id) => env.daoService.getAs(id) flatMap Future.successful
        case None => Future.successful(None)
      }

    def doAction(request: Request[AnyContent],
                              origRequestURL: Option[String],
                              resourceUri: Option[String],
                              httpMethod: (WSRequestHolder) => Future[WSResponse],
                              finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] =
      doGrantAction(getExistingToken(request), origRequestURL, resourceUri, httpMethod, finishAction).recoverWith {
        case e: ExpiredToken => doGrantAction(Future.successful(None), origRequestURL, resourceUri, httpMethod, finishAction)
      }

    private def doGrantAction(tokenOpt: Future[Option[OAuthAccessToken]],
                             origRequestURL: Option[String],
                             resourceUri: Option[String],
                             httpMethod: (WSRequestHolder) => Future[WSResponse],
                             finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] = {
      tokenOpt flatMap {
        case Some(token) =>
          performAction(token, resourceUri, httpMethod, finishAction)

        case None =>
          GrantType.withName(env.tokenHelper.grantType) match {
            case AuthorizationCode =>
              Future.successful(
                Redirect(env.tokenHelper.authRequestUri).withSession(
                  "oauth-state" -> env.tokenHelper.apiAuthState,
                  "orig-url" -> origRequestURL.get))

            case ClientCredentials =>
              env.tokenHelper.generateClientCredentialsAccessToken flatMap {
                ccToken => performAction(ccToken.get, resourceUri, httpMethod, finishAction)
              }
            case ResourceOwner =>
              env.tokenHelper.generateResourceOwnerAccessToken flatMap {
                ccToken => performAction(ccToken.get, resourceUri, httpMethod, finishAction)
              }
          }
      }
    }

    private def performAction(token: OAuthAccessToken,
                                           resourceUri: Option[String],
                                           httpMethod: (WSRequestHolder) => Future[WSResponse],
                                           finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] = {
      getHeaderedWSRequestHolder(token, resourceUri) match {
        case Some(holder) =>
          httpMethod(holder).map { response =>
            (response.json).validate[ValidationError] match {
              case s: JsSuccess[ValidationError] => throw new ExpiredToken()
              case e: JsError => finishAction(response)
            }
          }

        case None =>
          Future.successful(BadRequest("No resource URI given to token helper."))
      }
    }

    def getHeaderedWSRequestHolder(token: OAuthAccessToken, resourceUri: Option[String])(implicit ec: ExecutionContext): Option[WSRequestHolder] =
      resourceUri match {
        case Some(url: String) =>
          val authHdr = grabAuthTokenHeader(token.access_token)
          val headers = grabIDTokenHeader(token.id_token) match {
            case Some(x) => Seq(x, authHdr)
            case None => Seq(authHdr)
          }

          Some(WS.url(resourceUri.get).withHeaders(headers: _*))

        case None => None
      }

    private def grabAuthTokenHeader: PartialFunction[(String), (String, String)] = {
      case (data) => HeaderNames.AUTHORIZATION -> (s"${OAuthConfig.providerConfig.authHeaderPrefix} " + data)
    }

    private def grabIDTokenHeader: PartialFunction[(Option[String]), Option[(String, String)]] = {
      case Some(data) => Some(OAuthConfig.providerConfig.idHeaderName -> (s"${OAuthConfig.providerConfig.idHeaderPrefix} " + data))
      case None => None
    }

    def oauth2Redirect(codeOpt: Option[String] = None, stateOpt: Option[String] = None): Action[AnyContent] = {
      import play.api.libs.concurrent.Execution.Implicits._
      def doLogin(implicit request: Request[AnyContent]): Option[Future[Result]] =
        for {
          code <- codeOpt
          state <- stateOpt
          oauthState <- request.session.get("oauth-state")
          origRequestURL <- request.session.get("orig-url")
        } yield {
          Logger.debug("State is : " + state + " and stored state is : " + oauthState)

          if (state == oauthState)
            generateToken(code, state, origRequestURL)
          else
            Future.successful(BadRequest("Invalid github login"))
        }

      def generateToken(code: String, state: String, origRequestURL: String): Future[Result] =
        env.tokenHelper.generateAuthCodeAccessToken(code, state).map {
          case Some(token: OAuthAccessToken) =>
            val sessId = java.util.UUID.randomUUID().toString
            env.daoService.set(sessId, token, token.expires_in).map { _ => token }
            Redirect(origRequestURL).withSession("oauthSessionID" -> sessId)

          case None =>
            Unauthorized("Could not generate access token.")

        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }

      Action.async { implicit request =>
        doLogin.getOrElse(Future.successful(BadRequest("No parameters supplied")))
      }
    }
  }

}
