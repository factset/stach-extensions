using FactSet.Protobuf.Stach.V2;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IRowOrganizedStachBuilder
    {
        public IRowOrganizedStachBuilder SetPackage(RowOrganizedPackage package);

        public IRowOrganizedStachBuilder SetPackage(string package);

        public IRowOrganizedStachBuilder SetPackage(object package);

        public IRowOrganizedStachBuilder AddTable(string tableId, RowOrganizedPackage.Types.Table rowOrganizedTable);

        public IRowOrganizedStachBuilder AddTable(string tableId, string rowOrganizedTableString);

        public IRowOrganizedStachBuilder AddTable(string tableId, object rowOrganizedTableObject);

        public RowOrganizedPackage GetPackage();

        public IStachExtension Build();

    }
}