from google.protobuf import json_format
from google.protobuf.struct_pb2 import Struct

class StachUtilities:
    @staticmethod
    def get_value(val):
        if(type(val) is Struct):
            return str(json_format.MessageToDict(val))
        else:
            return val

