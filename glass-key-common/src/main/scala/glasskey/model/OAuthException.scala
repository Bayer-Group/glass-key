package glasskey.model

import scala.language.implicitConversions

abstract class OAuthException(val statusCode: Int, val description: String) extends Exception {

  def this(description: String) = this(400, description)

  val errorType: String

}

trait OAuthErrorHelper {
  def toOAuthErrorString(e: ValidationError): String =
    s"""error="${e.error}"""" +
      (if (!e.error_description.isEmpty) s""", error_description="${e.error_description}"""" else "")

  def toOAuthErrorFromDescription(desc: String): OAuthException =
    desc match {
      case "Bad credentials" => new AccessDenied(desc)
      case "token expired" => new ExpiredToken()
      case "token not found, expired or invalid" => new InvalidGrant(desc)
      case "grant_type is required" => new InvalidGrant(desc)
      case "redirect_uri is required if it was included in the authorization request." => new RedirectUriMismatch(desc)
      case "Invalid client or client credentials" => new InvalidClient(desc)
    }

  implicit def toOAuthValidationError(ex: OAuthException): ValidationError = new ValidationError(ex.errorType,
    ex.description, Some(ex.statusCode))

}

class InvalidRequest(description: String = "") extends OAuthException(description) {

  override val errorType = "invalid_request"

}

class InvalidClient(description: String = "") extends OAuthException(401, description) {

  override val errorType = "invalid_client"

}

class UnauthorizedClient(description: String = "") extends OAuthException(401, description) {

  override val errorType = "unauthorized_client"

}

class RedirectUriMismatch(description: String = "") extends OAuthException(401, description) {

  override val errorType = "redirect_uri_mismatch"

}

class AccessDenied(description: String = "") extends OAuthException(401, description) {

  override val errorType = "access_denied"

}

class UnsupportedResponseType(description: String = "") extends OAuthException(description) {

  override val errorType = "unsupported_response_type"

}

class InvalidGrant(description: String = "") extends OAuthException(401, description) {

  override val errorType = "invalid_grant"

}

class UnsupportedGrantType(description: String = "") extends OAuthException(description) {

  override val errorType = "unsupported_grant_type"

}

class InvalidScope(description: String = "") extends OAuthException(401, description) {

  override val errorType = "invalid_scope"

}

class InvalidToken(description: String = "") extends OAuthException(401, description) {

  override val errorType = "invalid_token"

}

class ExpiredToken() extends OAuthException(401, "The access token expired") {

  override val errorType = "invalid_token"

}

class InsufficientScope(description: String = "") extends OAuthException(401, description) {

  override val errorType = "insufficient_scope"

}
