package glasskey.model.validation

import glasskey.model.fetchers.IDTokenFetcherMock
import glasskey.resource.OIDCTokenData
import org.scalatest.{FlatSpec, Matchers}

import scala.util.parsing.combinator.RegexParsers

/**
 * Created by loande on 3/23/15.
 */
class OIDCAuthorizationClaimParserSpec extends FlatSpec with Matchers with RegexParsers with IDTokenFetcherMock with OIDCAuthorizationClaimParser {

  override val token = Some(new OIDCTokenData(
    mockToken,
    mockDecoder.verify(mockToken)))

  val exprAud = hasAudience("TPS_TEST")
  val exprAudBad = hasAudience("TPS_PROD")
  val exprSub = hasSubject("loande")
  val exprSubBad = hasSubject("blah")
  val exprSubBad2 = hasSubject("blah2")
  val validator = And(exprAud, exprSub)
  val exprLongAnd = And(validator, exprAudBad)
  val exprOr = Or(exprAud, exprSub)
  val exprLongBoth = Or(validator, exprAudBad)


  val sampleStaticValidatorBad = And(validator, exprSubBad)
  val sampleStaticValidatorOr = Or(exprSub, exprSubBad)
  val sampleStaticValidatorOrBad = Or(exprSubBad, exprSubBad2)
  val sampleStaticValidatorOrBad2 = Or(exprSubBad, And(exprSubBad2, exprAud))
  val sampleStaticValidatorParen = And(Or(exprSub, exprSubBad2), exprAud)
  val sampleStaticValidatorParenBad = Or(exprSubBad, And(exprSubBad2, exprAud))

  "Sample string" should "parse successfully" in {
   val exprParsed = parseAll(expr, "HAS_AUD_STRING('TPS_TEST')") match {
      case Success(res, _) => res
      case Failure(msg, _) => throw new Exception(msg)
      case Error(msg, _) => throw new Exception(msg)
   }
    exprParsed should be (exprAud)

    val exprParsed2 = parseAll(expr, "HAS_SUB_STRING('loande')") match {
      case Success(res, _) => res
      case Failure(msg, _) => throw new Exception(msg)
      case Error(msg, _) => throw new Exception(msg)
    }
    exprParsed2 should be (exprSub)
  }

  "AND" should "parse successfully" in {
    parseExpression("HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')") should be (validator)
    validator.toString should be ("HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')")
    evaluate should be (true)
  }

  "OR" should "parse successfully" in {
    parseExpression("HAS_AUD_STRING('TPS_TEST') or HAS_SUB_STRING('loande')") should be (exprOr)
    exprOr.toString should be ("HAS_AUD_STRING('TPS_TEST') or HAS_SUB_STRING('loande')")
    evaluateWith(exprOr) should be (true)
  }

  "longer and" should "parse successfully" in {
    parseExpression("(HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')) and HAS_AUD_STRING('TPS_PROD')") should be (exprLongAnd)
    exprLongAnd.toString should be ("(HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')) and HAS_AUD_STRING('TPS_PROD')")
    evaluateWith(exprLongAnd) should be (false)
  }

  "longer sentence" should "parse successfully" in {
    parseExpression("(HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')) or HAS_AUD_STRING('TPS_PROD')") should be (exprLongBoth)
    exprLongBoth.toString should be ("(HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')) or HAS_AUD_STRING('TPS_PROD')")
    evaluateWith(exprLongBoth) should be (true)
  }

  "Sample with bad token" should "not allow proper validation" in {
    parseExpression("(HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')) and HAS_SUB_STRING('blah')") should be (sampleStaticValidatorBad)
    sampleStaticValidatorBad.toString should be ("(HAS_AUD_STRING('TPS_TEST') and HAS_SUB_STRING('loande')) and HAS_SUB_STRING('blah')")
    evaluateWith(sampleStaticValidatorBad) should be (false)
  }

  "Sample with or" should "allow proper validation" in {
    parseExpression("HAS_SUB_STRING('loande') or HAS_SUB_STRING('blah')") should be (sampleStaticValidatorOr)
    sampleStaticValidatorOr.toString should be ("HAS_SUB_STRING('loande') or HAS_SUB_STRING('blah')")
    evaluateWith(sampleStaticValidatorOr) should be (true)
  }

  "Sample with bad or" should "not allow proper validation" in {
    parseExpression("HAS_SUB_STRING('blah') or HAS_SUB_STRING('blah2')") should be (sampleStaticValidatorOrBad)
    sampleStaticValidatorOrBad.toString should be ("HAS_SUB_STRING('blah') or HAS_SUB_STRING('blah2')")
    evaluateWith(sampleStaticValidatorOrBad) should be (false)
  }
  "Sample with bad or2" should "not allow proper validation" in {
    parseExpression("HAS_SUB_STRING('blah') or (HAS_SUB_STRING('blah2') and HAS_AUD_STRING('TPS_TEST'))") should be (sampleStaticValidatorOrBad2)
    sampleStaticValidatorOrBad2.toString should be ("HAS_SUB_STRING('blah') or (HAS_SUB_STRING('blah2') and HAS_AUD_STRING('TPS_TEST'))")
    evaluateWith(sampleStaticValidatorOrBad2) should be (false)
  }

  "Sample with parentheses" should "allow validation" in {
    parseExpression("(HAS_SUB_STRING('loande') or HAS_SUB_STRING('blah2')) and HAS_AUD_STRING('TPS_TEST')") should be (sampleStaticValidatorParen)
    sampleStaticValidatorParen.toString should be ("(HAS_SUB_STRING('loande') or HAS_SUB_STRING('blah2')) and HAS_AUD_STRING('TPS_TEST')")
    evaluateWith(sampleStaticValidatorParen) should be (true)
  }

  "Sample with parentheses bad" should "allow validation" in {
    parseExpression("HAS_SUB_STRING('blah') or (HAS_SUB_STRING('blah2') and HAS_AUD_STRING('TPS_TEST'))") should be (sampleStaticValidatorParenBad)
    sampleStaticValidatorParenBad.toString should be ("HAS_SUB_STRING('blah') or (HAS_SUB_STRING('blah2') and HAS_AUD_STRING('TPS_TEST'))")
    evaluateWith(sampleStaticValidatorParenBad) should be (false)
  }
}
