echo "run example menu:"

echo "1)upper value"
echo "2)word count"

read option
echo option is $option


if [ $option -eq 1 ];then
	./run_kafka_consumer.sh OriginalAndUppercasedTopic

elif [ $option -eq 2 ];then
	./run_kafka_consumer.sh streams-wordcount-output "--property print.key=true \
                            --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer"

fi
