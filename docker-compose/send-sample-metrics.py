#  Copyright 2018-2019 Expedia Group, Inc.
#
#       Licensed under the Apache License, Version 2.0 (the "License");
#       you may not use this file except in compliance with the License.
#      You may obtain a copy of the License at
#
#           http://www.apache.org/licenses/LICENSE-2.0
#
#       Unless required by applicable law or agreed to in writing, software
#       distributed under the License is distributed on an "AS IS" BASIS,
#       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#       See the License for the specific language governing permissions and
#       limitations under the License.

import msgpack
from kafka import KafkaProducer
from kafka import KafkaConsumer
import json
import time
from sys import stdout
import random

kafka_boostrap_servers=['localhost:19092']

def send_sample_metrics_handler():
    num_samples = 250    # Minimum of 80 events need to be in Kafka topic to see output
    print("Sending", num_samples, "random sample messages to Kafka...")

    producer = KafkaProducer(bootstrap_servers=kafka_boostrap_servers)
    for i in range(0, num_samples):
        send_msgpack_to_kafka(producer, topic='aa-metrics') # Send to kafka using msgpack format
        # send_json_to_kafka()              # Send to kafka using JSON format
    producer.flush()

    print("Checking for anomalies from Kafka...\n")
    weak_anomaly_values, strong_anomaly_values = consume_from_kafka('anomalies')
    print("\n-- Found", len(weak_anomaly_values), "WEAK Anomalies")
    print("-- Weak Anomalous values:", weak_anomaly_values)
    print("\n-- Found", len(strong_anomaly_values), "STRONG Anomalies")
    print("-- Strong Anomalous values:", strong_anomaly_values)
    

# Default Msgpack Serde - Use if ad-mapper.conf is set to "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde"
def send_msgpack_to_kafka(producer, topic):
    metric_data = {
        "OrgId": 1, 
        "Name": "samplemetric", 
        "Interval": 15,
        "Value": round(random.uniform(10,200), 1),
        "Unit": "unknown",
        "Time": int(time.time()),
        "Mtype": "gauge",
        "Tags": ["what=samplemetric", "m_application=sample-web", "region=primary", "env=prod"]
    }
    packed_metric_data = msgpack.packb(metric_data, use_bin_type=True)
    producer.send(topic, key=b'prod.primary.sample-web.sample-metric', value=packed_metric_data)


# JSON Serde - Use if ad-mapper.conf is set to "com.expedia.adaptivealerting.kafka.serde.MetricDataJsonSerde"
def send_json_to_kafka(producer, topic):
    metric_data = {
                    "metricDefinition": {
                        "key": "samplemetric",
                        "tags": {
                            "kv": {
                                "what": "samplemetric",
                                "m_application": "sample-web",
                                "region": "primary",
                                "env": "prod"
                            },
                            "v": []
                        },
                        "meta": {
                            "kv": {},
                            "v": []
                        }
                    },
                    "value": round(random.uniform(10,200), 1),
                    "timestamp": int(time.time())
                }
    metric_data = json.dumps(metric_data).encode()
    producer.send(topic, key=b'prod.primary.sample-web.sample-metric', value=metric_data)


def consume_from_kafka(topic):
    output_count = 0
    anomaly_count = 0
    strong_values = []
    weak_values = []
    consumer = KafkaConsumer(topic,
                            bootstrap_servers=kafka_boostrap_servers, 
                            auto_offset_reset='earliest', 
                            enable_auto_commit=False, 
                            consumer_timeout_ms=5000)
    
    for message in consumer:
        output_count+=1
        anomaly = json.loads(message.value)
        anomalyLevel = anomaly.get('anomalyResult',{}).get('anomalyLevel')
        if anomalyLevel == 'WEAK':
            anomaly_count+=1
            weak_values.append(anomaly.get('metricData').get('value'))
        elif anomalyLevel == 'STRONG':
            anomaly_count+=1
            strong_values.append(anomaly.get('metricData').get('value'))
        
        print('- Total Anomalies Found: ' + str(anomaly_count) + ' out of ' + str(output_count), end='\r')
        stdout.flush()

    return weak_values, strong_values


if __name__ == '__main__':
    send_sample_metrics_handler()