package glasskey.model.fetchers

import glasskey.config.OAuthConfig
import glasskey.model.{OAuthAccessToken, OAuthTerms, ProtectedResourceRequest}
import glasskey.resource._
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 3/4/15.
 */
class IDTokenRequestParameterMock extends IDTokenRequestParameter.Default(OAuthConfig.providerConfig.jwksUri) with IDTokenFetcherMock {
  override val decoder = mockDecoder
}

class IDTokenRequestParameterSpec  extends FlatSpec with Matchers {

  val idTokenRequestParamFetcher = new IDTokenRequestParameterMock()

  val fakeToken = new OAuthAccessToken(None,
    OAuthTerms.Bearer,
    None,
    "SomeRandomCrap",
    None)

  val fakeOIDCData = new OIDCTokenData(idTokenRequestParamFetcher.mockToken, Seq(
    new Nonce("crap"),
    new JwtId("TzZCnUZG6T2MkxUMjaMZW6"),
    new Expire(Int.box(1423525713)),
    new IssueAtTime(Int.box(1423523913)),
    new Subject("loande"),
    new AtHash("RJc8rNBx0R_ftiwVY_ZiUg"),
    new Audience("TPS_TEST"),
    new Issuer("https://ping.company.com")))

  val fakeRequestBad = new ProtectedResourceRequest(
    headers = Map("OIDC_ID_Token" -> Seq("Blah")), // The Authorization is here, but the token value is incorrect
    params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  val fakeRequestGoodEnoughToMatch = new ProtectedResourceRequest(
    headers = Map("Doesn't Matter" -> Seq("id_token ")), // This isn't really correct, but the value would be after this
    params = Map("id_token" -> Seq("")))

  val fakeRequestGreat = new ProtectedResourceRequest(
    headers = Map("Doesn't Matter" -> Seq("")), // This isn't really correct, but the value would be after this
    params = Map("id_token" -> Seq(s"${idTokenRequestParamFetcher.mockToken}")))

  "Fetcher" should "recognize good and bad requests" in {
    idTokenRequestParamFetcher.matches(fakeRequestBad) should be (false)
    idTokenRequestParamFetcher.matches(fakeRequestGoodEnoughToMatch) should be (true)
    idTokenRequestParamFetcher.matches(fakeRequestGreat) should be (true)
  }

  "Fetcher" should "fail due to incorrect pieces" in {
    intercept[MatchError] {
      idTokenRequestParamFetcher.fetch(fakeRequestGoodEnoughToMatch)
    }
  }

  "Fetcher" should "work with correct input" in {
    val result = idTokenRequestParamFetcher.fetch(fakeRequestGreat)
    result.get.tokenDetails sortBy (_.name) should be (fakeOIDCData.tokenDetails.sortBy(_.name))
  }
}
