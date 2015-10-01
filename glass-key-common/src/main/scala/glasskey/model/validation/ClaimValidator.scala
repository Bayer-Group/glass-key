package glasskey.model.validation

/**
 * Created by loande on 3/24/15.
 */
trait ClaimValidator[T] {
  def evaluate = evaluateWith(validator)
  def evaluateWith(expr: Expr[T]) = expr.validate(token)
  val token : Option[T] = None
  val validator : Expr[T]
}
