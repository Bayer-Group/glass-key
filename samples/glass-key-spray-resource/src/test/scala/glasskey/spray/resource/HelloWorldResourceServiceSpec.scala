package glasskey.spray.resource

import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

class HelloWorldResourceServiceSpec extends Specification with Specs2RouteTest with HttpService {
  def actorRefFactory = system
  val service = new HelloWorldResourceService(actorRefFactory)
  "MyService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> service.resourceRoute ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> service.resourceRoute ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(service.resourceRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
