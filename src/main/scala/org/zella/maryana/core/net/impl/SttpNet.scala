package org.zella.maryana.core.net.impl

import com.softwaremill.sttp.okhttp.monix.OkHttpMonixBackend
import monix.eval.Task
import org.zella.maryana.core.net.INet
import org.zella.maryana.core.net.INet.AppPackageNet
import play.api.libs.json.Json

class SttpNet extends INet {

  override def loadPackagesInfo(appPackages: Set[String]): Task[Seq[AppPackageNet]] = {
    import com.softwaremill.sttp._

    implicit val sttpBackend = OkHttpMonixBackend()

    val params: Map[String, String] = appPackages.map(p => ("app", p)).toMap

    sttp
      .get(uri"${INet.BaseUrl}/apps$params")
      .mapResponse(resp => Json.parse(resp).as[Seq[AppPackageNet]])
      .send()
      .map(_.unsafeBody)
  }


}


