package glasskey.play.client

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

object AuthCodeController extends SampleController(PlayClientRuntimeEnvironment("hello-authcode-client")) {

  override def index(name: String) = Action.async { implicit request =>
    doAction(
      request,
      Some(routes.AuthCodeController.index(name).absoluteURL()),
      Some("http://localhost:9000/aws/rest/api/greeting_secure_oidc?name=" + name),
      httpMethod,
      doSomethingWithResult)
  }
}
