import java.time.Duration

import org.apache.kafka.streams.{KafkaStreams, KeyValue}
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.logging.log4j.LogManager

object Covid19Totals extends App  {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes._
  val logger = LogManager.getRootLogger

  val config = Stream.initConf(appName = "owid-covid-totals")
  val builder = new StreamsBuilder()
  val lines: KStream[String, String] = builder.stream[String, String]("owid-covid-data")
  val filterHeaders : KStream[String, Array[String]]= lines.filter((key, line) => !line.contains("iso_code,location")).map((key, value) => (value.split(",")(1), value.split(",")))

  val totalCases : KStream[String, String]= filterHeaders.map((key,value) => (key, value(3)))
  val newCases: KTable[String, Long] = filterHeaders.map((key,value) => (key, value(4).toLong)).groupByKey.reduce(_ + _)(Materialized.as("covid-new-cases-store"))
  val totalDeaths: KStream[String, String] = filterHeaders.map((key,value) => (key, value(5)))
  val newDeaths: KTable[String, Long] = filterHeaders.map((key,value) => (key, value(6).toLong)).groupByKey.reduce(_ + _)(Materialized.as("covid-new-deaths-store"))

  totalCases.to("owid-covid-total-cases")
  newCases.toStream.to("owid-covid-new-cases")
  totalDeaths.to("owid-covid-total-deaths")
  newDeaths.toStream.to("owid-covid-new-deaths")

  val streams = new KafkaStreams(builder.build(), config)
  streams.cleanUp()
  streams.start()
  Thread.sleep(10000)


  val casesStore : ReadOnlyKeyValueStore[Nothing,Nothing]= streams.store("covid-new-cases-store", QueryableStoreTypes.keyValueStore())
  val casesDStore : ReadOnlyKeyValueStore[Nothing,Nothing]= streams.store("covid-new-deaths-store", QueryableStoreTypes.keyValueStore())


  while (true)
    {
      IterateState("newCases",casesStore)
      IterateState("newDeaths",casesDStore)
      Thread.sleep(10000)

    }


  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

  def IterateState(storeName : String,casesStore : ReadOnlyKeyValueStore[Nothing,Nothing]) = {
    var top5 = collection.mutable.Map[String,Integer]()

    val all = casesStore.all()
    while (all.hasNext) {
      val nextPair = all.next()
      logger.info(s"${storeName} for country ${nextPair.key} is ${nextPair.value.toString.toInt}")
      top5 = top5  + (nextPair.key.toString -> nextPair.value.toString.toInt)

    }
    all.close()
    logger.error(s"Top 5 in ${storeName} are:")
    logger.error(top5.toSeq.sortWith(_._2 > _._2).take(5))

  }
}
