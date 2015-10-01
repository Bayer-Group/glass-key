package glasskey.resource.validation

import glasskey.model.ValidatedToken

import scala.concurrent.ExecutionContext

trait OAuthValidator[R] {

  import glasskey.model.{OAuthTerms}

  import scala.concurrent.Future

  val grantType: String

  val clientSecret: String

  val clientId: String

  def getValidationResponse(accessToken: String)(implicit ec: ExecutionContext): Future[R]

  def validate(response: Future[R])(implicit ec: ExecutionContext): Future[ValidatedToken]

  def validationParams(accessToken: String): Map[String, String] = Map(OAuthTerms.ClientId -> clientId,
    OAuthTerms.ClientSecret -> clientSecret,
    OAuthTerms.GrantType -> grantType,
    OAuthTerms.Token -> accessToken)
}