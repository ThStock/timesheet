package timesheet

import com.sun.jna
import com.sun.jna.{Native, Pointer}
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.PointerByReference
import org.yaml.snakeyaml.{DumperOptions, Yaml}

import java.io.File
import java.lang.annotation.Native
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, OpenOption, StandardOpenOption}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.{ChronoField, TemporalField}
import scala.jdk.CollectionConverters.*

object CurrentWindowWorker {

  def main(args: Array[String]): Unit = {
    var latest = ""
    val options = new DumperOptions()
    options.setWidth(1024)
    options.setExplicitEnd(true)
    val yaml = new Yaml(options)
    while (true) {
      val cTitle = JnaUtil.getActiveWindowTitle()
      val currentProcess = JnaUtil.getActiveWindowProcess()
      val data = Map(
        "date" -> date(),
        "title" -> cTitle,
        "process" -> currentProcess
      )
      if (cTitle.isBlank && currentProcess == "Explorer.EXE") {
        // do nothing
      } else if (cTitle == "Task Switching" && currentProcess == "Explorer.EXE") {
        // do nothing
      } else if (latest != cTitle + currentProcess) {

        val o = yaml.dump(data.asJava).trim
        latest = cTitle + currentProcess
        println(o)
        write(o)
      }
      Thread.sleep(100)
    }
  }

  private def write(o: String) = {
    val options = Array[OpenOption](StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
    val home = System.getProperty("user.home")
    val nowName = new DateTimeFormatterBuilder()
      .appendLiteral(".timesheet-")
      .appendValue(ChronoField.YEAR)
      .appendLiteral("-")
      .appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .appendLiteral("-")
      .appendValue(ChronoField.DAY_OF_MONTH, 2)
      .appendLiteral(".yaml")

      .toFormatter.format(ZonedDateTime.now())
    val path = new File(new File(home), nowName).toPath.toAbsolutePath
    Files.write(path, o.lines().toList, StandardCharsets.UTF_8, options: _*)
  }

  def date(): String = {
    ZonedDateTime.now().withNano(0).withFixedOffsetZone().toString
  }
}
