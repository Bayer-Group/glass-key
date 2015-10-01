package glasskey.model.fetchers

import glasskey.model.OAuthTerms
import glasskey.resource.OIDCTokenData

object RequestParameter {

  import glasskey.model.OAuthAccessToken

  class Default(jwksUri: String) extends AccessTokenFetcher[(OAuthAccessToken, Option[OIDCTokenData])] {

    import glasskey.model.ProtectedResourceRequest

    val idTokenParam = new IDTokenRequestParameter.Default(jwksUri)

    override def matches(request: ProtectedResourceRequest): Boolean = {
      request.oauthToken.isDefined || request.accessToken.isDefined
    }

    override def fetch(request: ProtectedResourceRequest): (OAuthAccessToken, Option[OIDCTokenData]) = {
      val t = request.oauthToken.getOrElse(request.requireAccessToken)
      val idToken = if (idTokenParam.matches(request)) idTokenParam.fetch(request) else None
      (OAuthAccessToken(None, OAuthTerms.Bearer, None, t, None), idToken)
    }
  }

}
