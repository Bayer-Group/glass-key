package glasskey.config

import com.typesafe.config.Config

/**
 * Created by loande on 2/16/15.
 */
trait HttpConfig {
  val httpConfig : Config
  val Interface : String
  val Port : Int
}

object HttpConfig {
  class Default(config: Config) extends HttpConfig {
    val httpConfig = config.getConfig("http")
    lazy val Interface = httpConfig.getString("interface")
    lazy val Port = httpConfig.getInt("port")
  }
}
