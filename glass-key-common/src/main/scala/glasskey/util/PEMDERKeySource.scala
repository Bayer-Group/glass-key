package glasskey.util

import java.io.ByteArrayInputStream
import java.security.cert.{X509Certificate, CertificateFactory}
import java.security.{PublicKey}
import org.apache.commons.codec.binary.Base64

import scala.io.Source
import scala.util.Try

/**
 * Created by loande on 6/26/15.
 * This key source retrieves an PublicKey from a URL, retrieving which gets a PEM-contained, DER-encoded
 * certificate. See https://tools.ietf.org/html/rfc5280#ref-X.690
 */
trait PEMDERKeySource extends PublicKeySource {
  override def getPublicKey(keyId: String): Option[PublicKey] =
    getKey(keyId).map {
      k =>
        val is = new ByteArrayInputStream(Base64.decodeBase64(k.getBytes))
        val cert = CertificateFactory.getInstance("X.509").generateCertificate(is).asInstanceOf[X509Certificate]
        cert.getPublicKey
    }

  def getKey(kid: String): Option[String] = Try{
    val afterFirst = Source.fromURL(s"$source?v=$kid").getLines.drop(1).toSeq
    afterFirst.dropRight(1).mkString
  }.toOption

}
