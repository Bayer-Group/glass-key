package glasskey.model.fetchers

import glasskey.RuntimeEnvironment
import glasskey.model.{ProtectedResourceRequest, _}
import glasskey.resource.OIDCTokenData

/**
 * Created by loande on 12/1/2014.
 */

object AuthHeader {

  class Default(env: RuntimeEnvironment) extends AccessTokenFetcher[(OAuthAccessToken, Option[OIDCTokenData])] {
    val REGEXP_AUTHORIZATION = """^\s*(OAuth|Bearer)\s+([^\s\,]*)""".r
    val REGEXP_TRIM = """^\s*,\s*""".r
    val REGEXP_DIV_COMMA = """,\s*""".r
    val idTokenHdr = new IDTokenAuthHeader.Default(env.config.providerConfig.jwksUri, env.config.providerConfig.idHeaderName, env.config.providerConfig.idHeaderPrefix)

    override def matches(request: ProtectedResourceRequest): Boolean = {
      request.header(env.config.providerConfig.authHeaderName).exists { header =>
        REGEXP_AUTHORIZATION.findFirstMatchIn(header).isDefined
      }
    }

    override def fetch(request: ProtectedResourceRequest): (OAuthAccessToken, Option[OIDCTokenData]) = {
      val header = request.requireHeader(env.config.providerConfig.authHeaderName)
      val matcher = REGEXP_AUTHORIZATION.findFirstMatchIn(header).getOrElse {
        throw new InvalidRequest(s"${env.config.providerConfig.authHeaderName} is invalid")
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

