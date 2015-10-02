package glasskey.model

trait ValidatedData {
  val user_id: Option[String]
}

case class BaseValidatedData(user_id: Option[String]) extends ValidatedData

case class OAuthValidatedData(username: Option[String],
                         user_id: Option[String],
                         orgName: Option[String],
                         client_id: Option[String],
                         scopes: Option[Set[String]] = None) extends ValidatedData
