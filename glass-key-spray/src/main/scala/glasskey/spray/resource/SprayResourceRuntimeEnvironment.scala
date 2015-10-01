package glasskey.spray.resource

import glasskey.RuntimeEnvironment
import glasskey.config.OAuthConfig
import glasskey.resource.validation.{ValidationResponse, OAuthValidator}
import glasskey.spray.resource.validation.SprayOAuthValidator

import scala.concurrent.ExecutionContext

class SprayResourceRuntimeEnvironment[R](config: OAuthConfig, val tokenValidators: Iterable[OAuthValidator[R]]) extends RuntimeEnvironment(config)

object SprayResourceRuntimeEnvironment {

  def apply(clientConfigKey: String, config: OAuthConfig)(implicit ec: ExecutionContext) = {
    val clientConfig = config.clients(clientConfigKey)
    new SprayResourceRuntimeEnvironment[ValidationResponse](config,
      Seq(new SprayOAuthValidator.Default(config.providerConfig, clientConfig)))
  }

}
