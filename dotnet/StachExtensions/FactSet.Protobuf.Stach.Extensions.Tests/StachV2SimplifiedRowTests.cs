using System.Collections.Generic;
using System.IO;
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
    }
}