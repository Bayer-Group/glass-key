package glasskey.spray.client
import akka.actor.ActorRefFactory
import spray.routing.authentication._
import spray.routing._
import glasskey.model.ValidatedData
import glasskey.config.OAuthConfig
import glasskey.spray.resource.BearerTokenAuthenticator
import glasskey.spray.resource.validation.SprayOAuthValidator
/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldAuthCodeClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory) {

  def authenticator: ContextAuthenticator[ValidatedData] = BearerTokenAuthenticator(
     Seq(SprayOAuthValidator(OAuthConfig.clients("hello-authcode-client"))))

  override def helloWorldRoute(implicit env: SprayClientRuntimeEnvironment): Route =
    pathPrefix("api") {
      getPath("PingHelloWorld") {
        oauthCall(doHelloWorld, env)
      }~
      getPath("PingIdentityAuthCode") {
        oauth2Redirect
      }
    }
}

object HelloWorldAuthCodeClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldAuthCodeClientService(actorRefFactory)
}