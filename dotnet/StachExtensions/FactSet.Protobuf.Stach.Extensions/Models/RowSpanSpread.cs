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
        
        public int Position { get; set; }
        
        public int RowSpan { get; set; }
        
        public int ColSpan { get; set; }
        
        public Value Value { get; set; }
    }
}