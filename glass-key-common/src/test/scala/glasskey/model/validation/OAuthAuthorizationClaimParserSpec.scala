package glasskey.model.validation

import glasskey.model.{BaseValidatedData, ValidatedAccessToken}
import glasskey.model.fetchers.IDTokenFetcherMock
import org.scalatest.{FlatSpec, Matchers}

import scala.util.parsing.combinator.RegexParsers

/**
 * Created by loande on 3/25/15.
 */

class OAuthAuthorizationClaimParserSpec extends FlatSpec with Matchers with RegexParsers with IDTokenFetcherMock with OAuthAuthorizationClaimParser {

  override val token = Some(ValidatedAccessToken(Some("OUR_AWESOME_CLIENT"), None, None, Some("openid"), None, None,
    BaseValidatedData(Some("loande"), Some("loande"), Some("Monsanto"), Some("OUR_AWESOME_CLIENT"))))

  val clientIdClaim = hasClientId("OUR_AWESOME_CLIENT")
  val scopeClaim = hasScope("openid")
  val orgClaim = hasOrg("Monsanto")
  val userNameClaim = hasUser("loande")

  val clientIdClaimBad = hasClientId("OUR_LAME_CLIENT")
  val clientIdClaimBad2 = hasClientId("OUR_ALSO_LAME_CLIENT")
  val scopeClaimBad = hasScope("not_openid")
  val orgClaimBad = hasOrg("Badsanto")
  val userNameClaimBad = hasUser("cjcoff")

  override val validator: Expr[ValidatedAccessToken] = And(clientIdClaim, scopeClaim)
  val exprOr = Or(clientIdClaim, scopeClaim)
  val exprLongAnd = And(validator, userNameClaimBad)
  val exprLongBoth = Or(validator, userNameClaimBad)
  val exprOneBadOr = Or(clientIdClaim, clientIdClaimBad)
  val exprAllBadOr = Or(clientIdClaimBad, clientIdClaimBad2)
  val exprBadOrAndBad = Or(clientIdClaimBad, And(clientIdClaimBad2, scopeClaim))
  val exprComplexAndOr = And(exprOneBadOr, scopeClaim)
  val exprComplexBadOrAnd = Or(clientIdClaimBad, And(clientIdClaimBad2, scopeClaim))

  "Sample string" should "parse successfully" in {
    val exprParsed = parseAll(expr, "HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT')") match {
      case Success(res, _) => res
      case Failure(msg, _) => throw new Exception(msg)
      case Error(msg, _) => throw new Exception(msg)
    }
    exprParsed should be (clientIdClaim)

    val exprParsed2 = parseAll(expr, "HAS_ORG_STRING('Monsanto')") match {
      case Success(res, _) => res
      case Failure(msg, _) => throw new Exception(msg)
      case Error(msg, _) => throw new Exception(msg)
    }
    exprParsed2 should be (orgClaim)
  }

  "AND" should "parse successfully" in {
    parseExpression("HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') and HAS_SCOPE_STRING('openid')") should be (validator)
    validator.toString should be ("HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') and HAS_SCOPE_STRING('openid')")
    evaluate should be (true)
  }

  "OR" should "parse successfully" in {
    parseExpression("HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') or HAS_SCOPE_STRING('openid')") should be (exprOr)
    exprOr.toString should be ("HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') or HAS_SCOPE_STRING('openid')")
    evaluateWith(exprOr) should be (true)
  }

  "longer and" should "parse successfully" in {
    parseExpression("(HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') and HAS_SCOPE_STRING('openid')) and HAS_USERNAME_STRING('cjcoff')") should be (exprLongAnd)
    exprLongAnd.toString should be ("(HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') and HAS_SCOPE_STRING('openid')) and HAS_USERNAME_STRING('cjcoff')")
    evaluateWith(exprLongAnd) should be (false)
  }

  "longer sentence" should "parse successfully" in {
    parseExpression("(HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') and HAS_SCOPE_STRING('openid')) or HAS_USERNAME_STRING('cjcoff')") should be (exprLongBoth)
    exprLongBoth.toString should be ("(HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') and HAS_SCOPE_STRING('openid')) or HAS_USERNAME_STRING('cjcoff')")
    evaluateWith(exprLongBoth) should be (true)
  }

  "Sample with or" should "allow proper validation" in {
    parseExpression("HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') or HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT')") should be (exprOneBadOr)
    exprOneBadOr.toString should be ("HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') or HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT')")
    evaluateWith(exprOneBadOr) should be (true)
  }

  "Sample with bad or" should "not allow proper validation" in {
    parseExpression("HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT') or HAS_CLIENT_ID_STRING('OUR_ALSO_LAME_CLIENT')") should be (exprAllBadOr)
    exprAllBadOr.toString should be ("HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT') or HAS_CLIENT_ID_STRING('OUR_ALSO_LAME_CLIENT')")
    evaluateWith(exprAllBadOr) should be (false)
  }

  "Sample with bad or2" should "not allow proper validation" in {
    parseExpression("HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT') or (HAS_CLIENT_ID_STRING('OUR_ALSO_LAME_CLIENT') and HAS_SCOPE_STRING('openid'))") should be (exprBadOrAndBad)
    exprBadOrAndBad.toString should be ("HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT') or (HAS_CLIENT_ID_STRING('OUR_ALSO_LAME_CLIENT') and HAS_SCOPE_STRING('openid'))")
    evaluateWith(exprBadOrAndBad) should be (false)
  }

  "Sample with parentheses" should "allow validation" in {
    parseExpression("(HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') or HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT')) and HAS_SCOPE_STRING('openid')") should be (exprComplexAndOr)
    exprComplexAndOr.toString should be ("(HAS_CLIENT_ID_STRING('OUR_AWESOME_CLIENT') or HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT')) and HAS_SCOPE_STRING('openid')")
    evaluateWith(exprComplexAndOr) should be (true)
  }

  "Sample with parentheses bad" should "allow validation" in {
    parseExpression("HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT') or (HAS_CLIENT_ID_STRING('OUR_ALSO_LAME_CLIENT') and HAS_SCOPE_STRING('openid'))") should be (exprComplexBadOrAnd)
    exprComplexBadOrAnd.toString should be ("HAS_CLIENT_ID_STRING('OUR_LAME_CLIENT') or (HAS_CLIENT_ID_STRING('OUR_ALSO_LAME_CLIENT') and HAS_SCOPE_STRING('openid'))")
    evaluateWith(exprComplexBadOrAnd) should be (false)
  }
}
