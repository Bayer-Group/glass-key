package glasskey.resource

import glasskey.resource.OIDCTokenData._
/**
 * Created by loande on 2/10/15.
 */
case class OIDCTokenData (tokenData: String, tokenDetails: Seq[Claim[_]]) {
  override def toString: String = {
    val s = for (detail <- tokenDetails) yield {
      "Claim Key: " + detail.name + " Claim Value: " + detail.value
    }
    s.mkString("", "\n", "\n")
  }
}

object OIDCTokenData {
  val AUD_CLAIM: String = "aud"
  val SUB_CLAIM: String = "sub"
  val JTI_CLAIM: String = "jti"
  val ISS_CLAIM: String = "iss"
  val IAT_CLAIM: String = "iat"
  val EXP_CLAIM: String = "exp"
  val NONCE_CLAIM: String = "nonce"
  val AT_HASH_CLAIM: String = "at_hash"
}

trait Claim[T] {
  def name : String
  def value : T
  override def toString(): String = s"${name} : ${value}"
}

case class StringClaim(value: String, name: String) extends Claim[String]
case class IntClaim(value: Int, name: String) extends Claim[Int]

case class Audience(value:String) extends Claim[String] {
  def name = AUD_CLAIM
}

case class Subject(value:String) extends Claim[String] {
  def name = SUB_CLAIM
}

case class JwtId(value:String) extends Claim[String] {
  def name = JTI_CLAIM
}

case class Issuer(value:String) extends Claim[String] {
  def name = ISS_CLAIM
}

case class IssueAtTime(value:Int) extends Claim[Int] {
  def name = IAT_CLAIM
}

case class Expire(value:Int) extends Claim[Int] {
  def name = EXP_CLAIM
}

case class Nonce(value:String) extends Claim[String] {
  def name = NONCE_CLAIM
}

case class AtHash(value:String) extends Claim[String] {
  def name = AT_HASH_CLAIM
}