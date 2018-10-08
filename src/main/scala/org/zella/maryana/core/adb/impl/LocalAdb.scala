package org.zella.maryana.core.adb.impl

import monix.eval.Task
import org.zella.maryana.core.ProcessRunner
import org.zella.maryana.core.adb.IAdb
import org.zella.maryana.core.adb.IAdb.{CmdException, InstalledApp}

class LocalAdb(p: ProcessRunner) extends IAdb {

  private var adb: String = _

  override def removePackage(pck: String): Task[String] = {
    exec[String](s"$adb shell pm uninstall --user 0 $pck",
      out => Task.eval(out),
      (out, err) => Task.raiseError(new CmdException(err))
    )
  }

  override def adbAbout(): Task[String] = {
    exec[String](s"$adb version",
      out => Task.eval(out),
      (out, err) => Task.raiseError(new CmdException("Adb not found"))
    )
  }

  override def checkConnectedDevices(): Task[String] = {
    exec[String](s"$adb devices",
      out =>
        if (out.split(ProcessRunner.LineSeparator).length >= 2) Task.eval("Device connected") else
          Task.raiseError(new CmdException("Device not connected")),
      (out, err) => Task.raiseError(new CmdException("Device not connected"))
    )
  }

  override def listAllPackages(): Task[Seq[InstalledApp]] = {
    exec[Seq[InstalledApp]](s"$adb shell pm list packages -f",
      out =>
        Task.eval(out.split(ProcessRunner.LineSeparator).toSeq
          .map(p => p.substring(p.lastIndexOf("/") + 1))
          .map(s => InstalledApp("", s, "s"))),
      (out, err) => Task.raiseError(new CmdException(err))
    )
  }


  private def exec[T](cmd: String, onSuccess: String => Task[T], nonZeroParser: (String, String) => Task[T]): Task[T] = {
    val result = p.runSingle(cmd)
    result.exitCode match {
      case 0 => onSuccess(result.out)
      case failed => nonZeroParser(result.out, result.err)
    }
  }

  override def initialize(adb: String): Unit = {
    this.adb = adb
  }
}

