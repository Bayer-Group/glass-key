package glasskey.resource

import glasskey.model.{ValidatedAccessToken, ValidationError}

/**
 * Created by loande on 6/17/15.
 */
package object validation {
  type ValidationResponse = Either[ValidationError, ValidatedAccessToken]
}
