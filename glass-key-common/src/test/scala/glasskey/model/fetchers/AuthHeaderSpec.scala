package glasskey.model.fetchers

import glasskey.NeutralTestRuntimeEnvironment
import glasskey.model.{OAuthTerms, _}
import glasskey.resource.{Audience, OIDCTokenData}
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 3/2/15.
 */


class AuthHeaderSpec extends FlatSpec with Matchers {

  implicit val env = new NeutralTestRuntimeEnvironment()

  val authHdrFetcher = new AuthHeader.Default(env)

  val fakeToken = new OAuthAccessToken(None,
    OAuthTerms.Bearer,
    None,
    "SomeRandomCrap",
    None)

  val fakeOIDCData = new OIDCTokenData("Blah", Seq(new Audience("Blah")))

  val fakeRequestBad = new ProtectedResourceRequest(
    headers = Map("Authorization" -> Seq("Blah")), // The Authorization is here, but the token value is incorrect
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  val fakeRequestGoodEnoughToMatch = new ProtectedResourceRequest(
    headers = Map("Authorization" -> Seq("OAuth ")), // This isn't really correct, but the value would be after this
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  val fakeRequestGreat = new ProtectedResourceRequest(
    headers = Map("Authorization" -> Seq("OAuth SomeRandomCrap")), // This isn't really correct, but the value would be after this
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  val fakeRequestGreatwOpenID = new ProtectedResourceRequest(
    headers = Map("Authorization" -> Seq("OAuth SomeRandomCrap"),
      "OIDC_ID_Token" -> Seq("id_token SomeRandomOpenIDCrap")),
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  "Fetcher" should "recognize good and bad requests" in {
    authHdrFetcher.matches(fakeRequestBad) should be (false)
    authHdrFetcher.matches(fakeRequestGoodEnoughToMatch) should be (true)
    authHdrFetcher.matches(fakeRequestGreat) should be (true)
  }

  "Fetcher" should "retrieve the token containing SomeRandomCrap" in {
    intercept[InvalidRequest] {
      authHdrFetcher.fetch(fakeRequestBad)
    }
    authHdrFetcher.fetch(fakeRequestGoodEnoughToMatch)._1.access_token should have length 0
    authHdrFetcher.fetch(fakeRequestGreat)._1 should be (fakeToken)
  }

  "Fetcher" should "only contain OpenID information when available" in {
    authHdrFetcher.fetch(fakeRequestGreat)._1.id_token should be (None)
    intercept[MatchError] {
      authHdrFetcher.fetch(fakeRequestGreatwOpenID)._1.id_token
    }
  }
}
