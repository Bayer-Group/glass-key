package com.monsanto.rdit.oauth.sprayclient

import akka.actor.ActorRefFactory
import glasskey.config.OAuthConfig
import glasskey.model.OAuthAccessToken
import glasskey.spray.client.SprayClientRuntimeEnvironment
import spray.routing._


/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldAuthCodeClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory)(SprayClientRuntimeEnvironment("hello-authcode-client", new OAuthConfig.Default())) {

//  override def daoService = new SprayCacheService[OAuthAccessToken]()

  override def helloWorldRoute: Route =
    pathPrefix("api") {
      getPath("PingHelloWorld") {
        implicit def callNeedingOAuth:(OAuthAccessToken) => Route = doHelloWorld
        oauthCall
      }~
      getPath("PingIdentityAuthCode") {
        oauth2Redirect
      }
    }
}

object HelloWorldAuthCodeClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldAuthCodeClientService(actorRefFactory)
}