package glasskey.util

import scala.util.Try

/**
 * Created by loande on 6/29/15.
 */
trait FilePEMDERKeySource extends PEMDERKeySource {
  override def getKey(kid: String): Option[String] = Try{
    source.split("\n").drop(1).toSeq.dropRight(1).mkString
  }.toOption
}
