using System.Collections.Generic;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IStachExtension
    {
        List<Models.Table> ConvertToTable();
    }
}