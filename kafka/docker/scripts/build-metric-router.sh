#!/usr/bin/env bash

APP_NAME="metric-router"
DOTTED_APP_NAME="metric.router"
MAIN_CLASS="com.expedia.adaptivealerting.kafka.router.MetricRouter"

cd "$(dirname "$(realpath "$0")")";

. common.sh
