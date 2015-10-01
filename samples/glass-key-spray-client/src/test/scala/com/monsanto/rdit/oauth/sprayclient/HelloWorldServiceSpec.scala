package com.monsanto.rdit.oauth.sprayclient

import glasskey.model.OAuthTerms
import org.scalatest.{FlatSpec, Matchers}
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class HelloWorldServiceSpec extends FlatSpec with ScalatestRouteTest with HttpService with Matchers with SprayJsonSupport{
  def actorRefFactory = system
  val authCode = new HelloWorldAuthCodeClientService(actorRefFactory)
  val cc = new HelloWorldClientCredentialsClientService(actorRefFactory)
  val ro = new HelloWorldResourceOwnerClientService(actorRefFactory)

  "Spray OAuth Client" should "return a redirect for Auth Code requests" in {
    Get("/api/PingHelloWorld?name=larry") ~> authCode.helloWorldRoute ~> check {
      status.intValue should be (307)
      header("Location") match {
        case Some(hdr) =>
          hdr.value should include (authCode.env.config.providerConfig.authUrl)
          hdr.value should include (s"${OAuthTerms.ResponseType}=${OAuthTerms.AuthCodeResponseType}")
          hdr.value should include (s"${OAuthTerms.ClientId}=${authCode.env.tokenHelper.clientId}")
        case None => fail("Location header is not defined.")
      }
    }
  }



}
