package glasskey.spray.client
import akka.actor.ActorRefFactory
import spray.routing._
/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldClientCredentialsClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory) {
  override def helloWorldRoute(implicit env: SprayClientRuntimeEnvironment): Route =
    pathPrefix("api") {
      getPath("PingHelloWorldCC") {
        oauthCall(doHelloWorld, env)
      }
    }
}

object HelloWorldClientCredentialsClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldClientCredentialsClientService(actorRefFactory)
}