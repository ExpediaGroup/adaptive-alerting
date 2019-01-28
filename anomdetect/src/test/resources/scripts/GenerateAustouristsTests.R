library(fpp2)
library(forecast)
source("./HoltWintersNew.R")
source("./ets.R")

# Read data
data <- read.table("../datasets/austourists.csv", header = TRUE, sep = ",")
m = 4
tsdata = ts(data$value, frequency = m, end = c(2015, 4))

# Model params - taken from https://otexts.com/fpp2/holt-winters.html
alpha <- 0.441
beta <- 0.030
gamma <- 0.002

for (stype in c("multiplicative", "additive")) {
  res <- hw(tsdata, seasonal=stype, initial="simple", alpha=alpha, beta=beta, gamma=gamma)
  model.states <- res$model$states
  
  y <- ts(c(c(NaN), tsdata), frequency = m, end = c(2015, 4))
  y.hat <- c(0, res$fitted)
  df = data.frame(y, y.hat, model.states)
  fname = paste("../tests/austourists-tests-holtwinters-", stype, ".csv", sep = "")
  print(fname)
  write.csv(df, file = fname, row.names=FALSE)
}
