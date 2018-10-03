package org.zella.maryana.ui

import org.zella.maryana.ui.MainApp.AppPackage

trait IView {

  def showCommandError(text:String)

  def showInstalledPackages(packages:Seq[AppPackage])

  def showUninstallResult(text:String)

  def showAdbVer(adbVer:String)

  def showAdbVerFailed(adbVer:String)

  def showDeviceConnected()

  def showNoDeviceConnected()

}
