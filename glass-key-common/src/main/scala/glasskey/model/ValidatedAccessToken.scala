package glasskey.model

/**
 * Created by loande on 6/8/15.
 */
//Play doesn't care about names of parameters, only position, however Spray does
trait ValidatedToken {
  val client_id: Option[String]
  val access_token: ValidatedData
}

case class BaseValidatedAccessToken(client_id: Option[String], access_token: BaseValidatedData) extends ValidatedToken

case class ValidatedAccessToken(client_id: Option[String],
                                     token: Option[String],
                                     refreshToken: Option[String],
                                     scope: Option[String],
                                     token_type: Option[String],
                                     expires_in: Option[Long],
                                     access_token: OAuthValidatedData) extends ValidatedToken
