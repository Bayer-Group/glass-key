package com.monsanto.rdit.oauth.sprayclient

import akka.actor.ActorRefFactory
import glasskey.model.OAuthAccessToken
import glasskey.spray.client.{OAuthService, SprayClientRuntimeEnvironment}
import spray.routing._

/**
 * Created by LOANDE on 1/8/2015.
 */
abstract class SampleClientService(override val actorRefFactory: ActorRefFactory)(implicit val env: SprayClientRuntimeEnvironment) extends OAuthService {

  def helloWorldRoute: Route

  def doHelloWorld(tokenValue: OAuthAccessToken):Route = HelloWorldAction.hello(tokenValue)

  def printToken(tokenValue: OAuthAccessToken):Route = {
    complete(s"here's the token ${tokenValue.access_token}")
  }

  def refresh(tokenValue: OAuthAccessToken):Route = {
    refreshToken(tokenValue.refresh_token.get)
  }

  def getQueryString(uri: String, params: (String, String)*): String = {
    import java.net.URLEncoder

    params.foldLeft(uri + "?") { (s: String, param: (String, String)) =>
      s + URLEncoder.encode(param._1, "UTF-8") + "=" + URLEncoder.encode(param._2, "UTF-8") + "&"
    }.dropRight(1)
  }
}
