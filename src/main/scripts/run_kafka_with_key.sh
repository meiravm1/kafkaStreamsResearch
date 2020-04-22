topic=$1

container_id=`docker ps|grep kafka|gawk -F" " '{print $1}'`
echo $container_id
docker exec -it $container_id /opt/bitnami/kafka/bin/kafka-console-producer.sh --broker-list=kafka:29092 --topic $topic --property "parse.key=true" --property "key.separator=:" 
