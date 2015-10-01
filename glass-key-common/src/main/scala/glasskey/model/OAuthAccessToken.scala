package glasskey.model

/**
 * Created by loande on 6/3/15.
 */
case class OAuthAccessToken(refresh_token: Option[String],
                            token_type: String,
                            expires_in: Option[Long],
                            access_token: String,
                            id_token: Option[String])
