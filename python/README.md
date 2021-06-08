# Introduction

The folders "src" contains the stach extensiosn python package source code and the "tests" folder contain the test files and resources.


For manual install and testing from the source code follow the below instructions

## Installation

Navigate into the "src" folder and run the below commands

``` python
python setup.py sdist
pip install .
```

## Testing
Install the package manually following the above steps

Navigate to the "tests" folder and run the below command

```python
python -m unittest discover -p "test_*.py"
```