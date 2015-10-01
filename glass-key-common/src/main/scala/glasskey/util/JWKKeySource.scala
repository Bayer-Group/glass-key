package glasskey.util

import java.security.PublicKey

/**
 * Created by loande on 6/26/15.
 * This key source retrieves an PublicKey from a URL, retrieving which gets a JWK set (JSON Web Key Set), from
 * which a single key can be retrieved. See https://tools.ietf.org/html/draft-ietf-jose-json-web-key-41#section-5
 */

trait JWKKeySource extends PublicKeySource {

  import com.nimbusds.jose.jwk.{JWK, JWKSet, RSAKey}

  import scala.io.Source
  import scala.util.Try

  private def getKey(keyId: String): Option[RSAKey] =
    getJWK(keyId) match {
      case Some(key: RSAKey) => Some(key.asInstanceOf[RSAKey])
      case _ => None
    }

  private def getJWK(keyId: String): Option[JWK] = Try(getJWKSet.getKeyByKeyId(keyId)).toOption

  def getJWKSet: JWKSet = JWKSet.parse(Source.fromURL(source).mkString)

  override def getPublicKey(keyId: String): Option[PublicKey] = getKey(keyId).map(k => RSAKey.parse(k.toJSONObject).toRSAPublicKey)
}
