package com.monsanto.rdit.oauth.sprayclient


import akka.actor.ActorRefFactory
import glasskey.spray.client.SprayClientRuntimeEnvironment
import spray.routing._

/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldPrintAuthCodeTokenClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory) {

  val appCtx = "PingHelloWorldPrint"

  override def helloWorldRoute(implicit env: SprayClientRuntimeEnvironment): Route =
    pathPrefix("api") {
      getPath(appCtx) {
        oauthCall(printToken, env)
      }~
      getPath(s"${appCtx}AuthCode") {
        oauth2Redirect
      }
    }
}

object HelloWorldPrintAuthCodeTokenClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldPrintAuthCodeTokenClientService(actorRefFactory)
}