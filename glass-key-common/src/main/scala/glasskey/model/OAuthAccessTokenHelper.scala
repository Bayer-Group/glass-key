package glasskey.model

trait OAuthAccessTokenHelper {


  def clientId : String
  def clientSecret : String
  def grantType : String

  def tokenParams: Array[(String, String)] =
    Array[(String, String)](OAuthTerms.GrantType -> grantType,
      OAuthTerms.ClientId -> clientId, OAuthTerms.ClientSecret -> clientSecret)

  def getQueryString(uri: String, params: (String, String)*): String = {
    import java.net.URLEncoder

    params.foldLeft(uri + "?") { (s: String, param: (String, String)) =>
      s + URLEncoder.encode(param._1, "UTF-8") + "=" + URLEncoder.encode(param._2, "UTF-8") + "&"
    }.dropRight(1)
  }
}