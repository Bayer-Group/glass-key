package glasskey.util

import com.nimbusds.jose.jwk.JWKSet

/**
 * Created by loande on 3/2/15.
 */
trait FileJWKKeySource extends JWKKeySource {
  override def getJWKSet: JWKSet = JWKSet.parse(source)
}
