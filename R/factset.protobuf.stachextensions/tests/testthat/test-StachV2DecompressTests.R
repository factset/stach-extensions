
source("../test_constants.R")
library(rjson)

testthat::test_that("column organized utilities, get primary table ids",
{
  filePath = './../../inst/testdata/V2ColumnOrganizedCompressed.json'
  compressed <- rjson::fromJSON(file=filePath, simplify=TRUE)

  ids <- factset.protobuf.stachextensions::V2ColumnOrganizedStachUtilities$
    public_methods$
    GetPrimaryTableIds(compressed)

  expect_equal(length(ids), 1, info= "Length of ids should be 1")
  expect_equal(ids[1], "a649ec50-7e58-443d-b791-1340e9eebf24", info = "Primary table id should be a649ec50-7e58-443d-b791-1340e9eebf24")
})

testthat::test_that("column organized utilities, decompress",
{
  filePath = './../../inst/testdata/V2ColumnOrganizedCompressed.json'
  package <- rjson::fromJSON(file=filePath, simplify=TRUE)

  utility <- factset.protobuf.stachextensions::V2ColumnOrganizedStachUtilities$new()

  package <- utility$DecompressAll(package)

  primaryTableId <- "a649ec50-7e58-443d-b791-1340e9eebf24"
  actualData <- package[["tables"]][[primaryTableId]][["data"]][["columns"]][["1"]][["values"]]
  
  expectedData <- c(list(NULL, NULL, NULL), "Americas",
            "Asia Pacific", "Europe", "Middle East and Africa",
            list(NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL))

  expect_equal(length(actualData), length(expectedData))

  range <- 1:length(actualData)+1
  for(i in range) {
    expect_equal(actualData[i], expectedData[i])
  }
})
