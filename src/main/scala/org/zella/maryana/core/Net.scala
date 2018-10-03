package org.zella.maryana.core

import org.zella.maryana.core.Net.Mark
import play.api.libs.json.Json
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.libs.ws.StandaloneWSClient
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient

import scala.concurrent.duration._
import scala.concurrent.Future

class Net() {

  def getPackageInfo(appPackage: String): Future[String] = {
    val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()
    wsClient.underlying
    wsClient
      .url(s"${Net.BaseUrl}/apps=$appPackage")
      .addQueryStringParameters("app" -> appPackage)
      .withRequestTimeout(10.seconds)
      .get()
      .map(resp => resp.body[Mark].toString)
  }
}


case object Net {

  val BaseUrl = "https://mary-ann-prototype.appspot.com"

  case class Mark(mark: String, `type`: String) {
    override def toString: String = mark
  }

  object Mark {
    implicit val jsonFormat = Json.format[Mark]
  }

}