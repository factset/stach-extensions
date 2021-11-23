using FactSet.Protobuf.Stach.V2;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface ISimplifiedRowOrganizedStachBuilder
    {
        /// <summary>
        /// Builds and returns the Simplified Row Organized Stach Extension instance.
        /// </summary>
        /// <returns>SimplifiedRowOrganizedStachExtension instance</returns>
        ISimplifiedRowStachExtension Build();

        /// <summary>
        /// Sets the RowOrganizedPackage.
        /// </summary>
        /// <param name="package">RowOrganizedPackage object</param>
        /// <returns>builder instance</returns>
        ISimplifiedRowOrganizedStachBuilder SetPackage(RowOrganizedPackage package);

        /// <summary>
        /// Sets the RowOrganizedPackage by parsing the raw object input.
        /// </summary>
        /// <param name="package">RowOrganizedPackage object</param>
        /// <returns>builder instance</returns>
        ISimplifiedRowOrganizedStachBuilder SetPackage(object package);

        /// <summary>
        /// Sets the RowOrganizedPackage by parsing the string input.
        /// </summary>
        /// <param name="package">string form of RowOrganizedPackage object</param>
        /// <returns>builder instance</returns>
        ISimplifiedRowOrganizedStachBuilder SetPackage(string package);

        /// <summary>
        /// Add the Simplified Row Organized format's Table to the package in the builder.
        /// </summary>
        /// <param name="tableId">id of the table.</param>
        /// <param name="simplifiedRowOrganizedTable">SimplifiedRowOrganized Table</param>
        /// <returns>builder instance</returns>
        ISimplifiedRowOrganizedStachBuilder AddTable(string tableId, RowOrganizedPackage.Types.Table simplifiedRowOrganizedTable);

        /// <summary>
        /// Add the Simplified Row Organized format's Table to the package in the builder.
        /// </summary>
        /// <param name="tableId">id of the table.</param>
        /// <param name="simplifiedRowOrganizedTableString">SimplifiedRowOrganized Table in string format</param>
        /// <returns>builder instance</returns>
        ISimplifiedRowOrganizedStachBuilder AddTable(string tableId, string simplifiedRowOrganizedTableString);

        /// <summary>
        /// Add the Simplified Row Organized format's Table to the package in the builder.
        /// </summary>
        /// <param name="tableId">id of the table.</param>
        /// <param name="simplifiedRowOrganizedTableObject">SimplifiedRowOrganized Table object</param>
        /// <returns>builder instance</returns>
        ISimplifiedRowOrganizedStachBuilder AddTable(string tableId, object simplifiedRowOrganizedTableObject);

        /// <summary>
        /// Get the RowOrganizedPackage set for the builder.
        /// </summary>
        /// <returns>RowOrganizedPackage</returns>
        RowOrganizedPackage GetPackage();

    }
}