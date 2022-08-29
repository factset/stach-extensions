#' @docType class
#' @title V2ColumnOrganizedStachUtilities
#' @description Provide helper functions for column organized stach
#'
#' @importFrom R6 R6Class
#' @export
V2ColumnOrganizedStachUtilities <- R6::R6Class(
  "V2ColumnOrganizedStachUtilities",
  public = list(

    #' @description  Get Primary Table Ids
    #' @param package  Stach Data which is represented as a Package object
    #' @return Returns list of primary table ids
    GetPrimaryTableIds = function(package) {
      primaryTableIds = package[["primaryTableIds"]]

      return(primaryTableIds)
    },
    #' @description  Get decompress stach data
    #' @param package  Stach Data which is represented as a Package object
    #' @return Returns decompressed stach data
    DecompressAll = function(package) {
      ids <- self$GetPrimaryTableIds(package)

      for(id in ids) {
        package <- private$Decompress(package, id)
      }

      return(package)
    }
  ),
  private = list(
    Decompress = function(package, primaryTableId) {
      definitionColumns <- package[["tables"]][[primaryTableId]][["definition"]][["columns"]]
      dataColumns <- package[["tables"]][[primaryTableId]][["data"]][["columns"]]

      for (definitionColumn in definitionColumns) {
        column <- dataColumns[[definitionColumn[["id"]]]]
        if("ranges" %in% names(column)) {
          column <- private$DecompressColumn(column)
        }

        dataColumns[[definitionColumn[["id"]]]] <- column
      }

      package[["tables"]][[primaryTableId]][["data"]][["columns"]] <- dataColumns

      return(package)
    },
    DecompressColumn = function(column) {
      range <- column[["ranges"]]
      compressedValues <- column[["values"]]
      decompressedValues <- c()

      # Arrays start from element 1
      compressedIndex <- 1
      for(compressedValue in compressedValues) {
        currDecompressedIndex <- toString(length(decompressedValues))

        if(currDecompressedIndex %in% names(range)){
          rangeValue <- range[[currDecompressedIndex]]
          loopRange <- 1:rangeValue

          for(i in loopRange) {
            decompressedValues <- append(decompressedValues, compressedValues[compressedIndex])
          }

        } else {
          decompressedValues <- append(decompressedValues, compressedValues[compressedIndex])
        }

        compressedIndex <- compressedIndex + 1
      }

      column[["ranges"]] <- NULL
      column[["values"]] <- decompressedValues
      return(column)
    }
  )
)
