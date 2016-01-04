package glasskey.play.client

import glasskey.config.OAuthConfig

object ResourceOwnerController extends SampleController {
  override def env: PlayClientRuntimeEnvironment = PlayClientRuntimeEnvironment("hello-resource_owner-client")
}