package glasskey.util

import glasskey.model.fetchers.IDTokenFetcherMock
import org.scalatest.{FlatSpec, Matchers}


class OIDCTokenDecoderSpec extends FlatSpec with Matchers with IDTokenFetcherMock {

  "Checking a token" should "give a map of values" in {
    val jwt = mockDecoder.verify(mockToken)
    jwt should not be empty
    jwt.seq.size should be (8)
  }
}
