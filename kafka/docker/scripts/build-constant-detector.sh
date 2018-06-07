#!/usr/bin/env bash

APP_NAME="constant-detector"
DOTTED_APP_NAME="constant.detector"
MAIN_CLASS="com.expedia.adaptivealerting.kafka.detector.KafkaConstantThresholdOutlierDetector"

cd "$(dirname "$(realpath "$0")")";

. common.sh
