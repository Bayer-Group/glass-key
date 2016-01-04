package glasskey.resource.validation

trait OAuthValidator[R] extends Validator[R]{

  import glasskey.model.OAuthTerms

  val validationUri: String

  val grantType: String

  val clientSecret: String

  val clientId: String

  def validationParams(accessToken: String): Map[String, String] = Map(OAuthTerms.ClientId -> clientId,
    OAuthTerms.ClientSecret -> clientSecret,
    OAuthTerms.GrantType -> grantType,
    OAuthTerms.Token -> accessToken)
}