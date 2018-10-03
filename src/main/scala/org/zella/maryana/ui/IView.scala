package org.zella.maryana.ui

trait IView {

  def showCommandError(text:String)

  def showInstalledPackages(packages:List[String])

  def showUninstallResult(text:String)

  def showAdbVer(adbVer:String)

  def showAdbVerFailed(adbVer:String)

  def showDeviceConnected()

  def showNoDeviceConnected()

}
