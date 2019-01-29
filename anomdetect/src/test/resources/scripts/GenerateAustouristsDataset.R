library(fpp2)
library(tsibble)
library(readr)

# Convert time series data to csv (from: https://robjhyndman.com/hyndsight/ts2csv/)
ts2csv <- function(ts_data) {
  return(tsibble::as_tsibble(ts_data, gather = FALSE))
}

# austourists = Quarterly visitor nights (in millions) spent by international tourists to Australia. 1999-2015.
csv_text <- ts2csv(austourists)
readr::write_csv(csv_text, "../datasets/austourists.csv")
