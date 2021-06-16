using System;
using FactSet.Protobuf.Stach.Extensions.Models;
using FactSet.Protobuf.Stach.Extensions.V2;
using ColumnOrganizedStachBuilder = FactSet.Protobuf.Stach.Extensions.V1.ColumnOrganizedStachBuilder;

namespace FactSet.Protobuf.Stach.Extensions
{
    
    public static class StachExtensionFactory
    {

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

        public static IRowOrganizedStachBuilder GetRowOrganizedBuilder()
        {
            return new RowOrganizedStachBuilder();
        }
    }

}