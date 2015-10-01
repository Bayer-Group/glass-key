package com.monsanto.rdit.oauth.sprayclient


import akka.actor.ActorRefFactory
import glasskey.config.OAuthConfig
import glasskey.model.OAuthAccessToken
import glasskey.spray.client.SprayClientRuntimeEnvironment
import spray.routing._

/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldPrintAuthCodeTokenClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory)(SprayClientRuntimeEnvironment("hello-authcode-client", new OAuthConfig.Default())) {

  //  override def daoService = new SprayCacheService[OAuthAccessToken]()

  val appCtx = "PingHelloWorldPrint"

  override def helloWorldRoute: Route =
    pathPrefix("api") {
      getPath(appCtx) {
        implicit def callNeedingOAuth:(OAuthAccessToken) => Route = printToken
        oauthCall
      }~
      getPath(s"${appCtx}AuthCode") {
        oauth2Redirect
      }
    }
}

object HelloWorldPrintAuthCodeTokenClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldPrintAuthCodeTokenClientService(actorRefFactory)
}