package glasskey.spray

import glasskey.model.OAuthAccessToken
import glasskey.resource.OIDCTokenData

package object resource {
  import glasskey.model.ValidatedData
  import scala.concurrent.Future

  type AccessTokenAuthenticator = ((OAuthAccessToken, Option[OIDCTokenData])) => Future[Option[ValidatedData]]
}
