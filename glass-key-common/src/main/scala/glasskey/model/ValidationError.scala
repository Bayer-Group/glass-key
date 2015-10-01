package glasskey.model

/**
 * Created by loande on 6/17/15.
 */
case class ValidationError(error:String,
                          error_description:String,
                          status_code: Option[Int])
