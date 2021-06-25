using FactSet.Protobuf.Stach.Extensions.V2;
using ColumnOrganizedStachBuilder = FactSet.Protobuf.Stach.Extensions.V1.ColumnOrganizedStachBuilder;

namespace FactSet.Protobuf.Stach.Extensions
{
    
    public static class StachExtensionFactory
    {

        /// <summary>
        /// Get the column stach builder based on stach version.
        /// </summary>
        /// <typeparam name="T">can be Package or Stach.V2.Package depending on stach version</typeparam>
        /// <returns>ColumnOrganizedStachBuilder</returns>
        public static IColumnOrganizedStachBuilder<T> GetColumnOrganizedBuilder<T>()
        {
            if (typeof(T) == typeof(Package))
            {
                return (IColumnOrganizedStachBuilder<T>)new ColumnOrganizedStachBuilder();
            }
            if (typeof(T) == typeof(Stach.V2.Package))
            {
                return (IColumnOrganizedStachBuilder<T>)new V2.ColumnOrganizedStachBuilder();
            }

            return null;
        }

        /// <summary>
        /// Get the RowOrganizedStachBuilder instance.
        /// </summary>
        /// <returns>RowOrganizedStachBuilder</returns>
        public static IRowOrganizedStachBuilder GetRowOrganizedBuilder()
        {
            return new RowOrganizedStachBuilder();
        }
    }

}