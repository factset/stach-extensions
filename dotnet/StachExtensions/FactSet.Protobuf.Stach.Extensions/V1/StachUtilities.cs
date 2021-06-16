using System;
using FactSet.Protobuf.Stach.Table;

namespace FactSet.Protobuf.Stach.Extensions.V1
{
    public static class StachUtilities
    {
        /// <summary>
        /// The purpose of this function is to return the value from the provided SeriesData object.
        /// </summary>
        /// <exception cref="NotImplementedException">Thrown when datatype is not implemented</exception>
        /// <param name="seriesData"></param>
        /// <param name="dataType"></param>
        /// <param name="index"></param>
        /// <returns>Return data object from the SeriesData.</returns>
        public static object GetValueHelper(this SeriesData seriesData, DataType dataType, int index)
        {
            switch (dataType)
            {
                case DataType.Bool:
                    {
                        return seriesData.BoolArray?.Values?[index];
                    }
                case DataType.Double:
                    {
                        return seriesData.DoubleArray?.Values?[index];
                    }
                case DataType.Duration:
                    {
                        var v = seriesData.DurationArray?.Values?[index];
                        return v?.ToTimeSpan();
                    }
                case DataType.Float:
                    {
                        return seriesData.FloatArray?.Values?[index];
                    }
                case DataType.Int32:
                    {
                        return seriesData.Int32Array?.Values?[index];
                    }
                case DataType.Int64:
                    {
                        return seriesData.Int64Array?.Values?[index];
                    }
                case DataType.String:
                    {
                        return seriesData.StringArray?.Values?[index];
                    }
                case DataType.Timestamp:
                    {
                        var v = seriesData.TimestampArray?.Values?[index];
                        return v?.ToDateTime();
                    }
                default:
                    throw new NotImplementedException($"{dataType} is not implemented");
            }
        }
    }
}