using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using FactSet.Protobuf.Stach.Extensions.V2;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace FactSet.Protobuf.Stach.Extensions.Tests
{
    [TestClass]
    public class StachV2SimplifiedRowTests
    {
        private string input;
        private List<string> firstRow = new List<string>{"total0","group1", "group2", "Port.+Weight","Bench.+Weight","Difference"};
        private List<string> secondRow = new List<string>{"Total", "", "", "100", "", "100"};

        [TestInitialize]
        public async Task Init()
        {
            input = await File.OpenText("Resources/V2SimplifiedRowStachData.json").ReadToEndAsync();
        }

        [TestMethod]
        public void TestMethod1()
        {
            var rowStachBuilder = StachExtensionFactory.GetRowOrganizedBuilder();
            var stachExtension = rowStachBuilder.SetPackage(input).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader, "Header row is not set true for isHeader property");
            Assert.IsTrue(table[0].Rows.Count == 62);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);

            Assert.IsTrue(table[0].Metadata.Count == 18, "There is an incorrect amount of Metadata items");
            Assert.AreEqual("[ \"Single\" ]", table[0].Metadata["Report Frequency"]);

            Assert.IsTrue(table[0].RawMetadata.Count == 18, "There is an incorrect amount of RawMetadata items");
            Assert.AreEqual("Industry - Beginning of Period", StachUtilities.ValueToString(table[0].RawMetadata["Grouping Frequency"][1]));
        }
        
        [TestMethod]
        public void TestMethod2()
        {
            input = File.OpenText("Resources/V2SimplifiedRowWithStruct.json").ReadToEnd();
            var firstRow = new List<string>{"InputSecurity","Scenario", "Horizon", "Run Status", "CF Coupon"};
            var secondRow = new List<string>{"3140JQHD", "Base", "Base", "", @"{ ""20210625"": ""3.5"", ""20210725"": ""3.5"", ""20210825"": ""3.5"", ""20210925"": ""3.5"" }"};
            
            var rowStachBuilder = StachExtensionFactory.GetRowOrganizedBuilder();
            var stachExtension = rowStachBuilder.SetPackage(input).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader, "Header row is not set true for isHeader property");
            Assert.IsTrue(table[0].Rows.Count == 2);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);

            Assert.IsTrue(table[0].Metadata.Count == 1, "There is an incorrect amount of Metadata items");
            Assert.AreEqual("78647885fa4f4594b775a1431e62090c:", table[0].Metadata["CalculationId"]);

            Assert.IsTrue(table[0].RawMetadata.Count == 1, "There is an incorrect amount of RawMetadata items");
            Assert.AreEqual("78647885fa4f4594b775a1431e62090c:", StachUtilities.ValueToString(table[0].RawMetadata["CalculationId"][0]));
        }

        [TestMethod]
        public void TestMethod3()
        {
            var firstItem = new Dictionary<string, dynamic>(){{"total0", "Total"}, {"group1", null}, {"group2", null}, {"Port.+Weight", 100.0},
                {"Bench.+Weight", null}, {"Difference", 100.0}};
            var keysValue = new List<string>() { "total0 | group1 | group2", "Total", "Communications", "Communications | Wireless Telecommunications",
                "Finance | Regional Banks | State Bank of India", "Utilities | Electric Utilities | Power Grid Corporation of India Limited"};
            
            var simplifiedRowStachBuilder = StachExtensionFactory.GetSimplifiedRowOrganizedStachBuilder();
            var stachExtension = simplifiedRowStachBuilder.SetPackage(input).Build();
            var dynamicObject = stachExtension.ConvertToDynamicObjects();
            var transposedDynamicObject = stachExtension.ConvertToTransposedDynamicObjects();
            var table = stachExtension.ConvertToTable();

            Assert.IsTrue(dynamicObject.Count == 61, "There is an incorrect amount of items in the dynamic object");
            Assert.IsTrue(((IDictionary<string, dynamic>)transposedDynamicObject.First()).Count == 62, "There is an incorrect amount of items in the transposed dynamic object");
            Assert.IsTrue(table[0].Rows.Count == 62);
            Assert.IsTrue(transposedDynamicObject.Count == 3);
            foreach (var key in keysValue)
            {
                Assert.IsTrue(((IDictionary<string, dynamic>)transposedDynamicObject.First()).ContainsKey(key), $"{key} is missing from the transposed object's key list");
            }

            foreach (var item in firstItem)
            {
                Assert.IsTrue(((IDictionary<string, dynamic>)dynamicObject.First()).ContainsKey(item.Key), $"{item.Key} is missing from the dynamic object's key list");
            }
        }
    }
}