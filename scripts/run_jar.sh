#!/bin/sh
bootstrap_server=$1
java -cp "./build/libs/kafkaStreamScala.jar;./build/output/lib/*" Covid19Totals ${bootstrap_server}