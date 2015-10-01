package glasskey

import glasskey.config.OAuthConfig

/**
 * Created by loande on 3/2/15. This should only be used in tests
 */
class NeutralTestRuntimeEnvironment extends RuntimeEnvironment(new OAuthConfig.Default())