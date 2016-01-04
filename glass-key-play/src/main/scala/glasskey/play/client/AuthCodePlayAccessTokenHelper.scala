package glasskey.play.client

import glasskey.model.AuthCodeAccessTokenHelper
import play.api.libs.ws.{WSResponse, WSRequestHolder}
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by loande on 12/18/15.
  */
trait AuthCodePlayAccessTokenHelper extends PlayOAuthAccessTokenHelper with AuthCodeAccessTokenHelper {

  def authRequestUri: String

  override def getResult(resourceUri: String,
                httpMethod: (WSRequestHolder) => Future[WSResponse],
                finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] =
    Future.successful(
      Redirect(authRequestUri).withSession(
        "oauth-state" -> apiAuthState,
        "orig-url" -> redirectUri))
}

object AuthCodePlayAccessTokenHelper {
  def apply(id: String, secret: String, authCodeUrl: String, redirect: String, tokenUrl: String,
            basic: Boolean): AuthCodePlayAccessTokenHelper =
    new AuthCodePlayAccessTokenHelper {
      override var redirectUri: String = redirect
      override def clientSecret: String = secret
      override def authUrl: String = authCodeUrl
      override def clientId: String = id
      override def accessTokenUri: String = tokenUrl
      override def providerWantsBasicAuth: Boolean = basic
    }
}