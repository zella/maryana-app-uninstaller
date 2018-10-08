package org.zella.maryana.ui

import org.zella.maryana.core.adb.IAdb.InstalledApp

trait IView {

  def showProgress()

  def statusBar(msg:String)

  def clearStatusBar()

  def cancelProgress()

  def showCommandError(text: String)

  def showInstalledApps(apps: Seq[InstalledApp])

  def showUninstallResult(text: String)

  def showAdbVer(adbVer: String)

  def showAdbVerFailed(adbVer: String)

  def showDeviceConnected()

  def showNoDeviceConnected()

}
