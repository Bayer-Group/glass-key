package glasskey.spray.client

import akka.actor.ActorSystem
import glasskey.config.{OAuthProviderConfig, ClientConfig}
import glasskey.model.{OAuthAccessToken, OAuthAccessTokenHelper}
import glasskey.spray.resource.validation.PingValidationJsonProtocol
import spray.http.BasicHttpCredentials
import spray.http.HttpHeaders.Authorization

import scala.concurrent.{ExecutionContext, Future}

class SprayOAuthAccessTokenHelper(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
  resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String], override val grantType: String,
  authUrl: String, override val accessTokenUri: String, providerWantsBasicAuth : Boolean)
extends OAuthAccessTokenHelper.Default(clientId, clientSecret, apiRedirectUri, resourceOwnerUsername,
  resourceOwnerPassword, grantType, authUrl, accessTokenUri, providerWantsBasicAuth)  {

  def this(clientConfig: ClientConfig, providerConfig: OAuthProviderConfig) = this(clientConfig.clientId.get,
    clientConfig.clientSecret, clientConfig.apiRedirectUri, clientConfig.userName, clientConfig.userPassword,
    clientConfig.grantType.get, providerConfig.authUrl, providerConfig.accessTokenUrl,
    providerConfig.providerWantsBasicAuth)

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
    if (providerWantsBasicAuth) headersToAdd :+ Authorization(new BasicHttpCredentials(clientId, clientSecret.get))

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
