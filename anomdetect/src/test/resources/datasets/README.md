# datasets

These are public datasets for machine learning.

## cal-inflow.csv
- **Source:** Dua, D. and Karra Taniskidou, E. (2017). UCI Machine Learning Repository [http://archive.ics.uci.edu/ml]. Irvine, CA: University of California, School of Information and Computer Science.
- **Dataset:** https://archive.ics.uci.edu/ml/datasets/CalIt2+Building+People+Counts
- **Notes:** The original `CalIt2.data` file contains both the inflows and outflows, but here we do only inflows. Note that the original `CalIt2.data` data file contains two extra CRs that need to be removed.

## austourists.csv
- **Source:** 'fpp2' forecasting package - Rob Hyndman
- **Dataset:** Generated (time-series data converted to CSV) by ../scripts/GenerateAustouristsDataset.R
- **Notes:** 
  - Raw RDA format can be accessed here: https://github.com/robjhyndman/fpp2-package/blob/ffdb9be212be39ffb040bfb76472a3d70699a2e0/data/austourists.rda?raw=true
  - Code used to generate 'austourists' object is here: https://github.com/robjhyndman/fpp2-package/blob/3a1b96cd3e6b68ad5a34480f14998af7900325c0/data-raw/updatefppdata.R#L68-L79
  - Original Excel: https://github.com/robjhyndman/fpp2-package/blob/master/data-raw/austouristsUpdated2016.xlsx?raw=true

## white-noise-with-breakout-1.csv
- **Source:** Expedia - Adaptive Alerting
- **Notes:**
  - We synthesized this dataset to test the EDMX breakout detector.
  - The series is Gaussian white noise (m=0, sd=1), but we've mean-shifted the series by 2 starting from row 600.
