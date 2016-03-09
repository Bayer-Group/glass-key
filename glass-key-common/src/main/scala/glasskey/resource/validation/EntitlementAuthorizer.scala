package glasskey.resource.validation

import glasskey.model._
import glasskey.model.validation.{UnauthorizedException, RBACAuthZData}
import scala.concurrent.Future

/**
 * Created by loande on 6/17/15.
 */
trait EntitlementAuthorizer extends OAuthErrorHelper {

  val entitlementUri: String
  val desiredAuth: Seq[RBACAuthZData]

  def checkEntitlementUrl(): Unit = {
    def countSubstr( str:String, substr:String ) = substr.r.findAllMatchIn(str).length

    val stringInterpolationCharCount = countSubstr(entitlementUri, "%s")

    if (stringInterpolationCharCount != 1)
      throw new UnauthorizedException("Entitlement URL needs to accommodate receiving the user ID |" +
        s"to retrieve application entitlements for the user. Found $stringInterpolationCharCount |" +
        "instances of '%s' in the entitlement URL.")
  }

  def modEntitlementUrl(userId: String) = entitlementUri.format(userId)

  def getAuth(accessToken: String, userId: String): Future[Seq[RBACAuthZData]]

  def andAuthorized(usersAuth: Seq[RBACAuthZData]): Boolean = {
    val truthiness = testUserAuthWithDesired(usersAuth)(containsEntitlements)
    truthiness.foldLeft(true)(_ && _) && truthiness.size == desiredAuth.size
  }

  def orAuthorized(usersAuth: Seq[RBACAuthZData]): Boolean =
    testUserAuthWithDesired(usersAuth)(containsAnyOf).foldLeft(false)(_ || _)

  def testUserAuthWithDesired(usersAuth: Seq[RBACAuthZData])(setFunc: (Set[String], Set[String]) => Boolean): Seq[Boolean] =
    for {
      singleAppDesiredAuth <- desiredAuth
      matchingUserAppAuth <- usersAuth.filter(x => x.name == singleAppDesiredAuth.name)
    } yield setFunc(singleAppDesiredAuth.entitlements, matchingUserAppAuth.entitlements)


  private def missingEntitlements(desiredAuth: Set[String], usersAuth: Set[String]): String = (desiredAuth diff usersAuth).mkString(",")

  private def containsEntitlements(desiredAuth: Set[String], usersAuth: Set[String]): Boolean = (desiredAuth & usersAuth) == desiredAuth

  private def containsAnyOf(desiredAuth: Set[String], usersAuth: Set[String]): Boolean = desiredAuth.intersect(usersAuth).nonEmpty
}
