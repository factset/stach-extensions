# Contributing

## Getting Started

Fork the repository to your own account and then clone the repository to a suitable location on your local machine.

```bash
git clone [YOUR FORK OF THE PROJECT]
```

To update the project from within the project's folder you can run the following command:

```bash
git pull --rebase
```

### Building

### Java
```bash
mvn clean package
```

### python
```bash
python setup.py install
```

### Testing

To run the project's tests run the following command:

### Java
```bash
mvn test
```

### python
```bash
python -m unittest
```

### R

Make sure Rscript is in your path to run the commands in command line.

Running tests.

```
cd R/factset/protobuf.stachextensions/tests
Rscript testthat.R
```

Running the automatic documentation tool

```
Rscript build.R
```

## Feature Requests

We're always looking for suggestions to improve this project. If you have a suggestion for improving an existing feature, or would like to suggest a completely new feature, please file an issue with our [GitHub repository](https://github.com/factset/stach-extensions/issues).

## Bug Reports

Our project isn't always perfect, but we strive to always improve on that work. You may file bug reports on the [GitHub repository](https://github.com/factset/stach-extensions/issues) site.

## Pull Requests

Along with our desire to hear your feedback and suggestions, we're also interested in accepting direct assistance in the form of new code or documentation.

Please feel free to file pull requests against our [GitHub repository](https://github.com/factset/stach-extensions/pulls).