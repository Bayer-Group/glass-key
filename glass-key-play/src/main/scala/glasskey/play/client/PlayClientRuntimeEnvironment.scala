package glasskey.play.client

import glasskey.db.DBTokenService
import glasskey.{ClientRuntimeEnvironment, RuntimeEnvironment}
import glasskey.config.OAuthConfig

/**
 * Created by loande on 2/16/15.
 */

class PlayClientRuntimeEnvironment(config: OAuthConfig, val tokenHelper: PlayOAuthAccessTokenHelper) extends RuntimeEnvironment(config) with ClientRuntimeEnvironment {
  override val daoService = DBTokenService("oauth-token-db")
}

object PlayClientRuntimeEnvironment {

  def apply(clientConfigKey: String, config: OAuthConfig) = {
    val clientConfig = config.clients(clientConfigKey)
    new PlayClientRuntimeEnvironment(config, new PlayOAuthAccessTokenHelper(clientConfig, config.providerConfig))
  }
  def apply(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            grantType: String, authUrl: String, accessTokenUrl: String,
            providerWantsBasicAuth : Boolean) = {
    new PlayClientRuntimeEnvironment(new OAuthConfig.Default(), new PlayOAuthAccessTokenHelper(clientId,
      clientSecret, apiRedirectUri, resourceOwnerUsername, resourceOwnerPassword, grantType, authUrl,
      accessTokenUrl, providerWantsBasicAuth))
  }

}
