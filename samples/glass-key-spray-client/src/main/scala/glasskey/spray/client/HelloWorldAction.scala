package glasskey.spray.client

import glasskey.spray.client.model.HelloModelReturn
import glasskey.spray.client.model.HelloWorldJsonProtocol._
import glasskey.spray.model.OAuthAction
import glasskey.model.OAuthAccessToken
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}
import akka.util.Timeout
import spray.routing._
import spray.client.pipelining._
/**
 * Created by LOANDE on 11/18/2014.
 */
object HelloWorldAction extends OAuthAction with OAuthRejectionExceptionHandler {
  implicit val timeout: Timeout = Timeout(15.seconds)

  def hello(token: OAuthAccessToken)(implicit env: SprayClientRuntimeEnvironment): Route = {
    parameters('name) { name =>
      val pipeline = getHeaderedPipeline[HelloModelReturn](token.access_token, token.id_token)
      onComplete(pipeline {
        Get("http://localhost:8081/secure_hello?name=" + name)
      }) {
        case Success(helloMsg) => complete(helloMsg)
        case Failure(reason) => handleOAuthFailure(reason)
      }
    }
  }
}

