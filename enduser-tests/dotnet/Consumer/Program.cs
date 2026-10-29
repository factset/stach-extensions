using System;
using System.Data;
using System.IO;
using System.Linq;
using FactSet.Protobuf.Stach.Extensions;

// End-user consumer for the .NET SDK.
// References the package by id+version from a LOCAL NuGet feed (see run_enduser_test.ps1),
// exactly as a downstream consumer would after a real `nuget push`.

class Program
{
    static int Main(string[] args)
    {
        if (args.Length < 1)
        {
            Console.Error.WriteLine("Usage: Consumer <resources_dir>");
            return 2;
        }

        // Resolve the CLI argument to a canonical directory and require it to exist, so every
        // resource path below is built from a validated base rather than raw user input.
        string resourcesDir = Path.GetFullPath(args[0]);
        if (!Directory.Exists(resourcesDir))
        {
            Console.Error.WriteLine($"Resources directory not found: {resourcesDir}");
            return 2;
        }

        string jsonPath = ResourceFile(resourcesDir, "V3JsonStachResponse.json");
        string arrowFilePath = ResourceFile(resourcesDir, "V3ArrowFileStachResponse.arrow");
        string arrowStreamPath = ResourceFile(resourcesDir, "V3ArrowStreamStachResponse.arrow");

        // Prove the type comes from the installed NuGet package, not a project reference.
        string asmLocation = typeof(StachV3ExtensionFactory).Assembly.Location;
        Console.WriteLine($"Loaded assembly from: {asmLocation}");

        // 1) ConvertJsonToTable (accepts JSON content string)
        string jsonContent = File.ReadAllText(jsonPath);
        Check("ConvertJsonToTable", StachV3ExtensionFactory.ConvertJsonToTable(jsonContent));

        // 2) ConvertArrowFileToTable
        Check("ConvertArrowFileToTable", StachV3ExtensionFactory.ConvertArrowFileToTable(arrowFilePath));

        // 3) ConvertArrowStreamToTable
        byte[] arrowBytes = File.ReadAllBytes(arrowStreamPath);
        Check("ConvertArrowStreamToTable", StachV3ExtensionFactory.ConvertArrowStreamToTable(arrowBytes));

        Console.WriteLine("\nEND-USER TEST PASSED");
        return 0;
    }

    // Only plain file names are accepted, and the final path must stay inside the resources directory.
    static string ResourceFile(string resourcesDir, string fileName)
    {
        string path = Path.GetFullPath(Path.Combine(resourcesDir, fileName));
        string prefix = resourcesDir.TrimEnd(Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar) + Path.DirectorySeparatorChar;
        if (!path.StartsWith(prefix, StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException($"Resource path escapes the resources directory: {fileName}");
        return path;
    }

    static void Check(string name, DataSet result)
    {
        if (result == null) throw new Exception($"{name}: result was null");
        if (result.Tables.Count < 1) throw new Exception($"{name}: no tables returned");

        DataTable dt = result.Tables[0];
        var cols = dt.Columns.Cast<DataColumn>().Select(c => c.ColumnName).Take(4).ToArray();
        Console.WriteLine($"\n[{name}] rows={dt.Rows.Count}, cols={dt.Columns.Count}, first cols=[{string.Join(", ", cols)}]");
    }
}
