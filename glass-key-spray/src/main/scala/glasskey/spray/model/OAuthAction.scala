package glasskey.spray.model

import glasskey.config.OAuthConfig
import spray.http.HttpHeader

trait OAuthAction {

    import akka.actor.ActorSystem
    import spray.client.pipelining._
    import spray.http.HttpHeaders.RawHeader
    import spray.http.HttpRequest
    import spray.http.parser.HttpParser
    import spray.httpx.encoding.{Deflate, Gzip}
    import spray.httpx.unmarshalling.FromResponseUnmarshaller

    import scala.concurrent.{ExecutionContext, Future}

    implicit val system = ActorSystem()

    implicit def executor: ExecutionContext = system.dispatcher

    def getHeaderedPipeline[T](token: String,
                               id_token: Option[String] = None, addlHdrs : Option[List[HttpHeader]] = None)
                              (implicit evidence: FromResponseUnmarshaller[T]): HttpRequest => Future[T] =
      (getHeaders(token, id_token, addlHdrs)
        ~> encode(Gzip)
        ~> sendReceive
        ~> decode(Deflate) ~> decode(Gzip)
        ~> unmarshal[T])

    def getHeaders(accessToken: String, id_token: Option[String] = None,
                   addlHdrs : Option[List[HttpHeader]] = None): RequestTransformer =
      getHeaders(accessToken, id_token, OAuthConfig.providerConfig.authHeaderName,
        OAuthConfig.providerConfig.authHeaderPrefix,
        OAuthConfig.providerConfig.idHeaderName,
        OAuthConfig.providerConfig.idHeaderPrefix, addlHdrs)

    def getHeaders(accessToken: String, id_token: Option[String], authHdrName: String, authHdrPrefix: String,
                           idHdrName: String, idHdrPrefix: String, addlHdrs : Option[List[HttpHeader]]): RequestTransformer =
      addHeaders(getHttpHeaders(accessToken, id_token, authHdrName, authHdrPrefix,
        idHdrName, idHdrPrefix, addlHdrs))

    def getHttpHeaders(accessToken: String, id_token: Option[String], authHdrName: String, authHdrPrefix: String,
                           idHdrName: String, idHdrPrefix: String, addlHdrs : Option[List[HttpHeader]]): List[HttpHeader] = {
      val hdrs = id_token match {
        case Some(idTokenStr) =>
          val authHeader = RawHeader(s"${authHdrName}", s"${authHdrPrefix} $accessToken")
          val idTokenHeader = RawHeader(s"${idHdrName}", s"${idHdrPrefix} $idTokenStr")

          List(
            HttpParser.parseHeader(authHeader).left.flatMap(_ ⇒ Right(authHeader)).right.get,
            HttpParser.parseHeader(idTokenHeader).left.flatMap(_ ⇒ Right(idTokenHeader)).right.get)

        case None => val rawHeader = RawHeader(authHdrName, s"${authHdrPrefix}$accessToken")
          List(HttpParser.parseHeader(rawHeader).left.flatMap(_ ⇒ Right(rawHeader)).right.get)
      }
      hdrs ++ addlHdrs.toList.flatten
    }
}
