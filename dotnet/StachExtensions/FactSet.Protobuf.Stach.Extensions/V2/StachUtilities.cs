using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions.Models;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public static class StachUtilities
    {
        public static string ValueToString(Value value)
        {
            switch (value.KindCase)
            {
                case Value.KindOneofCase.BoolValue:
                    return value.BoolValue.ToString();
                
                case Value.KindOneofCase.ListValue:
                    return value.ListValue.ToString();
                
                case Value.KindOneofCase.NullValue:
                    return null;
                
                case Value.KindOneofCase.NumberValue:
                    return value.NumberValue.ToString();
                
                case Value.KindOneofCase.StringValue:
                    return value.StringValue;
                
                case Value.KindOneofCase.StructValue:
                    return JsonFormatter.Default.Format(value.StructValue);
                
                case Value.KindOneofCase.None:
                    return string.Empty;
                default:
                    return null;
            }
        }

        public static List<Value> CheckAddRowSpanItems(int position, int rowIndex,
            List<RowSpanSpread> rowSpanSpreadList)
        {
            var spreadValuesList = new List<Value>();

            if (rowSpanSpreadList.Count == 0)
            {
                return null;
            }

            var rowSpanSpreadItem = rowSpanSpreadList.FirstOrDefault(x => x.Position == position && rowIndex < x.RowSpan);

            if (rowSpanSpreadItem != null)
            {
                for (int i = 0; i < rowSpanSpreadItem.ColSpan; i++)
                {
                    spreadValuesList.Add(rowSpanSpreadItem.Value);
                    position++;
                }

                var recursiveSpreadValuesList =
                    StachUtilities.CheckAddRowSpanItems(position, rowIndex, rowSpanSpreadList);

                if (recursiveSpreadValuesList != null)
                {
                    spreadValuesList.AddRange(recursiveSpreadValuesList);
                }

                return spreadValuesList;
            }
            
            return null;
        }
    }
}