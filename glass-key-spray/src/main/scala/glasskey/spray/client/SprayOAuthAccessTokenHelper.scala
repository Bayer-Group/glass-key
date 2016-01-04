package glasskey.spray.client

import akka.actor.ActorSystem
import glasskey.config.{OAuthConfig, ClientConfig}
import glasskey.model._
import glasskey.spray.resource.validation.PingValidationJsonProtocol
import spray.http.BasicHttpCredentials
import spray.http.HttpHeaders.Authorization

import scala.concurrent.{ExecutionContext, Future}

trait SprayOAuthAccessTokenHelper extends OAuthAccessTokenHelper {

  def accessTokenUri: String
  def providerWantsBasicAuth: Boolean

  implicit val system = ActorSystem()

  def generateToken(params: (String, String)*)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] = {
    import PingValidationJsonProtocol._
    import spray.client.pipelining._
    import spray.http.HttpHeaders.Accept
    import spray.http.HttpRequest
    import spray.http.MediaTypes.`application/json`
    import spray.httpx.SprayJsonSupport._
    import spray.httpx.encoding.{Deflate, Gzip}

    val headersToAdd = List(Accept(`application/json`))
    if (providerWantsBasicAuth) headersToAdd :+ Authorization(new BasicHttpCredentials(clientId, clientSecret))

    val pipeline: HttpRequest => Future[Option[OAuthAccessToken]] = (
      encode(Gzip)
        ~> addHeaders(headersToAdd)
        ~> sendReceive
        ~> decode(Deflate) ~> decode(Gzip)
        ~> unmarshal[Option[OAuthAccessToken]]
      )

    pipeline {
      Post(getQueryString(accessTokenUri, params: _*))
    }
  }

  //  override def getQueryString(uri: String, params: (String, String)*): String = {
  //    Uri(uri) withQuery(params:_*) toString
  //  } Could be done this way in Spray if desired
}

object SprayOAuthAccessTokenHelper {
  def apply(id: String, secret: Option[String], redirect: Option[String],
            roUser: Option[String], roPass: Option[String],
            authorizationUrl: String, tokenUrl: String,
            basic: Boolean, grantType: GrantType): SprayOAuthAccessTokenHelper =
    grantType match {
      case AuthorizationCode => authCodeHelper(id, secret.get, authorizationUrl, redirect.get,
        tokenUrl, basic)
      case ResourceOwner => resourceOwnerHelper(id, secret.get, roUser.get,
        roPass.get, tokenUrl, basic)
      case RefreshToken => refreshHelper(id, secret.get, tokenUrl, basic)
      case ClientCredentials => clientCredentialsHelper(id, secret.get, tokenUrl, basic)
    }

  def authCodeHelper(id: String, secret: String, authorizationUrl: String, redirect: String, tokenUrl: String,
                     basic: Boolean) : SprayOAuthAccessTokenHelper with AuthCodeAccessTokenHelper =
    new SprayOAuthAccessTokenHelper with AuthCodeAccessTokenHelper {
      override def accessTokenUri: String = tokenUrl
      override def providerWantsBasicAuth: Boolean = basic
      override var redirectUri: String = redirect
      override def authUrl: String = authorizationUrl
      override def clientSecret: String = secret
      override def clientId: String = id
    }

  def resourceOwnerHelper(id: String, secret: String, roUser: String,
                          roPass: String, tokenUrl: String,
                          basic: Boolean): SprayOAuthAccessTokenHelper with ResourceOwnerAccessTokenHelper =
    new SprayOAuthAccessTokenHelper with ResourceOwnerAccessTokenHelper {
      override def resourceOwnerUsername: String = roUser
      override def resourceOwnerPassword: String = roPass
      override def clientSecret: String = secret
      override def clientId: String = id
      override def accessTokenUri: String = tokenUrl
      override def providerWantsBasicAuth: Boolean = basic
    }

  def refreshHelper(id: String, secret: String, tokenUrl: String,
                    basic: Boolean): SprayOAuthAccessTokenHelper with RefreshAccessTokenHelper =
    new SprayOAuthAccessTokenHelper with RefreshAccessTokenHelper {
      override def clientSecret: String = secret
      override def clientId: String = id
      override def accessTokenUri: String = tokenUrl
      override def providerWantsBasicAuth: Boolean = basic
    }

  def clientCredentialsHelper(id: String, secret: String, tokenUrl: String,
                              basic: Boolean): SprayOAuthAccessTokenHelper with OAuthAccessTokenHelper =
    new SprayOAuthAccessTokenHelper with  OAuthAccessTokenHelper {
      override def grantType: String = ClientCredentials.name
      override def clientSecret: String = secret
      override def clientId: String = id
      override def accessTokenUri: String = tokenUrl
      override def providerWantsBasicAuth: Boolean = basic
    }

  def apply(clientConfig: ClientConfig): SprayOAuthAccessTokenHelper = apply(clientConfig.clientId.get,
    clientConfig.clientSecret, clientConfig.apiRedirectUri, clientConfig.userName, clientConfig.userPassword,
    OAuthConfig.providerConfig.authUrl, OAuthConfig.providerConfig.accessTokenUrl,
    OAuthConfig.providerConfig.providerWantsBasicAuth, GrantType.withName(clientConfig.grantType.get))
}