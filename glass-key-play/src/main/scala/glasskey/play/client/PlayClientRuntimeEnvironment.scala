package glasskey.play.client

import glasskey.db.DBTokenService
import glasskey.ClientRuntimeEnvironment
import glasskey.config.OAuthConfig

/**
 * Created by loande on 2/16/15.
 */

class PlayClientRuntimeEnvironment(val tokenHelper: PlayOAuthAccessTokenHelper) extends ClientRuntimeEnvironment {
  override val daoService = DBTokenService("oauth-token-db")
}

object PlayClientRuntimeEnvironment {

  def apply(clientConfigKey: String) = {
    val clientConfig = OAuthConfig.clients(clientConfigKey)
    new PlayClientRuntimeEnvironment(new PlayOAuthAccessTokenHelper(clientConfig))
  }
  def apply(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            grantType: String, authUrl: String, accessTokenUrl: String,
            providerWantsBasicAuth : Boolean) = {
    new PlayClientRuntimeEnvironment(new PlayOAuthAccessTokenHelper(clientId,
      clientSecret, apiRedirectUri, resourceOwnerUsername, resourceOwnerPassword, grantType, authUrl,
      accessTokenUrl, providerWantsBasicAuth))
  }

}
