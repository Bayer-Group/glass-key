package glasskey.play.resource

import glasskey.config.OAuthConfig
import glasskey.play.resource.validation.PlayOAuthValidator
import glasskey.resource.validation.Validator
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext

class PlayResourceRuntimeEnvironment[R](val tokenValidators: Iterable[Validator[R]])

object PlayResourceRuntimeEnvironment {

  def apply(clientConfigKey: String)(implicit ec: ExecutionContext): PlayResourceRuntimeEnvironment[WSResponse] = {
    val clientConfig = OAuthConfig.clients(clientConfigKey)
    new PlayResourceRuntimeEnvironment[WSResponse](
      Seq(PlayOAuthValidator(clientConfig)))
  }
}
