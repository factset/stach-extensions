<img alt="FactSet" src="https://www.factset.com/hubfs/Assets/images/factset-logo.svg" height="56" width="290">

# stach-extensions

![build](https://img.shields.io/badge/Build-Todo-blue)
[![Maven](https://img.shields.io/maven-central/v/com.factset.protobuf/stachextensions)](https://mvnrepository.com/artifact/com.factset.protobuf/stachextensions)
[![PyPi](https://img.shields.io/pypi/v/fds.protobuf.stach.extensions)](https://pypi.org/project/fds.protobuf.stach.extensions/)
[![Apache-2 license](https://img.shields.io/badge/license-Apache2-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)


This repository contains extension libraries in different languages for parsing the [stach](https://factset.github.io/stachschema/#/README) format to more simpler to consume formats or data structures.

As of now the languages supported are Java and Python. The source code for the supported languages is organized in the respective language folders in the root directory

# Installation
 
## python
    
    pip install fds.protobuf.stach.extensions

## java
Add the below dependency to the project
  ```xml
  <dependency>
    <groupId>com.factset.protobuf</groupId>
    <artifactId>stachextensions</artifactId>
    <version>ARTIFACT_VERSION</version>
  </dependency>
  ```

# Usage

There are methods for converting the stach format to the tabular formats in the respective stach extensions classes as shown below and also refer to the tests folder inside each language for detailed usage

## python

``` python
# Stach v2 Row Organized format
stachBuilder = StachExtensionFactory.get_row_organized_builder(StachVersion.V2)
stachExtension = stachBuilder.set_package(data).build()  # data is the stach input in string or object format
dataFramesList = stachExtension.convert_to_dataframe()


# Stach v2 Column Organized format
stachBuilder = StachExtensionFactory.get_column_organized_builder(StachVersion.V2)
stachExtension = stachBuilder.set_package(data).build()  # data is the stach input in string or object format
dataFramesList = stachExtension.convert_to_dataframe()


# Stach v1 Column Organized format
stachBuilder = StachExtensionFactory.get_column_organized_builder(StachVersion.V1)
stachExtension = stachBuilder.set_package(data).build()  # data is the stach input in string or object format
dataFramesList = stachExtension.convert_to_dataframe()

```

## java
``` java

// Stach v2 Row Organized format
ColumnStachExtensionBuilder stachExtensionBuilder = StachExtensionFactory.getRowOrganizedBuilder(StachVersion.V2);
StachExtensions stachExtension = stachExtensionBuilder.setPackage(data).build();  // data is the stach input in string or object format
List<TableData> tableDataList = stachExtension.convertToTable();


// Stach v2 Column Organized format
ColumnStachExtensionBuilder stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V2);
StachExtensions stachExtension = stachExtensionBuilder.setPackage(data).build();  // data is the stach input in string or object format
List<TableData> tableDataList = stachExtension.convertToTable();


// Stach v1 Column Organized format
ColumnStachExtensionBuilder stachExtensionBuilder = StachExtensionFactory.getColumnOrganizedBuilder(StachVersion.V1);
StachExtensions stachExtension = stachExtensionBuilder.setPackage(data).build(); // data is the stach input in string or object format
List<TableData> tableDataList = stachExtension.convertToTable();

```

# Contributing

Please refer to the [CONTRIBUTING](CONTRIBUTING.md) file
 

# Copyright

Copyright 2021 FactSet Research Systems Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
