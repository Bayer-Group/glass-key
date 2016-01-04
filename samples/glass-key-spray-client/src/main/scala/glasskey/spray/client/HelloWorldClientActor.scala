package glasskey.spray.client

import akka.actor.Actor
import spray.routing._
import glasskey.spray.OAuthRejectionUnwrapper

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

  val authCodeEnv = SprayClientRuntimeEnvironment("hello-authcode-client")
  val ccEnv = SprayClientRuntimeEnvironment("hello-client_credentials-client")
  val roEnv = SprayClientRuntimeEnvironment("hello-resource_owner-client")

  def receive = runRoute(authCode.helloWorldRoute(authCodeEnv) ~
      cc.helloWorldRoute(ccEnv) ~
      ro.helloWorldRoute(roEnv) ~
      authCodePrinter.helloWorldRoute(authCodeEnv) ~
      refresh.helloWorldRoute(authCodeEnv))

}
