package glasskey.util

import java.security.PublicKey

/**
 * Created by loande on 6/26/15.
 */
trait PublicKeySource {
  def source: String
  def getPublicKey(keyId: String): Option[PublicKey]
}

object PublicKeySource {
  def apply(src: String, keySourceType: PublicKeySourceType):PublicKeySource = {
    keySourceType match {
      case JWK => new JWKKeySource {override def source = src}
      case FileJWK => new FileJWKKeySource {override def source = src}
      case PEMDER => new PEMDERKeySource {override def source = src}
    }
  }
}

sealed trait PublicKeySourceType

case object JWK extends PublicKeySourceType
case object FileJWK extends PublicKeySourceType
case object PEMDER extends PublicKeySourceType