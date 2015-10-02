package glasskey.spray.resource

import glasskey.config.{OAuthConfig, ClientConfig}
import glasskey.resource.validation.{ValidationResponse, Validator}
import glasskey.spray.resource.validation.SprayOAuthValidator

import scala.concurrent.ExecutionContext

class SprayResourceRuntimeEnvironment[R](val tokenValidators: Iterable[Validator[R]] = Iterable.empty)

object SprayResourceRuntimeEnvironment {

  def apply(validators: Iterable[Validator[ValidationResponse]])(implicit ec: ExecutionContext): SprayResourceRuntimeEnvironment[ValidationResponse] = {
    new SprayResourceRuntimeEnvironment[ValidationResponse](validators)
  }

  def apply(clientConfigKey: String)(implicit ec: ExecutionContext): SprayResourceRuntimeEnvironment[ValidationResponse] = {
    val clientConfig = OAuthConfig.clients(clientConfigKey)
    this(clientConfig)
  }

  def apply(clientConfig: ClientConfig)(implicit ec: ExecutionContext): SprayResourceRuntimeEnvironment[ValidationResponse] = {
    new SprayResourceRuntimeEnvironment[ValidationResponse](
      Seq(new SprayOAuthValidator.Default(clientConfig)))
  }
}
