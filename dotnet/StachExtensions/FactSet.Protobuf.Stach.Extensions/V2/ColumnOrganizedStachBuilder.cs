using Google.Protobuf;
using Newtonsoft.Json;

namespace FactSet.Protobuf.Stach.Extensions.V2
{
    public class ColumnOrganizedStachBuilder : IColumnOrganizedStachBuilder<Stach.V2.Package>
    {
        private Stach.V2.Package package;
        
        public IColumnOrganizedStachBuilder<Stach.V2.Package> SetPackage(Stach.V2.Package package)
        {
            this.package = package;
            return this;
        }

        public IColumnOrganizedStachBuilder<Stach.V2.Package> SetPackage(string package)
        {
            var jpSettings = JsonParser.Settings.Default;
            var jp = new JsonParser(jpSettings.WithIgnoreUnknownFields(true));
            this.package = jp.Parse<Stach.V2.Package>(package);
            return this;
        }

        public IColumnOrganizedStachBuilder<Stach.V2.Package> SetPackage(object package)
        {
            var pkgString = JsonConvert.SerializeObject(package);
            return SetPackage(pkgString);
        }

        public Stach.V2.Package GetPackage()
        {
            return package;
        }

        public IStachExtension Build()
        {
            return new ColumnOrganizedStachExtension(package);
        }
    }
}