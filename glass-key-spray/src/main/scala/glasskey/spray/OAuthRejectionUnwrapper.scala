package glasskey.spray

import glasskey.model.OAuthErrorHelper
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService

trait OAuthRejectionUnwrapper extends HttpService with SprayJsonSupport with OAuthErrorHelper {

  import glasskey.model.ValidationError
  import glasskey.spray.model.OAuthRejection
  import glasskey.spray.resource.validation.PingValidationJsonProtocol._
  import spray.http.StatusCodes.InternalServerError
  import spray.httpx.marshalling._
  import spray.routing.{RejectionHandler, Route}

  implicit val tokenRejectionHandler = RejectionHandler {
    case OAuthRejection(wrappedEx, headers) :: _ =>
      val error: ValidationError = wrappedEx
      complete((InternalServerError, error))
  }

//    def jsonify(response: HttpResponse): HttpResponse = {
//      HttpResponse(status = StatusCodes.InternalServerError, entity = marshalUnsafe(
//        new ValidationError("server_error", response.entity.asString, Some(response.status.intValue))))
//    }

//    implicit val apiRejectionHandler = RejectionHandler{
//      case rejections => mapHttpResponse(jsonify) {
//        RejectionHandler.Default(rejections)
//      }
//    }

  override def timeoutRoute: Route = complete(
    (InternalServerError, marshal(new ValidationError("timeout", "spray-can timed out servicing the request.", Some(500)))))
}
