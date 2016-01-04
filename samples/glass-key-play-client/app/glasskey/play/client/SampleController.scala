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

abstract class SampleController extends OAuthController.Default {

  def httpMethod: WSRequestHolder => Future[WSResponse] = { holder: WSRequestHolder => holder.get() }

  def doSomethingWithResult: WSResponse => Result = { response: WSResponse =>
    Ok(Json.toJson((response.json).validate[HelloModelReturn].get))
  }

  def index(name: String) = Action.async { implicit request =>
    doAction(
      request,
       "http://localhost:9000/secure_hello?name=" + name,
      httpMethod,
      doSomethingWithResult)
  }
}
