# Aquila Trainer

## Sample training request

```
{
  "metricDefinition": {
    "tags": {
      "kv": {
        "mtype": "count",
        "unit": "",
        "what": "bookings",
        "lob": "hotels",
        "pos": "expedia-com",
        "interval": "5m"
      },
      "v": []
    },
    "meta": {
      "kv": {},
      "v": []
    }
  },
  "startDate": "2018-04-29T00:00:00Z",
  "endDate": "2018-07-22T00:00:00Z",
  "params": {
    "intervalInMinutes": 5,
    "decompType": "MULTIPLICATIVE",
    "periodSize": 2016,
    "wmaWindowSize": 21
  }
}
```

For now we are using the entire metricDefinition. In the near future we will replace this with a metricDefinition ID.

## Build

```
$ eval $(minikube docker-env)
$ ./build.sh -p
$ ./build.sh -b
```
