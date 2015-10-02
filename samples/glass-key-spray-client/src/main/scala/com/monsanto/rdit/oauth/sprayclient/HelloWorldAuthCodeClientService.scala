package com.monsanto.rdit.oauth.sprayclient

import akka.actor.ActorRefFactory
import glasskey.config.OAuthConfig
import glasskey.spray.client.SprayClientRuntimeEnvironment
import spray.routing._
import glasskey.model.ValidatedData
import glasskey.spray.resource.BearerTokenAuthenticator
import glasskey.spray.resource.validation.SprayOAuthValidator
import spray.routing.authentication._
/**
 * Created by loande on 12/5/2014.
 */

class HelloWorldAuthCodeClientService(actorRefFactory: ActorRefFactory) extends SampleClientService(actorRefFactory) {

  def authenticator: ContextAuthenticator[ValidatedData] = BearerTokenAuthenticator(
     Seq(new SprayOAuthValidator.Default(OAuthConfig.clients("hello-authcode-client"))))

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