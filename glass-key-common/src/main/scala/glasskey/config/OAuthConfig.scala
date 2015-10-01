package glasskey.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

/**
 * Created by loande on 12/8/2014.
 */
trait OAuthConfig {
  val config: Config
  val providerConfig: OAuthProviderConfig
  val httpConfig: HttpConfig
  def getClients(config: Config): Map[String, ClientConfig]
  val clients: Map[String, ClientConfig]
}

object OAuthConfig {
  class Default extends OAuthConfig {
    override val config: Config = ConfigFactory.load.getConfig("oauth")
    override val providerConfig = new OAuthProviderConfig.Default(config.getConfig("provider"))
    override lazy val httpConfig = new HttpConfig.Default(config)

    def getClients(config: Config): Map[String, ClientConfig] = {
      (for (entry <- config.root().entrySet().asScala) yield (
        entry.getKey, ClientConfig(entry.getKey, config.entrySet().asScala)
        )).toMap
    }

    lazy val clients: Map[String, ClientConfig] = getClients (config.getConfig("clients"))
  }
}
