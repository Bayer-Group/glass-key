package com.monsanto.rdit.oauth.sprayclient.model

import spray.json.DefaultJsonProtocol

case class HelloModelReturn(id: Option[Int], message: Option[String], roles: Option[List[String]], scopes: Option[List[String]])

object HelloWorldJsonProtocol extends DefaultJsonProtocol {
  implicit def HelloWorldReturnFormat = jsonFormat4(HelloModelReturn)
}
