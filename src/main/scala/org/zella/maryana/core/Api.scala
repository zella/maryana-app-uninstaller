package org.zella.maryana.core

import org.zella.maryana.ui.IView

class Api(adb: String, p: ProcessRunner, view: IView) {

  def removePackage(pck: String) {
    exec[String](s"$adb shell pm uninstall --user 0 $pck",
      out => out,
      result => view.showUninstallResult(result),
      err => view.showCommandError(err)
    )
  }

  def adbAbout() {
    exec[String](s"$adb version",
      out => out,
      result => view.showAdbVer(result),
      err => view.showAdbVerFailed("adb not found")
    )
  }

  def checkConnectedDevices() {
    exec[String](s"$adb devices",
      out => {
        if (out.split(ProcessRunner.LineSeparator).length < 2) view.showNoDeviceConnected()
        else view.showDeviceConnected()
        out
      },
      result => (),
      err => view.showNoDeviceConnected()
    )
  }

  def listAllPackages(): Unit = {

    exec[List[String]](s"$adb shell pm list packages -f",
      out => {
        out.split(ProcessRunner.LineSeparator).toList
          .map(p => p.substring(p.lastIndexOf(".apk=") + 5))
      },
      result => view.showInstalledPackages(result),
      err => if (err.nonEmpty) view.showCommandError(err)
    )
  }


  private def exec[T](cmd: String, parser: String => T, onSuccess: T => Unit, onError: String => Unit): Unit = {
    val result = p.runSingle(cmd)
    result.exitCode match {
      case 0 => onSuccess(parser(result.out)) //TODO parsing error
      case failed => onError(result.err)
    }
  }

}
