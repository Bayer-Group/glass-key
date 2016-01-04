package glasskey.play.client

import glasskey.db.DBTokenService
import glasskey.ClientRuntimeEnvironment
import glasskey.config.OAuthConfig
import glasskey.model.GrantType

/**
 * Created by loande on 2/16/15.
 */

class PlayClientRuntimeEnvironment(val tokenHelper: PlayOAuthAccessTokenHelper) extends ClientRuntimeEnvironment {
  override val daoService = DBTokenService("oauth-token-db")
}

object PlayClientRuntimeEnvironment {

  def apply(clientConfigKey: String) = {
    val clientConfig = OAuthConfig.clients(clientConfigKey)
    new PlayClientRuntimeEnvironment(PlayOAuthAccessTokenHelper(clientConfig))
  }
  def apply(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            grantType: String, authUrl: String, accessTokenUrl: String,
            providerWantsBasicAuth : Boolean) = {
    new PlayClientRuntimeEnvironment(PlayOAuthAccessTokenHelper(clientId,
      clientSecret, apiRedirectUri, resourceOwnerUsername, resourceOwnerPassword, authUrl,
      accessTokenUrl, providerWantsBasicAuth, GrantType.withName(grantType)))
  }
}
