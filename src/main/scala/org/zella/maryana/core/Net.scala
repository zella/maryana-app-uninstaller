package org.zella.maryana.core

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.zella.maryana.core.Net.{AppPackageNet, Mark}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class Net(implicit val system: ActorSystem) {

  import play.api.libs.ws.JsonBodyReadables._

  implicit val materializer = ActorMaterializer()

  //  def getPackageInfo(appPackage: String): Future[Mark] = {
  //    val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()
  //    wsClient.underlying
  //    wsClient
  //      .url(s"${Net.BaseUrl}/app")
  //      .addQueryStringParameters("app" -> appPackage)
  //      .withRequestTimeout(Net.Timeout)
  //      .get()
  //      .map(resp => resp.body[JsValue])
  //  }

  def getPackagesInfo(appPackages: Set[String]): Future[Seq[AppPackageNet]] = {
    val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

    val params = appPackages.map(p => ("app", p)).toSeq

    wsClient.underlying
    wsClient
      .url(s"${Net.BaseUrl}/apps")
      .addQueryStringParameters(params: _*)
      .withRequestTimeout(Net.Timeout)
      .get()
      .map(resp => resp.body[JsValue].as[Seq[AppPackageNet]])
  }

}


case object Net {

  val BaseUrl = "https://mary-ann-prototype.appspot.com"
  val Timeout = 15.seconds

  case class Mark(mark: String, `type`: String)

  case class AppPackageNet(app: String, mark: Mark)


  object Mark {
    implicit val jsonFormat = Json.reads[Mark]
  }

  object AppPackageNet {
    implicit val jsonFormat = Json.reads[AppPackageNet]
  }


}