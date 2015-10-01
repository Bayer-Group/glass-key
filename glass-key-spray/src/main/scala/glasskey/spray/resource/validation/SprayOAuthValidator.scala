package glasskey.spray.resource.validation

import glasskey.config.{ClientConfig, OAuthProviderConfig}
import glasskey.resource.validation.ValidationResponse

import scala.concurrent.{ExecutionContext, Future}

trait SprayValidatorType {
  def name: String
}

object SprayOAuthValidator {

  import glasskey.model.OAuthErrorHelper
  import glasskey.resource.validation.OAuthValidator

  class Default(validationUri: String, override val grantType: String,
                override val clientSecret: String, override val clientId: String)
    extends OAuthValidator[ValidationResponse] with SprayValidatorType with OAuthErrorHelper {

    def this(providerConfig: OAuthProviderConfig, clientConfig: ClientConfig) = this(providerConfig.validationUri,
      providerConfig.validationGrantType, clientConfig.clientSecret.get, clientConfig.clientId.get)

    import akka.actor.ActorSystem
    import akka.event.{LogSource, Logging}
    import glasskey.model.{ValidatedAccessToken, ValidationError}
    import spray.http.{HttpRequest, HttpResponse}

    implicit val system = ActorSystem()

    implicit val sprayValidatorLogSource: LogSource[SprayValidatorType] = new LogSource[SprayValidatorType] {
      override def genString(a: SprayValidatorType): String = a.name

      override def genString(a: SprayValidatorType, s: ActorSystem): String = a.name + "," + s
    }

    val log = Logging(system, this)

    override def name: String = "spray-ping-validator"

    def showRequest(request: HttpRequest): Unit = log.debug("Something got here. Request is : " + request)

    def showResponse(response: HttpResponse): Unit = log.debug("Something got here. Response is : " + response)

    override def getValidationResponse(accessToken: String)(implicit ec: ExecutionContext): Future[ValidationResponse] = {
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
      pipeline(Post(validationUri, FormData(validationParams(accessToken))))
    }

    override def validate(resp: Future[ValidationResponse])(implicit ec: ExecutionContext): Future[ValidatedAccessToken] =
      resp map {
        case Left(x: ValidationError) => throw toOAuthErrorFromDescription(x.error_description)
        case Right(x: ValidatedAccessToken) => x
      }
  }

}

