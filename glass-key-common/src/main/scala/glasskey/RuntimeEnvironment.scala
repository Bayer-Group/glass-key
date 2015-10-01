package glasskey

import glasskey.config.OAuthConfig

/**
 * Created by loande on 2/16/15.
 */

class RuntimeEnvironment(val config: OAuthConfig)

object RuntimeEnvironment {
  def apply =
    new RuntimeEnvironment(new OAuthConfig.Default())
}
