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

        string resourcesDir = args[0];
        string jsonPath = Path.Combine(resourcesDir, "V3JsonStachResponse.json");
        string arrowFilePath = Path.Combine(resourcesDir, "V3ArrowFileStachResponse.arrow");
        string arrowStreamPath = Path.Combine(resourcesDir, "V3ArrowStreamStachResponse.arrow");

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

    static void Check(string name, DataSet result)
    {
        if (result == null) throw new Exception($"{name}: result was null");
        if (result.Tables.Count < 1) throw new Exception($"{name}: no tables returned");

        DataTable dt = result.Tables[0];
        var cols = dt.Columns.Cast<DataColumn>().Select(c => c.ColumnName).Take(4).ToArray();
        Console.WriteLine($"\n[{name}] rows={dt.Rows.Count}, cols={dt.Columns.Count}, first cols=[{string.Join(", ", cols)}]");
    }
}
