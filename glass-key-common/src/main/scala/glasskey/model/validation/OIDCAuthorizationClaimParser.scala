package glasskey.model.validation

import glasskey.resource.OIDCTokenData

/**
 * Created by loande on 3/23/15.
 */
trait OIDCAuthorizationClaimParser extends AuthorizationClaimParser[OIDCTokenData] with OIDCClaimValidator {
  def audit: Parser[Expr[OIDCTokenData]] = "HAS_AUD_STRING('" ~> param <~ "')" ^^ { param => hasAudience(param)}
  def subject: Parser[Expr[OIDCTokenData]] = "HAS_SUB_STRING('" ~> param <~ "')" ^^ { param => hasSubject(param)}
  def jti: Parser[Expr[OIDCTokenData]] = "HAS_JTI_STRING('" ~> param <~ "')" ^^ { param => hasJwtId(param)}
  def issuer: Parser[Expr[OIDCTokenData]] = "HAS_ISSUER_STRING('" ~> param <~ "')" ^^ { param => hasIssuer(param)}
  def iat: Parser[Expr[OIDCTokenData]] = "HAS_IAT_INT('" ~> param <~ "')" ^^ { param => hasIssueAtTime(param.toInt)}
  def exp: Parser[Expr[OIDCTokenData]] = "HAS_EXP_INT('" ~> param <~ "')" ^^ { param => hasExpire(param.toInt)}
  def nonce: Parser[Expr[OIDCTokenData]] = "HAS_NONCE_STRING('" ~> param <~ "')" ^^ { param => hasNonce(param)}
  def atHash: Parser[Expr[OIDCTokenData]] = "HAS_SUB_STRING('" ~> param <~ "')" ^^ { param => hasAtHash(param)}

  override def tokenSpecificExpressions : Parser[Expr[OIDCTokenData]] = (audit | subject | jti | issuer | iat | exp | nonce | atHash)
}
