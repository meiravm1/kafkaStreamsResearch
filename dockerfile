from openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app

COPY ./build/output/lib/* ./scripts/run_jar.sh ./kafkaStreamScala.jar ./
ENTRYPOINT ["./run_jar.sh"]