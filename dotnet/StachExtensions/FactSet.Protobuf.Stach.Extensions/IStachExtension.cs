using System.Collections.Generic;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IStachExtension
    {
        /// <summary>
        /// Converts the Package/RowOrganizedPackage to list of Table objects.
        /// </summary>
        /// <returns>The list of converted Table objects. The Table contains data (as list of Row objects) and metadata.</returns>
        List<Models.Table> ConvertToTable();
    }
}