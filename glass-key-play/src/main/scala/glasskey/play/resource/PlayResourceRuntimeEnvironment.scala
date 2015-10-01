package glasskey.play.resource

import glasskey.RuntimeEnvironment
import glasskey.config.OAuthConfig
import glasskey.play.resource.validation.PlayOAuthValidator
import glasskey.resource.validation.OAuthValidator
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext

class PlayResourceRuntimeEnvironment[R](config: OAuthConfig, val tokenValidators: Iterable[OAuthValidator[R]]) extends RuntimeEnvironment(config)

object PlayResourceRuntimeEnvironment {

  def apply(clientConfigKey: String, config: OAuthConfig)(implicit ec: ExecutionContext) = {
    val clientConfig = config.clients(clientConfigKey)
    new PlayResourceRuntimeEnvironment[WSResponse](config,
      Seq(new PlayOAuthValidator.Default(config.providerConfig, clientConfig)))
  }
}
