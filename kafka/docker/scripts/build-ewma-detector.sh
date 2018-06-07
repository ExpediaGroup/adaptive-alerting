#!/usr/bin/env bash

APP_NAME="ewma-detector"
DOTTED_APP_NAME="ewma.detector"
MAIN_CLASS="com.expedia.adaptivealerting.kafka.detector.KafkaEwmaOutlierDetector"

cd "$(dirname "$(realpath "$0")")";

. common.sh
