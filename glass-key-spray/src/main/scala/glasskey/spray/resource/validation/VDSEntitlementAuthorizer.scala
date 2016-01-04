package glasskey.spray.resource.validation

import glasskey.model._
import glasskey.model.validation.RBACAuthZData
import glasskey.resource.validation.EntitlementAuthorizer
import glasskey.spray.model.OAuthAction
import spray.http.HttpHeaders.Authorization
import spray.http.{OAuth2BearerToken, HttpRequest}
import spray.httpx.encoding.{Deflate, Gzip}
import spray.httpx.unmarshalling.FromResponseUnmarshaller
import scala.concurrent.Future
import spray.client.pipelining._

/**
 * Created by loande on 6/17/15.
 */
trait VDSEntitlementAuthorizer extends EntitlementAuthorizer with OAuthErrorHelper with OAuthAction {
  def getEntitlementPipeline[T : FromResponseUnmarshaller](accessToken: String): HttpRequest => Future[T] = {
    (addHeaders(Authorization(OAuth2BearerToken(accessToken)))
      ~> encode(Gzip)
      ~> sendReceive
      ~> decode(Deflate) ~> decode(Gzip)
      ~> unmarshal[T])
  }

  override def getAuth(accessToken: String, userId: String): Future[Seq[RBACAuthZData]] = {
    import PingValidationJsonProtocol._
    import spray.httpx.SprayJsonSupport._

    checkEntitlementUrl()

    getEntitlementPipeline[Seq[RBACAuthZData]](accessToken)(implicitly[FromResponseUnmarshaller[Seq[RBACAuthZData]]]) {
      Get(modEntitlementUrl(userId))
    }
  }
}


object VDSEntitlementAuthorizer {

  def apply(entUri: String, desired: Seq[RBACAuthZData]): VDSEntitlementAuthorizer = new VDSEntitlementAuthorizer {
    override val desiredAuth: Seq[RBACAuthZData] = desired
    override val entitlementUri: String = entUri
  }

  def apply(entUri: String, desiredSingleAuth: RBACAuthZData): VDSEntitlementAuthorizer = apply(entUri, Seq(desiredSingleAuth))
}
