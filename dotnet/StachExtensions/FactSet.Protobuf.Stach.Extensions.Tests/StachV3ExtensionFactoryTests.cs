using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.IO;

namespace FactSet.Protobuf.Stach.Extensions.Tests
{
    [TestClass]
    public class StachV3ExtensionFactoryTests
    {
        private string jsonFileContent;
        private string arrowFilePath;
        private string arrowStreamFilePath;

        [TestInitialize]
        public void Init()
        {
            string baseDirectory = AppContext.BaseDirectory;
            string jsonFilePath = Path.Combine(baseDirectory, "Resources", "StachV3JsonResponse.json");

            if (!File.Exists(jsonFilePath))
                throw new FileNotFoundException($"Test resource file not found at: {jsonFilePath}");

            jsonFileContent = File.ReadAllText(jsonFilePath);

            if (string.IsNullOrWhiteSpace(jsonFileContent))
                throw new InvalidOperationException($"File at {jsonFilePath} is empty");

            arrowFilePath = Path.Combine(baseDirectory, "Resources", "StachV3ArrowFileResponse.arrow");

            if (!File.Exists(arrowFilePath))
                throw new FileNotFoundException($"Test resource file not found at: {arrowFilePath}");

            arrowStreamFilePath = Path.Combine(baseDirectory, "Resources", "StachV3ArrowStreamResponse.arrow");

            if (!File.Exists(arrowStreamFilePath))
                throw new FileNotFoundException($"Test resource file not found at: {arrowStreamFilePath}");
        }

        [TestMethod]
        public void ConvertJsonToTable_WithCompleteStachV3Structure_SuccessfullyConverts()
        {
            // Act
            var result = StachV3ExtensionFactory.ConvertJsonToTable(jsonFileContent);

            // Assert - DataSet
            Assert.IsNotNull(result, "DataSet should not be null");
            Assert.AreEqual(1, result.Tables.Count, "DataSet should contain exactly 1 table");

            // Assert - DataTable shape
            var dt = result.Tables[0];
            Assert.IsNotNull(dt, "DataTable should not be null");            
           
            // Assert - primary_key column was removed
            Assert.IsFalse(dt.Columns.Contains("primary_key"), "primary_key column should be removed");
        }

        [TestMethod]
        public void ConvertArrowFileToTable_WithCompleteStachV3Structure_SuccessfullyConverts()
        {
            // Act
            var result = StachV3ExtensionFactory.ConvertArrowFileToTable(arrowFilePath);

            // Assert - DataSet
            Assert.IsNotNull(result, "DataSet should not be null");
            Assert.AreEqual(1, result.Tables.Count, "DataSet should contain exactly 1 table");

            // Assert - DataTable shape
            var dt = result.Tables[0];
            Assert.IsNotNull(dt, "DataTable should not be null");

            // Assert - primary_key column was removed
            Assert.IsFalse(dt.Columns.Contains("primary_key"), "primary_key column should be removed");
        }

        [TestMethod]
        public void ConvertArrowStremToTable_WithCompleteStachV3Structure_SuccessfullyConverts()
        {
            // Act
            byte[] arrowBytes = File.ReadAllBytes(arrowStreamFilePath);

            var result = StachV3ExtensionFactory.ConvertArrowStreamToTable(arrowBytes);

            // Assert - DataSet
            Assert.IsNotNull(result, "DataSet should not be null");
            Assert.AreEqual(1, result.Tables.Count, "DataSet should contain exactly 1 table");

            // Assert - DataTable shape
            var dt = result.Tables[0];
            Assert.IsNotNull(dt, "DataTable should not be null");

            // Assert - primary_key column was removed
            Assert.IsFalse(dt.Columns.Contains("primary_key"), "primary_key column should be removed");
        }
    }
}
