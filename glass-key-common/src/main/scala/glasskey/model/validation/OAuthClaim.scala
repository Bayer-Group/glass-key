package glasskey.model.validation

/**
 * Created by loande on 6/8/15.
 */
sealed trait OAuthClaim {
  def name : String
}

case object Scope extends OAuthClaim {
  override def name: String = "scope"
}
case object Client_Id extends OAuthClaim {
  override def name: String = "client_id"
}
case object Org extends OAuthClaim {
  override def name: String = "org"
}
case object UserName extends OAuthClaim {
  override def name: String = "username"
}

object OAuthClaim {
  val values = Seq(Scope, Client_Id, Org, UserName)
  def toValue(name: String) : Option[OAuthClaim] = values.find(_.name == name)
}