using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace FactSet.Protobuf.Stach.Extensions.Tests
{
    [TestClass]
    public class StachV2ColumnTests
    {
        private string input;
        private List<string> firstRow = new List<string>{"total0","group1", "group2", "Port.+Weight","Bench.+Weight","Difference"};
        private List<string> secondRow = new List<string>{"Total", "", "", "100", "", "100"};

        [TestInitialize]
        public async Task Init()
        {
            input = await File.OpenText("Resources/V2ColumnStachData.json").ReadToEndAsync();
        }

        [TestMethod]
        public void TestConvert()
        {
            var columnStachBuilder = StachExtensionFactory.GetColumnOrganizedBuilder<Stach.V2.Package>();
            var stachExtension = columnStachBuilder.SetPackage(input).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader, "Header row is not set true for isHeader property");
            Assert.IsTrue(table[0].Rows.Count == 62);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);
        }
    }
}