package glasskey.model

/**
  * Created by loande on 12/18/15.
  */
trait RefreshAccessTokenHelper extends OAuthAccessTokenHelper {

  override def grantType: String = RefreshToken.name

  def refreshTokenParams(refreshToken: String): Array[(String, String)] =
    tokenParams :+ (OAuthTerms.RefreshToken -> refreshToken)
}