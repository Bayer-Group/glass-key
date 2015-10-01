package glasskey.model.fetchers

import glasskey.NeutralTestRuntimeEnvironment
import glasskey.model._
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 3/4/15.
 */
class RequestParameterSpec extends FlatSpec with Matchers {

  implicit val env = new NeutralTestRuntimeEnvironment()
  val reqParamFetcher = new RequestParameter.Default(env.config.providerConfig.jwksUri)


  val fakeToken = new OAuthAccessToken(None,
    OAuthTerms.Bearer,
    None,
    "SomeRandomCrap",
    None)

  val fakeRequestBad = new ProtectedResourceRequest(
    headers = Map("Doesn't matter" -> Seq("Should only be looking at params")),
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  val fakeRequestGoodEnoughToMatch = new ProtectedResourceRequest(
    headers = Map("Doesn't Matter" -> Seq("Should only be looking at params")),
    params = Map("oauth_token" -> Seq("")))
  val fakeRequestGoodEnoughToMatch2 = new ProtectedResourceRequest(
    headers = Map("Doesn't Matter" -> Seq("Should only be looking at params")),
    params = Map("access_token" -> Seq("")))

  val fakeRequestGreat = new ProtectedResourceRequest(
    headers = Map("Doesn't Matter" -> Seq("Should only be looking at params")),
    params = Map("oauth_token" -> Seq("SomeRandomCrap")))

  val fakeRequestGreat2 = new ProtectedResourceRequest(
    headers = Map("Doesn't Matter" -> Seq("Should only be looking at params")),
    params = Map("access_token" -> Seq("SomeRandomCrap")))

  val fakeRequestGreatwOpenIDInHdr = new ProtectedResourceRequest(
    headers = Map("Authorization" -> Seq("OAuth SomeRandomCrap"),
      "OIDC_ID_Token" -> Seq("id_token SomeRandomOpenIDCrap")),
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  val fakeRequestGreatwOpenID = new ProtectedResourceRequest(
    headers = Map("Authorization" -> Seq("OAuth SomeRandomCrap")),
    params = Map("id_token" -> Seq("SomeRandomOpenIDCrap"), "access_token" -> Seq("SomeRandomCrap")))

  "Fetcher" should "recognize good and bad requests" in {
    reqParamFetcher.matches(fakeRequestBad) should be (false)
    reqParamFetcher.matches(fakeRequestGoodEnoughToMatch) should be (true)
    reqParamFetcher.matches(fakeRequestGoodEnoughToMatch2) should be (true)
    reqParamFetcher.matches(fakeRequestGreat) should be (true)
    reqParamFetcher.matches(fakeRequestGreat2) should be (true)
  }

  "Fetcher" should "retrieve the token containing SomeRandomCrap" in {
    intercept[InvalidRequest] {
      reqParamFetcher.fetch(fakeRequestBad)
    }
    reqParamFetcher.fetch(fakeRequestGoodEnoughToMatch)._1.access_token should have length 0
    reqParamFetcher.fetch(fakeRequestGoodEnoughToMatch2)._1.access_token should have length 0
    reqParamFetcher.fetch(fakeRequestGreat)._1 should be (fakeToken)
    reqParamFetcher.fetch(fakeRequestGreat2)._1 should be (fakeToken)
  }
  "Fetcher" should "only contain OpenID information when available" in {
    reqParamFetcher.fetch(fakeRequestGreat)._1.id_token should be (None)
    intercept[InvalidRequest] {
      reqParamFetcher.fetch(fakeRequestGreatwOpenIDInHdr)._1.id_token
    }
    intercept[MatchError] {
      reqParamFetcher.fetch(fakeRequestGreatwOpenID)._1.id_token
    }
  }
}
