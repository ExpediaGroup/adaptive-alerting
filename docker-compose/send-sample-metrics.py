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
from kafka.errors import KafkaError
import json
import time
from sys import stdout
import random
import threading

kafka_boostrap_servers=['localhost:19092']

def produce_to_kafka(topic):
    num_samples = 1000000    # Minimum of 80 events need to be in Kafka topic to see output
    try:
        time.sleep(1)   # Wait to make sure we establish a connection to anomalies kafka first
        producer = KafkaProducer(bootstrap_servers=kafka_boostrap_servers)
        print("Sending", num_samples, "random sample messages to Kafka...")
        for i in range(0, num_samples):
            send_msgpack_to_kafka(producer, topic='aa-metrics') # Send to kafka using msgpack format
            # send_json_to_kafka()              # Send to kafka using JSON format
        producer.flush()
    except KafkaError as e:
        print("Error connecting to", topic, "Kafka. Make sure your Docker Compose environment is up and running. EXCEPTION:",e)

# Default Msgpack Serde - Use if ad-mapper.conf is set to "com.expedia.adaptivealerting.kafka.serde.MetricDataMessagePackSerde"
def send_msgpack_to_kafka(producer, topic):
    random_tag_index = int(random.uniform(1,200000))
    metric_data = {
        "OrgId": 1, 
        "Name": "samplemetric", 
        "Interval": 15,
        "Value": round(random.uniform(10,200), 1),
        "Unit": "unknown",
        "Time": int(time.time()),
        "Mtype": "gauge",
        "Tags": [f"what=samplemetric{random_tag_index}", "m_application=sample-web", "region=primary", "env=prod"]
    }
    packed_metric_data = msgpack.packb(metric_data, use_bin_type=True)
    try:
        producer.send(topic, key=b'prod.primary.sample-web.sample-metric', value=packed_metric_data)
    except KafkaError as e:
        print("Error connecting to kafka producer...",e)
    

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
    try:
        producer.send(topic, key=b'prod.primary.sample-web.sample-metric', value=metric_data)
    except KafkaError as e:
        print("Error connecting to kafka producer...",e)

def consume_from_kafka(topic):
    output_count = 0
    anomaly_count = 0
    anomaly_values = {"WEAK": [], "STRONG": []}

    try:
        consumer = KafkaConsumer(topic,
                                bootstrap_servers=kafka_boostrap_servers, 
                                auto_offset_reset='latest', 
                                enable_auto_commit=False, 
                                consumer_timeout_ms=5000)
        
        print("Checking for anomalies from Kafka...\n")
        for message in consumer:
            output_count+=1
            anomaly = json.loads(message.value)
            anomaly_level = anomaly.get('anomalyResult',{}).get('anomalyLevel')
            if anomaly_level == 'WEAK' or anomaly_level == 'STRONG':
                anomaly_count+=1
                anomaly_values[anomaly_level].append(anomaly.get('metricData').get('value'))
            
            print('- Total Anomalies Found: ' + str(anomaly_count) + ' out of ' + str(output_count), end='\r')
            stdout.flush()
    except KafkaError as e:
        print("Error connecting to", topic, "Kafka. Make sure your Docker environment is up and running. EXCEPTION:",e)
        return False

    if anomaly_count == 0:
        print("No Anomalies were found. Make sure your Docker Compose environment is up and running.")
    else:
        print("\n\n-- Found", len(anomaly_values['WEAK']), "WEAK Anomalies")
        # print("-- Weak Anomalous values:", anomaly_values['WEAK'])
        print("\n-- Found", len(anomaly_values['STRONG']), "STRONG Anomalies")
        # print("-- Strong Anomalous values:", anomaly_values['STRONG'])

    return anomaly_values


if __name__ == '__main__':
    # print(f"starttime: {int(time.time()*1000)}")
    # t = threading.Thread(target=consume_from_kafka, args=['anomalies'])
    # t.start()
    produce_to_kafka('aa-metrics')
    # print(f"endtime: {int(time.time()*1000)}")