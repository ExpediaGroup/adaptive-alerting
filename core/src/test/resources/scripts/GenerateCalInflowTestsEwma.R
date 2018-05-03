# Adapted from http://wilsonfreitas.github.io/posts/computing-ewma-2.html
# based on standard EWMA approach.
ewma.filter <- function(alpha, values) {
  filter(alpha * values, 1 - alpha, "recursive", sides = 1, init = values[1])
}

# My far less elegant implementation, but one that generates the running variance.
# I include the known.mean as a sanity check.
build.tests <- function(alpha, small.mult, large.mult, data, known.mean) {
  n <- nrow(data)

  date <- data$date
  observed <- data$observed
  mean <- vector(mode = "double", length = n)
  var <- vector(mode = "double", length = n)

  mean[1] <- observed[1]
  var[1] <- 0.0

  for (i in 2:n) {
    diff <- data$observed[i] - mean[i - 1]
    incr <- alpha * diff
    mean[i] <- mean[i - 1] + incr
    
    # Welford's algo
    var[i] <- (1 - alpha) * (var[i - 1] + diff * incr)
  }
  
  return(data.frame(date, observed, mean, known.mean, var))
}

# Read data
data <- read.table("../datasets/cal-inflow.csv", header = TRUE, sep = ",")
date <- data$date
observed <- data$observed

# Model params
alpha <- 0.05
small.mult <- 2
large.mult <- 3

# Tests
known.mean <- ewma.filter(alpha, data$observed)
tests <- build.tests(alpha, small.mult, large.mult, data, known.mean)

# Plot
plot(data$observed[1:480], type="l", col="blue")
lines(known.mean[1:480], col="black")

# Dump tests
write.table(tests, "../tests/cal-inflow-tests-emwa.csv", sep = ",", row.names = FALSE)
