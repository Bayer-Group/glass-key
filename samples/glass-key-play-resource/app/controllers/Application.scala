package controllers

import glasskey.model.OAuthValidatedData
import glasskey.play.resource.{OAuthRequest, OAuthAction, PlayResourceRuntimeEnvironment}
import play.api.libs.json.Json
import play.api.mvc._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  def workToDo(name: String): OAuthRequest[AnyContent] => play.api.mvc.Result = {
    authInfo: OAuthRequest[AnyContent] =>
      Ok(Json.toJson(authInfo.user.user_id match {
        case Some(usrname) => Map("message" -> ("Hello to a user with username = " + usrname + ", you said your name was " + name))
        case None => Map("message" -> ("Hello client with id = " + authInfo.user.asInstanceOf[OAuthValidatedData].client_id.getOrElse("CANT_FIND_IT") + ", you said your name was " + name))
      }))
  }

  def index(name: String) = {
    implicit val env = PlayResourceRuntimeEnvironment("hello-validation-client")(defaultContext)
    OAuthAction(workToDo(name))
  }
}