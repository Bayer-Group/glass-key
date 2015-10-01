package glasskey.resource.validation

import glasskey.model._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by loande on 6/17/15.
 */
trait EntitlementValidator extends OAuthValidator[EntitlementValidationResponse] with OAuthErrorHelper {

  val entitlementsNeeded: Set[String]

  override def validate(response: Future[EntitlementValidationResponse])(implicit ec: ExecutionContext): Future[ValidatedEntitlementAccessToken] = {
    response map {
      case Left(x: ValidationError) => throw toOAuthErrorFromDescription(x.error_description)
      case Right(x: ValidatedEntitlementAccessToken) =>
        if (containsEntitlements(makeSet(x.access_token))) x
        else throw new InvalidToken(s"Token validation failed, entitlements: [${missingEntitlements(makeSet(x.access_token))}] not present.")
    }
  }

  private def makeSet(data: ValidatedEntitlementData) = data.applicationentitlements match {
    case Left(multiple) => multiple
    case Right(single) => Set(single)
  }

  private def missingEntitlements(entitlementsInToken: Set[String]): String = (entitlementsNeeded diff entitlementsInToken).mkString(",")

  private def containsEntitlements(entitlementsInToken: Set[String]): Boolean = (entitlementsNeeded & entitlementsInToken) == entitlementsNeeded
}
