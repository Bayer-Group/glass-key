package glasskey.spray.client
import akka.actor.ActorRefFactory
import spray.routing._
/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldAuthCodeRefreshTokenClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory) {
  //  override def daoService = new SprayCacheService[OAuthAccessToken]()

  val appCtx = "PingHelloWorldRefresh"

  override def helloWorldRoute(implicit env: SprayClientRuntimeEnvironment): Route =
    pathPrefix("api") {
      getPath(appCtx) {
        oauthCall(refresh, env)
      }~
        getPath(s"${appCtx}AuthCode") {
          oauth2Redirect
        }
    }
}

object HelloWorldAuthCodeRefreshTokenClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldPrintAuthCodeTokenClientService(actorRefFactory)
}