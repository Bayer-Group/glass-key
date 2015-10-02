package glasskey.model.fetchers

import glasskey.config.OAuthConfig
import glasskey.model.{ProtectedResourceRequest, _}
import glasskey.resource.OIDCTokenData
import scala.language.postfixOps
/**
 * Created by loande on 7/20/2014.
 */

object Cookie {

  class Default(cookieName: String) extends AccessTokenFetcher[(OAuthAccessToken, Option[OIDCTokenData])] {
    val idTokenHdr = new IDTokenAuthHeader.Default(OAuthConfig.providerConfig.jwksUri, OAuthConfig.providerConfig.idHeaderName, OAuthConfig.providerConfig.idHeaderPrefix)

    override def matches(request: ProtectedResourceRequest): Boolean =
      request.header("Cookie").exists { header => header.contains(cookieName)}


    override def fetch(request: ProtectedResourceRequest): (OAuthAccessToken, Option[OIDCTokenData]) = {
      val cookieValues = request.headers.filter(h => h._1 == "Cookie").unzip._2.flatten
      val authCookieValues = cookieValues.filter { cookieVal => cookieVal.split("=")(0) == cookieName }
      val token = if (authCookieValues.nonEmpty && authCookieValues.head.split("=").size > 1) authCookieValues.head.split('=')(1) else throw new InvalidRequest(s"No auth token found for cookie $cookieName.")
      val idToken = if (idTokenHdr.matches(request)) idTokenHdr.fetch(request) else None

      (OAuthAccessToken(None,
        OAuthTerms.Bearer,
        None,
        token,
        None),
        idToken)
    }
  }

}

