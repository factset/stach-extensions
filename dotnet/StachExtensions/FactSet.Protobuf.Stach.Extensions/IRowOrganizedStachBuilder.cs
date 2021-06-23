using FactSet.Protobuf.Stach.V2;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IRowOrganizedStachBuilder
    {
        IRowOrganizedStachBuilder SetPackage(RowOrganizedPackage package);

        IRowOrganizedStachBuilder SetPackage(string package);

        IRowOrganizedStachBuilder SetPackage(object package);

        IRowOrganizedStachBuilder AddTable(string tableId, RowOrganizedPackage.Types.Table rowOrganizedTable);

        IRowOrganizedStachBuilder AddTable(string tableId, string rowOrganizedTableString);
        
        IRowOrganizedStachBuilder AddTable(string tableId, object rowOrganizedTableObject);

        RowOrganizedPackage GetPackage();

        IStachExtension Build();

    }
}