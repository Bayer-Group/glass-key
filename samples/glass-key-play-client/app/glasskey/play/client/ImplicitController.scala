package glasskey.play.client

import play.api.mvc._

/**
 * Created by loande on 3/17/15.
 */
object ImplicitController extends Controller{
  def index(name: String) = Action {
    Ok(views.html.implicit_grant(name))
  }
}
