package glasskey.play.client

import play.api.libs.ws.WSResponse
import play.api.mvc.{Result, Action}
import play.api.libs.concurrent.Execution.Implicits._
/**
 * Created by loande on 4/16/15.
 */
object ResourceOwnerControllerCF extends SampleController(PlayClientRuntimeEnvironment(
  "cf_portal_client", Some("cf_portal_secret"), None, Some("admin"), Some("CG1Reh7oGhJyWdXf"), "password",
  "https://login.cf.company.com/oauth/authorize", "https://uaa.cf.company.com/oauth/token", true)) {

  def doSomething(response: WSResponse): Result = {
    Ok("Your results are: " + response.json)
  }


  override def index(name: String) = Action.async { implicit request =>
    doAction(
      request,
      None,
      Some("https://api.cf.company.com/v2/users?results-per-page=20&page=1"),
      httpMethod,
      doSomething)
  }
}
