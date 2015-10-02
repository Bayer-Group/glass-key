package glasskey.model.validation

/**
 * Created by loande on 8/13/15.
 */
case class UnauthorizedException(msg: String) extends Throwable(msg)
