topic=$1
other_options=$2
container_id=`docker ps|grep kafka|gawk -F" " '{print $1}'`
echo $container_id

docker exec -it $container_id /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server=kafka:29092 --topic $topic ${other_options} 
