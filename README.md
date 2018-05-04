[![Build Status](https://travis-ci.org/ExpediaDotCom/haystack-adaptive-alerting.svg?branch=master)](https://travis-ci.org/ExpediaDotCom/haystack-adaptive-alerting)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/ExpediaDotCom/haystack-adaptive-alerting/blob/master/LICENSE)

# haystack-adaptive-alerting

Streaming anomaly detection with automated model selection.

Strictly, we distinguish _outliers_ from _anomalies_: outliers are unusual data points that may indicate an anomaly in
the underlying data-generating process. But it's possible to have an outlier that arises out of pure chance (e.g.,
sampling error), and it's possible to have low-grade anomalies in the process that generate weak outliers difficult to
distinguish from noise. Most of what we're doing here is supporting anomaly detection via outlier analysis, and the
technical challenge is to apply outlier detection models in such a way that they effectively distinguish anomalies from
noise.

Internally, most outlier detectors compute for any given data point an outlier score that depends upon the specific
model being used. The public output is a classification: normal (non-outlier), weak outlier or strong outlier.

## Outlier detectors

We will offer a number of outlier detectors for streaming time series. We are interested in multiple approaches
(e.g., extreme value analysis, probabilistic/statistical, linear, proximity and so forth).

- `ConstantThresholdOutlierDetector`: Detector based on constant thresholds applied to a single tail.
- `EwmaOutlierDetector`: Detector based on an [exponentially weighted moving average](https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average) filter
- More to come.

## Automated model selection

The goal here is to automatically select and fit the best model available for any given time series, since we expect
this to improve the signal-to-noise ratio for alerting, thus increasing our ability to scale our monitoring to a larger
number of metrics.

This is just a goal at this point. Watch this space for future developments.

## Reference

- [Probabilistic Reasoning for Streaming Anomaly Detection](https://www.ll.mit.edu/mission/cybersec/publications/publication-files/full_papers/2012_08_05_Carter_IEEESSP_FP.pdf): Presents PEWMA as an improvement over EWMA.
