package glasskey

import glasskey.resource.{StringClaim, OIDCTokenData, Claim}

/**
 * Created by loande on 8/13/15.
 */
package object model {
  def getUserId(claims: Seq[Claim[_]]): String = {
    claims.find(claim => claim.name == OIDCTokenData.SUB_CLAIM).getOrElse(StringClaim(OIDCTokenData.SUB_CLAIM, "No userId found in ID token.")).value.asInstanceOf[String]
  }
}
