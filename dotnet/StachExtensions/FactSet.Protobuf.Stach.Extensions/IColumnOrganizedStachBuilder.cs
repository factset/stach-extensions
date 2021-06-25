namespace FactSet.Protobuf.Stach.Extensions
{
    public interface IColumnOrganizedStachBuilder<T>
    {
        /// <summary>
        ///  Sets the package object.
        /// </summary>
        /// <param name="package">package object</param>
        /// <returns>builder instance</returns>
        IColumnOrganizedStachBuilder<T> SetPackage(T package);

        /// <summary>
        ///  Sets the package object by parsing the input in string format.
        /// </summary>
        /// <param name="package">string form of package object</param>
        /// <returns>builder instance</returns>
        IColumnOrganizedStachBuilder<T> SetPackage(string package);

        /// <summary>
        /// Sets the package object by parsing the input object.
        /// </summary>
        /// <param name="package">package object</param>
        /// <returns>builder instance</returns>
        IColumnOrganizedStachBuilder<T> SetPackage(object package);

        /// <summary>
        /// Get the package object set for the builder.
        /// </summary>
        /// <returns>Package instance</returns>
        T GetPackage();

        /// <summary>
        /// Builds and returns the stach extensions instance.
        /// </summary>
        /// <returns>instance of column organized stach extensions.</returns>
        IStachExtension Build();
    }
}