package com.monsanto.rdit.oauth.sprayresource

import akka.actor.{Actor, ActorRefFactory}
import glasskey.config.OAuthConfig
import glasskey.model.{OAuthValidatedData, ValidatedData}
import glasskey.spray.resource.validation.SprayOAuthValidator
import glasskey.spray.resource.{BearerTokenAuthenticator, SprayResourceRuntimeEnvironment}
import glasskey.spray.OAuthRejectionUnwrapper
import com.monsanto.rdit.oauth.sprayresource.HelloWorldJsonProtocol._
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService
import spray.routing.authentication._
import scala.concurrent.ExecutionContext

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class HelloWorldResourceServiceActor extends Actor with OAuthRejectionUnwrapper {
  val service = new HelloWorldResourceService(actorRefFactory)
  def actorRefFactory = context
  def receive = runRoute(service.resourceRoute)
}

// this trait defines our service behavior independently from the service actor
class HelloWorldResourceService(var actorRefFactory: ActorRefFactory) extends HttpService with SprayJsonSupport {
  implicit def executionContext: ExecutionContext = actorRefFactory.dispatcher
  implicit val env = SprayResourceRuntimeEnvironment("hello-validation-client")
  val authenticator = ContextAuthenticator[ValidatedData] = BearerTokenAuthenticator(
    Seq(new SprayOAuthValidator.Default(OAuthConfig.clients("hello-validation-client"))))

  val resourceRoute =
    pathPrefix("aws" ) {
      pathPrefix("rest") {
        pathPrefix("api") {
          path("greeting_secure_oidc") {
            parameter("name") { (name) =>
              authenticate(authenticator) { restrictedUser ⇒
                get { ctx =>
                  ctx.complete {
                    restrictedUser.user_id match {
                      case Some(usrid) => HelloModelReturn(Some(1), Some("Hello " + usrid + ", you said your name was " + name), None, None)
                      case None => HelloModelReturn(Some(1), Some("Hello client with id = " + restrictedUser.asInstanceOf[OAuthValidatedData].client_id.get + ", you said your name was " + name), None, None)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
}
object HelloWorldResourceService {
  def apply(actorRefFactory: ActorRefFactory) = new HelloWorldResourceService(actorRefFactory)
}
