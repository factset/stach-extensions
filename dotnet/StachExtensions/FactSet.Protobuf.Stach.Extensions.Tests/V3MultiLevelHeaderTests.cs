using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace FactSet.Protobuf.Stach.Extensions.Tests
{
    [TestClass]
    public class V3MultiLevelHeaderTests
    {
        private const int ExpectedRowCount = 9521;

        private static readonly string[] ExpectedHeaders =
        {
            "group0", "group1", "group2", "group3", "group4", "group5",
            "Security Name",
            "26-OCT-2026 | Port. Weight",
            "26-OCT-2026 | Bench Weight",
            "26-OCT-2026 | Variation in Ending Weight",
            "3-string",
            "27-OCT-2026 | Port. Weight",
            "27-OCT-2026 | Bench Weight",
            "27-OCT-2026 | Variation in Ending Weight",
            "6-string",
        };

        private static string ResourcePath(string fileName)
        {
            return Path.Combine(AppContext.BaseDirectory, "Resources", fileName);
        }

        // Collapse line breaks and repeated spaces, so "Port.\nWeight" compares equal to "Port. Weight".
        private static string NormalizeHeader(string header)
        {
            return Regex.Replace((header ?? string.Empty).Trim(), @"\s+", " ");
        }

        private static List<string> HeadersOf(DataSet ds)
        {
            return ds.Tables[0].Columns.Cast<DataColumn>().Select(c => NormalizeHeader(c.ColumnName)).ToList();
        }

        [TestMethod]
        public void ArrowStream_HeadersMatchExpected()
        {
            var ds = StachV3ExtensionFactory.ConvertArrowStreamToTable(File.ReadAllBytes(ResourcePath("V3MultiLevelHeaderArrowStreamResponse.arrow")));
            CollectionAssert.AreEqual(ExpectedHeaders, HeadersOf(ds));
        }

        [TestMethod]
        public void ArrowStream_RowCountMatchesExpected()
        {
            var ds = StachV3ExtensionFactory.ConvertArrowStreamToTable(File.ReadAllBytes(ResourcePath("V3MultiLevelHeaderArrowStreamResponse.arrow")));
            Assert.AreEqual(ExpectedRowCount, ds.Tables[0].Rows.Count);
        }

        [TestMethod]
        public void ArrowFilePath_HeadersMatchExpected()
        {
            var ds = StachV3ExtensionFactory.ConvertArrowFileToTable(ResourcePath("V3MultiLevelHeaderArrowStreamResponse.arrow"));
            CollectionAssert.AreEqual(ExpectedHeaders, HeadersOf(ds));
        }

        [TestMethod]
        public void Json_HeadersMatchExpected()
        {
            var json = File.ReadAllText(ResourcePath("V3MultiLevelHeaderJsonResponse.json"));
            DataSet ds;
            try
            {
                ds = StachV3ExtensionFactory.ConvertJsonToTable(json);
            }
            catch (Exception e)
            {
                Assert.Fail("V3MultiLevelHeaderJsonResponse.json could not be converted (incomplete or invalid JSON?): " + e.Message);
                return;
            }
            CollectionAssert.AreEqual(ExpectedHeaders, HeadersOf(ds));
        }

        [TestMethod]
        public void ArrowAndJson_HeadersMatch()
        {
            var arrowHeaders = HeadersOf(StachV3ExtensionFactory.ConvertArrowStreamToTable(File.ReadAllBytes(ResourcePath("V3MultiLevelHeaderArrowStreamResponse.arrow"))));
            var json = File.ReadAllText(ResourcePath("V3MultiLevelHeaderJsonResponse.json"));
            var jsonHeaders = HeadersOf(StachV3ExtensionFactory.ConvertJsonToTable(json));
            CollectionAssert.AreEqual(arrowHeaders, jsonHeaders);
        }
    }
}
