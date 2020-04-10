#!/usr/bin/env bash

APP_NAME="adaptive-alerting-visualizer"
DOTTED_APP_NAME="visualizer"
MAIN_CLASS="com.expedia.adaptivealerting.kafka.AnomalyVisualizer"

cd `cd -P -- "$(dirname -- "$0")" && pwd -P`

. common.sh
