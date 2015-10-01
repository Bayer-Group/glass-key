package com.monsanto.rdit.oauth.sprayclient

import akka.actor.Actor
import glasskey.spray.OAuthRejectionUnwrapper
import spray.routing.HttpService

/**
 * Created by LOANDE on 12/21/2014.
 */
class HelloWorldClientActor extends Actor with HttpService with OAuthRejectionUnwrapper{
  def actorRefFactory = context
  val cc = new HelloWorldClientCredentialsClientService(actorRefFactory)
  val authCode = new HelloWorldAuthCodeClientService(actorRefFactory)
  val authCodePrinter = new HelloWorldPrintAuthCodeTokenClientService(actorRefFactory)
  val ro = new HelloWorldResourceOwnerClientService(actorRefFactory)
  val refresh = new HelloWorldAuthCodeRefreshTokenClientService(actorRefFactory)

  def receive = runRoute(authCode.helloWorldRoute ~
    cc.helloWorldRoute ~
    ro.helloWorldRoute ~
    authCodePrinter.helloWorldRoute ~
    refresh.helloWorldRoute)
}
