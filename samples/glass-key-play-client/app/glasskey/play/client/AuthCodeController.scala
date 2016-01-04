package glasskey.play.client

import glasskey.model.AuthCodeAccessTokenHelper
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

object AuthCodeController extends SampleController {

  override def env: PlayClientRuntimeEnvironment = PlayClientRuntimeEnvironment("hello-authcode-client")

  override def index(name: String) = Action.async { implicit request =>
    env.tokenHelper.asInstanceOf[AuthCodeAccessTokenHelper].redirectUri = routes.AuthCodeController.index(name).absoluteURL()
    doAction(
      request,
      "http://localhost:9000/secure_hello?name=" + name,
      httpMethod,
      doSomethingWithResult)
  }
}
