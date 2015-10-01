package glasskey.model

/**
 * Created by loande on 6/17/15.
 */
case class ValidatedEntitlementAccessToken(client_id: Option[String],
                                           token: Option[String],
                                           refreshToken: Option[String],
                                           scope: Option[String],
                                           token_type: Option[String],
                                           expires_in: Option[Long],
                                           access_token: ValidatedEntitlementData) extends ValidatedToken
