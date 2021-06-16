using FactSet.Protobuf.Stach.Extensions.Models;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IColumnOrganizedStachBuilder<T>
    {
        
        public IColumnOrganizedStachBuilder<T> SetPackage(T package);

        public IColumnOrganizedStachBuilder<T> SetPackage(string package);

        public IColumnOrganizedStachBuilder<T> SetPackage(object package);

        public T GetPackage();

        public IStachExtension Build();
    }
}