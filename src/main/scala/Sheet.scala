import org.joda.time._

object Sheet extends App {

  val days:Seq[DayEntry] = Seq(
    //
    )

  days.groupBy(_.day)
    .map(DayEntry.check)
    .map(DayEntry.daySumWithDetails)
    .toSeq.reverse.foreach(println)
  println("Σ: " + DayEntry.sumOf(days))

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
  }

  object DayEntry {
    import org.joda.time.format._
    val formatter: PeriodFormatter = new PeriodFormatterBuilder()
      .appendHours().appendSuffix("h")
      .appendSeparator(" ")
      .appendMinutes().appendSuffix("m")
      .toFormatter();
    val dateFormatter = new DateTimeFormatterBuilder()
      .appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2)
      .toFormatter();

    def sum(p: Period, de: DayEntry) = p.plus(de.period)

    def daySum(el: (String, Seq[DayEntry])): String = sumOf(el._2)

    def daySumWithDetails(el:(String, Seq[DayEntry])):String = {
      val entries = el._2.sortBy(_.interval.getStartMillis)
      val stats = "\n" + entries.flatMap(_.status)
        .map(" W: " + _  ).toSet.mkString("\n")
      return entries.mkString("\n") + stats + "\n => Σ: " + daySum(el) + "\n---"
    }

    def sumOf(days: Seq[DayEntry]): String = {
      val sum = days.foldLeft(Period.ZERO)(DayEntry.sum).normalizedStandard
      return DayEntry.formatter.print(sum)
    }

    def check(el: (String, Seq[DayEntry])):(String, Seq[DayEntry]) = {
      val dayEntries = el._2
      var stats:Seq[String] = Seq()
      if (dayEntries.filter(_.pause > 0).size <= 0) {
        stats = stats :+ "add a pause"
      }

      val intervals:Seq[Interval] = el._2.map(_.interval).sortBy(_.getStartMillis)

      case class Abut(s:Seq[Interval]) {
        private val a:Interval = s(0)
        private val b:Interval = s(1)
        def abuts:Boolean = !a.abuts(b)
        private def fmt(date:DateTime) = dateFormatter.print(date)
        override def toString = "%s/%s - %s/%s".format(fmt(a.getStart), fmt(a.getEnd), fmt(b.getStart), fmt(b.getEnd))
      }

      val abuts:Seq[Abut] = intervals.sliding(2).map(Abut).toSeq
      stats = stats ++ abuts.filter(_.abuts).map("not abuting " + _)
      return (el._1, el._2.map(d => DayEntry(d.day, d.start, d.end, d.pause, d.description, stats)))
    }
  }
}
