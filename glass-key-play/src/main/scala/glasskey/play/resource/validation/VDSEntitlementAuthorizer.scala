package glasskey.play.resource.validation

import com.ning.http.client.AsyncHttpClientConfig
import glasskey.config.OAuthConfig
import glasskey.model._
import glasskey.model.validation.RBACAuthZData
import glasskey.resource.validation._
import play.api.libs.json.{JsError, JsSuccess}
import play.api.libs.ws.{WS, DefaultWSClientConfig}
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}
import play.api.Play.current
import scala.concurrent.{Future, ExecutionContext}
import PingIdentityAccessTokenValidatorFormats._
/**
 * Created by loande on 6/18/15.
 */
class VDSEntitlementAuthorizer(override val entitlementUri: String,
                              override val desiredAuth: Seq[RBACAuthZData])(implicit ec: ExecutionContext) extends EntitlementAuthorizer with OAuthErrorHelper {

  def this(entUri: String, desiredSingleAuth: RBACAuthZData)(implicit ec: ExecutionContext) = this(entUri, Seq(desiredSingleAuth))

  override def getAuth(accessToken: String, userId: String): Future[Seq[RBACAuthZData]] = {

    val builder = new AsyncHttpClientConfig.Builder(new NingAsyncHttpClientConfigBuilder(new DefaultWSClientConfig()).build())
      .setCompressionEnabled(true)

    implicit val sslClient = new NingWSClient(builder.build())
    WS.url(modEntitlementUrl(userId)).withHeaders(OAuthConfig.providerConfig.authHeaderName -> s"${OAuthConfig.providerConfig.authHeaderPrefix}$accessToken")
      .get()
      .map {
      response => {
        response.json.validate[Seq[RBACAuthZData]] match {
          case s: JsSuccess[Seq[RBACAuthZData]] => s.get
          case e: JsError => throw new OIDCDataNotAvailableException("Entitlement data could not be read.")
        }
      }
    }
  }
}
