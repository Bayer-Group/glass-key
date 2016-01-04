package glasskey.spray.resource.validation

import akka.event.Logging
import glasskey.config.{ClientConfig, OAuthConfig}
import glasskey.resource.validation.{OAuthValidator, ValidationResponse}
import glasskey.model.{OAuthErrorHelper, OAuthAccessToken}
import glasskey.resource.OIDCTokenData

import scala.concurrent.{ExecutionContext, Future}

trait SprayValidatorType {
  def name: String
}

trait SprayOAuthValidator extends OAuthValidator[ValidationResponse] with SprayValidatorType with OAuthErrorHelper {
  import akka.actor.ActorSystem
  import akka.event.{LogSource, Logging}
  import glasskey.model.{ValidatedAccessToken, ValidationError}
  import spray.http.{HttpRequest, HttpResponse}

  implicit val system = ActorSystem()

  val log: akka.event.LoggingAdapter

  override def name: String = "spray-ping-validator"

  implicit val sprayValidatorLogSource: LogSource[SprayValidatorType] = new LogSource[SprayValidatorType] {
    override def genString(a: SprayValidatorType): String = a.name
    override def genString(a: SprayValidatorType, s: ActorSystem): String = a.name + "," + s
  }

  def showRequest(request: HttpRequest): Unit = log.debug("Something got here. Request is : " + request)

  def showResponse(response: HttpResponse): Unit = log.debug("Something got here. Response is : " + response)

  override def getValidationResponse(tokenData: (OAuthAccessToken, Option[OIDCTokenData]))(implicit ec: ExecutionContext): Future[ValidationResponse] = {
    import glasskey.spray.resource.validation.PingValidationJsonProtocol._
    import spray.client.pipelining._
    import spray.http.FormData
    import spray.httpx.SprayJsonSupport._
    import spray.httpx.encoding.Deflate

    val pipeline = (logRequest(showRequest _)
      ~> sendReceive
      ~> decode(Deflate)
      ~> logResponse(showResponse _)
      ~> unmarshal[ValidationResponse])
    pipeline(Post(validationUri, FormData(validationParams(tokenData._1.access_token))))
  }

  override def validate(resp: Future[ValidationResponse])(implicit ec: ExecutionContext): Future[ValidatedAccessToken] =
    resp map {
      case Left(x: ValidationError) => throw toOAuthErrorFromDescription(x.error_description)
      case Right(x: ValidatedAccessToken) => x
    }
}

object SprayOAuthValidator {

  def apply(validUri: String, grant: String, secret: String, id: String): SprayOAuthValidator = new SprayOAuthValidator {
    override val log: akka.event.LoggingAdapter = Logging(system, this)
    override val validationUri: String = validUri
    override val grantType: String = grant
    override val clientSecret: String = secret
    override val clientId: String = id
  }

  def apply(clientConfig: ClientConfig): SprayOAuthValidator = apply(OAuthConfig.providerConfig.validationUri,
      OAuthConfig.providerConfig.validationGrantType, clientConfig.clientSecret.get, clientConfig.clientId.get)
}

