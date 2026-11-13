import com.factset.protobuf.stach.extensions.StachV3ExtensionsFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * End-user consumer for the Java SDK.
 * Resolves the dependency from the local ~/.m2 repository by GAV, exactly as a downstream
 * user would after a real `mvn deploy` to Maven Central.
 *
 * Usage: EndUserTest <resources_dir>
 */
public class EndUserTest {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: EndUserTest <resources_dir>");
            System.exit(2);
        }
        String resourcesDir = args[0];

        // Prove the class is loaded from the installed jar, not a local module.
        String src = StachV3ExtensionsFactory.class
                .getProtectionDomain().getCodeSource().getLocation().toString();
        System.out.println("Loaded factory from: " + src);

        StachV3ExtensionsFactory factory = new StachV3ExtensionsFactory();

        // 1) ConvertJsonToTable (accepts JSON content string)
        String jsonContent = new String(Files.readAllBytes(
                Paths.get(resourcesDir, "V3JsonStachResponse.json")));
        check("ConvertJsonToTable", factory.ConvertJsonToTable(jsonContent));

        // 2) ConvertArrowFileToTable
        String arrowFilePath = Paths.get(resourcesDir, "V3ArrowFileStachResponse.arrow").toString();
        check("ConvertArrowFileToTable", factory.ConvertArrowFileToTable(arrowFilePath));

        // 3) ConvertArrowStreamToTable
        byte[] arrowBytes = Files.readAllBytes(
                Paths.get(resourcesDir, "V3ArrowStreamStachResponse.arrow"));
        check("ConvertArrowStreamToTable", factory.ConvertArrowStreamToTable(arrowBytes));

        System.out.println("\nEND-USER TEST PASSED");
    }

    private static void check(String name, List<List<Map<String, String>>> result) {
        if (result == null) throw new RuntimeException(name + ": result was null");
        if (result.isEmpty()) throw new RuntimeException(name + ": no tables returned");
        List<Map<String, String>> table = result.get(0);
        if (table == null || table.isEmpty()) throw new RuntimeException(name + ": first table empty");
        System.out.println("\n[" + name + "] tables=" + result.size()
                + ", rows=" + table.size()
                + ", columns=" + table.get(0).keySet());
    }
}
