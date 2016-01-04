package glasskey.play

import glasskey.config.OAuthConfig
import glasskey.model.{ExpiredToken, ValidationError, OAuthAccessToken}
import play.api.http.HeaderNames
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.{WS, WSResponse, WSRequestHolder}
import play.api.mvc.Result
import play.api.Play.current
import glasskey.play.resource.validation.PingIdentityAccessTokenValidatorFormats._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by loande on 12/18/15.
  */
package object client {
  def performAction(token: OAuthAccessToken,
                            resourceUri: String,
                            httpMethod: (WSRequestHolder) => Future[WSResponse],
                            finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] = {
    val holder = getHeaderedWSRequestHolder(token, resourceUri)

    httpMethod(holder).map { response =>
      (response.json).validate[ValidationError] match {
        case s: JsSuccess[ValidationError] => throw new ExpiredToken()
        case e: JsError => finishAction(response)
      }
    }
  }

  def getHeaderedWSRequestHolder(token: OAuthAccessToken, resourceUri: String)(implicit ec: ExecutionContext): WSRequestHolder = {
    val authHdr = grabAuthTokenHeader(token.access_token)
    val headers = grabIDTokenHeader(token.id_token) match {
      case Some(x) => Seq(x, authHdr)
      case None => Seq(authHdr)
    }
    WS.url(resourceUri).withHeaders(headers: _*)
  }

  private def grabAuthTokenHeader: PartialFunction[(String), (String, String)] = {
    case (data) => HeaderNames.AUTHORIZATION -> (s"${OAuthConfig.providerConfig.authHeaderPrefix} " + data)
  }

  private def grabIDTokenHeader: PartialFunction[(Option[String]), Option[(String, String)]] = {
    case Some(data) => Some(OAuthConfig.providerConfig.idHeaderName -> (s"${OAuthConfig.providerConfig.idHeaderPrefix} " + data))
    case None => None
  }
}
