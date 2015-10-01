package glasskey.spray.resource.validation

import akka.actor.ActorSystem
import glasskey.model.OAuthErrorHelper
import glasskey.resource.validation.{EntitlementValidator, EntitlementValidationResponse, OAuthValidator}
import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by loande on 6/17/15.
 */
class VDSEntitlementValidator(validationUri: String, override val grantType: String,
                              override val clientSecret: String, override val clientId: String,
                              val entitlementsNeeded: Set[String])
  extends OAuthValidator[EntitlementValidationResponse] with EntitlementValidator with OAuthErrorHelper {

  implicit val system = ActorSystem()

  override def getValidationResponse(accessToken: String)(implicit ec: ExecutionContext): Future[EntitlementValidationResponse] = {
    import spray.client.pipelining._
    import spray.http.FormData
    import spray.httpx.encoding.Deflate
    import glasskey.spray.resource.validation.PingValidationJsonProtocol._
    import spray.httpx.SprayJsonSupport._

    val pipeline = (sendReceive
      ~> decode(Deflate)
      ~> unmarshal[EntitlementValidationResponse])
    pipeline(Post(validationUri, FormData(validationParams(accessToken))))
  }
}
