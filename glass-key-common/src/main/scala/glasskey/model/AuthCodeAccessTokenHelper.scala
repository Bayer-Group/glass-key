package glasskey.model

import java.util.UUID

/**
  * Created by loande on 12/18/15.
  */
trait AuthCodeAccessTokenHelper extends OAuthAccessTokenHelper {

  def authUrl: String
  var redirectUri: String
  override def grantType: String = AuthorizationCode.name

  val apiAuthState = UUID.randomUUID().toString
  lazy val authRequestUri = Seq(s"$authUrl?",
    s"${OAuthTerms.ResponseType}=${OAuthTerms.AuthCodeResponseType}&",
    s"${OAuthTerms.Scope}=${OAuthTerms.OpenIDScope}&",
    s"${OAuthTerms.ClientId}=$clientId&",
    s"${OAuthTerms.State}=$apiAuthState&",
    s"${OAuthTerms.RedirectURI}=$redirectUri").mkString("")

  def authCodeParams(code: String): Array[(String, String)] =
    tokenParams :+ (OAuthTerms.RedirectURI -> redirectUri) :+ (OAuthTerms.AuthCodeResponseType -> code)
}
