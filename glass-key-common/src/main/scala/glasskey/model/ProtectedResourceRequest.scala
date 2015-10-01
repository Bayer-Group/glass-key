package glasskey.model

import scala.collection.immutable.TreeMap
import scala.math.Ordering

/**
 * Created by loande on 12/1/2014.
 */

class RequestBase(headers: TreeMap[String, Seq[String]], params: Map[String, Seq[String]]) {

  def this(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) = {
    this(new TreeMap[String, Seq[String]]()(Ordering.by(_.toLowerCase)) ++ headers, params)
  }

  def header(name: String): Option[String] = headers.get(name).flatMap {
    _.headOption
  }

  def requireHeader(name: String): String = header(name).getOrElse(throw new InvalidRequest("required header: " + name))
  def param(name: String): Option[String] = params.get(name).flatMap(values => values.headOption)
  def requireParam(name: String): String = param(name).getOrElse(throw new InvalidRequest("required parameter: " + name))
}

case class ProtectedResourceRequest(headers: Map[String, Seq[String]], params: Map[String, Seq[String]]) extends RequestBase(headers, params) {

  def oauthToken: Option[String] = param("oauth_token")
  def idToken: Option[String] = param("id_token")
  def accessToken: Option[String] = param("access_token")
  def requireAccessToken: String = requireParam("access_token")
}
