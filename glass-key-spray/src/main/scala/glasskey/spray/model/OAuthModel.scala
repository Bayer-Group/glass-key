package glasskey.spray.model

import spray.json.DefaultJsonProtocol

object OAuthJsonProtocol extends DefaultJsonProtocol {
  import glasskey.model.OAuthAccessToken

  implicit val oAuthTokenFormat = jsonFormat5(OAuthAccessToken)
}
