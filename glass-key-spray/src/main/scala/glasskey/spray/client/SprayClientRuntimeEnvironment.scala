package glasskey.spray.client

import glasskey.db.DAOService
import glasskey.ClientRuntimeEnvironment
import glasskey.config.OAuthConfig

class SprayClientRuntimeEnvironment(val tokenHelper: SprayOAuthAccessTokenHelper) extends ClientRuntimeEnvironment{
  import glasskey.db.DBTokenService
  import glasskey.model.OAuthAccessToken

  override val daoService: DAOService[OAuthAccessToken] = DBTokenService("oauth-token-db")
}

object SprayClientRuntimeEnvironment {
  def apply(clientConfigKey: String) = {
    val clientConfig = OAuthConfig.clients(clientConfigKey)
    new SprayClientRuntimeEnvironment(new SprayOAuthAccessTokenHelper(clientConfig))
  }
  def apply(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            grantType: String, authUrl: String, accessTokenUrl: String,
            providerWantsBasicAuth : Boolean) = {
    new SprayClientRuntimeEnvironment(new SprayOAuthAccessTokenHelper(clientId,
      clientSecret, apiRedirectUri, resourceOwnerUsername, resourceOwnerPassword, grantType, authUrl,
      accessTokenUrl, providerWantsBasicAuth))
  }
}


