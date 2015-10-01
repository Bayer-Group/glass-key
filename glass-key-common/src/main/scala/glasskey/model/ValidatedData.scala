package glasskey.model

trait ValidatedData {
  val client_id: Option[String]
  val orgName: Option[String]
  val username: Option[String]
  val user_id: Option[String]
}

case class BaseValidatedData(username: Option[String],
                         user_id: Option[String],
                         orgName: Option[String],
                         client_id: Option[String],
                         scopes: Option[Set[String]] = None) extends ValidatedData
