using FactSet.Protobuf.Stach.V2;
using Google.Protobuf;
using Newtonsoft.Json;
using JsonConverter = System.Text.Json.Serialization.JsonConverter;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class RowOrganizedStachBuilder : IRowOrganizedStachBuilder
    {
        private RowOrganizedPackage package;

        public IRowOrganizedStachBuilder SetPackage(RowOrganizedPackage package)
        {
            this.package = package;
            return this;
        }

        public IRowOrganizedStachBuilder SetPackage(string package)
        {
            var jpSettings = JsonParser.Settings.Default;
            var jp = new JsonParser(jpSettings.WithIgnoreUnknownFields(true));
            this.package = jp.Parse<RowOrganizedPackage>(package);
            return this;
        }

        public IRowOrganizedStachBuilder SetPackage(object package)
        {
            var pkgString = JsonConvert.SerializeObject(package);
            return SetPackage(pkgString);
        }

        public IRowOrganizedStachBuilder AddTable(string tableId, RowOrganizedPackage.Types.Table rowOrganizedTable)
        {
            if (package == null)
            {
                package = new RowOrganizedPackage();
            }

            package.Tables.Add(tableId, rowOrganizedTable);
            return this;
        }

        public IRowOrganizedStachBuilder AddTable(string tableId, string rowOrganizedTableString)
        {
            var jpSettings = JsonParser.Settings.Default;
            var jp = new JsonParser(jpSettings.WithIgnoreUnknownFields(true));
            var table = jp.Parse<RowOrganizedPackage.Types.Table>(rowOrganizedTableString);
            if (package == null)
            {
                package = new RowOrganizedPackage();
            }

            package.Tables.Add(tableId, table);
            return this;
        }

        public IRowOrganizedStachBuilder AddTable(string tableId, object rowOrganizedTableObject)
        {
            var tableString = JsonConvert.SerializeObject(rowOrganizedTableObject);
            return AddTable(tableId, tableString);
        }

        public RowOrganizedPackage GetPackage()
        {
            return package;
        }

        public IStachExtension Build()
        {
           return new RowOrganizedStachExtension(package);
        }
    }
}