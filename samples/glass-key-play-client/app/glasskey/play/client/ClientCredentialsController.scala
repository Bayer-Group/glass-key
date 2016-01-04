package glasskey.play.client

import glasskey.config.OAuthConfig


object ClientCredentialsController extends SampleController {
  override def env: PlayClientRuntimeEnvironment = PlayClientRuntimeEnvironment("hello-client_credentials-client")
}