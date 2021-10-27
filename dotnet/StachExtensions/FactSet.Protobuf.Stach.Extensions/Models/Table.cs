using System;
using System.Collections.Generic;
using System.Linq;

namespace FactSet.Protobuf.Stach.Extensions.Models
{
    /// <summary>
    /// Represents the custom model class for Tables to be generated from stach data.
    /// </summary>
    public class Table
    {
        /// <summary>
        /// Gets or sets the Row object.
        /// </summary>
        public List<Row> Rows { get; set; }

        /// <summary>
        /// Metadata of the table.
        /// </summary>
        public Dictionary<string, string> Metadata { get; set; }

           /// <summary>
        /// Raw metadata of the table.
        /// </summary>
        public Dictionary<string, List<Google.Protobuf.WellKnownTypes.Value>> RawMetadata { get; set; }

        /// <summary>
        /// The purpose of this function is to concatenate member of Row array with specified separator between each member i.e a newline.
        /// </summary>
        /// <returns>string</returns>
        public override string ToString()
        {
            return string.Join(Environment.NewLine, Rows);
        }
    }
    
    /// <summary>
    /// Represents the custom model class for the Rows inside the generated tables.
    /// </summary>
    public class Row
    {
        /// <summary>
        /// Gets or sets the Cells object.
        /// </summary>
        public List<string> Cells { get; set; }

        /// <summary>
        /// To know whether a row is a header row.
        /// </summary>
        public bool isHeader { get; set; }

        /// <summary>
        /// The purpose of this function is to concatenate member of Cell array with specified separator between each member i.e ",".
        /// </summary>
        /// <returns>string</returns>
        public override string ToString()
        {
            return string.Join(",", Cells.Select(c => c.Replace(",", "")));
        }
    }

}