package glasskey.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 2/26/15.
 */
class OAuthProviderConfigSpec extends FlatSpec with Matchers {
  val providerConfig = new OAuthProviderConfig.Default(ConfigFactory.load.getConfig("oauth")getConfig("provider"))

  "loading library oauthConfig from file" should "have values from reference.conf" in {
    providerConfig.authHeaderName should be ("Authorization")
  }
}
