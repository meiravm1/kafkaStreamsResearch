import java.util.Properties

import io.confluent.examples.streams.MapFunctionScalaExample.args
import org.apache.kafka.streams.StreamsConfig
object Stream {
  def main(args: Array[String]): Unit = {

    print ("hello")
  }

  def initConf(appName :String,bootstrapServer :String = "192.168.56.101:29094" ): Properties = {
    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, appName)
      val bootstrapServers = bootstrapServer
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      p
    }
    config
  }
}
