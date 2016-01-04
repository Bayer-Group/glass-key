package glasskey.model.validation

import glasskey.model.ValidatedAccessToken
import glasskey.resource.{Claim, OIDCTokenData}

import scala.reflect.ClassTag
import scala.util.parsing.combinator.RegexParsers
/**
 * Created by loande on 3/20/15.
 */
sealed trait Expr[T] {
  def validate(token:Option[T]):Boolean
  val containsExprs: Boolean
}

case class And[T](left: Expr[T], right: Expr[T]) extends Expr[T] { self =>
  override val containsExprs = true
  override def validate(token: Option[T]): Boolean =
    left.validate(token) && right.validate(token)
  override def toString: String = {
    (if (left.containsExprs) s"(${left})" else s"${left}") + " and " +
    (if (right.containsExprs) s"(${right})" else s"${right}")
  }
}

case class Or[T](left: Expr[T], right: Expr[T]) extends Expr[T] {
  override val containsExprs = true
  override def validate(token: Option[T]): Boolean =
    left.validate(token) || right.validate(token)
  override def toString: String = {
      (if (left.containsExprs) s"(${left})" else s"${left}") + " or " +
      (if (right.containsExprs) s"(${right})" else s"${right}")
  }
}
case class OIDCHas[T](claimName: String, value: T)(implicit tag: ClassTag[T]) extends Expr[OIDCTokenData] {
  override val containsExprs = false
  override def validate(token:Option[OIDCTokenData]) : Boolean =
    token.isDefined && checkValue(token.get)

  def checkValue(token: OIDCTokenData): Boolean = {
    token.tokenDetails.find(claim => claim.name == claimName) match {
      case Some(c: Claim[T]) => c.value == value
      case None => false
    }
  }
  override def toString: String = {
    val withQuotes: String = if (value.isInstanceOf[String]) s"'$value'" else value.toString
    s"HAS_${claimName.toUpperCase}_${tag.runtimeClass.getSimpleName.toUpperCase}(${withQuotes})"
  }
}

case class OAuthHas[T](claim: OAuthClaim, value: T)(implicit tag: ClassTag[T]) extends Expr[ValidatedAccessToken] {
  override val containsExprs = false
  override def validate(token:Option[ValidatedAccessToken]) : Boolean =
    token.isDefined && checkValue(token.get)

  def checkValue(token: ValidatedAccessToken): Boolean = {
    claim match {
      case Scope => checkClaimOption[String](token.scope)
      case Client_Id => checkClaimOption[String](token.client_id)
      case Org => checkClaimOption[String](token.access_token.orgName)
      case UserName => checkClaimOption[String](token.access_token.username)
    }
  }
  override def toString: String = {
    val withQuotes: String = if (value.isInstanceOf[String]) s"'$value'" else value.toString
    s"HAS_${claim.getClass.getSimpleName.split("\\$").last.toUpperCase}_${tag.runtimeClass.getSimpleName.toUpperCase}(${withQuotes})"
  }
  private def checkClaimOption[T](opt : Option[T]) = opt.isDefined && opt.get == value
}

trait AuthorizationClaimParser[T] extends RegexParsers {

  def param: Parser[String] = "^[a-zA-Z0-9_]*".r ^^ (new String(_))

  def tokenSpecificExpressions : Parser[Expr[T]]

  def expr:   Parser[Expr[T]]            = term ~ rep(ors) ^^ {    case a ~b  => (a /: b)((acc,f) => f(acc))}
  def ors:    Parser[Expr[T] => Expr[T]] = "or" ~ term ^^ {        case "or" ~ b => Or(_, b)}
  def term:   Parser[Expr[T]]            = factor ~ rep(ands) ^^ { case a ~ b => (a /: b)((acc,f) => f(acc))}
  def ands:   Parser[Expr[T] => Expr[T]] = "and" ~ factor ^^ {     case "and" ~ b => And(_, b) }
  def factor: Parser[Expr[T]]            = tokenSpecificExpressions | "(" ~> expr <~ ")"

  def parseExpression(s: String): Expr[T] = parseAll(expr, s) match {
    case Success(res, _) => res
    case Failure(msg, _) => throw new Exception(msg)
    case Error(msg, _) => throw new Exception(msg)
  }
}