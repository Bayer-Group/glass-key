package glasskey.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 2/26/15.
 */
class OAuthConfigClientMock(override val config: Config = ConfigFactory.load("oauth_sample_client.conf").getConfig("oauth"))
  extends OAuthConfig.Default

class OAuthConfigResourceMock(override val config: Config = ConfigFactory.load("oauth_sample_resource.conf").getConfig("oauth"))
  extends OAuthConfig.Default

class OAuthConfigSpec extends FlatSpec with Matchers {

  // Need to recreate this case in specific lib tests so that it does throw the exception (helps with runtime knowledge)
  "loading library oauthConfig from file" should "not throw an exception due to lazy values" in {
    val config = new OAuthConfig.Default()
  }

  "Initialized oauthConfig" should "have good values" in {
    val config = new OAuthConfigClientMock
    config.providerConfig.authHeaderName should be ("Authorization") //From lib conf file
    config.clients.size should be (3) // from app-specific conf file (normally application.conf)
  }
}
