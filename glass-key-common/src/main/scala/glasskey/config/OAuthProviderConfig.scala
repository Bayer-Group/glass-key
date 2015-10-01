package glasskey.config

import com.typesafe.config.Config



/**
 * Created by loande on 12/8/2014.
 */
trait OAuthProviderConfig {
  val authUrl : String
  val accessTokenUrl : String
  val authHeaderName : String
  val authHeaderPrefix : String
  val idHeaderName : String
  val idHeaderPrefix : String
  val authCookieName : String
  val validationUri : String
  val jwksUri : String
  val validationGrantType : String
  val providerWantsBasicAuth : Boolean
}
object OAuthProviderConfig {
  class Default(c: Config) extends OAuthProviderConfig {
    val authUrl = c.getString("authUrl")
    val accessTokenUrl = c.getString("accessTokenUrl")
    val authHeaderName = c.getString("authHeaderName")
    val authHeaderPrefix = c.getString("authHeaderPrefix")
    val idHeaderName = c.getString("idHeaderName")
    val idHeaderPrefix = c.getString("idHeaderPrefix")
    val authCookieName = c.getString("authCookieName")
    val validationUri = c.getString("validation-uri")
    val jwksUri = c.getString("jwksUri")
    val validationGrantType = c.getString("validation-grant-type")
    val providerWantsBasicAuth = c.getBoolean("provider-wants-basic-auth")
  }
}
