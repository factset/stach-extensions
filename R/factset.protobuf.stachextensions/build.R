install.packages("roxygen2", repos = "http://cran.us.r-project.org")

# Updating the package with documentation via roxygen2
library(roxygen2)
roxygen2::roxygenise()
