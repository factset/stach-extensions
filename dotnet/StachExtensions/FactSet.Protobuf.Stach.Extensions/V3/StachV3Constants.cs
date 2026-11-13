using System.Collections.Generic;

namespace FactSet.Protobuf.Stach.Extensions.V3
{
    internal static class StachV3Constants
    {
        internal const string ColumnSeparator = " | ";
        internal const string GroupPrefix = "group";
        internal const string LevelNameColumn = "level";
        internal const string SecurityNameColumn = "security";
        internal static readonly HashSet<string> IgnoredColumns = new HashSet<string>
        {
            "primary_key",
            "row_path",
            "level",
            "aggregate_rows",
            //"Security Name",
            "pk", 
            "rp", 
            "lvl", 
            "ag"
        };
    }
}
