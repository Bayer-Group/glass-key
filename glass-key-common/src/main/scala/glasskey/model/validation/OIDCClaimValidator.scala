package glasskey.model.validation

import glasskey.resource.OIDCTokenData
import glasskey.resource.OIDCTokenData._

/**
 * Created by loande on 3/20/15.
 */
trait OIDCClaimValidator extends ClaimValidator[OIDCTokenData] {
  def hasAudience(aud: String) = OIDCHas(AUD_CLAIM, aud)
  def hasSubject(sub: String) = OIDCHas(SUB_CLAIM, sub)
  def hasJwtId(jwtId: String) = OIDCHas(JTI_CLAIM, jwtId)
  def hasIssuer(iss: String) = OIDCHas(ISS_CLAIM, iss)
  def hasIssueAtTime(iat: Int) = OIDCHas(IAT_CLAIM, iat)
  def hasExpire(exp: Int) = OIDCHas(EXP_CLAIM, exp)
  def hasNonce(nonce: String) = OIDCHas(NONCE_CLAIM, nonce)
  def hasAtHash(atHash: String) = OIDCHas(AT_HASH_CLAIM, atHash)
}