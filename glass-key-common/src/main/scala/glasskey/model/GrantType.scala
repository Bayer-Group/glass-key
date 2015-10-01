package glasskey.model

sealed trait GrantType {
  val name: String
}

case object AuthorizationCode extends GrantType {
  val name = OAuthTerms.AuthorizationCode
}

case object ClientCredentials extends GrantType {
  val name = OAuthTerms.ClientCredentials
}

case object ResourceOwner extends GrantType {
  val name = OAuthTerms.Password
}

case object RefreshToken extends GrantType {
  val name = OAuthTerms.RefreshToken
}

object GrantType {
  val values = Seq(AuthorizationCode, ClientCredentials, ResourceOwner, RefreshToken)

  final def withName(s: String): GrantType =
    values.find(_.name == s).getOrElse(throw new NoSuchElementException(s"No value found for '$s'"))
}
