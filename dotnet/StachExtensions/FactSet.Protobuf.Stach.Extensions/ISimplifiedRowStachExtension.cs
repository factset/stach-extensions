using System.Collections.Generic;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface ISimplifiedRowStachExtension : IStachExtension
    {
        List<dynamic> ConvertToDynamicObject();

        List<dynamic> ConvertToTransposedDynamicObject();

        string GetStringifiedCsv(List<dynamic> outputObject);
    }
}