package glasskey.spray.client
import akka.actor.ActorRefFactory
import spray.routing._

/**
 * Created by LOANDE on 12/23/2014.
 */
class HelloWorldResourceOwnerClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory) {
  def helloWorldRoute(implicit env: SprayClientRuntimeEnvironment): Route =
    pathPrefix("api") {
      getPath("PingHelloWorldRO") {
        oauthCall(doHelloWorld, env)
      }
    }
}

object HelloWorldResourceOwnerClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldResourceOwnerClientService(actorRefFactory)
}