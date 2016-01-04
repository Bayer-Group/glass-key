package glasskey.play.client

import glasskey.config.{OAuthConfig, ClientConfig}
import glasskey.model.{OAuthAccessTokenHelper, OAuthException, ValidationError, _}
import glasskey.play.resource.validation.PingIdentityAccessTokenValidatorFormats._
import play.api.Play.current
import play.api.http.HeaderNames
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.libs.ws.{WSResponse, WSRequestHolder, WS}
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.mvc._
import play.mvc.Http.MimeTypes

import scala.concurrent.{ExecutionContext, Future}

trait PlayOAuthAccessTokenHelper extends OAuthAccessTokenHelper with Results {

  def accessTokenUri: String
  def providerWantsBasicAuth: Boolean

  def generateToken(params: (String, String)*)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] = {
    val tokenResponse = WS.url(getQueryString(accessTokenUri, params: _*)).
    withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON)

    val ifAuth = if (providerWantsBasicAuth) tokenResponse.withAuth(clientId, clientSecret, BASIC) else tokenResponse

    ifAuth.post(Results.EmptyContent()).flatMap { response =>
      parseToken(response.json) match {
        case Left(x) => Future.successful(Some(x))
        case Right(x) => Future.successful(None)
      }
    }
  }

  def parseToken(value: JsValue): Either[OAuthAccessToken, OAuthException] =
    value.validate[ValidationError] match {
      case s: JsSuccess[ValidationError] => Right(toOAuthErrorFromDescription(s.get.error_description))
      case e: JsError => Left(value.validate[OAuthAccessToken].get)
    }

  def getResult(resourceUri: String, httpMethod: (WSRequestHolder) => Future[WSResponse],
                finishAction: (WSResponse) => Result)(implicit ec: ExecutionContext): Future[Result] =
    generateToken(tokenParams: _*) flatMap {
      ccToken => performAction(ccToken.get, resourceUri, httpMethod, finishAction)
    }
}

object PlayOAuthAccessTokenHelper {

  def apply(id: String, secret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            authUrl: String, tokenUri: String,
            basic: Boolean, grantType: GrantType): PlayOAuthAccessTokenHelper =
    grantType match {
      case AuthorizationCode => AuthCodePlayAccessTokenHelper(id, secret.get, authUrl, apiRedirectUri.get,
        tokenUri, basic)
      case ResourceOwner => resourceOwnerHelper(id, secret.get, resourceOwnerUsername.get,
        resourceOwnerPassword.get, tokenUri, basic)
      case RefreshToken => refreshHelper(id, secret.get, tokenUri, basic)
      case ClientCredentials => clientCredentialsHelper(id, secret.get, tokenUri, basic)
    }

  def resourceOwnerHelper(id: String, secret: String, roUser: String,
                          roPass: String, tokenUri: String,
                          basic: Boolean): PlayOAuthAccessTokenHelper with ResourceOwnerAccessTokenHelper =
    new PlayOAuthAccessTokenHelper with ResourceOwnerAccessTokenHelper {
      override def resourceOwnerUsername: String = roUser
      override def resourceOwnerPassword: String = roPass
      override def clientSecret: String = secret
      override def clientId: String = id
      override def accessTokenUri: String = tokenUri
      override def providerWantsBasicAuth: Boolean = basic
    }

  def refreshHelper(id: String, secret: String, tokenUri: String,
                    basic: Boolean): PlayOAuthAccessTokenHelper with RefreshAccessTokenHelper =
    new PlayOAuthAccessTokenHelper with RefreshAccessTokenHelper {
      override def clientSecret: String = secret
      override def clientId: String = id
      override def accessTokenUri: String = tokenUri
      override def providerWantsBasicAuth: Boolean = basic
    }

  def clientCredentialsHelper(id: String, secret: String, tokenUri: String,
                              basic: Boolean): PlayOAuthAccessTokenHelper with OAuthAccessTokenHelper =
    new PlayOAuthAccessTokenHelper with  OAuthAccessTokenHelper {
      override def grantType: String = ClientCredentials.name
      override def clientSecret: String = secret
      override def clientId: String = id
      override def accessTokenUri: String = tokenUri
      override def providerWantsBasicAuth: Boolean = basic
    }

  def apply(clientConfig: ClientConfig): PlayOAuthAccessTokenHelper = apply(clientConfig.clientId.get,
      clientConfig.clientSecret, clientConfig.apiRedirectUri, clientConfig.userName, clientConfig.userPassword,
      OAuthConfig.providerConfig.authUrl, OAuthConfig.providerConfig.accessTokenUrl,
      OAuthConfig.providerConfig.providerWantsBasicAuth, GrantType.withName(clientConfig.grantType.get))
}



