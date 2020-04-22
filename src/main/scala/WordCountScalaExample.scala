import java.time.Duration
import java.util.Properties

import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}

object WordCountScalaExample extends App{

  import org.apache.kafka.streams.scala.Serdes._
  import org.apache.kafka.streams.scala.ImplicitConversions._

  val conf = Stream.initConf("WordCountScalaExample")
  val builder = new StreamsBuilder()

  val textLines : KStream[String,String]= builder.stream[String,String]("streams-plaintext-input")
  val wordCounts : KTable[String,Long] = textLines.flatMapValues(line => line.toLowerCase().split("\\W+")).groupBy((_,word) => word).count()
  wordCounts.toStream.to("streams-wordcount-output")
  val streams : KafkaStreams  = new KafkaStreams(builder.build(),conf)
  streams.cleanUp()
  streams.start()
  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }

}
