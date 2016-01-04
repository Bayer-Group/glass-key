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

trait VDSEntitlementAuthorizer extends EntitlementAuthorizer with OAuthErrorHelper {

  implicit val ec: ExecutionContext

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

object VDSEntitlementAuthorizer {

  def apply(entUri: String, desired: Seq[RBACAuthZData])(implicit exc: ExecutionContext): VDSEntitlementAuthorizer  =
    new VDSEntitlementAuthorizer {
      override val desiredAuth: Seq[RBACAuthZData] = desired
      override val entitlementUri: String = entUri
      override implicit val ec: ExecutionContext = exc
    }
  def apply(entUri: String, desiredSingleAuth: RBACAuthZData)(implicit ec: ExecutionContext): VDSEntitlementAuthorizer = apply(entUri, Seq(desiredSingleAuth))
}
