package glasskey.spray.client

import glasskey.db.DAOService
import glasskey.ClientRuntimeEnvironment
import glasskey.config.OAuthConfig
import glasskey.model.GrantType

class SprayClientRuntimeEnvironment(val tokenHelper: SprayOAuthAccessTokenHelper) extends ClientRuntimeEnvironment{
  import glasskey.db.DBTokenService
  import glasskey.model.OAuthAccessToken

  override val daoService: DAOService[OAuthAccessToken] = DBTokenService("oauth-token-db")
}

object SprayClientRuntimeEnvironment {
  def apply(clientConfigKey: String) = {
    val clientConfig = OAuthConfig.clients(clientConfigKey)
    new SprayClientRuntimeEnvironment(SprayOAuthAccessTokenHelper(clientConfig))
  }
  def apply(clientId: String, clientSecret: Option[String], apiRedirectUri: Option[String],
            resourceOwnerUsername: Option[String], resourceOwnerPassword: Option[String],
            grantType: String, authUrl: String, accessTokenUrl: String,
            providerWantsBasicAuth : Boolean) = {
    new SprayClientRuntimeEnvironment(SprayOAuthAccessTokenHelper(clientId,
      clientSecret, apiRedirectUri, resourceOwnerUsername, resourceOwnerPassword, authUrl,
      accessTokenUrl, providerWantsBasicAuth, GrantType.withName(grantType)))
  }
}