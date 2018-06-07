#!/usr/bin/env bash

APP_NAME="pewma-detector"
DOTTED_APP_NAME="pewma.detector"
MAIN_CLASS="com.expedia.adaptivealerting.kafka.detector.KafkaPewmaOutlierDetector"

cd "$(dirname "$(realpath "$0")")";

. common.sh
