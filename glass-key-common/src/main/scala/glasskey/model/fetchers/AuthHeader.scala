package glasskey.model.fetchers

import glasskey.config.OAuthConfig
import glasskey.model.{ProtectedResourceRequest, _}
import glasskey.resource.OIDCTokenData

/**
 * Created by loande on 12/1/2014.
 */

object AuthHeader {

  class Default extends AccessTokenFetcher[(OAuthAccessToken, Option[OIDCTokenData])] {    val REGEXP_AUTHORIZATION = """^\s*(OAuth|Bearer)\s+([^\s\,]*)""".r
    val REGEXP_TRIM = """^\s*,\s*""".r
    val REGEXP_DIV_COMMA = """,\s*""".r
    val idTokenHdr = new IDTokenAuthHeader.Default(OAuthConfig.providerConfig.jwksUri, OAuthConfig.providerConfig.idHeaderName, OAuthConfig.providerConfig.idHeaderPrefix)

    override def matches(request: ProtectedResourceRequest): Boolean = {
      request.header(OAuthConfig.providerConfig.authHeaderName).exists { header =>
        REGEXP_AUTHORIZATION.findFirstMatchIn(header).isDefined
      }
    }

    override def fetch(request: ProtectedResourceRequest): (OAuthAccessToken, Option[OIDCTokenData]) = {
      val header = request.requireHeader(OAuthConfig.providerConfig.authHeaderName)
      val matcher = REGEXP_AUTHORIZATION.findFirstMatchIn(header).getOrElse {
        throw new InvalidRequest(s"${OAuthConfig.providerConfig.authHeaderName} is invalid")
      }

      val token = matcher.group(2)

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

