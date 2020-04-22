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
  val newCases: KTable[String, Long] = filterHeaders.map((key,value) => (key+"_"+ "newCases", value(4).toLong)).groupByKey.reduce(_ + _)(Materialized.as("covid-new-cases-store"))
  val totalDeaths: KStream[String, String] = filterHeaders.map((key,value) => (key, value(5)))
  val newDeaths: KTable[String, Long] = filterHeaders.map((key,value) => (key+"_"+ "newDeaths", value(6).toLong)).groupByKey.reduce(_ + _)(Materialized.as("covid-new-deaths-store"))

  totalCases.to("owid-covid-total-cases")
  newCases.toStream.to("owid-covid-new-cases")
  totalDeaths.to("owid-covid-total-deaths")
  newDeaths.toStream.to("owid-covid-new-deaths")

  val streams = new KafkaStreams(builder.build(), config)
  streams.cleanUp()
  streams.start()
  Thread.sleep(10000)
  logger.info("is it even written anywhere")
  val casesStore : ReadOnlyKeyValueStore[Nothing,Nothing]= streams.store("covid-new-cases-store", QueryableStoreTypes.keyValueStore())
  IterateState("newCases","newDeaths",casesStore)

  val casesDStore : ReadOnlyKeyValueStore[Nothing,Nothing]= streams.store("covid-new-deaths-store", QueryableStoreTypes.keyValueStore())
  IterateState("newDeaths","newDeaths",casesDStore)

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

  def IterateState(storeName : String,secondStoreName : String,casesStore : ReadOnlyKeyValueStore[Nothing,Nothing]) = {

    logger.info("is it even written anywhere??")
    val all = casesStore.all()
    while (all.hasNext) {
      val nextPair = all.next()
      if (nextPair.key.toString.contains(storeName))
        logger.info(s"${storeName} for country ${nextPair.key.toString.replace(storeName,"")} is ${nextPair.value.toString.toInt}")
      else
        logger.info(s"${secondStoreName} for country ${nextPair.key.toString.replace("secondStoreName","")} is ${nextPair.value.toString.toInt}")

    }
  }
}
