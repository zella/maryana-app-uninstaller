package org.zella.maryana.controller

import monix.eval.Task
import monix.execution.Scheduler
import org.zella.maryana.core.adb.IAdb
import org.zella.maryana.core.adb.IAdb.InstalledApp
import org.zella.maryana.core.net.INet
import org.zella.maryana.core.net.INet.AppPackageNet
import org.zella.maryana.ui.IView

import scala.util.{Failure, Success}

class Controller(adb: IAdb, net: INet, view: IView)(implicit sc: Scheduler) {

  def initialize(adbPath: String): Unit = adb.initialize(adbPath)

  def removePackage(pck: String): Unit = {
    view.showProgress()
    adb.removePackage(pck)
      .doOnCancel({
        view.cancelProgress()
        Task.unit
      })
      .doOnFinish(_ => {
        view.cancelProgress()
        Task.unit
      })
      .runAsync.onComplete {
      case Success(res) => view.showUninstallResult(res)
      case Failure(e) => view.showCommandError(e.getMessage)
    }
  }

  //FIXME implement refresh in one chain

  def adbAbout(): Unit = {
    view.showProgress()
    adb.adbAbout()
      .doOnCancel({
        view.cancelProgress()
        Task.unit
      })
      .doOnFinish(_ => {
        view.cancelProgress()
        Task.unit
      }).
      runAsync.onComplete {
      case Success(res) => view.showAdbVer(res)
      case Failure(e) => view.showAdbVerFailed(e.getMessage)
    }
  }

  def checkConnectedDevices(): Unit = {
    view.showProgress()
    adb.checkConnectedDevices()
      .doOnCancel({
        view.cancelProgress()
        Task.unit
      })
      .doOnFinish(_ => {
        view.cancelProgress()
        Task.unit
      }).
      runAsync.onComplete {
      case Success(res) => view.showDeviceConnected()
      case Failure(e) => view.showNoDeviceConnected()
    }
  }

  def listAllPackages(): Unit = {

    def fillMarks(in: Seq[InstalledApp], fromNet: Seq[AppPackageNet]): Seq[InstalledApp] = {
      val maps: Map[String, AppPackageNet] = fromNet.groupBy(_.app).mapValues(_.head)
      in.map(app => app.copy(mark = maps.get(app.pkg).map(_.mark)))
    }

    view.showProgress()
    adb.listAllPackages()
      .doOnCancel({
        view.cancelProgress()
        Task.unit
      })
      .doOnFinish(_ => {
        view.cancelProgress()
        Task.unit
      }).flatMap(apps => {
      view.showInstalledApps(apps)
      view.statusBar("Loading info about app from cloud database...")
      Task.zip2(net.loadPackagesInfo(apps.map(_.pkg).toSet).doOnCancel({
        view.clearStatusBar()
        Task.unit
      }), Task.pure(apps))
    }).runAsync.onComplete {
      case Success((loadedPackages, localApps)) =>
        view.clearStatusBar()
        view.showInstalledApps(fillMarks(localApps, loadedPackages))
      case Failure(e) =>
        view.clearStatusBar()
        view.showCommandError(e.getMessage)
    }
  }

}
