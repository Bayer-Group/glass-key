package glasskey.play.client

import HelloWorldFormats._
import play.api.libs.json.{Json}
import play.api.libs.ws.{WSRequestHolder, WSResponse}
import play.api.mvc.{Action, Result}
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

/**
 * Created by loande on 1/7/2015.
 */

abstract class SampleController(override val env: PlayClientRuntimeEnvironment) extends OAuthController.Default(env){

  def httpMethod(holder : WSRequestHolder):  Future[WSResponse] = holder.get()

  def doSomethingWithResult(response: WSResponse): Result = {
    Ok(Json.toJson((response.json).validate[HelloModelReturn].get))
  }


  def index(name: String) = Action.async { implicit request =>
    doAction(
      request,
      None,
      Some("http://localhost:9000/aws/rest/api/greeting_secure_oidc?name=" + name),
      httpMethod,
      doSomethingWithResult)
  }
}
