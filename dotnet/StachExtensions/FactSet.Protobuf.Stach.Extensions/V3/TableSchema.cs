using System.Collections.Generic;

namespace FactSet.Protobuf.Stach.Extensions.V3
{
    internal class TableSchema
    {
        public string Name;
        public string Version;
        public List<Dictionary<string, object>> Columns;
        public List<Dictionary<string, string>> Rows;
        public string ViewName;
        public Dictionary<string, string> ViewHeaders;
        public List<string> ViewColumns;
        public Dictionary<string, object> GroupResult;
        public Dictionary<string, object> SplitResult;
        public Dictionary<string, object> PrimaryKeys;
    }
}
