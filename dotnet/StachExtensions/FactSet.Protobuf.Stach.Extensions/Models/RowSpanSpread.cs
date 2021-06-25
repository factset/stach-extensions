using Google.Protobuf.WellKnownTypes;

namespace FactSet.Protobuf.Stach.Extensions.Models
{
    public class RowSpanSpread
    {
        public RowSpanSpread(int position, int rowSpan, int colSpan, Value value)
        {
            Position = position;
            RowSpan = rowSpan;
            ColSpan = colSpan;
            Value = value;
        }
        
        /// <summary>
        /// Position at which the value needs to be added.
        /// </summary>
        public int Position { get; set; }
        
        /// <summary>
        /// The number of rows across which the value has to be spread.
        /// </summary>
        public int RowSpan { get; set; }
        
        /// <summary>
        /// The number of columns across which the value has to be spread.
        /// </summary>
        public int ColSpan { get; set; }
        
        /// <summary>
        /// The value that is to be spread.
        /// </summary>
        public Value Value { get; set; }
    }
}