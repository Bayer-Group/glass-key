package glasskey.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by loande on 2/26/15.
 */
class HttpConfigSpec extends FlatSpec with Matchers {
  val httpConfigObj = ConfigFactory.parseString("  http {\n    interface = \"0.0.0.0\"\n    port = 8080\n  }")
  val httpConfig = new HttpConfig.Default(httpConfigObj)

  "Initialized httpConfig" should "have good values" in {
    httpConfig.Interface should equal ("0.0.0.0")
    httpConfig.Port should equal (8080)
  }

}
