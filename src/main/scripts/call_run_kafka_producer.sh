echo "run example menu:"

echo "1)upper value (produce message in key:value format)"
echo "2)word count"

read option
echo option is $option
if [ $option -eq 1 ];then
	./run_kafka_producer.sh TextLinesTopic  "--property "parse.key=true" --property "key.separator=:"" 
elif [ $option -eq 2 ];then
	 ./run_kafka_producer.sh streams-plaintext-input

fi
