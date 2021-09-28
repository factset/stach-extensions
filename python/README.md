# Introduction

The folders "src" contains the stach extensiosn python package source code and the "tests" folder contain the test files and resources.


For manual install and testing from the source code follow the below instructions

## Installation

``` python
cd src
python setup.py sdist
```
Copy and paste `fds.protobuf.stach.extensions-1.0.1.tar` from `src/dist` to `tests`
``` python
cd ../tests
```
Create a virtual environment
``` python
pip install -r requirements.txt
pip install fds.protobuf.stach.extensions-1.0.1.tar
```

## Testing
Install the package manually following the above steps

Navigate to the "tests" folder and run the below command

```python
python -m unittest discover -p "test_*.py"
```
#### Or

Follow [these](https://code.visualstudio.com/docs/python/testing#_configure-tests) steps to run tests within VSCode