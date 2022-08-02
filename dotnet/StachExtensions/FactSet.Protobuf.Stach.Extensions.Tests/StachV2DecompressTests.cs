using System;
using System.IO;
using System.Threading.Tasks;
using System.Collections.Generic;
using Microsoft.VisualStudio.TestTools.UnitTesting;

using FactSet.Protobuf.Stach.Extensions.V2;
using Newtonsoft.Json.Linq;

namespace FactSet.Protobuf.Stach.Extensions.Tests 
{
    [TestClass]
    public class DecompressTests
    {
        private string fileInput;
        private List<String> decompressedExpected = new List<String> {
            null, null, null, "Americas", "Asia Pacific",
            "Europe", "Middle East and Africa", null, null, null, 
            null, null, null, null, null
        };

        [TestInitialize]
        public async Task init()
        {
            fileInput = await File.OpenText("Resources/V2ColumnStachDataCompressed.json").ReadToEndAsync();
        }

        [TestMethod]
        public void TestGetPrimaryTableIds()
        {
            List<String> primaryTableIds = ColumnOrganizedStachUtilities.GetPrimaryTableIds(fileInput);

            Assert.AreEqual(primaryTableIds.Count, 1);
            Assert.AreEqual(primaryTableIds[0], "a649ec50-7e58-443d-b791-1340e9eebf24");
        }

        [TestMethod]
        public void TestDecompress() 
        {
            string decompressed = ColumnOrganizedStachUtilities.Decompress(fileInput);

            JObject decompressedJson = JObject.Parse(decompressed);
            string dataColumnId = "1";
            string primaryTableId = ColumnOrganizedStachUtilities.GetPrimaryTableIds(fileInput)[0];
            List<String> dataValues = getDataValues<String>(decompressedJson, primaryTableId, dataColumnId);
            
            Assert.AreEqual(dataValues.Count, decompressedExpected.Count);

            for(int i = 0; i < dataValues.Count; i++) {
                String expectedValue = decompressedExpected[i];
                String actualValue = dataValues[i];

                Assert.AreEqual(expectedValue, actualValue);
            }
        }

        private List<T> getDataValues<T>(JObject decompressed, string primaryTableId, string dataColumnId) {
            JObject firstDataColumns = (JObject)decompressed["tables"][primaryTableId]["data"]["columns"][dataColumnId];
            JArray valuesJSON = (JArray)firstDataColumns["values"];

            return valuesJSON.ToObject<List<T>>();
        }
    }
}
