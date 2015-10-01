package glasskey.util

import java.security.interfaces.RSAPublicKey

import com.nimbusds.jose.jwk.{JWK, RSAKey}
import com.typesafe.config.ConfigFactory
import glasskey.model.fetchers.IDTokenAccessTokenValidatorMock
import org.scalatest._
import org.scalatest.enablers.Emptiness._

import scala.collection.JavaConverters._
import scala.collection.mutable

class PEMDERKeySourceSpec extends FlatSpec with Matchers with IDTokenAccessTokenValidatorMock {
  "Iterating RSA keys" should "convert to RSA public keys" in {
    val util = new PEMDERKeySource { override def source = ConfigFactory.load.getConfig("oauth").getConfig("provider").getString("access-token-JWT-key-url") }
    val pubKey = util.getPublicKey("1")
    pubKey shouldBe a [Option[RSAPublicKey]]
    pubKey shouldBe 'defined
  }

  "From file: Iterating RSA keys" should "convert to RSA public keys" in {
    val util = new FilePEMDERKeySource { override def source = staticX509PEMDER }
    val pubKey = util.getPublicKey("1")
    pubKey shouldBe a [Option[RSAPublicKey]]
    pubKey shouldBe 'defined
  }
}
