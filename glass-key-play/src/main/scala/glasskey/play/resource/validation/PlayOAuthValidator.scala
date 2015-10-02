package glasskey.play.resource.validation

import glasskey.model.OAuthAccessToken
import glasskey.resource.OIDCTokenData
import glasskey.resource.validation.OAuthValidator
import play.api.libs.ws.WSResponse
import glasskey.config.OAuthConfig
import glasskey.config.ClientConfig

import scala.concurrent.ExecutionContext

trait PlayOAuthValidator extends OAuthValidator[WSResponse] {
  import glasskey.model.ValidatedAccessToken
  import play.api.Logger

  import scala.concurrent.Future

  def validationUrl: String

  override def getValidationResponse(tokenData: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[WSResponse] = {
    import com.ning.http.client.AsyncHttpClientConfig
    import play.api.Play.current
    import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}
    import play.api.libs.ws.{DefaultWSClientConfig, WS, WSRequestHolder}

    val queryString = validationParams(tokenData._1.access_token)
      .foldLeft("")((a, t) => a + (t._1 + "=" + t._2 + "&"))
      .dropRight(1)

    Logger.debug("Sending " + queryString + " as the body to " + validationUrl)

    // You can directly use the builder for specific options once you have secure TLS defaults...
    val builder = new AsyncHttpClientConfig.Builder(new NingAsyncHttpClientConfigBuilder(new DefaultWSClientConfig()).build())
      .setCompressionEnabled(true)

    implicit val sslClient = new NingWSClient(builder.build())
    val holder: WSRequestHolder = WS.url(validationUrl).withHeaders("Content-Type" -> "application/x-www-form-urlencoded")

    holder.post(queryString)
  }

  override def validate(resp: Future[WSResponse])(implicit ec: ExecutionContext): Future[ValidatedAccessToken] =
    resp.map { response => {
      import PingIdentityAccessTokenValidatorFormats._
      import glasskey.model.ValidationError
      import play.api.libs.json.{JsError, JsSuccess}

      Logger.debug("Received validation response of: " + response.json)

      response.json.validate[ValidationError] match {
        case s: JsSuccess[ValidationError] => throw toOAuthErrorFromDescription(s.get.error_description)
        case e: JsError => response.json.validate[ValidatedAccessToken].get
      }
    }
    }
}

object PlayOAuthValidator {

  import glasskey.model.ValidatedData
  import glasskey.resource.validation.OAuthValidator
  import play.api.libs.ws.WSResponse

  class Default[U <: ValidatedData](override val validationUrl: String, override val grantType: String,
                                override val clientSecret: String, override val clientId: String)
    extends OAuthValidator[WSResponse] with PlayOAuthValidator {
    def this(clientConfig: ClientConfig) = this(OAuthConfig.providerConfig.validationUri,
      OAuthConfig.providerConfig.validationGrantType, clientConfig.clientSecret.get, clientConfig.clientId.get)
  }
}
