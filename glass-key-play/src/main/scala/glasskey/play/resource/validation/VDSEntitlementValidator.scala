package glasskey.play.resource.validation

import glasskey.model.{ValidatedEntitlementAccessToken, ValidationError, OAuthErrorHelper}
import glasskey.resource.validation._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by loande on 6/18/15.
 */
class VDSEntitlementValidator(validationUri: String, override val grantType: String,
                              override val clientSecret: String, override val clientId: String,
                              val entitlementsNeeded: Set[String]) extends OAuthValidator[EntitlementValidationResponse]
  with EntitlementValidator with OAuthErrorHelper {

  override def getValidationResponse(accessToken: String)(implicit ec: ExecutionContext): Future[EntitlementValidationResponse] = {
    import com.ning.http.client.AsyncHttpClientConfig
    import play.api.Play.current
    import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient}
    import play.api.libs.ws.{DefaultWSClientConfig, WS}
    import PingIdentityAccessTokenValidatorFormats._

    val queryString = validationParams(accessToken)
      .foldLeft("")((a, t) => a + (t._1 + "=" + t._2 + "&"))
      .dropRight(1)

    Logger.debug("Sending " + queryString + " as the body to " + validationUri)

    // You can directly use the builder for specific options once you have secure TLS defaults...
    val builder = new AsyncHttpClientConfig.Builder(new NingAsyncHttpClientConfigBuilder(new DefaultWSClientConfig()).build())
      .setCompressionEnabled(true)

    implicit val sslClient = new NingWSClient(builder.build())
    WS.url(validationUri).withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .post(queryString)
      .map {
        response => {
          response.json.validate[ValidationError] match {
            case s: JsSuccess[ValidationError] => Left(s.get)
            case e: JsError => Right(response.json.validate[ValidatedEntitlementAccessToken].get)
          }
        }
      }
  }
}
