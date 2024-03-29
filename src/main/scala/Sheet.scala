import org.joda.time.*
import org.joda.time.format.{DateTimeFormatterBuilder, PeriodFormatter, PeriodFormatterBuilder}

object Sheet extends App {

  val days:Seq[DayEntry] = Seq(
    DayEntry("2023-01-27", "21:00", "21:15", 0, "update dependecies")
    )

  days.groupBy(_.day).toSeq.sortBy(_._1)
    .map(DayEntry.check)
    .map(DayEntry.daySumWithDetails)
    .foreach(println)

  println("Σ: " + DayEntry.formattedSumOf(days))

  case class DayEntry(day: String, start: String, end: String, pause: Int, description: String, status:Seq[String] = Nil) {

    private def asDate(day: String, value: String) = DateTime.parse(day + "T" + value + ":00")

    def interval: Interval = {
      new Interval(asDate(day, start), asDate(day, end))
    }

    def period: Period = {
      val pauseDuration = Minutes.minutes(pause).toStandardDuration()
      val dur = interval.toDuration
        .minus(pauseDuration)
      return dur.toPeriod()
    }

    override def toString: String = {
      val durFormatted = DayEntry.formatter.print(period)
      return "%s %s to %s p(%2s) %7s => %s".format(day, start, end, pause, durFormatted, description)
    }

    def withStats(stats:Seq[String]) = DayEntry(day, start, end, pause, description, stats)
  }

  object DayEntry {

    val formatter: PeriodFormatter = new PeriodFormatterBuilder()
      .appendHours().appendSuffix("h")
      .appendSeparator(" ")
      .appendMinutes().appendSuffix("m")
      .toFormatter();
    val dateFormatter = new DateTimeFormatterBuilder()
      .appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2)
      .toFormatter();

    private def sumOf(days: Seq[DayEntry]): Period = {
      return days.foldLeft(Period.ZERO)((p: Period, de: DayEntry) => p.plus(de.period))
        .toStandardDuration.toPeriod(PeriodType.time)
    }

    def formattedSumOf(days:Seq[DayEntry]):String = {
      return formatter.print(sumOf(days))
    }

    def daySumWithDetails(el:(String, Seq[DayEntry])):String = {
      val entries = el._2.sortBy(_.interval.getStartMillis)
      val stats = "\n" + entries.flatMap(_.status)
        .map(" W: " + _  ).toSet.mkString("\n")
      return entries.mkString("\n") + stats + "\n => Σ: " + formattedSumOf(el._2) + "\n---"
    }

    def check(mapEntry: (String, Seq[DayEntry])):(String, Seq[DayEntry]) = {
      val dayEntries = mapEntry._2
      var stats:Seq[String] = Seq()
      if (dayEntries.filter(_.pause > 0).size <= 0) {
        stats = stats :+ "add a pause"
      }

      val intervals:Seq[Interval] = mapEntry._2.map(_.interval).sortBy(_.getStartMillis)

      case class Abut(s:Seq[Interval]) {
        private val a:Interval = s(0)
        private val b:Interval = s(1)
        def abuts:Boolean = a.abuts(b)
        private def fmt(date:DateTime) = dateFormatter.print(date)
        override def toString = "%s/%s - %s/%s".format(fmt(a.getStart), fmt(a.getEnd), fmt(b.getStart), fmt(b.getEnd))
      }
      if (intervals.length > 1) {
        val intervalPairs:Seq[Abut] = intervals.sliding(2).map(Abut.apply).toSeq
        stats = stats ++ intervalPairs.filterNot(_.abuts).map("not abuting " + _)
      }
      return (mapEntry._1, mapEntry._2.map(_.withStats(stats)))
    }
  }
}
