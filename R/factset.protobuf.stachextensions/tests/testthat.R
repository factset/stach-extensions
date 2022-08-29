library(testthat)
library(devtools)

# Load local library via path.
load_all(path="../../factset.protobuf.stachextensions")

test_check("factset.protobuf.stachextensions")
