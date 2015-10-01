package glasskey.config

case class ClientConfig(clientId: Option[String],
                        clientSecret: Option[String],
                        apiRedirectUri: Option[String],
                        grantType: Option[String],
                        userName: Option[String],
                        userPassword: Option[String])

object ClientConfig {

  import java.util.Map.Entry

  import com.typesafe.config.ConfigValue

  def getStringValue(searchString: String, c: Iterable[Entry[String, ConfigValue]]): Option[String] =
    c.find(entry => entry.getKey.equals(searchString)) match {
      case Some(found) => Some(found.getValue.unwrapped().asInstanceOf[String])
      case None => None
    }

  def apply(clientKey: String, c: Iterable[Entry[String, ConfigValue]]): ClientConfig =
    new ClientConfig(getStringValue(clientKey + ".client-id", c),
      getStringValue(clientKey + ".client-secret", c), getStringValue(clientKey + ".api-redirect-uri", c),
      getStringValue(clientKey + ".grant-type", c), getStringValue(clientKey + ".username", c),
      getStringValue(clientKey + ".userpw", c))
}
