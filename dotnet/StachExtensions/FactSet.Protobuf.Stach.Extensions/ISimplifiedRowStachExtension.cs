using System.Collections.Generic;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface ISimplifiedRowStachExtension : IStachExtension
    {
        /// <summary>
        /// Converts the RowOrganizedPackage of SimplifiedRow format to a list of dynamic objects.
        /// </summary>
        /// <returns>List of dynamic objects</returns>
        List<dynamic> ConvertToDynamicObjects();
        
        /// <summary>
        /// Converts the RowOrganizedPackage of SimplifiedRow format to a list of transposed dynamic objects.
        /// It concatenates groupings with a delimiter of pipe
        /// </summary>
        /// <returns>List of transposed dynamic objects</returns>
        List<dynamic> ConvertToTransposedDynamicObjects();
    }
}