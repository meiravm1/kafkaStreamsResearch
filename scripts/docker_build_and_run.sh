git pull
docker build --tag kafkastreams_covid:1.0 .
#docker run -it --rm --name kafkastreams_covid kafkastreams_covid:1.0 kafka:29092
docker-compose -f ./scripts/docker-compose.yml up