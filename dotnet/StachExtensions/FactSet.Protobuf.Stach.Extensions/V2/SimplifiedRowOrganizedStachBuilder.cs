using FactSet.Protobuf.Stach.V2;
using Google.Protobuf;
using Newtonsoft.Json;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class SimplifiedRowOrganizedStachBuilder : ISimplifiedRowOrganizedStachBuilder
    {
        private RowOrganizedPackage package;

        public ISimplifiedRowOrganizedStachBuilder SetPackage(RowOrganizedPackage package)
        {
            this.package = package;
            return this;
        }

        public ISimplifiedRowOrganizedStachBuilder SetPackage(string package)
        {
            var jpSettings = JsonParser.Settings.Default;
            var jp = new JsonParser(jpSettings.WithIgnoreUnknownFields(true));
            this.package = jp.Parse<RowOrganizedPackage>(package);
            return this;
        }

        public ISimplifiedRowOrganizedStachBuilder SetPackage(object package)
        {
            var pkgString = JsonConvert.SerializeObject(package);
            return SetPackage(pkgString);
        }

        public ISimplifiedRowOrganizedStachBuilder AddTable(string tableId, RowOrganizedPackage.Types.Table simplifiedRowOrganizedTable)
        {
            if (package == null)
            {
                package = new RowOrganizedPackage();
            }

            package.Tables.Add(tableId, simplifiedRowOrganizedTable);
            return this;
        }

        public ISimplifiedRowOrganizedStachBuilder AddTable(string tableId, string simplifiedRowOrganizedTableString)
        {
            var jpSettings = JsonParser.Settings.Default;
            var jp = new JsonParser(jpSettings.WithIgnoreUnknownFields(true));
            var table = jp.Parse<RowOrganizedPackage.Types.Table>(simplifiedRowOrganizedTableString);
            if (package == null)
            {
                package = new RowOrganizedPackage();
            }

            package.Tables.Add(tableId, table);
            return this;
        }

        public ISimplifiedRowOrganizedStachBuilder AddTable(string tableId, object simplifiedRowOrganizedTableObject)
        {
            var tableString = JsonConvert.SerializeObject(simplifiedRowOrganizedTableObject);
            return AddTable(tableId, tableString);
        }

        public RowOrganizedPackage GetPackage()
        {
            return package;
        }

        public ISimplifiedRowStachExtension Build()
        {
            return new SimplifiedRowOrganizedStachExtension(package);
        }
    }
}