package glasskey.model.validation

import glasskey.model.ValidatedAccessToken

/**
 * Created by loande on 3/24/15.
 */
trait OAuthClaimValidator extends ClaimValidator[ValidatedAccessToken] {
  def hasScope(scope: String) = OAuthHas(Scope, scope)
  def hasClientId(clientId: String) = OAuthHas(Client_Id, clientId)
  def hasOrg(orgName: String) = OAuthHas(Org, orgName)
  def hasUser(userName: String) = OAuthHas(UserName, userName)
}