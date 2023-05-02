package timesheet

import com.sun.jna
import com.sun.jna.{Native, Pointer}
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.PointerByReference
import org.yaml.snakeyaml.DumperOptions.ScalarStyle
import org.yaml.snakeyaml.{DumperOptions, Yaml}

import java.io.File
import java.lang.annotation.Native
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, OpenOption, StandardOpenOption}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.{ChronoField, TemporalField}
import java.util.stream.Collectors
import scala.jdk.CollectionConverters.*

object CurrentWindowWorker {
  def main(args: Array[String]): Unit = {
    var latest: (String, String) = ("", "")
    var n = now()
    val options = new DumperOptions()
    options.setWidth(1024)
    options.setExplicitEnd(true)
    options.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)

    val yaml = new Yaml(options)
    val icon = InfoIcon.create()
    while (true) {
      val cTitle = JnaUtil.getActiveWindowTitle()
      val currentProcess = JnaUtil.getActiveWindowProcess()
      if (latest != ((cTitle, currentProcess))) {
        val newN = now()
        val previous = java.time.Duration.between(n, newN)
        n = newN
        val data = Map(
          "date" -> format(n),
          "title" -> cTitle,
          "process" -> currentProcess,
          "previous-duration/" + latest._2 -> previous.toString,
        )
        val o = yaml.dump(data.asJava).trim
        latest = (cTitle, currentProcess)
        println(o)
        write(o)
        icon.signalWrite()
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
    Files.write(path, o.lines().collect(Collectors.toList), StandardCharsets.UTF_8, options: _*)
  }

  def format(now: ZonedDateTime): String = {
    now.toString
  }

  private def now(): ZonedDateTime = {
    ZonedDateTime.now().withNano(0).withFixedOffsetZone()
  }
}
