package glasskey.model.fetchers

import glasskey.model.ProtectedResourceRequest
import glasskey.resource.OIDCTokenData
import glasskey.util.{JWK, JWTTokenDecoder}

/**
 * Created by loande on 3/4/15.
 */

trait IDTokenRequestParameter extends AccessTokenFetcher[Option[OIDCTokenData]] {

  def decoder : JWTTokenDecoder

  def fetch(paramValue: String): Option[OIDCTokenData] = {
    getOIDCToken(paramValue)
  }

  override def matches(request: ProtectedResourceRequest): Boolean = request.idToken.isDefined

  override def fetch(request: ProtectedResourceRequest): Option[OIDCTokenData] =
    request.idToken match {
      case Some(idTokenStr) => Some(new OIDCTokenData(idTokenStr, decoder.verify(idTokenStr)))
      case None => None
    }

  private def getOIDCToken(token: String): Option[OIDCTokenData] = Some(new OIDCTokenData(token, decoder.verify(token)))
}

object IDTokenRequestParameter {
  class Default(jwksUri: String) extends IDTokenRequestParameter {
    override val decoder = JWTTokenDecoder(jwksUri, JWK)
  }
}
