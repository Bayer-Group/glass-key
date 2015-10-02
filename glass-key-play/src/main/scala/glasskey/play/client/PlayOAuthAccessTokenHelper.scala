package glasskey.play.client

import glasskey.config.{OAuthConfig, ClientConfig}
import glasskey.model.{OAuthAccessTokenHelper, OAuthException, ValidationError, _}
import glasskey.play.resource.validation.PingIdentityAccessTokenValidatorFormats._
import play.api.Play.current
import play.api.http.HeaderNames
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.libs.ws.WS
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.mvc._
import play.mvc.Http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

class PlayOAuthAccessTokenHelper(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
                                 resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
                                 override val grantType: String, authUrl: String, override val accessTokenUri: String,
                                 providerWantsBasicAuth : Boolean) extends
OAuthAccessTokenHelper.Default(clientId, clientSecret, apiRedirectUri, resourceOwnerUsername,
  resourceOwnerPassword, grantType, authUrl, accessTokenUri, providerWantsBasicAuth)  {

  def this(clientConfig: ClientConfig) = this(clientConfig.clientId.get,
    clientConfig.clientSecret, clientConfig.apiRedirectUri, clientConfig.userName, clientConfig.userPassword,
    clientConfig.grantType.get, OAuthConfig.providerConfig.authUrl, OAuthConfig.providerConfig.accessTokenUrl,
    OAuthConfig.providerConfig.providerWantsBasicAuth)

  def generateToken(params: (String, String)*)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] = {
    val tokenResponse = WS.url(getQueryString(accessTokenUri, params: _*)).
      withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)

    val ifAuth = if (providerWantsBasicAuth) tokenResponse.withAuth(clientId, clientSecret.get, BASIC) else tokenResponse

    ifAuth.post(Results.EmptyContent()).flatMap {
      response => parseToken(response.json) match {
        case Left(x) => Future.successful(Some(x))
        case Right(x) => Future.successful(None)
      }
    }
  }
  def parseToken(value: JsValue): Either[OAuthAccessToken, OAuthException] = {
    value.validate[ValidationError] match {
      case s: JsSuccess[ValidationError] => Right(toOAuthErrorFromDescription(s.get.error_description))
      case e: JsError => Left(value.validate[OAuthAccessToken].get)
    }
  }
}

