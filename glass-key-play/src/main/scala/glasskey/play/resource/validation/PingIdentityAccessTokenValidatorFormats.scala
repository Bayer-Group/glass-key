package glasskey.play.resource.validation

import glasskey.model.OAuthErrorHelper

import scala.language.implicitConversions
import glasskey.model.validation.RBACAuthZData


object PingIdentityAccessTokenValidatorFormats extends OAuthErrorHelper {

  import glasskey.model.OAuthTerms._
  import glasskey.model._
  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  val baseValidatedWrites: Writes[BaseValidatedData] =
    (__ \ "user_id").writeNullable[String].contramap { (base: BaseValidatedData) => base.user_id }

  val baseValidatedReads = (__ \ 'user_id).readNullable[String].map{ l => BaseValidatedData(l) }

  implicit val oauthValidatedFormat = (
    (__ \ "Username").readNullable[String]
      ~ (__ \ "user_id").readNullable[String]
      ~ (__ \ "OrgName").readNullable[String]
      ~ (__ \ ClientId).readNullable[String]
      ~ (__ \ "scopes").readNullable[Set[String]]
    )

  implicit val oauthValidatedReads: Reads[OAuthValidatedData] = (oauthValidatedFormat)(OAuthValidatedData)

  implicit val accessTokenFormat = (
    (__ \ OAuthTerms.RefreshToken).readNullable[String]
      ~ (__ \ OAuthTerms.TokenType).read[String]
      ~ (__ \ OAuthTerms.ExpiresIn).readNullable[Long]
      ~ (__ \ OAuthTerms.AccessToken).read[String]
      ~ (__ \ OAuthTerms.IdToken).readNullable[String]
    )

  implicit val accessTokenReads: Reads[OAuthAccessToken] = (accessTokenFormat)(OAuthAccessToken)

  implicit val validatedTokenFormat = (
    (__ \ ClientId).read[Option[String]]
      ~ (__ \ "token").read[Option[String]].orElse(Reads.pure(None))
      ~ (__ \ OAuthTerms.AccessToken).read[Option[String]].orElse(Reads.pure(None))
      ~ (__ \ "scope").read[Option[String]]
      ~ (__ \ "token_type").read[Option[String]]
      ~ (__ \ "expires_in").read[Option[Long]]
      ~ (__ \ "access_token").read[OAuthValidatedData](oauthValidatedReads)
    )

  implicit val validatedTokenReads: Reads[ValidatedAccessToken] = (validatedTokenFormat)(ValidatedAccessToken)

  implicit val errorFormat = (
    (__ \ "error").read[String]
      ~ (__ \ "error_description").read[String]
      ~ (__ \ "status_code").readNullable[Int]
    )

  implicit val validationErrorReads: Reads[ValidationError] = (errorFormat)(ValidationError)

  implicit val validationErrorWrites = (
    (__ \ "error").write[String] ~
      (__ \ "error_description").write[String] ~
      (__ \ "status_code").writeNullable[Int]
    )(unlift(ValidationError.unapply))

  implicit def eitherReads[A, B](implicit A: Reads[A], B: Reads[B]): Reads[Either[A, B]] =
    Reads[Either[A, B]] { json =>
      A.reads(json) match {
        case JsSuccess(value, path) => JsSuccess(Left(value), path)
        case JsError(e1) => B.reads(json) match {
          case JsSuccess(value, path) => JsSuccess(Right(value), path)
          case JsError(e2) => JsError(JsError.merge(e1, e2))
        }
      }
    }

  implicit val entitlementDataFormat = (
    (__ \ "Username").readNullable[String]
      ~ (__ \ "user_id").readNullable[String]
      ~ (__ \ "OrgName").readNullable[String]
      ~ (__ \ "applicationentitlements").read[Either[Set[String], String]]
      ~ (__ \ ClientId).readNullable[String]
      ~ (__ \ "scopes").readNullable[Set[String]]
    )

  implicit val entitlementDataReads: Reads[ValidatedEntitlementData] = (entitlementDataFormat)(ValidatedEntitlementData)

  implicit val entitlementValidationFormat = (
    (__ \ ClientId).read[Option[String]]
      ~ (__ \ "token").read[Option[String]].orElse(Reads.pure(None))
      ~ (__ \ OAuthTerms.AccessToken).read[Option[String]].orElse(Reads.pure(None))
      ~ (__ \ "scope").read[Option[String]]
      ~ (__ \ "token_type").read[Option[String]]
      ~ (__ \ "expires_in").read[Option[Long]]
      ~ (__ \ "access_token").read[ValidatedEntitlementData](entitlementDataReads)
    )

  implicit val entitlementValidationReads: Reads[ValidatedEntitlementAccessToken] = (entitlementValidationFormat)(ValidatedEntitlementAccessToken)

  implicit val rbacFormat = (
    (__ \ "app").read[String]
    ~ (__ \ "entitlements").read[Set[String]]
    )
  implicit val rbacReads: Reads[RBACAuthZData] = (rbacFormat)(RBACAuthZData)

  //  implicit def errorCodeToErrorString[A](e: OAuthException): OAuthValidationError = new OAuthValidationError(e.errorType, e.description, Some(e.statusCode))
}
