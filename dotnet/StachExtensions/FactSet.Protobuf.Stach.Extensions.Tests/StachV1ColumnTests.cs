using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace FactSet.Protobuf.Stach.Extensions.Tests
{
    [TestClass]
    public class StachV1ColumnTests
    {
        private string input;
        private List<string> firstRow = new List<string>{"total0","group1", "group2", "Port.+Weight","Bench.+Weight","Difference"};
        private List<string> secondRow = new List<string>{"Total", "null", "null", "100", "NaN", "100"};
        
        [TestInitialize]
        public async Task Init()
        {
            input = await File.OpenText("Resources/V1ColumnStachData.json").ReadToEndAsync();
        }
        
        [TestMethod]
        public void TestConvert()
        {
            var columnStachBuilder = StachExtensionFactory.GetColumnOrganizedBuilder<Package>();
            var stachExtension = columnStachBuilder.SetPackage(input).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader, "Header row is not set true for isHeader property");
            Assert.IsTrue(table[0].Rows.Count == 62);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);

            Assert.IsTrue(table[0].Metadata.Count == 18, "There is an incorrect amount of Metadata items");
            Assert.AreEqual("Single", table[0].Metadata["Report Frequency"]);

            Assert.IsTrue(table[0].RawMetadata.Count == 18, "There is an incorrect amount of RawMetadata items");
            Assert.AreEqual("\"Economic Sector - Beginning of Period|Industry - Beginning of Period\"", table[0].RawMetadata["Grouping Frequency"][0].ToString());
        }
        
        [TestMethod]
        public void TestConvertWithStruct()
        {
            input = File.OpenText("Resources/V1ColumnStachWithStruct.json").ReadToEnd();
            var firstRow = new List<string>{"InputSecurity","Scenario", "Horizon", "CF Coupon"};
            var secondRow = new List<string>{"3140JQHD", "Base", "Base", @"{""20210625"":3.5,""20210725"":3.5,""20210825"":3.5,""20210925"":3.5}"};
            var columnStachBuilder = StachExtensionFactory.GetColumnOrganizedBuilder<Package>();
            var stachExtension = columnStachBuilder.SetPackage(input).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader, "Header row is not set true for isHeader property");
            Assert.IsTrue(table[0].Rows.Count == 2);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);

            Assert.IsTrue(table[0].Metadata.Count == 0, "There is an incorrect amount of Metadata items");
            Assert.IsTrue(table[0].RawMetadata.Count == 0,"There is an incorrect amount of RawMetadata items");
        }
    }
}