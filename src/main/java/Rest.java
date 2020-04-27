import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
//import org.apache.kafka.streams.state.QueryableStoreTypes;
//import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
//import org.glassfish.jersey.se.ResourceConfig;
//import org.glassfish.jersey.servlet.ServletContainer;
//import io.confluent.examples.streams.interactivequeries.HostStoreInfo;
//import io.confluent.examples.streams.interactivequeries.MetadataService;

@Path("kafka-music")

public class Rest {
    /**
     *  A simple REST proxy that runs embedded in the  This is used to
     *  demonstrate how a developer can use the Interactive Queries APIs exposed by Kafka Streams to
     *  locate and query the State Stores within a Kafka Streams Application.
     */



        private final KafkaStreams streams;
        //private final MetadataService metadataService;
        private final HostInfo hostInfo;
        private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
        private Server jettyServer;
        private final LongSerializer serializer = new LongSerializer();
        private static final Logger log = LoggerFactory.getLogger(Rest.class);


    Rest(final KafkaStreams streams, final HostInfo hostInfo) {
            this.streams = streams;
            this.metadataService = new MetadataService(streams);
            this.hostInfo = hostInfo;
        }


        @GET
        @Path("/charts/genre/{genre}")
        @Produces(MediaType.APPLICATION_JSON)
        public List<SongPlayCountBean> genreCharts(@PathParam("genre") final String genre) {

            // The genre might be hosted on another instance. We need to find which instance it is on
            // and then perform a remote lookup if necessary.
            final HostStoreInfo
                    host =
                    metadataService.streamsMetadataForStoreAndKey(KafkaMusicExample.TOP_FIVE_SONGS_BY_GENRE_STORE, genre, new
                            StringSerializer());

            // genre is on another instance. call the other instance to fetch the data.
            if (!thisHost(host)) {
                return fetchSongPlayCount(host, "kafka-music/charts/genre/" + genre);
            }

            // genre is on this instance
            return topFiveSongs(genre.toLowerCase(), KafkaMusicExample.TOP_FIVE_SONGS_BY_GENRE_STORE);

        }
    /**
     * Start an embedded Jetty Server
     * @throws Exception from jetty
     */
    void start() throws Exception {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server();
        jettyServer.setHandler(context);

        final ResourceConfig rc = new ResourceConfig();
        rc.register(this);
        rc.register(JacksonFeature.class);

        final ServletContainer sc = new ServletContainer(rc);
        final ServletHolder holder = new ServletHolder(sc);
        context.addServlet(holder, "/*");

        final ServerConnector connector = new ServerConnector(jettyServer);
        connector.setHost(hostInfo.host());
        connector.setPort(hostInfo.port());
        jettyServer.addConnector(connector);

        context.start();

        try {
            jettyServer.start();
        } catch (final java.net.SocketException exception) {
            log.error("Unavailable: " + hostInfo.host() + ":" + hostInfo.port());
            throw new Exception(exception.toString());
        }
    }

    /**
     * Stop the Jetty Server
     * @throws Exception from jetty
     */
    void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }
}
