package glasskey.util

import java.security.{NoSuchAlgorithmException, Signature, SignatureException}

import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import glasskey.resource.{Claim, OIDCTokenData, _}
import org.apache.commons.codec.binary.Base64

import scala.collection.JavaConversions._


trait JWTTokenDecoder {

  protected def algorithms: Map[String, String]
  protected def mapper: ObjectMapper with ScalaObjectMapper
  protected def rsaKeySource: PublicKeySource


  def verify(token: String): Seq[Claim[_]] = {
    val pieces = token.split("\\.")
    val jwtHeader = grabHeader(pieces)

    getJWT((pieces, getAlgorithm(jwtHeader), getKeyId(jwtHeader)))
  }

  private def grabHeader: PartialFunction[(Array[String]), JsonNode] = {
    case (pieces) if pieces.length == 3 =>
      decodeAndParse(pieces(0))
  }

  private def getJWT: PartialFunction[(Array[String], Option[String], Option[String]), Seq[Claim[_]]] = {
    case (pieces, Some(algo), Some(kid)) if "SHA256withRSA" == algo =>
      try {
        rsaKeySource.getPublicKey(kid) match {
          case Some(pub) =>
            val sig = Signature.getInstance(algo)
            sig.initVerify(pub)
            sig.update((pieces(0) ++ "." ++ pieces(1)).getBytes)

            if (!sig.verify(Base64.decodeBase64(pieces(2)))) throw new SignatureException("Signature verification failed.")
            decodeClaims(decodeAndParse(pieces(1)))
          case None => throw new IllegalStateException("No public key for key id: " + kid)
        }
      } catch {
        case e: NoSuchAlgorithmException =>
          throw new IllegalStateException("Platform is missing RSAwithSHA256 signature algorithm", e)
      }
  }

  def decodeClaims(node: JsonNode) : Seq[Claim[_]] = {
    val something = for (blah <- node.fields) yield {
      blah.getKey match {
        case OIDCTokenData.SUB_CLAIM => Subject(blah.getValue.asText)
        case OIDCTokenData.AUD_CLAIM => Audience(blah.getValue.asText)
        case OIDCTokenData.JTI_CLAIM => JwtId(blah.getValue.asText)
        case OIDCTokenData.ISS_CLAIM => Issuer(blah.getValue.asText)
        case OIDCTokenData.IAT_CLAIM => IssueAtTime(blah.getValue.asInt)
        case OIDCTokenData.EXP_CLAIM => Expire(blah.getValue.asInt)
        case OIDCTokenData.NONCE_CLAIM => Nonce(blah.getValue.asText)
        case OIDCTokenData.AT_HASH_CLAIM => AtHash(blah.getValue.asText)
        case x => new Claim[String] {
          override def name: String = x
          override def value: String = blah.getValue.asText
        }
      }
    }
    something.toSeq
  }

  def decodeAndParse(jwtStr: String): JsonNode =
    mapper.readValue[JsonNode](new String(Base64.decodeBase64(jwtStr)))

  private def getAlgorithm: PartialFunction[(JsonNode), Option[String]] = {
    case (jwtHeader) if jwtHeader.has("alg") =>
      algorithms.get(jwtHeader.get("alg").asInstanceOf[TextNode].asText())
  }

  private def getKeyId: PartialFunction[(JsonNode), Option[String]] = {
    case (jwtHeader) if jwtHeader.has("kid") =>
      Some(jwtHeader.get("kid").asInstanceOf[TextNode].asText())
  }
}

object JWTTokenDecoder {
  def apply(src: String, keySourceType: PublicKeySourceType): JWTTokenDecoder = apply(PublicKeySource(src, keySourceType))

  def apply(util: PublicKeySource): JWTTokenDecoder = {
    val objMapper = new ObjectMapper with ScalaObjectMapper
    objMapper.registerModule(DefaultScalaModule)
    val algoMap = Map("RS256" -> "SHA256withRSA")
    new JWTTokenDecoder {
      override def algorithms = algoMap
      override def mapper = objMapper
      override def rsaKeySource = util
    }
  }
}