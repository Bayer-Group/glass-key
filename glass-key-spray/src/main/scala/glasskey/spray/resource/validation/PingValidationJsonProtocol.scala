package glasskey.spray.resource.validation

import glasskey.model.{ValidatedEntitlementData, ValidatedEntitlementAccessToken, BaseValidatedData}
import spray.json._

object PingValidationJsonProtocol extends DefaultJsonProtocol {

  import glasskey.model.{OAuthAccessToken, ValidatedAccessToken, ValidationError}

  implicit val pingValidationErrorFormat = jsonFormat3(ValidationError)
  implicit val pingValidDataFormat = jsonFormat5(BaseValidatedData)
  implicit val pingAccesstokenFormat = jsonFormat7(ValidatedAccessToken)
  implicit val pingOAuthAccesstokenFormat = jsonFormat5(OAuthAccessToken)

  implicit def entitlementValidatedDataFormat = jsonFormat[Option[String], Option[String], Option[String], Either[Set[String], String],
    Option[String], Option[Set[String]], ValidatedEntitlementData](ValidatedEntitlementData.apply,
      "username", "user_id", "orgName", "applicationentitlements", "client_id", "scopes")

  implicit val entitlementTokenFormat = jsonFormat7(ValidatedEntitlementAccessToken)

  implicit def validationResponseFormat[U]: RootJsonFormat[Either[ValidationError, ValidatedAccessToken]] =
    new RootJsonFormat[Either[ValidationError, ValidatedAccessToken]] {
      val format = DefaultJsonProtocol.eitherFormat[ValidationError, ValidatedAccessToken]
      def write(either: Either[ValidationError, ValidatedAccessToken]): JsValue = format.write(either)
      def read(value: JsValue): Either[ValidationError, ValidatedAccessToken] = format.read(value)
    }

  implicit def entitlementValidationResponseFormat[U]: RootJsonFormat[Either[ValidationError, ValidatedEntitlementAccessToken]] =
    new RootJsonFormat[Either[ValidationError, ValidatedEntitlementAccessToken]] {
      val format = DefaultJsonProtocol.eitherFormat[ValidationError, ValidatedEntitlementAccessToken]
      def write(either: Either[ValidationError, ValidatedEntitlementAccessToken]): JsValue = format.write(either)
      def read(value: JsValue): Either[ValidationError, ValidatedEntitlementAccessToken] = format.read(value)
    }
}
