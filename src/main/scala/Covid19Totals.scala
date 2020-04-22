import java.time.Duration

import WordCountScalaExample.streams
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}

object Covid19Totals extends App {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._

  val config = Stream.initConf(appName = "owid-covid-totals")
  val builder = new StreamsBuilder()
  val lines: KStream[String, String] = builder.stream[String, String]("owid-covid-data")
  val filterHeaders : KStream[String, Array[String]]= lines.filter((key, line) => !line.contains("iso_code,location")).map((key, value) => (value.split(",")(1), value.split(",")))

  val totalCases : KStream[String, String]= filterHeaders.map((key,value) => (key, value(3)))
  val newCases: KTable[String, Long] = filterHeaders.map((key,value) => (key, value(4).toLong)).groupByKey.reduce(_ + _)
  val totalDeaths: KStream[String, String] = filterHeaders.map((key,value) => (key, value(5)))
  val newDeaths: KTable[String, Long] = filterHeaders.map((key,value) => (key, value(6).toLong)).groupByKey.reduce(_ + _)

  totalCases.to("owid-covid-total-cases")
  newCases.toStream.to("owid-covid-new-cases")
  totalDeaths.to("owid-covid-total-deaths")
  newDeaths.toStream.to("owid-covid-new-deaths")

  val streams = new KafkaStreams(builder.build(), config)
  streams.cleanUp()
  streams.start()
  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

}
