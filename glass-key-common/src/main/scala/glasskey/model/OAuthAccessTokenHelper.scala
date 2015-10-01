package glasskey.model

trait OAuthAccessTokenHelper {

  import scala.concurrent.{ExecutionContext, Future}

  def clientId : String

  def clientSecret : Option[String]

  def grantType : String

  def authRequestUri : String

  def apiAuthState : String

  def accessTokenUri : String

  def providerWantsBasicAuth : Boolean

  def generateClientCredentialsAccessToken(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]]

  def generateResourceOwnerAccessToken(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]]

  def grantTypeParams(grant_type: GrantType, authCode: Option[String]): Array[(String, String)]

  def generateAuthCodeAccessToken(authCode: String, state: String)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]]

  def generateToken(params: (String, String)*)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]]

  def getQueryString(uri: String, params: (String, String)*): String
}

object OAuthAccessTokenHelper {


  abstract class Default(override val clientId: String,
                         override val clientSecret: Option[String],
                         apiRedirectUri: Option[String],
                         resourceOwnerUsername: Option[String],
                         resourceOwnerPassword: Option[String],
                         override val grantType:String,
                         authUrl: String, override val accessTokenUri: String,
                         override val providerWantsBasicAuth : Boolean) extends OAuthAccessTokenHelper {

    import java.util.UUID

    import scala.concurrent.{ExecutionContext, Future}


    val apiAuthState = UUID.randomUUID().toString
    lazy val authRequestUri = Seq(s"$authUrl?",
      s"${OAuthTerms.ResponseType}=${OAuthTerms.AuthCodeResponseType}&",
      s"${OAuthTerms.Scope}=${OAuthTerms.OpenIDScope}&",
      s"${OAuthTerms.ClientId}=$clientId&",
      s"${OAuthTerms.State}=$apiAuthState&",
      s"${OAuthTerms.RedirectURI}=${apiRedirectUri.get}").mkString("")

    def generateClientCredentialsAccessToken(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
      generateToken(grantTypeParams(ClientCredentials, None): _*)

    def generateResourceOwnerAccessToken(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
      generateToken(grantTypeParams(ResourceOwner, None): _*)

    def grantTypeParams(grant_type: GrantType, addtlInfo: Option[String]): Array[(String, String)] = {
      val paramMap = Array[(String, String)](OAuthTerms.GrantType -> grant_type.name,
        OAuthTerms.ClientId -> clientId)

      val withAuth = if (!clientSecret.isEmpty) paramMap :+ OAuthTerms.ClientSecret -> clientSecret.get else paramMap //un-necessary for auth code and resource owner, but if configured it is necessary

      grant_type match {
        case AuthorizationCode => withAuth :+ (OAuthTerms.RedirectURI -> apiRedirectUri.get) :+ (OAuthTerms.AuthCodeResponseType -> addtlInfo.get)
        case ResourceOwner => withAuth :+ (OAuthTerms.UserName -> resourceOwnerUsername.get) :+ (OAuthTerms.Password -> resourceOwnerPassword.get)
        case RefreshToken => withAuth :+ (OAuthTerms.RefreshToken -> addtlInfo.get)
        case _ => withAuth // no-op on client_credentials
      }
    }

    def generateAuthCodeAccessToken(authCode: String, state: String)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
      generateToken(grantTypeParams(AuthorizationCode, Some(authCode)): _*)

    def refreshToken(refreshToken: String)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]] =
      generateToken(grantTypeParams(RefreshToken, Some(refreshToken)): _*)

    def generateToken(params: (String, String)*)(implicit ec: ExecutionContext): Future[Option[OAuthAccessToken]]

    def getQueryString(uri: String, params: (String, String)*): String = {
      import java.net.URLEncoder

      params.foldLeft(uri + "?") { (s: String, param: (String, String)) =>
        s + URLEncoder.encode(param._1, "UTF-8") + "=" + URLEncoder.encode(param._2, "UTF-8") + "&"
      }.dropRight(1)
    }
  }

}
