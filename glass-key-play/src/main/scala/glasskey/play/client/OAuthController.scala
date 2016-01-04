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

  def env: PlayClientRuntimeEnvironment

  def doAction(request: Request[AnyContent],
               resourceUri: String,
               httpMethod: (WSRequestHolder) => Future[WSResponse],
               finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result]

  def oauth2Redirect(code: String, state: String): Action[AnyContent]
}

object OAuthController {

  abstract class Default extends OAuthController {

    import glasskey.model._

    private def getExistingToken(request: Request[AnyContent])(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
      request.session.get("oauthSessionID") match {
        case Some(id) => env.daoService.getAs(id) flatMap Future.successful
        case None => Future.successful(None)
      }

    def doAction(request: Request[AnyContent],
                 resourceUri: String,
                 httpMethod: (WSRequestHolder) => Future[WSResponse],
                 finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] =
      (for {
        existingToken <- getExistingToken(request)
        actionResult <- doGrantAction(existingToken, resourceUri, httpMethod, finishAction)
      } yield actionResult) recoverWith {
        case e: ExpiredToken => doGrantAction(None, resourceUri, httpMethod, finishAction)
      }

    private def doGrantAction(tokenOpt: Option[OAuthAccessToken],
                             resourceUri: String,
                             httpMethod: (WSRequestHolder) => Future[WSResponse],
                             finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] = {
      tokenOpt match {
        case Some(token) =>
          performAction(token, resourceUri, httpMethod, finishAction)
        case None =>
          env.tokenHelper.getResult(resourceUri, httpMethod, finishAction)
      }
    }

    def oauth2Redirect(code: String, state: String): Action[AnyContent] = {
      import play.api.libs.concurrent.Execution.Implicits._
      def doLogin(implicit request: Request[AnyContent]): Option[Future[Result]] =
        for {
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
        env.tokenHelper.generateToken(env.tokenHelper.asInstanceOf[AuthCodeAccessTokenHelper].authCodeParams(code): _*).map {
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
