from openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./build/output/lib/  ./build/output/lib/
COPY ./scripts/run_jar.sh ./scripts/
COPY ./build/libs/kafkaStreamScala.jar ./build/libs/
RUN chmod 755 ./scripts/run_jar.sh
ENTRYPOINT ["./scripts/run_jar.sh"]