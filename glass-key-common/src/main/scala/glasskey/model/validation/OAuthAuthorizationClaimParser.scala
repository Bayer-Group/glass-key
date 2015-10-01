package glasskey.model.validation

import glasskey.model.ValidatedAccessToken

/**
 * Created by loande on 3/24/15.
 */
trait OAuthAuthorizationClaimParser extends AuthorizationClaimParser[ValidatedAccessToken] with OAuthClaimValidator {
  def audit: Parser[Expr[ValidatedAccessToken]] = "HAS_SCOPE_STRING('" ~> param <~ "')" ^^ { param => hasScope(param)}
  def subject: Parser[Expr[ValidatedAccessToken]] = "HAS_CLIENT_ID_STRING('" ~> param <~ "')" ^^ { param => hasClientId(param)}
  def jti: Parser[Expr[ValidatedAccessToken]] = "HAS_ORG_STRING('" ~> param <~ "')" ^^ { param => hasOrg(param)}
  def user: Parser[Expr[ValidatedAccessToken]] = "HAS_USERNAME_STRING('" ~> param <~ "')" ^^ {param => hasUser(param)}

  override def tokenSpecificExpressions : Parser[Expr[ValidatedAccessToken]] = (audit | subject | jti | user)
}
