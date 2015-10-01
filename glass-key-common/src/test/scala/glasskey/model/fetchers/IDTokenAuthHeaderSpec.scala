package glasskey.model.fetchers

import glasskey.model.{OAuthAccessToken, OAuthTerms, ProtectedResourceRequest}
import glasskey.resource._
import glasskey.{NeutralTestRuntimeEnvironment, RuntimeEnvironment}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by loande on 3/2/15.
  */

class IDTokenAuthHdrMock(implicit env: RuntimeEnvironment) extends IDTokenAuthHeader.Default(env.config.providerConfig.jwksUri,
  env.config.providerConfig.idHeaderName, env.config.providerConfig.idHeaderPrefix) with IDTokenFetcherMock {
  override val decoder = mockDecoder
}


class IDTokenAuthHeaderSpec extends FlatSpec with Matchers {

   implicit val env = new NeutralTestRuntimeEnvironment()

   val idTokenAuthHeaderFetcher = new IDTokenAuthHdrMock()

  val fakeToken = new OAuthAccessToken(None,
    OAuthTerms.Bearer,
    None,
    "SomeRandomCrap",
    None)
   val fakeOIDCData = new OIDCTokenData(idTokenAuthHeaderFetcher.mockToken, Seq(
     Nonce("crap"),
     JwtId("TzZCnUZG6T2MkxUMjaMZW6"),
     Expire(Int.box(1423525713)),
     IssueAtTime(Int.box(1423523913)),
     Subject("loande"),
     AtHash("RJc8rNBx0R_ftiwVY_ZiUg"),
     Audience("TPS_TEST"),
     Issuer("https://ping.company.com")))

   val fakeRequestBad = new ProtectedResourceRequest(
     headers = Map("OIDC_ID_Token" -> Seq("Blah")), // The Authorization is here, but the token value is incorrect
     params = Map("id_token" -> Seq("Should only be looking at header")))

   val fakeRequestGoodEnoughToMatch = new ProtectedResourceRequest(
     headers = Map("OIDC_ID_Token" -> Seq("id_token ")), // This isn't really correct, but the value would be after this
     params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

   val fakeRequestGreat = new ProtectedResourceRequest(
     headers = Map("OIDC_ID_Token" -> Seq(s"id_token ${idTokenAuthHeaderFetcher.mockToken}")), // This isn't really correct, but the value would be after this
     params = Map("Doesn't Matter" -> Seq("Should only be looking at header")))

  "Fetcher" should "recognize good and bad requests" in {
    idTokenAuthHeaderFetcher.matches(fakeRequestBad) should be (false)
    idTokenAuthHeaderFetcher.matches(fakeRequestGoodEnoughToMatch) should be (true)
    idTokenAuthHeaderFetcher.matches(fakeRequestGreat) should be (true)
  }

   "Fetcher" should "fail due to incorrect pieces" in {
     intercept[MatchError] {
       idTokenAuthHeaderFetcher.fetch(fakeRequestGoodEnoughToMatch)
     }
   }

  "Fetcher" should "work with correct input" in {
    val result = idTokenAuthHeaderFetcher.fetch(fakeRequestGreat)
    result.get.tokenDetails sortBy (_.name) should be (fakeOIDCData.tokenDetails.sortBy(_.name))
  }
 }
