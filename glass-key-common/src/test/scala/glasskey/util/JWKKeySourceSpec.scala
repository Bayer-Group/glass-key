package glasskey.util

import java.security.interfaces.RSAPublicKey

import com.nimbusds.jose.jwk.{JWK, RSAKey}
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.scalatest.enablers.Emptiness._

import scala.collection.JavaConverters._
import scala.collection.mutable

class JWKKeySourceSpec extends FlatSpec with Matchers {
  "Iterating RSA keys" should "convert to RSA public keys" in {
    val util = new JWKKeySource{ override def source = ConfigFactory.load.getConfig("oauth").getConfig("provider").getString("jwksUri") }
    val jwkSet = util.getJWKSet
    val jwkList : mutable.Buffer[JWK] = jwkSet.getKeys.asScala
    val justRSAKeys: mutable.Buffer[JWK] = jwkList flatMap {
      case rsa: RSAKey => Some(rsa)
      case other => None
    }
    for (rsaKey <- justRSAKeys) {
      val pubKey = util.getPublicKey(rsaKey.getKeyID)
      pubKey shouldBe a [Option[RSAPublicKey]]
      pubKey shouldBe 'defined
    }
  }

  "Iterating non-RSA keys" should "be empty scala Options" in {
    val util = new JWKKeySource { override def source = ConfigFactory.load.getConfig("oauth").getConfig("provider").getString("jwksUri") }
    val jwkSet = util.getJWKSet
    val jwkList : mutable.Buffer[JWK] = jwkSet.getKeys.asScala
    val nonRSAKeys: mutable.Buffer[JWK] = jwkList flatMap {
      case rsa: RSAKey => None
      case other => Some(other)
    }

    for (nonRSAKey <- nonRSAKeys) {
      val pubKey = util.getPublicKey(nonRSAKey.getKeyID)
      pubKey shouldBe a [Option[RSAPublicKey]]
      pubKey shouldBe empty  // It's empty because it's probably an elliptical curve key
    }
  }
}
