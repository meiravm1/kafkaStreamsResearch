from openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./build/output/lib/* ./scripts/run_jar.sh ./build/libs/kafkaStreamScala.jar ./
ENTRYPOINT ["./scripts/run_jar.sh"]