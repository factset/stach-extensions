using Google.Protobuf;
using Newtonsoft.Json;

namespace FactSet.Protobuf.Stach.Extensions.V1
{
    public class ColumnOrganizedStachBuilder : IColumnOrganizedStachBuilder<Package>

    {
        private Package package;
        
        public IColumnOrganizedStachBuilder<Package> SetPackage(Package package)
        {
            this.package = package;
            return this;
        }

        public IColumnOrganizedStachBuilder<Package> SetPackage(string package)
        {
            var jpSettings = JsonParser.Settings.Default;
            var jp = new JsonParser(jpSettings.WithIgnoreUnknownFields(true));
            this.package = jp.Parse<Package>(package);
            return this;
        }

        public IColumnOrganizedStachBuilder<Package> SetPackage(object package)
        {
            var pkgString = JsonConvert.SerializeObject(package);
            return SetPackage(pkgString);
        }

        public Package GetPackage()
        {
            return package;
        }

        public IStachExtension Build()
        {
            return new ColumnOrganizedStachExtension(package);
        }
    }
}