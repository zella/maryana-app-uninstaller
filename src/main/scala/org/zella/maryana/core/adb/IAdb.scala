package org.zella.maryana.core.adb

import monix.eval.Task
import org.zella.maryana.core.net.INet.Mark

trait IAdb {

  def initialize(adb:String)

  def removePackage(pck: String): Task[String]

  def adbAbout(): Task[String]

  def checkConnectedDevices(): Task[String]

  def listAllPackages(): Task[Seq[IAdb.InstalledApp]]
}

object IAdb {

  case class InstalledApp(apk: String, pkg: String, topic: String, mark: Option[Mark] = None)

  class CmdException(msg: String) extends RuntimeException(msg)

}


