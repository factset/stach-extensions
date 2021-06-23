using FactSet.Protobuf.Stach.Extensions.Models;

namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IColumnOrganizedStachBuilder<T>
    {
        
        IColumnOrganizedStachBuilder<T> SetPackage(T package);

        IColumnOrganizedStachBuilder<T> SetPackage(string package);

        IColumnOrganizedStachBuilder<T> SetPackage(object package);

        T GetPackage();

        IStachExtension Build();
    }
}