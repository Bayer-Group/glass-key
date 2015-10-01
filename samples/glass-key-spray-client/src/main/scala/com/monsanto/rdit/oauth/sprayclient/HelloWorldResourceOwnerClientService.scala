package com.monsanto.rdit.oauth.sprayclient

import akka.actor.ActorRefFactory
import glasskey.config.OAuthConfig
import glasskey.model.OAuthAccessToken
import glasskey.spray.client.SprayClientRuntimeEnvironment
import spray.routing._

/**
 * Created by LOANDE on 12/23/2014.
 */
class HelloWorldResourceOwnerClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory)(SprayClientRuntimeEnvironment("hello-resource_owner-client", new OAuthConfig.Default())) {
  def helloWorldRoute: Route =
    pathPrefix("api") {
      getPath("PingHelloWorldRO") {
        implicit def callNeedingOAuth:(OAuthAccessToken) => Route = doHelloWorld
        oauthCall
      }
    }
}

object HelloWorldResourceOwnerClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldResourceOwnerClientService(actorRefFactory)
}