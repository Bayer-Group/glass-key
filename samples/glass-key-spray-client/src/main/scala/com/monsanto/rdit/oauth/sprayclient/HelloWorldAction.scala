package com.monsanto.rdit.oauth.sprayclient

import akka.util.Timeout
import glasskey.model.OAuthAccessToken
import glasskey.spray.client.{SprayClientRuntimeEnvironment, OAuthRejectionExceptionHandler}
import glasskey.spray.model.OAuthAction
import com.monsanto.rdit.oauth.sprayclient.model.HelloModelReturn
import com.monsanto.rdit.oauth.sprayclient.model.HelloWorldJsonProtocol._
import spray.client.pipelining._
import spray.routing._

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created by LOANDE on 11/18/2014.
 */
object HelloWorldAction extends OAuthAction with OAuthRejectionExceptionHandler {
  implicit val timeout: Timeout = Timeout(15.seconds)

  def hello(token: OAuthAccessToken)(implicit env: SprayClientRuntimeEnvironment): Route = {
    parameters('name) { name =>
      val pipeline = getHeaderedPipeline[HelloModelReturn](token.access_token, token.id_token)
      onComplete(pipeline {
        Get("http://localhost:8081/aws/rest/api/greeting_secure_oidc?name=" + name)
      }) {
        case Success(helloMsg) => complete(helloMsg)
        case Failure(reason) => handleOAuthFailure(reason)
      }
    }
  }
}

