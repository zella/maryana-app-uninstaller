package org.zella.maryana.core

import org.zella.maryana.core.ProcessRunner.Result

class ProcessRunner {

  def runSingle(command: String): Result = {

    val out = StringBuilder.newBuilder
    val err = StringBuilder.newBuilder

    import scala.sys.process._
    try {
      val exitCode = command ! ProcessLogger(o => {

        out.append(o)
        if (o.nonEmpty)
        out.append(ProcessRunner.LineSeparator)
      }, e => {
        err.append(e)
        if (e.nonEmpty)
          err.append(ProcessRunner.LineSeparator)

      })
      Result(out.toString().trim, err.toString().trim, exitCode)
    }
    catch {
      case e: Throwable => Result(out.toString(), err.toString().dropRight(1), -1)
    }

  }

}

object ProcessRunner {

  val LineSeparator = System.getProperty("line.separator")

  case class Result(out: String, err: String, exitCode: Int)

}

