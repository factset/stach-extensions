using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace FactSet.Protobuf.Stach.Extensions.Tests
{
    [TestClass]
    public class StachV2RowTests
    {
        private string input;
        private string inputTable;
        private string inputWithRowColSpan;
        private List<string> firstRow = new List<string>{"total0","group1", "group2","Difference"};
        private List<string> secondRow = new List<string>{"Total", "", "", "--",};

        [TestInitialize]
        public async Task Init()
        {
            input = await File.OpenText("Resources/V2RowStachData.json").ReadToEndAsync();
            inputTable = await File.OpenText("Resources/V2RowStachTable.json").ReadToEndAsync();
            inputWithRowColSpan = await File.OpenText("Resources/V2RowStachWithRowAndColSpan.json").ReadToEndAsync();
        }

        [TestMethod]
        public void TestConvert()
        {
            var rowStachBuilder = StachExtensionFactory.GetRowOrganizedBuilder();
            var stachExtension = rowStachBuilder.SetPackage(input).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader, "Header row is not set true for isHeader property");
            Assert.IsTrue(table[0].Rows.Count == 635);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);
            
            Assert.IsTrue(table[0].Metadata.Count == 18, "There is an incorrect amount of Metadata items");
            Assert.AreEqual("[ \"Single\" ]", table[0].Metadata["Report Frequency"]);

            Assert.IsTrue(table[0].RawMetadata.Count == 18, "There is an incorrect amount of RawMetadata items");
            Assert.AreEqual("\"Industry - FactSet - Beginning of Period\"", table[0].RawMetadata["Grouping Frequency"][1].ToString());
        }
        
        [TestMethod]
        public void TestConvertAddTable()
        {
            var rowStachBuilder = StachExtensionFactory.GetRowOrganizedBuilder();
            var stachExtension = rowStachBuilder.SetPackage(input).AddTable("table1", inputTable)
                .AddTable("table2", inputTable).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table.Count == 3);
            Assert.IsTrue(table[0].Rows.Count == 635);
            Assert.IsTrue(table[1].Rows.Count == 630);
            CollectionAssert.AreEqual(table[0].Rows[0].Cells, firstRow);
            CollectionAssert.AreEqual(table[0].Rows[1].Cells, secondRow);

            Assert.IsTrue(table[1].Metadata.Count == 18, "There is an incorrect amount of Metadata items");
            Assert.AreEqual("[ \"Single\" ]", table[1].Metadata["Report Frequency"]);

            Assert.IsTrue(table[1].RawMetadata.Count == 18, "There is an incorrect amount of RawMetadata items");
            Assert.AreEqual("\"Industry - FactSet - Beginning of Period\"", table[1].RawMetadata["Grouping Frequency"][1].ToString());
        }
        
        [TestMethod]
        public void TestConvertRowAndColSpan()
        {
            var thirdRow = new List<string>
            {
                "Function", "Region", "Continent 1", "Continent 2", "Fund", "Bench", "Abbr", "Fund", "Bench", "Fund",
                "Bench"
            };
            var fourthRow = new List<string>
                {"Max", "", "", "", "88.3", "89.62", "", "17.17", "15.67", "86.07", "89.18"};
            
            var rowStachBuilder = StachExtensionFactory.GetRowOrganizedBuilder();
            var stachExtension = rowStachBuilder.SetPackage(inputWithRowColSpan).Build();
            var table = stachExtension.ConvertToTable();
            
            Assert.IsTrue(table[0].Rows[0].isHeader);
            Assert.IsTrue(table[0].Rows[0].Cells.Count == 11);
            
            Assert.IsTrue(table[0].Rows[1].isHeader);
            Assert.IsTrue(table[0].Rows[1].Cells.Count == 11);
            
            Assert.IsTrue(table[0].Rows[2].isHeader);
            Assert.IsTrue(table[0].Rows[2].Cells.Count == 11);
            
            CollectionAssert.AreEqual(table[0].Rows[2].Cells, thirdRow);
            CollectionAssert.AreEqual(table[0].Rows[2].Cells, thirdRow);
            
            Assert.IsTrue(!table[0].Rows[3].isHeader);
            CollectionAssert.AreEqual(table[0].Rows[3].Cells, fourthRow);

            Assert.IsTrue(table[0].Metadata.Count == 1, "There is an incorrect amount of Metadata items");
            Assert.AreEqual("Risk Analysis", table[0].Metadata["m_h"]);

            Assert.IsTrue(table[0].RawMetadata.Count == 1, "There is an incorrect amount of RawMetadata items");
            Assert.AreEqual("\"Risk Analysis\"", table[0].RawMetadata["m_h"][0].ToString());
        }
    }
}