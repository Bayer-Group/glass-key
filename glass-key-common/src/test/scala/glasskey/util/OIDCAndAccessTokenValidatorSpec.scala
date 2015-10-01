package glasskey.util

import java.security.{MessageDigest, Signature, SignatureException}

import glasskey.model.fetchers.IDTokenAccessTokenValidatorMock
import glasskey.resource.OIDCTokenData
import org.apache.commons.codec.binary.Base64
import org.scalatest.{FlatSpec, Matchers}


class OIDCAndAccessTokenValidatorSpec extends FlatSpec with Matchers with IDTokenAccessTokenValidatorMock {

  "Checking a JWT Access Token via ID token" should "validate" in {
    val jwt = mockIDDecoder.verify(id_token)
    jwt should not be empty
    jwt.seq.size should be (9)
    val atHash = jwt find {e => e.name == OIDCTokenData.AT_HASH_CLAIM}
    atHash.isDefined should be (true)
    val jwtHeader = Map("alg" -> "RS256", "kid" -> "629ie")
    val hasher = MessageDigest.getInstance("SHA-256")
    val hashedAsciiRep = hasher.digest(access_token.getBytes("US-ASCII"))
    val b64UrlEncodedHalf = new String(Base64.encodeBase64URLSafe(copyHalf(hashedAsciiRep)), "US-ASCII")
    atHash.get.value should be (b64UrlEncodedHalf)
    val pieces = access_token.split("\\.")
    val atClaims = mockIDDecoder.decodeClaims(mockIDDecoder.decodeAndParse(pieces(1)))
    atClaims.size should be (9)
    val atToken = OIDCTokenData(pieces(1), atClaims)
  }

  "Checking a JWT Access token via X509 Cert" should "validate" in {
    val jwt = mockJWTATDecoder.verify(access_token)
    jwt should not be empty
    jwt.seq.size should be (9)
    val idToken = OIDCTokenData(access_token, jwt)
  }

  def copyHalf(array:Array[Byte]):Array[Byte] = {
    val newArray = new Array[Byte](array.size/2)
    Array.copy(array, 0, newArray, 0, array.size/2)
    newArray
  }
}
