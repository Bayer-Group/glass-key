package glasskey.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

/**
 * Created by loande on 12/8/2014.
 */

object OAuthConfig {
    val config: Config = ConfigFactory.load.getConfig("oauth")
    val providerConfig = new OAuthProviderConfig.Default(config.getConfig("provider"))
    lazy val httpConfig = new HttpConfig.Default(config)

    def getClients(config: Config): Map[String, ClientConfig] = {
      (for (entry <- config.root().entrySet().asScala) yield (
        entry.getKey, ClientConfig(entry.getKey, config.entrySet().asScala)
        )).toMap
    }

    lazy val clients: Map[String, ClientConfig] = getClients (config.getConfig("clients"))
}
