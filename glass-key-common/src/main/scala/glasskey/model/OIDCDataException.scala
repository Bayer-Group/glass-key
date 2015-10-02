package glasskey.model

/**
 * Created by loande on 7/7/15.
 */
class OIDCDataNotAvailableException(ex: OAuthException) extends Throwable(ex) {
  def this(message: String) = this(new InvalidToken(message))
}
