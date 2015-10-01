package glasskey.spray.client

import spray.httpx.SprayJsonSupport
import spray.routing.Directives

trait OAuthRejectionExceptionHandler extends Directives with SprayJsonSupport {

  import java.nio.channels.UnresolvedAddressException

  import glasskey.model.{ExpiredToken, ValidationError}
  import glasskey.spray.model.OAuthRejection
  import glasskey.spray.resource.validation.PingValidationJsonProtocol._
  import spray.can.Http.ConnectionAttemptFailedException
  import spray.http.StatusCodes.InternalServerError
  import spray.httpx.UnsuccessfulResponseException
  import spray.httpx.marshalling._
  import spray.routing.{RejectionHandler, Route}

  def handleOAuthFailure: PartialFunction[Throwable, Route] = {
    case e: UnsuccessfulResponseException =>
//      val error: ValidationError = Unmarshaller.unmarshalUnsafe[ValidationError](e.response.entity)
//      error.status_code = Some(e.response.status.intValue)
      reject(new OAuthRejection(new ExpiredToken(), Nil)) //TODO: extend this to cover more than an expired token

    case e: UnresolvedAddressException =>
      complete((InternalServerError, marshalUnsafe(
        new ValidationError("server_error", "Unresolved address blah", Some(InternalServerError.intValue)))))

    case e: ConnectionAttemptFailedException =>
      complete((InternalServerError, marshalUnsafe(
        new ValidationError("connection_error", e.getLocalizedMessage, Some(InternalServerError.intValue)))))

  }

  def oauthClientRejectionHandler(routeToRetry: Route): RejectionHandler = RejectionHandler {
    case OAuthRejection(wrappedEx, headers) :: _ => wrappedEx match {
      case _: ExpiredToken => routeToRetry
    }
  }
}
