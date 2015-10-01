package com.monsanto.rdit.oauth.sprayclient

import akka.actor.ActorRefFactory
import glasskey.config.OAuthConfig
import glasskey.model.OAuthAccessToken
import glasskey.spray.client.SprayClientRuntimeEnvironment
import spray.routing._


/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldClientCredentialsClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory)(SprayClientRuntimeEnvironment("hello-client_credentials-client", new OAuthConfig.Default())) {
  override def helloWorldRoute: Route =
    pathPrefix("api") {
      getPath("PingHelloWorldCC") {
        implicit def callNeedingOAuth:(OAuthAccessToken) => Route = doHelloWorld
        oauthCall
      }
    }
}

object HelloWorldClientCredentialsClientService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldClientCredentialsClientService(actorRefFactory)
}