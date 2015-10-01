package glasskey.spray.model

import glasskey.model.OAuthException
import spray.http.HttpHeader
import spray.routing.Rejection

case class OAuthRejection(wrappedEx: OAuthException, challengeHeaders: List[HttpHeader]) extends Rejection
