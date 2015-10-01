package glasskey.play.client

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
/**
 * Created by LOANDE on 12/13/2014.
 */
case class HelloModelReturn(id: Option[Int], message: Option[String], roles: Option[List[String]], scopes: Option[List[String]])


object HelloWorldFormats {
  implicit val helloFormat = (
    (__ \ "id").read[Option[Int]].orElse(Reads.pure(None))
      ~ (__ \ "message").read[Option[String]]
      ~ (__ \ "roles").read[Option[List[String]]].orElse(Reads.pure(None))
      ~ (__ \ "scopes").read[Option[List[String]]].orElse(Reads.pure(None))
    )
  implicit val helloReads: Reads[HelloModelReturn] = (helloFormat)(HelloModelReturn)

  implicit val helloWrites = (
    (__ \ "id").writeNullable[Int]
      ~ (__ \ "message").writeNullable[String]
      ~ (__ \ "roles").writeNullable[List[String]]
      ~ (__ \ "scopes").writeNullable[List[String]]
    )(unlift(HelloModelReturn.unapply))
}