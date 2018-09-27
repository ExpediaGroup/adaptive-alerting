#!/usr/bin/env sh

# Copyright 2018 Expedia Group, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

[ -z "$JAVA_XMS" ] && JAVA_XMS=1024m
[ -z "$JAVA_XMX" ] && JAVA_XMX=1024m

set -e
JAVA_OPTS="${JAVA_OPTS} \
    -javaagent:${APP_HOME}/${JMXTRANS_AGENT}.jar=${APP_HOME}/jmxtrans-agent.xml \
    -XX:+UseConcMarkSweepGC \
    -XX:+UseParNewGC \
    -Xmx${JAVA_XMX} \
    -Xms${JAVA_XMS} \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.port=1098 \
    -Dapplication.name=${APP_NAME} \
    -Dapplication.home=${APP_HOME}"

exec java ${JAVA_OPTS} -jar "${APP_HOME}/aquila-detect.jar" com.expedia.aquila.detect.AquilaDetectorApp
