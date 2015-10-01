package glasskey.model.fetchers

import glasskey.model.InvalidRequest
import glasskey.resource.OIDCTokenData
import glasskey.util.{JWK, JWTTokenDecoder}

trait IDTokenAuthHeader extends AccessTokenFetcher[Option[OIDCTokenData]] {
  import glasskey.model.ProtectedResourceRequest

  val REGEXP_ID_TOKEN = """^\s*(id_token)\s+([^\s\,]*)""".r
  val REGEXP_TRIM = """^\s*,\s*""".r
  val REGEXP_DIV_COMMA = """,\s*""".r

  def decoder : JWTTokenDecoder
  def idHeaderName: String
  def idHeaderPfx: String

  override def matches(request: ProtectedResourceRequest): Boolean =
    request.header(idHeaderName).exists { header =>
      REGEXP_ID_TOKEN.findFirstMatchIn(header).isDefined
    }

  def fetch(header: String): Option[OIDCTokenData] = {
    val matcher = REGEXP_ID_TOKEN.findFirstMatchIn(header).getOrElse {
      throw new InvalidRequest(s"${idHeaderName} is invalid")
    }
    val token = matcher.group(2)
    getOIDCToken(token)
  }

  override def fetch(request: ProtectedResourceRequest): Option[OIDCTokenData] =
    fetch(request.requireHeader(idHeaderName))

  private def getOIDCToken(token: String): Option[OIDCTokenData] = Some(new OIDCTokenData(token, decoder.verify(token)))
}

object IDTokenAuthHeader {


  class Default(jwksUri: String, idHeader: String, idHeaderPrefix: String) extends IDTokenAuthHeader {
    override val decoder = JWTTokenDecoder(jwksUri, JWK)
    override val idHeaderName = idHeader
    override val idHeaderPfx = idHeaderPrefix
  }

}
