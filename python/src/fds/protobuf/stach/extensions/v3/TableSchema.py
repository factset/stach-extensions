class TableSchema:
    def __init__(self):
        self.Name = None
        self.Version = None
        self.Columns = []
        self.Rows = []
        self.ViewName = None
        self.ViewHeaders = {}
        self.ViewColumns = []
        self.GroupResult = {}
        self.SplitResult = {}
        self.PrimaryKeys = {}