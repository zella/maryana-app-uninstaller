package org.zella.maryana.core.net

import monix.eval.Task
import play.api.libs.json.Json

import scala.concurrent.duration._

trait INet {

  def loadPackagesInfo(appPackages: Set[String]): Task[Seq[INet.AppPackageNet]]
}

case object INet {

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
