package glasskey.model


/**
  * Created by loande on 12/18/15.
  */
trait ResourceOwnerAccessTokenHelper extends OAuthAccessTokenHelper {

  def resourceOwnerUsername: String
  def resourceOwnerPassword: String
  override def grantType: String = ResourceOwner.name

  override def tokenParams: Array[(String, String)] =
    super[OAuthAccessTokenHelper].tokenParams :+
      (OAuthTerms.UserName -> resourceOwnerUsername) :+ (OAuthTerms.Password -> resourceOwnerPassword)
}