package glasskey.spray.client

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.io.Tcp._
import akka.pattern.ask
import akka.event._
import akka.actor.ActorDSL._
import spray.can.Http
import glasskey.config.OAuthConfig

object Boot {

  // we need an ActorSystem to host our application in
  // goto http://http://localhost:8080/api/PingHelloWorld in the browser, and as long as request completes
  // in < 4 seconds the data should appear
  implicit val system = ActorSystem("oauth-client-actor-system")

  def main(args: Array[String]): Unit = {
    val app = new OAuthClientActorService()
    app.run(args)
  }
}

class OAuthClientActorService(implicit system: ActorSystem) {

  def run(args: Array[String]): Unit = {

    val log = Logging(system, getClass)

    val callbackActor = actor(new Act {
      become {
        case b@Bound(connection) => log.info(b.toString)
        case cf@CommandFailed(command) => log.error(cf.toString)
        case all => log.debug("Backend Service Received a message from Akka.IO: " + all.toString)
      }
    })

    /* Spray Service */
    val rootActor = system.actorOf(Props(classOf[HelloWorldClientActor]), "oauth-client-actor-system")
    IO(Http).tell(Http.Bind(rootActor, OAuthConfig.httpConfig.Interface, OAuthConfig.httpConfig.Port), callbackActor)

    log.info("OAuth Service Ready")
  }
}
