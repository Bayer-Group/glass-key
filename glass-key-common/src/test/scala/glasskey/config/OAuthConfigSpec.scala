package glasskey.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 2/26/15.
 */
class OAuthConfigSpec extends FlatSpec with Matchers {

  // Need to recreate this case in specific lib tests so that it does throw the exception (helps with runtime knowledge)
  "loading library oauthConfig from file" should "not throw an exception due to lazy values" in {
    val config = OAuthConfig.config
  }
}
