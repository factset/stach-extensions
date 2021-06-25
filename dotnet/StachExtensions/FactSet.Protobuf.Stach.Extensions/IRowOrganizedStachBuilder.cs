using FactSet.Protobuf.Stach.V2;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IRowOrganizedStachBuilder
    {
        /// <summary>
        /// Sets the RowOrganizedPackage.
        /// </summary>
        /// <param name="package">RowOrganizedPackage object</param>
        /// <returns>builder instance</returns>
        IRowOrganizedStachBuilder SetPackage(RowOrganizedPackage package);

        /// <summary>
        /// Sets the RowOrganizedPackage by parsing the string input.
        /// </summary>
        /// <param name="package">string form of RowOrganizedPackage object</param>
        /// <returns>builder instance</returns>
        IRowOrganizedStachBuilder SetPackage(string package);

        /// <summary>
        /// Sets the RowOrganizedPackage by parsing the raw object input.
        /// </summary>
        /// <param name="package">RowOrganizedPackage object</param>
        /// <returns>builder instance</returns>
        IRowOrganizedStachBuilder SetPackage(object package);

        /// <summary>
        /// Add the RowOrganized Table to the package in the builder.
        /// </summary>
        /// <param name="tableId">id of the table.</param>
        /// <param name="rowOrganizedTable">RowOrganized Table</param>
        /// <returns>builder instance</returns>
        IRowOrganizedStachBuilder AddTable(string tableId, RowOrganizedPackage.Types.Table rowOrganizedTable);

        /// <summary>
        /// Add the RowOrganized Table to the package in the builder.
        /// </summary>
        /// <param name="tableId">id of the table.</param>
        /// <param name="rowOrganizedTableString">RowOrganized Table in string format</param>
        /// <returns>builder instance</returns>
        IRowOrganizedStachBuilder AddTable(string tableId, string rowOrganizedTableString);
        
        /// <summary>
        /// Add the RowOrganized Table to the package in the builder.
        /// </summary>
        /// <param name="tableId">id of the table.</param>
        /// <param name="rowOrganizedTableObject">RowOrganized Table object</param>
        /// <returns>builder instance</returns>
        IRowOrganizedStachBuilder AddTable(string tableId, object rowOrganizedTableObject);

        /// <summary>
        /// Get the RowOrganizedPackage set for the builder.
        /// </summary>
        /// <returns>RowOrganizedPackage</returns>
        RowOrganizedPackage GetPackage();

        /// <summary>
        /// Builds and returns the Row Organized Stach Extension instance.
        /// </summary>
        /// <returns>RowOrganizedStachExtension instance</returns>
        IStachExtension Build();

    }
}