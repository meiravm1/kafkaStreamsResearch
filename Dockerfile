from openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./build/output/lib/* ./scripts/run_jar.sh ./build/libs/kafkaStreamScala.jar ./
RUN chmod 755 ./run_jar.sh
ENTRYPOINT ["./run_jar.sh"]