from bitnami/kafka:2
USER root
RUN mkdir -p /opt/app
RUN mkdir -p /opt/app/logs
WORKDIR /opt/app

COPY ./build/output/lib/  ./build/output/lib/
COPY ./scripts/run_jar.sh ./scripts/
COPY ./build/libs/kafkaStreamScala.jar ./build/libs/
RUN chmod 755 ./scripts/run_jar.sh
ENTRYPOINT ["./scripts/run_jar.sh"]