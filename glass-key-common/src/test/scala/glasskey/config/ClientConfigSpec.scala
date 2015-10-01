package glasskey.config

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class ClientConfigSpec extends FlatSpec with Matchers {

  val sampleClientConfig = "clients {\n      hello-authcode-client {\n        grant-type = \"authorization_code\" # must be one of authorization_code or client_credentials\n        client-id = \"TPS_TEST\" # enter client-id provided by ping here or in application_private.conf file\n        client-secret = \"TPS_TEST\" # shouldn't need for resource owner grant, but necessary since this client has one established\n        api-redirect-uri = \"http://localhost:8080/api/PingIdentityAuthCode\"\n      }\n      hello-client_credentials-client {\n        grant-type = \"client_credentials\"\n        client-id = \"TPS_TEST\" # enter client-id provided by ping here or in application_private.conf file\n        client-secret = \"TPS_TEST\" # enter client-secret provided by ping here or in application_private.conf file\n      }\n      hello-resource_owner-client {\n        grant-type = \"password\"\n        client-id = \"TPS_TEST\" # enter client-id provided by ping here or in application_private.conf file\n        client-secret = \"TPS_TEST\" # shouldn't need for resource owner grant, but necessary since this client has one established\n        username = \"NA1000TPS-CI\"\n        userpw = \"TopSecret!\"\n      }\n      hello-validation-client {\n        client-id = \"TPS_VALIDATOR\" # enter client-id provided by ping here or in application_private.conf file\n        client-secret = \"TPS_VALIDATOR\" # enter client-secret provided by ping here or in application_private.conf file\n      }\n    }"
  val cfg = ConfigFactory.parseString(sampleClientConfig).getConfig("clients")
  val constructedValidationClient = new ClientConfig(Some("TPS_VALIDATOR"), Some("TPS_VALIDATOR"),
    None, None, None, None)
  val constructedCCClient = new ClientConfig(Some("TPS_TEST"), Some("TPS_TEST"), None,
    Some("client_credentials"), None, None)
  val constructedAuthCodeClient = new ClientConfig(Some("TPS_TEST"), Some("TPS_TEST"),
    Some("http://localhost:8080/api/PingIdentityAuthCode"), Some("authorization_code"), None, None)
  val constructedPasswordClient = new ClientConfig(Some("TPS_TEST"), Some("TPS_TEST"), None, Some("password"),
    Some("NA1000TPS-CI"), Some("TopSecret!"))

  val validationClient = ClientConfig(cfg.root().entrySet().asScala.toSeq.head.getKey, cfg.entrySet().asScala)
  val ccClient = ClientConfig(cfg.root().entrySet().asScala.toSeq(1).getKey, cfg.entrySet().asScala)
  val authCodeClient = ClientConfig(cfg.root().entrySet().asScala.toSeq(2).getKey, cfg.entrySet().asScala)
  val passwordClient = ClientConfig(cfg.root().entrySet().asScala.toSeq(3).getKey, cfg.entrySet().asScala)

  "Initialized config" should "hold stuff in config file" in {
    validationClient should equal (constructedValidationClient)
    ccClient should equal (constructedCCClient)
    authCodeClient should equal (constructedAuthCodeClient)
    passwordClient should equal (constructedPasswordClient)
  }
}
