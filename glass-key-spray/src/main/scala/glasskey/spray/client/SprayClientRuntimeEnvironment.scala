package glasskey.spray.client

import glasskey.db.DAOService
import glasskey.{ClientRuntimeEnvironment, RuntimeEnvironment}
import glasskey.config.OAuthConfig
import glasskey.config.OAuthConfig.Default

class SprayClientRuntimeEnvironment(config: OAuthConfig, val tokenHelper: SprayOAuthAccessTokenHelper) extends RuntimeEnvironment(config) with ClientRuntimeEnvironment{
  import glasskey.db.DBTokenService
  import glasskey.model.OAuthAccessToken

  override val daoService: DAOService[OAuthAccessToken] = DBTokenService("oauth-token-db")
}

object SprayClientRuntimeEnvironment {
  def apply(clientConfigKey: String, config: OAuthConfig) = {
    val clientConfig = config.clients(clientConfigKey)
    new SprayClientRuntimeEnvironment(config, new SprayOAuthAccessTokenHelper(clientConfig, config.providerConfig))
  }
  def apply(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            grantType: String, authUrl: String, accessTokenUrl: String,
            providerWantsBasicAuth : Boolean) = {
    new SprayClientRuntimeEnvironment(new Default(), new SprayOAuthAccessTokenHelper(clientId,
      clientSecret, apiRedirectUri, resourceOwnerUsername, resourceOwnerPassword, grantType, authUrl,
      accessTokenUrl, providerWantsBasicAuth))
  }
}


