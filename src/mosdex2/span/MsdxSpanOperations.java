/**
 * 
 */
package io.github.JeremyBloom.mosdex2.span;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxSparkDataframe;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxFile;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;

/**
 * Tests the basic operations on MsdxSpan using the Operator interfaces to call the result schema.
 * Uses the transshipment model and produces LP format tables.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxSpanOperations {
	
	static MsdxSparkDataframe.Factory dfFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
	
	/**
	 * This class tests various operations on spans, 
	 * in the manner that they will actually be used to construct solver modeling objects.
	 * Each test has two counterparts: the first displays the results of applying an operation 
	 * or series of operations. The second is a pass/fail in which the actual results are compared 
	 * with the expected results. The latter can be run silently as a JUnit test suite.
	 */
	public MsdxSpanOperations() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The main routine executes all of the display tests.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("Span Operations Tests");
		Msdx.GLOBAL.showDisplay();
		
		Msdx.GLOBAL.out.println("MOSDEX Objects Test");
		displayObjectsTest();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Select Test");
		displaySelectTest();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Mapping Test 1");
		displayMappingTest1();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Mapping Test 2");
		displayMappingTest2();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Join Test 1");
		displayJoinTest1();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Join Test 1 Alternative Multiply");
		displayJoinTest1Alt();
		Msdx.GLOBAL.out.println();	
		
		Msdx.GLOBAL.out.println("Join Test Quadratic Terms");
		displayJoinTestQuadraticTerms();
		Msdx.GLOBAL.out.println();	
		
		Msdx.GLOBAL.out.println("Join Extra Test");
		displayJoinTestExtraWarehouses();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Inner Join Test");
		displayJoinTestInner();		
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Outer Join Test");
		displayJoinTestOuter();		
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Reduce Test");
		displayReduceTest();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Join Test 2");
		displayJoinTest2();
		Msdx.GLOBAL.out.println();
		
	}//main
	
	/**
	 * Setup the test data as a MOSDEX File representing a transshipment model. 
	 * This data is used for all tests in this class.
	 */
	static MsdxFile transshipment() {
		
		String mosdexFile= 
			/*'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
  "MODULES": [
    { "NAME": "generalTransshipment",
      "CLASS": "MODEL",
      "HEADING": {
        "DESCRIPTION": [
          "General Transshipment Problem",
          "instance form", "with a function calls for output",
          "MOSDEX 2-0 Syntax"
        ],
        "VERSION": "net1b 2-1",
        "REFERENCE": ["https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod"],
        "AUTHOR": ["Jeremy A. Bloom (jeremyblmca@gmail.com)"],
        "NOTICES": ["Copyright © 2019 Jeremy A. Bloom"],
        "MATH": [
          "var Ship {(i,j) in LINKS} >= 0, <= capacity[i,j]; # packages to be shipped",
          "minimize Total_Cost: sum {(i,j) in LINKS} cost[i,j] * Ship[i,j];",
          "subject to",
            "Balance {k in CITIES}: ",
              "sum {(k,j) in LINKS} Ship[k,j] - sum {(i,k) in LINKS} Ship[i,k] = supply[k] - demand[k];"
        ]
      },
      "TABLES": [
        { "NAME": "ship",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS":
              ["Name",  "origin",  "destination",  "Column",     "LowerBound", "UpperBound", "Value"],
            "TYPES":
              ["STRING","STRING",  "STRING",       "STRING",     "DOUBLE",     "DOUBLE",    "DOUBLE_FUNCTION"]
          },
          "INSTANCE": [
            ["ship", "PITT",    "NE",           "ship_PITT_NE",   0.0,          250.0,   "PrimalValue(Column)"],
            ["ship", "PITT",    "SE",           "ship_PITT_SE",   0.0,          250.0,   "PrimalValue(Column)"],
            ["ship", "NE",      "BOS",          "ship_NE_BOS",    0.0,          100.0,   "PrimalValue(Column)" ],
            ["ship", "NE",      "EWR",          "ship_NE_EWR",    0.0,          100.0,   "PrimalValue(Column)" ],
            ["ship", "NE",      "BWI",          "ship_NE_BWI",    0.0,          100.0,   "PrimalValue(Column)" ],
            ["ship", "SE",      "EWR",          "ship_SE_EWR",    0.0,          100.0,   "PrimalValue(Column)" ],
            ["ship", "SE",      "BWI",          "ship_SE_BWI",    0.0,          100.0,   "PrimalValue(Column)" ],
            ["ship", "SE",      "ATL",          "ship_SE_ATL",    0.0,          100.0,   "PrimalValue(Column)" ],
            ["ship", "SE",      "MCO",          "ship_SE_MCO",    0.0,          100.0,   "PrimalValue(Column)" ]
          ]
      },
        { "NAME": "balance",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS":
              ["Name",     "city",   "Row",          "Sense",  "RHS"  ,  "Dual"],
            "TYPES":
              ["STRING",   "STRING", "STRING",       "STRING", "DOUBLE", "DOUBLE_FUNCTION"]
          },
          "INSTANCE": [
            ["balance", "PITT", "balance_PITT",    "EQ",     450.0,    "DualValue(Row)"],
            ["balance", "NE",   "balance_NE",      "EQ",     0.0,      "DualValue(Row)"  ],
            ["balance", "SE",   "balance_SE",      "EQ",     0.0,      "DualValue(Row)"  ],
            ["balance", "BOS",  "balance_BOS",     "EQ",     -90.0,    "DualValue(Row)" ],
            ["balance", "EWR",  "balance_EWR",     "EQ",     -120.0,   "DualValue(Row)" ],
            ["balance", "BWI",  "balance_BWI",     "EQ",     -120.0,   "DualValue(Row)" ],
            ["balance", "ATL",  "balance_ATL",     "EQ",     -70.0,    "DualValue(Row)" ],
            ["balance", "MCO",  "balance_MCO",     "EQ",     -50.0 ,   "DualValue(Row)" ]
          ]
        },
        { "NAME": "totalCost",
          "CLASS": "OBJECTIVE",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS":  ["Name",      "Row",         "Constant", "Sense",    "Value"],
            "TYPES":   ["STRING",    "STRING",      "DOUBLE",   "STRING",   "DOUBLE_FUNCTION"]
          },
          "INSTANCE": [["totalCost", "totalCost",    0.0,       "MINIMIZE", "ObjectiveValue(Row)"]]
        },
        { "NAME": "balance_shipFrom",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS":
              ["Row",         "Column",       "Coefficient"],
            "TYPES":
              ["STRING",      "STRING",       "DOUBLE"]
          },
          "INSTANCE": [
            ["balance_PITT", "ship_PITT_NE", 1.0          ],
            ["balance_PITT", "ship_PITT_SE", 1.0          ],
            ["balance_NE",   "ship_NE_BOS",  1.0          ],
            ["balance_NE",   "ship_NE_EWR",  1.0          ],
            ["balance_NE",   "ship_NE_BWI",  1.0          ],
            ["balance_SE",   "ship_SE_EWR",  1.0          ],
            ["balance_SE",   "ship_SE_BWI",  1.0          ],
            ["balance_SE",   "ship_SE_ATL",  1.0          ],
            ["balance_SE",   "ship_SE_MCO",  1.0          ]
          ]
        },
        { "NAME": "balance_shipTo",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS":
              ["Row",        "Column",         "Coefficient"],
            "TYPES": 
              ["STRING",     "STRING",         "DOUBLE"]
          },
          "INSTANCE": [
            ["balance_NE",  "ship_PITT_NE",   -1.0         ],
            ["balance_SE",  "ship_PITT_SE",   -1.0         ],
            ["balance_BOS", "ship_NE_BOS",    -1.0         ],
            ["balance_EWR", "ship_NE_EWR",    -1.0         ],
            ["balance_EWR", "ship_SE_EWR",    -1.0         ],
            ["balance_BWI", "ship_NE_BWI",    -1.0         ],
            ["balance_BWI", "ship_SE_BWI",    -1.0         ],
            ["balance_ATL", "ship_SE_ATL",    -1.0         ],
            ["balance_MCO", "ship_SE_MCO",    -1.0         ]
          ]
        },
        { "NAME": "total_ship",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS":
              ["Kind",    "Row",      "Column",       "Coefficient"],
            "TYPES":
              ["STRING",  "STRING",   "STRING",       "DOUBLE"]
          },
          "INSTANCE": [
            ["LINEAR",  "totalCost", "ship_PITT_NE", 2.5          ],
            ["LINEAR",  "totalCost", "ship_PITT_SE", 3.5          ],
            ["LINEAR",  "totalCost", "ship_NE_BOS",  1.7          ],
            ["LINEAR",  "totalCost", "ship_NE_EWR",  0.7          ],
            ["LINEAR",  "totalCost", "ship_NE_BWI",  1.3          ],
            ["LINEAR",  "totalCost", "ship_SE_EWR",  1.3          ],
            ["LINEAR",  "totalCost", "ship_SE_BWI",  0.8          ],
            ["LINEAR",  "totalCost", "ship_SE_ATL",  0.2          ],
            ["LINEAR",  "totalCost", "ship_SE_MCO",  2.1          ]
          ]
        }
      ]
    },
    { "NAME": "Quadratic Objective Terms",
      "CLASS": "MODEL",
      "HEADING": {
        "DESCRIPTION": ["Quadratic Objective Terms in General Transshipment Problem"]
      },
      "TABLES": [
        { "NAME": "total_ship_ship",
          "CLASS": "TERM",
          "KIND": "QUADRATIC",
          "SCHEMA": {
            "FIELDS":
              ["Kind",    "Row",      "Column",        "Column2",       "Coefficient"],
            "TYPES":
              ["STRING",  "STRING",   "STRING",   	   "STRING",        "DOUBLE"]
          },
          "INSTANCE": [
            ["QUADRATIC",  "totalCost", "ship_PITT_NE", "ship_PITT_NE",	1.0   ],
            ["QUADRATIC",  "totalCost", "ship_PITT_SE", "ship_PITT_SE",	1.0   ],
            ["QUADRATIC",  "totalCost", "ship_PITT_NE", "ship_PITT_SE",	0.5   ],
            ["QUADRATIC",  "totalCost", "ship_PITT_SE", "ship_PITT_NE",	0.5   ]
          ]
        }
     ]
    },
    { "NAME": "moreWarehouses",
      "CLASS": "MODEL",
      "HEADING": {
        "DESCRIPTION": ["More Warehouses in General Transshipment Problem"]
      },
      "TABLES": [
        { "NAME": "moreBalance_shipFrom",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS":
              ["Row",         "Column",       "Coefficient"],
            "TYPES":
              ["STRING",      "STRING",       "DOUBLE"]
          },
          "INSTANCE": [
            ["balance_NE",   "ship_NE_SE ",  1.0          ],
            ["balance_SE",   "ship_SE_SFO",  1.0          ]
          ]
        }
      ]
    }
  ]
}
			'''*/
		"{\r\n"
		+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
		+ "  \"MODULES\": [\r\n"
		+ "    { \"NAME\": \"generalTransshipment\",\r\n"
		+ "      \"CLASS\": \"MODEL\",\r\n"
		+ "      \"HEADING\": {\r\n"
		+ "        \"DESCRIPTION\": [\r\n"
		+ "          \"General Transshipment Problem\",\r\n"
		+ "          \"instance form\", \"with a function calls for output\",\r\n"
		+ "          \"MOSDEX 2-0 Syntax\"\r\n"
		+ "        ],\r\n"
		+ "        \"VERSION\": \"net1b 2-1\",\r\n"
		+ "        \"REFERENCE\": [\"https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod\"],\r\n"
		+ "        \"AUTHOR\": [\"Jeremy A. Bloom (jeremyblmca@gmail.com)\"],\r\n"
		+ "        \"NOTICES\": [\"Copyright © 2019 Jeremy A. Bloom\"],\r\n"
		+ "        \"MATH\": [\r\n"
		+ "          \"var Ship {(i,j) in LINKS} >= 0, <= capacity[i,j]; # packages to be shipped\",\r\n"
		+ "          \"minimize Total_Cost: sum {(i,j) in LINKS} cost[i,j] * Ship[i,j];\",\r\n"
		+ "          \"subject to\",\r\n"
		+ "            \"Balance {k in CITIES}: \",\r\n"
		+ "              \"sum {(k,j) in LINKS} Ship[k,j] - sum {(i,k) in LINKS} Ship[i,k] = supply[k] - demand[k];\"\r\n"
		+ "        ]\r\n"
		+ "      },\r\n"
		+ "      \"TABLES\": [\r\n"
		+ "        { \"NAME\": \"ship\",\r\n"
		+ "          \"CLASS\": \"VARIABLE\",\r\n"
		+ "          \"KIND\": \"CONTINUOUS\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Name\",  \"origin\",  \"destination\",  \"Column\",     \"LowerBound\", \"UpperBound\", \"Value\"],\r\n"
		+ "            \"TYPES\":\r\n"
		+ "              [\"STRING\",\"STRING\",  \"STRING\",       \"STRING\",     \"DOUBLE\",     \"DOUBLE\",    \"DOUBLE_FUNCTION\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"ship\", \"PITT\",    \"NE\",           \"ship_PITT_NE\",   0.0,          250.0,   \"PrimalValue(Column)\"],\r\n"
		+ "            [\"ship\", \"PITT\",    \"SE\",           \"ship_PITT_SE\",   0.0,          250.0,   \"PrimalValue(Column)\"],\r\n"
		+ "            [\"ship\", \"NE\",      \"BOS\",          \"ship_NE_BOS\",    0.0,          100.0,   \"PrimalValue(Column)\" ],\r\n"
		+ "            [\"ship\", \"NE\",      \"EWR\",          \"ship_NE_EWR\",    0.0,          100.0,   \"PrimalValue(Column)\" ],\r\n"
		+ "            [\"ship\", \"NE\",      \"BWI\",          \"ship_NE_BWI\",    0.0,          100.0,   \"PrimalValue(Column)\" ],\r\n"
		+ "            [\"ship\", \"SE\",      \"EWR\",          \"ship_SE_EWR\",    0.0,          100.0,   \"PrimalValue(Column)\" ],\r\n"
		+ "            [\"ship\", \"SE\",      \"BWI\",          \"ship_SE_BWI\",    0.0,          100.0,   \"PrimalValue(Column)\" ],\r\n"
		+ "            [\"ship\", \"SE\",      \"ATL\",          \"ship_SE_ATL\",    0.0,          100.0,   \"PrimalValue(Column)\" ],\r\n"
		+ "            [\"ship\", \"SE\",      \"MCO\",          \"ship_SE_MCO\",    0.0,          100.0,   \"PrimalValue(Column)\" ]\r\n"
		+ "          ]\r\n"
		+ "      },\r\n"
		+ "        { \"NAME\": \"balance\",\r\n"
		+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Name\",     \"city\",   \"Row\",          \"Sense\",  \"RHS\"  ,  \"Dual\"],\r\n"
		+ "            \"TYPES\":\r\n"
		+ "              [\"STRING\",   \"STRING\", \"STRING\",       \"STRING\", \"DOUBLE\", \"DOUBLE_FUNCTION\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"balance\", \"PITT\", \"balance_PITT\",    \"EQ\",     450.0,    \"DualValue(Row)\"],\r\n"
		+ "            [\"balance\", \"NE\",   \"balance_NE\",      \"EQ\",     0.0,      \"DualValue(Row)\"  ],\r\n"
		+ "            [\"balance\", \"SE\",   \"balance_SE\",      \"EQ\",     0.0,      \"DualValue(Row)\"  ],\r\n"
		+ "            [\"balance\", \"BOS\",  \"balance_BOS\",     \"EQ\",     -90.0,    \"DualValue(Row)\" ],\r\n"
		+ "            [\"balance\", \"EWR\",  \"balance_EWR\",     \"EQ\",     -120.0,   \"DualValue(Row)\" ],\r\n"
		+ "            [\"balance\", \"BWI\",  \"balance_BWI\",     \"EQ\",     -120.0,   \"DualValue(Row)\" ],\r\n"
		+ "            [\"balance\", \"ATL\",  \"balance_ATL\",     \"EQ\",     -70.0,    \"DualValue(Row)\" ],\r\n"
		+ "            [\"balance\", \"MCO\",  \"balance_MCO\",     \"EQ\",     -50.0 ,   \"DualValue(Row)\" ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        { \"NAME\": \"totalCost\",\r\n"
		+ "          \"CLASS\": \"OBJECTIVE\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":  [\"Name\",      \"Row\",         \"Constant\", \"Sense\",    \"Value\"],\r\n"
		+ "            \"TYPES\":   [\"STRING\",    \"STRING\",      \"DOUBLE\",   \"STRING\",   \"DOUBLE_FUNCTION\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [[\"totalCost\", \"totalCost\",    0.0,       \"MINIMIZE\", \"ObjectiveValue(Row)\"]]\r\n"
		+ "        },\r\n"
		+ "        { \"NAME\": \"balance_shipFrom\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Row\",         \"Column\",       \"Coefficient\"],\r\n"
		+ "            \"TYPES\":\r\n"
		+ "              [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"balance_PITT\", \"ship_PITT_NE\", 1.0          ],\r\n"
		+ "            [\"balance_PITT\", \"ship_PITT_SE\", 1.0          ],\r\n"
		+ "            [\"balance_NE\",   \"ship_NE_BOS\",  1.0          ],\r\n"
		+ "            [\"balance_NE\",   \"ship_NE_EWR\",  1.0          ],\r\n"
		+ "            [\"balance_NE\",   \"ship_NE_BWI\",  1.0          ],\r\n"
		+ "            [\"balance_SE\",   \"ship_SE_EWR\",  1.0          ],\r\n"
		+ "            [\"balance_SE\",   \"ship_SE_BWI\",  1.0          ],\r\n"
		+ "            [\"balance_SE\",   \"ship_SE_ATL\",  1.0          ],\r\n"
		+ "            [\"balance_SE\",   \"ship_SE_MCO\",  1.0          ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        { \"NAME\": \"balance_shipTo\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Row\",        \"Column\",         \"Coefficient\"],\r\n"
		+ "            \"TYPES\": \r\n"
		+ "              [\"STRING\",     \"STRING\",         \"DOUBLE\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"balance_NE\",  \"ship_PITT_NE\",   -1.0         ],\r\n"
		+ "            [\"balance_SE\",  \"ship_PITT_SE\",   -1.0         ],\r\n"
		+ "            [\"balance_BOS\", \"ship_NE_BOS\",    -1.0         ],\r\n"
		+ "            [\"balance_EWR\", \"ship_NE_EWR\",    -1.0         ],\r\n"
		+ "            [\"balance_EWR\", \"ship_SE_EWR\",    -1.0         ],\r\n"
		+ "            [\"balance_BWI\", \"ship_NE_BWI\",    -1.0         ],\r\n"
		+ "            [\"balance_BWI\", \"ship_SE_BWI\",    -1.0         ],\r\n"
		+ "            [\"balance_ATL\", \"ship_SE_ATL\",    -1.0         ],\r\n"
		+ "            [\"balance_MCO\", \"ship_SE_MCO\",    -1.0         ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        { \"NAME\": \"total_ship\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Kind\",    \"Row\",      \"Column\",       \"Coefficient\"],\r\n"
		+ "            \"TYPES\":\r\n"
		+ "              [\"STRING\",  \"STRING\",   \"STRING\",       \"DOUBLE\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_PITT_NE\", 2.5          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_PITT_SE\", 3.5          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_NE_BOS\",  1.7          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_NE_EWR\",  0.7          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_NE_BWI\",  1.3          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_SE_EWR\",  1.3          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_SE_BWI\",  0.8          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_SE_ATL\",  0.2          ],\r\n"
		+ "            [\"LINEAR\",  \"totalCost\", \"ship_SE_MCO\",  2.1          ]\r\n"
		+ "          ]\r\n"
		+ "        }\r\n"
		+ "      ]\r\n"
		+ "    },\r\n"
		+ "    { \"NAME\": \"Quadratic Objective Terms\",\r\n"
		+ "      \"CLASS\": \"MODEL\",\r\n"
		+ "      \"HEADING\": {\r\n"
		+ "        \"DESCRIPTION\": [\"Quadratic Objective Terms in General Transshipment Problem\"]\r\n"
		+ "      },\r\n"
		+ "      \"TABLES\": [\r\n"
		+ "        { \"NAME\": \"total_ship_ship\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"QUADRATIC\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Kind\",    \"Row\",      \"Column\",        \"Column2\",       \"Coefficient\"],\r\n"
		+ "            \"TYPES\":\r\n"
		+ "              [\"STRING\",  \"STRING\",   \"STRING\",   	   \"STRING\",        \"DOUBLE\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"QUADRATIC\",  \"totalCost\", \"ship_PITT_NE\", \"ship_PITT_NE\",	1.0   ],\r\n"
		+ "            [\"QUADRATIC\",  \"totalCost\", \"ship_PITT_SE\", \"ship_PITT_SE\",	1.0   ],\r\n"
		+ "            [\"QUADRATIC\",  \"totalCost\", \"ship_PITT_NE\", \"ship_PITT_SE\",	0.5   ],\r\n"
		+ "            [\"QUADRATIC\",  \"totalCost\", \"ship_PITT_SE\", \"ship_PITT_NE\",	0.5   ]\r\n"
		+ "          ]\r\n"
		+ "        }\r\n"
		+ "     ]\r\n"
		+ "    },\r\n"
		+ "    { \"NAME\": \"moreWarehouses\",\r\n"
		+ "      \"CLASS\": \"MODEL\",\r\n"
		+ "      \"HEADING\": {\r\n"
		+ "        \"DESCRIPTION\": [\"More Warehouses in General Transshipment Problem\"]\r\n"
		+ "      },\r\n"
		+ "      \"TABLES\": [\r\n"
		+ "        { \"NAME\": \"moreBalance_shipFrom\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\":\r\n"
		+ "              [\"Row\",         \"Column\",       \"Coefficient\"],\r\n"
		+ "            \"TYPES\":\r\n"
		+ "              [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [\"balance_NE\",   \"ship_NE_SE \",  1.0          ],\r\n"
		+ "            [\"balance_SE\",   \"ship_SE_SFO\",  1.0          ]\r\n"
		+ "          ]\r\n"
		+ "        }\r\n"
		+ "      ]\r\n"
		+ "    }\r\n"
		+ "  ]\r\n"
		+ "}\r\n";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			dfFactory, 
			Msdx.GLOBAL.mapper, 
			false);
			
		MsdxFile mosdex= factory.readFile(MsdxInputSource.fromString(mosdexFile));
		return mosdex;			
	}//transshipment
	
	/**
	 * This test simply creates a span from a MOSDEX table.
	 */
	static void displayObjectsTest() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		ship.show("Ship", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displayObjectsTest
	
	@Test
	public void objectsTest() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Ship",
  "SCHEMA": {
    "FIELDS": [ "Name", "origin", "destination", "Column", "LowerBound", "UpperBound", "Value" ],
    "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE_FUNCTION" ]
  },
  "INSTANCE": [
    [ "ship", "PITT", "NE", "ship_PITT_NE", 0.0, 250.0, "PrimalValue(Column)" ],
    [ "ship", "PITT", "SE", "ship_PITT_SE", 0.0, 250.0, "PrimalValue(Column)" ],
    [ "ship", "NE", "BOS", "ship_NE_BOS", 0.0, 100.0, "PrimalValue(Column)" ],
    [ "ship", "NE", "EWR", "ship_NE_EWR", 0.0, 100.0, "PrimalValue(Column)" ],
    [ "ship", "NE", "BWI", "ship_NE_BWI", 0.0, 100.0, "PrimalValue(Column)" ],
    [ "ship", "SE", "EWR", "ship_SE_EWR", 0.0, 100.0, "PrimalValue(Column)" ],
    [ "ship", "SE", "BWI", "ship_SE_BWI", 0.0, 100.0, "PrimalValue(Column)" ],
    [ "ship", "SE", "ATL", "ship_SE_ATL", 0.0, 100.0, "PrimalValue(Column)" ],
    [ "ship", "SE", "MCO", "ship_SE_MCO", 0.0, 100.0, "PrimalValue(Column)" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Ship\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Name\", \"origin\", \"destination\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE_FUNCTION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"ship\", \"PITT\", \"NE\", \"ship_PITT_NE\", 0.0, 250.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"PITT\", \"SE\", \"ship_PITT_SE\", 0.0, 250.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"NE\", \"BOS\", \"ship_NE_BOS\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"NE\", \"EWR\", \"ship_NE_EWR\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"NE\", \"BWI\", \"ship_NE_BWI\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"SE\", \"EWR\", \"ship_SE_EWR\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"SE\", \"BWI\", \"ship_SE_BWI\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"SE\", \"ATL\", \"ship_SE_ATL\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "    [ \"ship\", \"SE\", \"MCO\", \"ship_SE_MCO\", 0.0, 100.0, \"PrimalValue(Column)\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		ship.show("Ship", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//objectsTest
	
	/**
	 * This test selects a subset of the fields of a span.
	 */
	static void displaySelectTest() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		parameters.show("Ship Parameters", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displaySelectTest
	
	@Test
	public void selectTest() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Ship Parameters",
  "SCHEMA": {
    "FIELDS": [ "Name", "Column", "LowerBound", "UpperBound" ],
    "TYPES": [ "STRING", "STRING", "DOUBLE", "DOUBLE" ]
  },
  "INSTANCE": [
    [ "ship", "ship_PITT_NE", 0.0, 250.0 ],
    [ "ship", "ship_PITT_SE", 0.0, 250.0 ],
    [ "ship", "ship_NE_BOS", 0.0, 100.0 ],
    [ "ship", "ship_NE_EWR", 0.0, 100.0 ],
    [ "ship", "ship_NE_BWI", 0.0, 100.0 ],
    [ "ship", "ship_SE_EWR", 0.0, 100.0 ],
    [ "ship", "ship_SE_BWI", 0.0, 100.0 ],
    [ "ship", "ship_SE_ATL", 0.0, 100.0 ],
    [ "ship", "ship_SE_MCO", 0.0, 100.0 ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Ship Parameters\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Name\", \"Column\", \"LowerBound\", \"UpperBound\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"ship\", \"ship_PITT_NE\", 0.0, 250.0 ],\r\n"
			+ "    [ \"ship\", \"ship_PITT_SE\", 0.0, 250.0 ],\r\n"
			+ "    [ \"ship\", \"ship_NE_BOS\", 0.0, 100.0 ],\r\n"
			+ "    [ \"ship\", \"ship_NE_EWR\", 0.0, 100.0 ],\r\n"
			+ "    [ \"ship\", \"ship_NE_BWI\", 0.0, 100.0 ],\r\n"
			+ "    [ \"ship\", \"ship_SE_EWR\", 0.0, 100.0 ],\r\n"
			+ "    [ \"ship\", \"ship_SE_BWI\", 0.0, 100.0 ],\r\n"
			+ "    [ \"ship\", \"ship_SE_ATL\", 0.0, 100.0 ],\r\n"
			+ "    [ \"ship\", \"ship_SE_MCO\", 0.0, 100.0 ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		parameters.show("Ship Parameters", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//selectTest
	
	/**
	 * This test transforms the records in a span using a mapping operation to create variables.
	 */
	static void displayMappingTest1() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
//		showSchema(makeVariable.getResultSchema(), "makeVariable.getResultSchema", Msdx.GLOBAL.out);
		MsdxSpan variables = parameters.map(makeVariable);
		variables.show("Ship Variables", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));				
	}//displayMappingTest1
	
	@Test
	public void mappingTest1() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Ship Variables",
  "SCHEMA": {
    "FIELDS": [ "Name", "Column", "Variable" ],
    "TYPES": [ "STRING", "STRING", "VARIABLE" ]
  },
  "INSTANCE": [
    [ "ship", "ship_PITT_NE", "0.0 <= ship_PITT_NE <= 250.0" ],
    [ "ship", "ship_PITT_SE", "0.0 <= ship_PITT_SE <= 250.0" ],
    [ "ship", "ship_NE_BOS", "0.0 <= ship_NE_BOS <= 100.0" ],
    [ "ship", "ship_NE_EWR", "0.0 <= ship_NE_EWR <= 100.0" ],
    [ "ship", "ship_NE_BWI", "0.0 <= ship_NE_BWI <= 100.0" ],
    [ "ship", "ship_SE_EWR", "0.0 <= ship_SE_EWR <= 100.0" ],
    [ "ship", "ship_SE_BWI", "0.0 <= ship_SE_BWI <= 100.0" ],
    [ "ship", "ship_SE_ATL", "0.0 <= ship_SE_ATL <= 100.0" ],
    [ "ship", "ship_SE_MCO", "0.0 <= ship_SE_MCO <= 100.0" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Ship Variables\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Name\", \"Column\", \"Variable\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"VARIABLE\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"ship\", \"ship_PITT_NE\", \"0.0 <= ship_PITT_NE <= 250.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_PITT_SE\", \"0.0 <= ship_PITT_SE <= 250.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_NE_BOS\", \"0.0 <= ship_NE_BOS <= 100.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_NE_EWR\", \"0.0 <= ship_NE_EWR <= 100.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_NE_BWI\", \"0.0 <= ship_NE_BWI <= 100.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_SE_EWR\", \"0.0 <= ship_SE_EWR <= 100.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_SE_BWI\", \"0.0 <= ship_SE_BWI <= 100.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_SE_ATL\", \"0.0 <= ship_SE_ATL <= 100.0\" ],\r\n"
			+ "    [ \"ship\", \"ship_SE_MCO\", \"0.0 <= ship_SE_MCO <= 100.0\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(makeVariable);
		variables.show("Ship Variables", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//mappingTest1
	
	/**
	 * This test transforms the records in a span using a mapping operation to create constraints.
	 */
	static void displayMappingTest2() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan balance = factory.create(
			mosdex.getTable("balance").getInstance().getDataframe());
		MsdxSpan parameters = balance.select("Name", "Row", "Sense", "RHS");
		
		OperatorWithOneArgument makeConstraint= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Constraint", Constraint.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint((String) parameter.get("Row"), "");
				constraint.setSenseAndRHS(
					(String) parameter.get("Sense"), 
					parameter.get("RHS").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeConstraint*/;/*return*/
		
		MsdxSpan constraints = parameters.map(makeConstraint);
		constraints.show("Balance Constraints", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displayMappingTest2
	
	@Test
	public void mappingTest2() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Balance Constraints",
  "SCHEMA": {
    "FIELDS": [ "Name", "Row", "Constraint", "Expression" ],
    "TYPES": [ "STRING", "STRING", "CONSTRAINT", "EXPRESSION" ]
  },
  "INSTANCE": [
    [ "balance", "balance_PITT", "EQ 450.0", "empty" ],
    [ "balance", "balance_NE", "EQ 0.0", "empty" ],
    [ "balance", "balance_SE", "EQ 0.0", "empty" ],
    [ "balance", "balance_BOS", "EQ -90.0", "empty" ],
    [ "balance", "balance_EWR", "EQ -120.0", "empty" ],
    [ "balance", "balance_BWI", "EQ -120.0", "empty" ],
    [ "balance", "balance_ATL", "EQ -70.0", "empty" ],
    [ "balance", "balance_MCO", "EQ -50.0", "empty" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Balance Constraints\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Name\", \"Row\", \"Constraint\", \"Expression\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"CONSTRAINT\", \"EXPRESSION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance\", \"balance_PITT\", \"EQ 450.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_NE\", \"EQ 0.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_SE\", \"EQ 0.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_BOS\", \"EQ -90.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_EWR\", \"EQ -120.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_BWI\", \"EQ -120.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_ATL\", \"EQ -70.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_MCO\", \"EQ -50.0\", \"empty\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan balance = factory.create(
				mosdex.getTable("balance").getInstance().getDataframe());
			MsdxSpan parameters = balance.select("Name", "Row", "Sense", "RHS");
			
			OperatorWithOneArgument makeConstraint= new OperatorWithOneArgument() {

				@Override
				public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
					if(this.resultSchema != null)
						throw new IllegalStateException("Result schema has already been defined");
					this.resultSchema= MsdxContainer.<Class<?>>builder()
						.copyItem(inputSchema, "Name")
						.copyItem(inputSchema, "Row")
						.addItem("Constraint", Constraint.class)
						.addItem("Expression", Expression.class)
						.build();
					return this;
				}

				@Override
				public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
					Constraint constraint= new Constraint((String) parameter.get("Row"), "");
					constraint.setSenseAndRHS(
						(String) parameter.get("Sense"), 
						parameter.get("RHS").toString());
		
					return MsdxRecord.builder(this.getResultSchema())
						.copyItem(parameter, "Name")
						.copyItem(parameter, "Row")
						.addItem("Constraint", constraint)
						.addItem("Expression", new Expression())
						.build();		
				}//apply
				
			}/*makeConstraint*/;/*return*/
			
			MsdxSpan constraints = parameters.map(makeConstraint);
			constraints.show("Balance Constraints", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//mappingTest2
	
	/**
	 * This test joins the records of two spans to form the product of a coefficient with a variable.
	 */
	static void displayJoinTest1() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(makeVariable);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		
		MsdxSpan joined= balance_shipFrom.leftJoin(
			variables, 
			"Column", 
			MsdxSpan.merge());
		
		OperatorWithOneArgument multiply= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(joined.getSchema())
					.removeItem("Coefficient")
					.removeItem("Name")
					.removeItem("Variable")
					.addItem("Expression", Expression.class)
					.build();
				// termSchema= {Row, Column, Expression};
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> term) {
				Expression product= Expression.multiply(
					term.get("Coefficient").toString(), 
					(Variable) term.get("Variable"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(term, "Row")
					.copyItem(term, "Column")
					.addItem("Expression", product)
					.build();
			}//apply
			
		};//multiply
				
		MsdxSpan terms= joined.map(
			multiply/* .withResultSchema(joined.getSchema()) */);
		terms.show("Balance_shipFrom Terms Joined With Ship Variables", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));				
	}//displayJoinTest1
	
	@Test
	public void joinTest1() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Balance_shipFrom Terms Joined With Ship Variables",
  "SCHEMA": {
    "FIELDS": [ "Row", "Column", "Expression" ],
    "TYPES": [ "STRING", "STRING", "EXPRESSION" ]
  },
  "INSTANCE": [
    [ "balance_PITT", "ship_PITT_NE", "1.0*ship_PITT_NE" ],
    [ "balance_PITT", "ship_PITT_SE", "1.0*ship_PITT_SE" ],
    [ "balance_NE", "ship_NE_BOS", "1.0*ship_NE_BOS" ],
    [ "balance_NE", "ship_NE_EWR", "1.0*ship_NE_EWR" ],
    [ "balance_NE", "ship_NE_BWI", "1.0*ship_NE_BWI" ],
    [ "balance_SE", "ship_SE_EWR", "1.0*ship_SE_EWR" ],
    [ "balance_SE", "ship_SE_BWI", "1.0*ship_SE_BWI" ],
    [ "balance_SE", "ship_SE_ATL", "1.0*ship_SE_ATL" ],
    [ "balance_SE", "ship_SE_MCO", "1.0*ship_SE_MCO" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Balance_shipFrom Terms Joined With Ship Variables\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Row\", \"Column\", \"Expression\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"EXPRESSION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_NE\", \"1.0*ship_PITT_NE\" ],\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_SE\", \"1.0*ship_PITT_SE\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BOS\", \"1.0*ship_NE_BOS\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_EWR\", \"1.0*ship_NE_EWR\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BWI\", \"1.0*ship_NE_BWI\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_EWR\", \"1.0*ship_SE_EWR\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_BWI\", \"1.0*ship_SE_BWI\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_ATL\", \"1.0*ship_SE_ATL\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_MCO\", \"1.0*ship_SE_MCO\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
				mosdex.getTable("ship").getInstance().getDataframe());
			MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
			
			OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
				
				@Override
				public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
					if(this.resultSchema != null)
						throw new IllegalStateException("Result schema has already been defined");
					this.resultSchema= MsdxContainer.<Class<?>>builder()
						.copyItem(inputSchema, "Name")
						.copyItem(inputSchema, "Column")
						.addItem("Variable", Variable.class)
						.build();
					return this;
				}

				@Override
				public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
					Variable variable= new Variable((String) parameter.get("Column"), "");
					variable.setBounds(
						parameter.get("LowerBound").toString(), 
						parameter.get("UpperBound").toString());;
		
					return MsdxRecord.builder(this.getResultSchema())
						.copyItem(parameter, "Name")
						.copyItem(parameter, "Column")
						.addItem("Variable", variable)
						.build();		
				}//apply
				
			};//makeVariable
					
			MsdxSpan variables = parameters.map(makeVariable);
			
			MsdxSpan balance_shipFrom = factory.create(
				mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
			
			MsdxSpan joined= balance_shipFrom.leftJoin(
				variables, 
				"Column", 
				MsdxSpan.merge());
			
			OperatorWithOneArgument multiply= new OperatorWithOneArgument() {

				@Override
				public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
					if(this.resultSchema != null)
						throw new IllegalStateException("Result schema has already been defined");
					this.resultSchema= MsdxContainer.<Class<?>>builder()
						.copyItems(joined.getSchema())
						.removeItem("Coefficient")
						.removeItem("Name")
						.removeItem("Variable")
						.addItem("Expression", Expression.class)
						.build();
					// termSchema= {Row, Column, Expression};
					return this;
				}

				@Override
				public MsdxContainer<Object> apply(MsdxContainer<Object> term) {
					Expression product= Expression.multiply(
						term.get("Coefficient").toString(), 
						(Variable) term.get("Variable"));
					
					return MsdxRecord.builder(this.getResultSchema())
						.copyItem(term, "Row")
						.copyItem(term, "Column")
						.addItem("Expression", product)
						.build();
				}//apply
				
			};//multiply
					
			MsdxSpan terms= joined.map(
				multiply/* .withResultSchema(joined.getSchema()) */);
			terms.show("Balance_shipFrom Terms Joined With Ship Variables", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//joinTest1
	
	/**
	 * This test joins the records of two spans to form the product of a coefficient with a variable, 
	 * using an alternative definition of the multiply operation.
	 */
	static void displayJoinTest1Alt() {
		//Alternative multiply
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(makeVariable);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
//		Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTest1Alt: balance_shipFrom schema= " + balance_shipFrom.getSchema().toString());

		OperatorWithTwoArguments multiply= new OperatorWithTwoArguments() {
			
			String tableType;
			{
				tableType= "LINEAR";
			}
			
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> leftInputSchema,	//either a term parameters schema or a joined schema
				String leftKeyFieldName, 
				MsdxContainer<Class<?>> variablesSchema, 
				String variablesKeyFieldName) 
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= leftInputSchema;			//{Row, Column, nothing or (Variable and Column2), Coefficient}
				this.leftKeyFieldName= leftKeyFieldName;		//should be "Column"
				this.rightInputSchema= variablesSchema;			//{Column, Name, Variable}
				this.rightKeyFieldName= variablesKeyFieldName;	//should be "Column"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(leftInputSchema.merge(variablesSchema.delete(variablesKeyFieldName)))
					.removeItem("Name")
					.removeItem("Variable")
					.removeItem("Coefficient")
					.addItem("Expression", Expression.class)
					.build();
					// termSchema= {Row, Column, Column2 or nothing, Expression};
//					Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTest1Alt: multiply result schema= " + this.resultSchema.toString());
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> variable) {
				Expression product;
				if (this.tableType.equals("LINEAR"))
					product= Expression.multiply(
						left.get("Coefficient").toString(), 
						(Variable) variable.get("Variable"));
				else //tableType.equals("QUADRATIC")
					product= Expression.multiply(
						left.get("Coefficient").toString(),
						(Variable) left.get("Variable"),
						(Variable) variable.get("Variable"));	//Variable2
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left, "Row")
					.copyItem(left, "Column")
					.copyItem(left, "Column2")	//if present
					.addItem("Expression", product)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> left,
				Optional<MsdxContainer<Object>> right) 
			{
				if(!left.isPresent())
					throw new IllegalArgumentException("Missing left container");	//can't happen
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left.get(), "Row")
					.copyItem(left.get(), "Column")
					.copyItem(left.get(), "Column2")	//if present
					.addItem("Expression", null)
					.build();
			}//noKeyMatch
			
		}/*OperatorWithTwoArguments*/;//multiply
					
		MsdxSpan terms= balance_shipFrom.leftJoin(
			variables, 
			"Column", 
			multiply);
			
		terms.show("Balance_shipFrom Terms Joined With Ship Variables", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));				
	}//displayJoinTest1Alt
	
	@Test
	public void joinTest1Alt() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Balance_shipFrom Terms Joined With Ship Variables",
  "SCHEMA": {
    "FIELDS": [ "Row", "Column", "Expression" ],
    "TYPES": [ "STRING", "STRING", "EXPRESSION" ]
  },
  "INSTANCE": [
    [ "balance_PITT", "ship_PITT_NE", "1.0*ship_PITT_NE" ],
    [ "balance_PITT", "ship_PITT_SE", "1.0*ship_PITT_SE" ],
    [ "balance_NE", "ship_NE_BOS", "1.0*ship_NE_BOS" ],
    [ "balance_NE", "ship_NE_EWR", "1.0*ship_NE_EWR" ],
    [ "balance_NE", "ship_NE_BWI", "1.0*ship_NE_BWI" ],
    [ "balance_SE", "ship_SE_EWR", "1.0*ship_SE_EWR" ],
    [ "balance_SE", "ship_SE_BWI", "1.0*ship_SE_BWI" ],
    [ "balance_SE", "ship_SE_ATL", "1.0*ship_SE_ATL" ],
    [ "balance_SE", "ship_SE_MCO", "1.0*ship_SE_MCO" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Balance_shipFrom Terms Joined With Ship Variables\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Row\", \"Column\", \"Expression\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"EXPRESSION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_NE\", \"1.0*ship_PITT_NE\" ],\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_SE\", \"1.0*ship_PITT_SE\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BOS\", \"1.0*ship_NE_BOS\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_EWR\", \"1.0*ship_NE_EWR\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BWI\", \"1.0*ship_NE_BWI\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_EWR\", \"1.0*ship_SE_EWR\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_BWI\", \"1.0*ship_SE_BWI\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_ATL\", \"1.0*ship_SE_ATL\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_MCO\", \"1.0*ship_SE_MCO\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		//Alternative multiply
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(makeVariable);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
//		Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTest1Alt: balance_shipFrom schema= " + balance_shipFrom.getSchema().toString());

		OperatorWithTwoArguments multiply= new OperatorWithTwoArguments() {
			
			String tableType;
			{
				tableType= "LINEAR";
			}
			
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> leftInputSchema,	//either a term parameters schema or a joined schema
				String leftKeyFieldName, 
				MsdxContainer<Class<?>> variablesSchema, 
				String variablesKeyFieldName) 
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= leftInputSchema;			//{Row, Column, nothing or (Variable and Column2), Coefficient}
				this.leftKeyFieldName= leftKeyFieldName;		//should be "Column"
				this.rightInputSchema= variablesSchema;			//{Column, Name, Variable}
				this.rightKeyFieldName= variablesKeyFieldName;	//should be "Column"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(leftInputSchema.merge(variablesSchema.delete(variablesKeyFieldName)))
					.removeItem("Name")
					.removeItem("Variable")
					.removeItem("Coefficient")
					.addItem("Expression", Expression.class)
					.build();
					// termSchema= {Row, Column, Column2 or nothing, Expression};
//					Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTest1Alt: multiply result schema= " + this.resultSchema.toString());
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> variable) {
				Expression product;
				if (this.tableType.equals("LINEAR"))
					product= Expression.multiply(
						left.get("Coefficient").toString(), 
						(Variable) variable.get("Variable"));
				else //tableType.equals("QUADRATIC")
					product= Expression.multiply(
						left.get("Coefficient").toString(),
						(Variable) left.get("Variable"),
						(Variable) variable.get("Variable"));	//Variable2
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left, "Row")
					.copyItem(left, "Column")
					.copyItem(left, "Column2")	//if present
					.addItem("Expression", product)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> left,
				Optional<MsdxContainer<Object>> right) 
			{
				if(!left.isPresent())
					throw new IllegalArgumentException("Missing left container");	//can't happen
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left.get(), "Row")
					.copyItem(left.get(), "Column")
					.copyItem(left.get(), "Column2")	//if present
					.addItem("Expression", null)
					.build();
			}//noKeyMatch
			
		}/*OperatorWithTwoArguments*/;//multiply
					
		MsdxSpan terms= balance_shipFrom.leftJoin(
			variables, 
			"Column", 
			multiply);
			
		terms.show("Balance_shipFrom Terms Joined With Ship Variables", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//joinTest1Alt
	
	/**
	 * This test joins the records of three spans to form the product of a coefficient with two variables.
	 * The operation shows no missing, or unmatched, records.
	 */
	static void displayJoinTestQuadraticTerms() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters
			.map(makeVariable)
			.key("Column");
				
		MsdxSpan total_ship_ship = factory.create(mosdex.getTable("total_ship_ship").getInstance().getDataframe());

//		total_ship_ship.show("Quadratic Terms", Msdx.GLOBAL.out);
		
		OperatorWithTwoArguments merge= MsdxSpan.merge();
		Map<String, Set<Object>> unmatchedKeys= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan joined= total_ship_ship.leftJoin(variables, "Column", merge)
			.map(merge.unmatchedKeys(unmatchedKeys));
//		joined.show("Join With First Variables", Msdx.GLOBAL.out);
			
		OperatorWithTwoArguments multiply= new OperatorWithTwoArguments() {
			
			String tableType;
			{
				tableType= "QUADRATIC";
			}
			
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> leftInputSchema,	//either a term parameters schema or a joined schema
				String leftKeyFieldName, 
				MsdxContainer<Class<?>> variablesSchema, 
				String variablesKeyFieldName) 
			{
//				if(this.resultSchema != null)
//					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= leftInputSchema;			//{Row, Column, nothing or (Variable and Column2), Coefficient}
				this.leftKeyFieldName= leftKeyFieldName;		//should be "Column" or "Column2"
				this.rightInputSchema= variablesSchema;			//{Column or Column2, Name, Variable or Variable2}
				this.rightKeyFieldName= variablesKeyFieldName;	//should be "Column" or "Column2"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(leftInputSchema.merge(variablesSchema.delete(variablesKeyFieldName)))
					.removeItem("Name")
					.removeItem("Kind")
					.removeItem("Variable")
					.removeItem("Variable2")	//if present
					.removeItem("Coefficient")
					.addItem("Expression", Expression.class)
					.build();
					// termSchema= {Row, Column, Column2 or nothing, Expression};
//					Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTest1Alt: multiply result schema= " + this.resultSchema.toString());
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> variable) {
				Expression product;
				if (this.tableType.equals("LINEAR"))
					product= Expression.multiply(
						left.get("Coefficient").toString(), 
						(Variable) variable.get("Variable"));
				else //tableType.equals("QUADRATIC")
					product= Expression.multiply(
						left.get("Coefficient").toString(),
						(Variable) left.get("Variable"),
						(Variable) variable.get("Variable"));	//Variable2
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left, "Row")
					.copyItem(left, "Column")
					.copyItem(left, "Column2")	//if present
					.addItem("Expression", product)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> left,
				Optional<MsdxContainer<Object>> right) 
			{
				if(!left.isPresent())
					throw new IllegalArgumentException("Missing left container");	//can't happen
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left.get(), "Row")
					.copyItem(left.get(), "Column")
					.copyItem(left.get(), "Column2")	//if present
					.addItem("Expression", null)
					.build();
			}//noKeyMatch
			
		}/*OperatorWithTwoArguments*/;//multiply
		
		multiply.withResultSchema(
			joined.getSchema(), "Column2", 
			variables.getSchema()
				.renameField("Column", "Column2")
				.renameField("Variable", "Variable2"), 
			"Column2");			
		MsdxSpan terms= joined.leftJoin(variables, "Column2", multiply)
			.map(multiply.unmatchedKeys(unmatchedKeys));
		terms.show("Balance_shipFrom Terms Joined With Ship Variables", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));				
		Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTestQuadraticTerms: missing= " + unmatchedKeys.get("left").toString());	
	}//displayJoinTestQuadraticTerms
	
	@Test
	public void joinTestQuadraticTerms() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Balance_shipFrom Terms Joined With Ship Variables",
  "SCHEMA": {
    "FIELDS": [ "Row", "Column", "Column2", "Expression" ],
    "TYPES": [ "STRING", "STRING", "STRING", "EXPRESSION" ]
  },
  "INSTANCE": [
    [ "totalCost", "ship_PITT_NE", "ship_PITT_NE", "1.0*ship_PITT_NE^2" ],
    [ "totalCost", "ship_PITT_SE", "ship_PITT_SE", "1.0*ship_PITT_SE^2" ],
    [ "totalCost", "ship_PITT_NE", "ship_PITT_SE", "0.5*ship_PITT_NE*ship_PITT_SE" ],
    [ "totalCost", "ship_PITT_SE", "ship_PITT_NE", "0.5*ship_PITT_SE*ship_PITT_NE" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Balance_shipFrom Terms Joined With Ship Variables\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Row\", \"Column\", \"Column2\", \"Expression\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"EXPRESSION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"totalCost\", \"ship_PITT_NE\", \"ship_PITT_NE\", \"1.0*ship_PITT_NE^2\" ],\r\n"
			+ "    [ \"totalCost\", \"ship_PITT_SE\", \"ship_PITT_SE\", \"1.0*ship_PITT_SE^2\" ],\r\n"
			+ "    [ \"totalCost\", \"ship_PITT_NE\", \"ship_PITT_SE\", \"0.5*ship_PITT_NE*ship_PITT_SE\" ],\r\n"
			+ "    [ \"totalCost\", \"ship_PITT_SE\", \"ship_PITT_NE\", \"0.5*ship_PITT_SE*ship_PITT_NE\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		String expectedMissing= "missing= []";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters
			.map(makeVariable)
			.key("Column");
				
		MsdxSpan total_ship_ship = factory.create(mosdex.getTable("total_ship_ship").getInstance().getDataframe());

		OperatorWithTwoArguments merge= MsdxSpan.merge();
		Map<String, Set<Object>> unmatchedKeys= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan joined= total_ship_ship.leftJoin(variables, "Column", merge)
			.map(merge.unmatchedKeys(unmatchedKeys));
			
		OperatorWithTwoArguments multiply= new OperatorWithTwoArguments() {
			
			String tableType;
			{
				tableType= "QUADRATIC";
			}
			
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> leftInputSchema,	//either a term parameters schema or a joined schema
				String leftKeyFieldName, 
				MsdxContainer<Class<?>> variablesSchema, 
				String variablesKeyFieldName) 
			{
				this.leftInputSchema= leftInputSchema;			//{Row, Column, nothing or (Variable and Column2), Coefficient}
				this.leftKeyFieldName= leftKeyFieldName;		//should be "Column" or "Column2"
				this.rightInputSchema= variablesSchema;			//{Column or Column2, Name, Variable or Variable2}
				this.rightKeyFieldName= variablesKeyFieldName;	//should be "Column" or "Column2"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(leftInputSchema.merge(variablesSchema.delete(variablesKeyFieldName)))
					.removeItem("Name")
					.removeItem("Kind")
					.removeItem("Variable")
					.removeItem("Variable2")	//if present
					.removeItem("Coefficient")
					.addItem("Expression", Expression.class)
					.build();
					// termSchema= {Row, Column, Column2 or nothing, Expression};
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> variable) {
				Expression product;
				if (this.tableType.equals("LINEAR"))
					product= Expression.multiply(
						left.get("Coefficient").toString(), 
						(Variable) variable.get("Variable"));
				else //tableType.equals("QUADRATIC")
					product= Expression.multiply(
						left.get("Coefficient").toString(),
						(Variable) left.get("Variable"),
						(Variable) variable.get("Variable"));	//Variable2
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left, "Row")
					.copyItem(left, "Column")
					.copyItem(left, "Column2")	//if present
					.addItem("Expression", product)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> left,
				Optional<MsdxContainer<Object>> right) 
			{
				if(!left.isPresent())
					throw new IllegalArgumentException("Missing left container");	//can't happen
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left.get(), "Row")
					.copyItem(left.get(), "Column")
					.copyItem(left.get(), "Column2")	//if present
					.addItem("Expression", null)
					.build();
			}//noKeyMatch
			
		}/*OperatorWithTwoArguments*/;//multiply
		
		multiply.withResultSchema(
			joined.getSchema(), "Column2", 
			variables.getSchema()
				.renameField("Column", "Column2")
				.renameField("Variable", "Variable2"), 
			"Column2");			
		MsdxSpan terms= joined.leftJoin(variables, "Column2", multiply)
			.map(multiply.unmatchedKeys(unmatchedKeys));
		terms.show("Balance_shipFrom Terms Joined With Ship Variables", MsdxOutputDestination.toStream(dst));
		String actualMissing= "missing= " + unmatchedKeys.get("left").toString();	
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		assertEquals(expectedMissing, actualMissing);
		
	}//joinTestQuadraticTerms
	
	/**
	 * This test performs a left join of the records of two spans in which additional rows are added.
	 * The operation identifies the missing records, which are unmatched in the join.
	 */
	static void displayJoinTestExtraWarehouses() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(
			makeVariable/* .withResultSchema(parameters.getSchema()) */);
				
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		MsdxSpan moreWarehouses = factory.create(
			mosdex.getTable("moreBalance_shipFrom").getInstance().getDataframe());
		balance_shipFrom= balance_shipFrom.union(moreWarehouses);
		
//		balance_shipFrom.show("More warehouses", Msdx.GLOBAL.out);
		
		OperatorWithTwoArguments joiner= MsdxSpan.merge();
		Map<String, Set<Object>> unmatchedKeys= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan joined= balance_shipFrom.leftJoin(variables, "Column", joiner)
			.map(joiner.unmatchedKeys(unmatchedKeys));
		joined.show("Join With Extra Warehouses", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		Msdx.GLOBAL.out.println("In MsdxSpanOperations.joinTestExtraWarehouses: missing= " + unmatchedKeys.get("left").toString());		
	}//displayJoinTestExtraWarehouses
	
	@Test
	public void joinTestExtraWarehouses() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Join With Extra Warehouses",
  "SCHEMA": {
    "FIELDS": [ "Row", "Column", "Coefficient", "Name", "Variable" ],
    "TYPES": [ "STRING", "STRING", "DOUBLE", "STRING", "VARIABLE" ]
  },
  "INSTANCE": [
    [ "balance_PITT", "ship_PITT_NE", 1.0, "ship", "0.0 <= ship_PITT_NE <= 250.0" ],
    [ "balance_PITT", "ship_PITT_SE", 1.0, "ship", "0.0 <= ship_PITT_SE <= 250.0" ],
    [ "balance_NE", "ship_NE_BOS", 1.0, "ship", "0.0 <= ship_NE_BOS <= 100.0" ],
    [ "balance_NE", "ship_NE_EWR", 1.0, "ship", "0.0 <= ship_NE_EWR <= 100.0" ],
    [ "balance_NE", "ship_NE_BWI", 1.0, "ship", "0.0 <= ship_NE_BWI <= 100.0" ],
    [ "balance_SE", "ship_SE_EWR", 1.0, "ship", "0.0 <= ship_SE_EWR <= 100.0" ],
    [ "balance_SE", "ship_SE_BWI", 1.0, "ship", "0.0 <= ship_SE_BWI <= 100.0" ],
    [ "balance_SE", "ship_SE_ATL", 1.0, "ship", "0.0 <= ship_SE_ATL <= 100.0" ],
    [ "balance_SE", "ship_SE_MCO", 1.0, "ship", "0.0 <= ship_SE_MCO <= 100.0" ],
    [ "balance_NE", "ship_NE_SE ", 1.0, null, null ],
    [ "balance_SE", "ship_SE_SFO", 1.0, null, null ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Join With Extra Warehouses\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\", \"Name\", \"Variable\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"STRING\", \"VARIABLE\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_NE\", 1.0, \"ship\", \"0.0 <= ship_PITT_NE <= 250.0\" ],\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_SE\", 1.0, \"ship\", \"0.0 <= ship_PITT_SE <= 250.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BOS\", 1.0, \"ship\", \"0.0 <= ship_NE_BOS <= 100.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_EWR\", 1.0, \"ship\", \"0.0 <= ship_NE_EWR <= 100.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BWI\", 1.0, \"ship\", \"0.0 <= ship_NE_BWI <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_EWR\", 1.0, \"ship\", \"0.0 <= ship_SE_EWR <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_BWI\", 1.0, \"ship\", \"0.0 <= ship_SE_BWI <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_ATL\", 1.0, \"ship\", \"0.0 <= ship_SE_ATL <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_MCO\", 1.0, \"ship\", \"0.0 <= ship_SE_MCO <= 100.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_SE \", 1.0, null, null ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_SFO\", 1.0, null, null ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		String expectedMissing= "missing= [ship_NE_SE , ship_SE_SFO]";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(
			makeVariable);
				
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		MsdxSpan moreWarehouses = factory.create(
			mosdex.getTable("moreBalance_shipFrom").getInstance().getDataframe());
		balance_shipFrom= balance_shipFrom.union(moreWarehouses);
		
		OperatorWithTwoArguments joiner= MsdxSpan.merge();
		Map<String, Set<Object>> unmatchedKeys= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan joined= balance_shipFrom.leftJoin(variables, "Column", joiner)
			.map(joiner.unmatchedKeys(unmatchedKeys));
		joined.show("Join With Extra Warehouses", MsdxOutputDestination.toStream(dst));
		String actualMissing= "missing= " + unmatchedKeys.get("left").toString();		
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		assertEquals(expectedMissing, actualMissing);
		
	}//joinTestExtraWarehouses
	
	/**
	 * This test performs an inner join of the records of two spans.
	 * The operation discard any missing records, which are unmatched in the join.
	 */
	static void displayJoinTestInner() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(
			makeVariable/* .withResultSchema(parameters.getSchema()) */);
				
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		MsdxSpan moreWarehouses = factory.create(
			mosdex.getTable("moreBalance_shipFrom").getInstance().getDataframe());
		balance_shipFrom= balance_shipFrom.union(moreWarehouses);
		
//		balance_shipFrom.show("More warehouses", Msdx.GLOBAL.out);
		
		OperatorWithTwoArguments joiner= MsdxSpan.merge();
		MsdxSpan joined= balance_shipFrom.leftJoin(variables, "Column", joiner)
			.filter(joiner.inner());

		joined.show("Join Without Extra Warehouses", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displayJoinTestInner
	
	@Test
	public void joinTestInner() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Join Without Extra Warehouses",
  "SCHEMA": {
    "FIELDS": [ "Row", "Column", "Coefficient", "Name", "Variable" ],
    "TYPES": [ "STRING", "STRING", "DOUBLE", "STRING", "VARIABLE" ]
  },
  "INSTANCE": [
    [ "balance_PITT", "ship_PITT_NE", 1.0, "ship", "0.0 <= ship_PITT_NE <= 250.0" ],
    [ "balance_PITT", "ship_PITT_SE", 1.0, "ship", "0.0 <= ship_PITT_SE <= 250.0" ],
    [ "balance_NE", "ship_NE_BOS", 1.0, "ship", "0.0 <= ship_NE_BOS <= 100.0" ],
    [ "balance_NE", "ship_NE_EWR", 1.0, "ship", "0.0 <= ship_NE_EWR <= 100.0" ],
    [ "balance_NE", "ship_NE_BWI", 1.0, "ship", "0.0 <= ship_NE_BWI <= 100.0" ],
    [ "balance_SE", "ship_SE_EWR", 1.0, "ship", "0.0 <= ship_SE_EWR <= 100.0" ],
    [ "balance_SE", "ship_SE_BWI", 1.0, "ship", "0.0 <= ship_SE_BWI <= 100.0" ],
    [ "balance_SE", "ship_SE_ATL", 1.0, "ship", "0.0 <= ship_SE_ATL <= 100.0" ],
    [ "balance_SE", "ship_SE_MCO", 1.0, "ship", "0.0 <= ship_SE_MCO <= 100.0" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Join Without Extra Warehouses\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\", \"Name\", \"Variable\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"STRING\", \"VARIABLE\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_NE\", 1.0, \"ship\", \"0.0 <= ship_PITT_NE <= 250.0\" ],\r\n"
			+ "    [ \"balance_PITT\", \"ship_PITT_SE\", 1.0, \"ship\", \"0.0 <= ship_PITT_SE <= 250.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BOS\", 1.0, \"ship\", \"0.0 <= ship_NE_BOS <= 100.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_EWR\", 1.0, \"ship\", \"0.0 <= ship_NE_EWR <= 100.0\" ],\r\n"
			+ "    [ \"balance_NE\", \"ship_NE_BWI\", 1.0, \"ship\", \"0.0 <= ship_NE_BWI <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_EWR\", 1.0, \"ship\", \"0.0 <= ship_SE_EWR <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_BWI\", 1.0, \"ship\", \"0.0 <= ship_SE_BWI <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_ATL\", 1.0, \"ship\", \"0.0 <= ship_SE_ATL <= 100.0\" ],\r\n"
			+ "    [ \"balance_SE\", \"ship_SE_MCO\", 1.0, \"ship\", \"0.0 <= ship_SE_MCO <= 100.0\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan parameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = parameters.map(
			makeVariable);
				
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		MsdxSpan moreWarehouses = factory.create(
			mosdex.getTable("moreBalance_shipFrom").getInstance().getDataframe());
		balance_shipFrom= balance_shipFrom.union(moreWarehouses);
		
		OperatorWithTwoArguments joiner= MsdxSpan.merge();
		MsdxSpan joined= balance_shipFrom.leftJoin(variables, "Column", joiner)
			.filter(joiner.inner());

		joined.show("Join Without Extra Warehouses", MsdxOutputDestination.toStream(dst));		
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);	
	}//joinTestInner
	
	/**
	 * This test performs an outer join of the records of two spans.
	 */
	static void displayJoinTestOuter() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan balance = factory.create(
			mosdex.getTable("balance").getInstance().getDataframe());
		MsdxSpan parameters = balance.select("Name", "Row", "Sense", "RHS");
		
		OperatorWithOneArgument makeConstraint= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Constraint", Constraint.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint((String) parameter.get("Row"), "");
				constraint.setSenseAndRHS(
					(String) parameter.get("Sense"), 
					parameter.get("RHS").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeConstraint*/;/*return*/
		
		MsdxSpan constraints = parameters.map(makeConstraint);
		
		MsdxSpan totalCost = factory.create(
			mosdex.getTable("totalCost").getInstance().getDataframe());
		MsdxSpan parameters2 = totalCost.select("Name", "Row", "Sense", "Constant");
		
		OperatorWithOneArgument makeObjective= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Objective", Objective.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Objective objective= new Objective(
					(String) parameter.get("Row"),
					(String) parameter.get("Sense"), 
					(Double) parameter.get("Constant"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Objective", objective)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeObjective*/;/*return*/
		
		MsdxSpan objectives = parameters2.map(makeObjective);
		
		MsdxSpan allRows= objectives.outerJoin(constraints, "Row", MsdxSpan.merge());
		
//		allRows.show("All Rows", Msdx.GLOBAL.out, null);
//		allRows.apply().forEach(container -> Msdx.GLOBAL.out.println(container.getItems().toString()));
		allRows.show("All Rows", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displayJoinTestOuter

	@Test
	public void joinTestOuter() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "All Rows",
  "SCHEMA": {
    "FIELDS": [ "Name", "Row", "Objective", "Expression", "Constraint" ],
    "TYPES": [ "STRING", "STRING", "OBJECTIVE", "EXPRESSION", "CONSTRAINT" ]
  },
  "INSTANCE": [
    [ "totalCost", "totalCost", "MINIMIZE ", "empty", null ],
    [ "balance", "balance_PITT", "EQ 450.0", "empty", null ],
    [ "balance", "balance_NE", "EQ 0.0", "empty", null ],
    [ "balance", "balance_SE", "EQ 0.0", "empty", null ],
    [ "balance", "balance_BOS", "EQ -90.0", "empty", null ],
    [ "balance", "balance_EWR", "EQ -120.0", "empty", null ],
    [ "balance", "balance_BWI", "EQ -120.0", "empty", null ],
    [ "balance", "balance_ATL", "EQ -70.0", "empty", null ],
    [ "balance", "balance_MCO", "EQ -50.0", "empty", null ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"All Rows\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Name\", \"Row\", \"Objective\", \"Expression\", \"Constraint\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"OBJECTIVE\", \"EXPRESSION\", \"CONSTRAINT\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"totalCost\", \"totalCost\", \"MINIMIZE \", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_PITT\", \"EQ 450.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_NE\", \"EQ 0.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_SE\", \"EQ 0.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_BOS\", \"EQ -90.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_EWR\", \"EQ -120.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_BWI\", \"EQ -120.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_ATL\", \"EQ -70.0\", \"empty\", null ],\r\n"
			+ "    [ \"balance\", \"balance_MCO\", \"EQ -50.0\", \"empty\", null ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan balance = factory.create(
			mosdex.getTable("balance").getInstance().getDataframe());
		MsdxSpan parameters = balance.select("Name", "Row", "Sense", "RHS");
		
		OperatorWithOneArgument makeConstraint= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Constraint", Constraint.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint((String) parameter.get("Row"), "");
				constraint.setSenseAndRHS(
					(String) parameter.get("Sense"), 
					parameter.get("RHS").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeConstraint*/;/*return*/
		
		MsdxSpan constraints = parameters.map(makeConstraint);
		
		MsdxSpan totalCost = factory.create(
			mosdex.getTable("totalCost").getInstance().getDataframe());
		MsdxSpan parameters2 = totalCost.select("Name", "Row", "Sense", "Constant");
		
		OperatorWithOneArgument makeObjective= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Objective", Objective.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Objective objective= new Objective(
					(String) parameter.get("Row"),
					(String) parameter.get("Sense"), 
					(Double) parameter.get("Constant"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Objective", objective)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeObjective*/;/*return*/
		
		MsdxSpan objectives = parameters2.map(makeObjective);
		
		MsdxSpan allRows= objectives.outerJoin(constraints, "Row", MsdxSpan.merge());
		
		allRows.show("All Rows", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//joinTestOuter
	
	/**
	 * This test performs a reduce operation of the records of a span 
	 * in which the terms are summed to form a linear expression.
	 */
	static void displayReduceTest() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan vParameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = vParameters.map(makeVariable);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		
		MsdxSpan joined= balance_shipFrom.leftJoin(
			variables, 
			"Column",
			MsdxSpan.merge());
		
		OperatorWithOneArgument multiply= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(joined.getSchema())
					.removeItem("Coefficient")
					.removeItem("Name")
					.removeItem("Variable")
					.addItem("Expression", Expression.class)
					.build();
				// termSchema= {Row, Column, Expression};
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> term) {
				Expression product= Expression.multiply(
					term.get("Coefficient").toString(), 
					(Variable) term.get("Variable"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(term, "Row")
					.copyItem(term, "Column")
					.addItem("Expression", product)
					.build();
			}//apply
			
		};//multiply
				
		MsdxSpan terms = joined.map(multiply);
		
		OperatorWithTwoArguments add= new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(MsdxContainer<Class<?>> sumSchema,
					String sumKeyFieldName, MsdxContainer<Class<?>> termSchema, String termKeyFieldName) 
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= sumSchema;
				this.leftKeyFieldName= sumKeyFieldName;
				this.rightInputSchema= termSchema;
				this.rightKeyFieldName= termKeyFieldName;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(termSchema, "Row")
					.addItem("Expression", Expression.class)
					.build();
					//expressionSchema= {Row, Expression}
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> sum, MsdxContainer<Object> term) {
				Expression sumPlusTerm= ((Expression) sum.get("Expression")).add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum, "Row")
					.addItem("Expression", sumPlusTerm)
					.build();
			}

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> sum, Optional<MsdxContainer<Object>> term) {
				if(!sum.isPresent())
					throw new IllegalStateException("Empty sum");
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum.get(), "Row")
					.copyItem(sum.get(), "Expression")	//nothing added
					.build();
			}
				
		};//add
		
		MsdxSpan reduced = terms.reduceByKey("Row",
				add/* .withResultSchema(MsdxContainer.<Class<?>>empty(), terms.getSchema()) */);

//		reduced.show("Balance_shipFrom Terms Reduced by City", Msdx.GLOBAL.out, null);				
		reduced.show("Balance_shipFrom Terms Reduced by Citys", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displayReduceTest
	
	@Test
	public void reduceTest() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Balance_shipFrom Terms Reduced by Citys",
  "SCHEMA": {
    "FIELDS": [ "Row", "Expression" ],
    "TYPES": [ "STRING", "EXPRESSION" ]
  },
  "INSTANCE": [
    [ "balance_PITT", "1.0*ship_PITT_NE + 1.0*ship_PITT_SE" ],
    [ "balance_NE", "1.0*ship_NE_BOS + 1.0*ship_NE_EWR + 1.0*ship_NE_BWI" ],
    [ "balance_SE", "1.0*ship_SE_EWR + 1.0*ship_SE_BWI + 1.0*ship_SE_ATL + 1.0*ship_SE_MCO" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Balance_shipFrom Terms Reduced by Citys\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Row\", \"Expression\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"EXPRESSION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance_PITT\", \"1.0*ship_PITT_NE + 1.0*ship_PITT_SE\" ],\r\n"
			+ "    [ \"balance_NE\", \"1.0*ship_NE_BOS + 1.0*ship_NE_EWR + 1.0*ship_NE_BWI\" ],\r\n"
			+ "    [ \"balance_SE\", \"1.0*ship_SE_EWR + 1.0*ship_SE_BWI + 1.0*ship_SE_ATL + 1.0*ship_SE_MCO\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan vParameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = vParameters.map(makeVariable);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		
		MsdxSpan joined= balance_shipFrom.leftJoin(
			variables, 
			"Column",
			MsdxSpan.merge());
		
		OperatorWithOneArgument multiply= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(joined.getSchema())
					.removeItem("Coefficient")
					.removeItem("Name")
					.removeItem("Variable")
					.addItem("Expression", Expression.class)
					.build();
				// termSchema= {Row, Column, Expression};
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> term) {
				Expression product= Expression.multiply(
					term.get("Coefficient").toString(), 
					(Variable) term.get("Variable"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(term, "Row")
					.copyItem(term, "Column")
					.addItem("Expression", product)
					.build();
			}//apply
			
		};//multiply
				
		MsdxSpan terms = joined.map(multiply);
		
		OperatorWithTwoArguments add= new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(MsdxContainer<Class<?>> sumSchema,
					String sumKeyFieldName, MsdxContainer<Class<?>> termSchema, String termKeyFieldName) 
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= sumSchema;
				this.leftKeyFieldName= sumKeyFieldName;
				this.rightInputSchema= termSchema;
				this.rightKeyFieldName= termKeyFieldName;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(termSchema, "Row")
					.addItem("Expression", Expression.class)
					.build();
					//expressionSchema= {Row, Expression}
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> sum, MsdxContainer<Object> term) {
				Expression sumPlusTerm= ((Expression) sum.get("Expression")).add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum, "Row")
					.addItem("Expression", sumPlusTerm)
					.build();
			}

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> sum, Optional<MsdxContainer<Object>> term) {
				if(!sum.isPresent())
					throw new IllegalStateException("Empty sum");
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum.get(), "Row")
					.copyItem(sum.get(), "Expression")	//nothing added
					.build();
			}
				
		};//add
		
		MsdxSpan reduced = terms.reduceByKey("Row", add);

		reduced.show("Balance_shipFrom Terms Reduced by Citys", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//reduceTest
	
	/**
	 * This test performs a join operation of the records of two spans 
	 * in which linear expressions are joined with constraints. 
	 * Note that in this example, not all the constraints have associated linear expressions; 
	 * these are marked as empty in the result span.
	 */
	static void displayJoinTest2() {
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan vParameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = vParameters.map(
			makeVariable/* .withResultSchema(vParameters.getSchema()) */);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		
		MsdxSpan joined= balance_shipFrom.leftJoin(
			variables, 
			"Column", 
			MsdxSpan.merge());
					
		OperatorWithOneArgument multiply= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(joined.getSchema())
					.removeItem("Coefficient")
					.removeItem("Name")
					.removeItem("Variable")
					.addItem("Expression", Expression.class)
					.build();
				// termSchema= {Row, Column, Expression};
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> term) {
				Expression product= Expression.multiply(
					term.get("Coefficient").toString(), 
					(Variable) term.get("Variable"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(term, "Row")
					.copyItem(term, "Column")
					.addItem("Expression", product)
					.build();
			}//apply
			
		};//multiply
				
		MsdxSpan terms = joined.map(multiply/* .withResultSchema(joined.getSchema()) */);
		
		OperatorWithTwoArguments add= new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(MsdxContainer<Class<?>> sumSchema,
					String sumKeyFieldName, MsdxContainer<Class<?>> termSchema, String termKeyFieldName) 
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= sumSchema;
				this.leftKeyFieldName= sumKeyFieldName;
				this.rightInputSchema= termSchema;
				this.rightKeyFieldName= termKeyFieldName;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(termSchema, "Row")
					.addItem("Expression", Expression.class)
					.build();
					//expressionSchema= {Row, Expression}
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> sum, MsdxContainer<Object> term) {
				Expression sumPlusTerm= ((Expression) sum.get("Expression")).add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum, "Row")
					.addItem("Expression", sumPlusTerm)
					.build();
			}

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> sum, Optional<MsdxContainer<Object>> term) {
				if(!sum.isPresent())
					throw new IllegalStateException("Empty sum");
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum.get(), "Row")
					.copyItem(sum.get(), "Expression")	//nothing added
					.build();
			}
				
		};//add
		
		MsdxSpan reduced = terms.reduceByKey("Row",
				add/* .withResultSchema(MsdxContainer.<Class<?>>empty(), terms.getSchema()) */);

		MsdxSpan balance = factory.create(
			mosdex.getTable("balance").getInstance().getDataframe());
		MsdxSpan cParameters = balance.select("Name", "Row", "Sense", "RHS");
		
		OperatorWithOneArgument makeConstraint= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Constraint", Constraint.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint((String) parameter.get("Row"), "");
				constraint.setSenseAndRHS(
					(String) parameter.get("Sense"), 
					parameter.get("RHS").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeConstraint*/;/*return*/
		
		MsdxSpan constraints = cParameters.map(makeConstraint/* .withResultSchema(cParameters.getSchema()) */);
		
		OperatorWithTwoArguments combiner= new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> constraintsSchema, 
				String constraintsKeyFieldName, 
				MsdxContainer<Class<?>> reducedSchema,
				String reducedSchemaKeyFieldName)
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= constraintsSchema;
				this.leftKeyFieldName= constraintsKeyFieldName;
				this.rightInputSchema= reducedSchema;
				this.rightKeyFieldName= reducedSchemaKeyFieldName;
				this.resultSchema= constraintsSchema.merge(reducedSchema.delete(reducedSchemaKeyFieldName));
				// join2Schema= {Row, Constraint, Expression};
				return this;
			}

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> constraint, MsdxContainer<Object> term) {
				Expression expression= (Expression) constraint.get("Expression");
				Expression sum;
				if(expression==null)
					sum= new Expression();
				sum= expression.add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(constraint, "Name")
					.copyItem(constraint, "Row")
					.addItem("Expression", sum)
					.copyItem(constraint, "Constraint")
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> constraint, Optional<MsdxContainer<Object>> term) {
				if(!constraint.isPresent())
					throw new IllegalStateException("Empty constraint");
				return constraint.get();	//nothing attached to current constraint
			}
			
		};//combiner
			
		MsdxSpan joined2= constraints.leftJoin(reduced, "Row", combiner);	
		joined2.show("Balance Constraints joined with Reduced Balance_shipFrom Terms", MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
	}//displayJoinTest2
	
	@Test
	public void joinTest2() throws JSONException {
		
		String expected= 
/*			'''				
{
  "NAME": "Balance Constraints joined with Reduced Balance_shipFrom Terms",
  "SCHEMA": {
    "FIELDS": [ "Name", "Row", "Constraint", "Expression" ],
    "TYPES": [ "STRING", "STRING", "CONSTRAINT", "EXPRESSION" ]
  },
  "INSTANCE": [
    [ "balance", "balance_PITT", "1.0*ship_PITT_NE + 1.0*ship_PITT_SE", "EQ 450.0" ],
    [ "balance", "balance_NE", "1.0*ship_NE_BOS + 1.0*ship_NE_EWR + 1.0*ship_NE_BWI", "EQ 0.0" ],
    [ "balance", "balance_SE", "1.0*ship_SE_EWR + 1.0*ship_SE_BWI + 1.0*ship_SE_ATL + 1.0*ship_SE_MCO", "EQ 0.0" ],
    [ "balance", "balance_BOS", "EQ -90.0", "empty" ],
    [ "balance", "balance_EWR", "EQ -120.0", "empty" ],
    [ "balance", "balance_BWI", "EQ -120.0", "empty" ],
    [ "balance", "balance_ATL", "EQ -70.0", "empty" ],
    [ "balance", "balance_MCO", "EQ -50.0", "empty" ]
  ]
}
			'''
*/
			"{\r\n"
			+ "  \"NAME\": \"Balance Constraints joined with Reduced Balance_shipFrom Terms\",\r\n"
			+ "  \"SCHEMA\": {\r\n"
			+ "    \"FIELDS\": [ \"Name\", \"Row\", \"Constraint\", \"Expression\" ],\r\n"
			+ "    \"TYPES\": [ \"STRING\", \"STRING\", \"CONSTRAINT\", \"EXPRESSION\" ]\r\n"
			+ "  },\r\n"
			+ "  \"INSTANCE\": [\r\n"
			+ "    [ \"balance\", \"balance_PITT\", \"1.0*ship_PITT_NE + 1.0*ship_PITT_SE\", \"EQ 450.0\" ],\r\n"
			+ "    [ \"balance\", \"balance_NE\", \"1.0*ship_NE_BOS + 1.0*ship_NE_EWR + 1.0*ship_NE_BWI\", \"EQ 0.0\" ],\r\n"
			+ "    [ \"balance\", \"balance_SE\", \"1.0*ship_SE_EWR + 1.0*ship_SE_BWI + 1.0*ship_SE_ATL + 1.0*ship_SE_MCO\", \"EQ 0.0\" ],\r\n"
			+ "    [ \"balance\", \"balance_BOS\", \"EQ -90.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_EWR\", \"EQ -120.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_BWI\", \"EQ -120.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_ATL\", \"EQ -70.0\", \"empty\" ],\r\n"
			+ "    [ \"balance\", \"balance_MCO\", \"EQ -50.0\", \"empty\" ]\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile mosdex= transshipment();
		MsdxJavaSpan.Factory factory = new MsdxJavaSpan.Factory();
		//Test Code
		MsdxSpan ship = factory.create(
			mosdex.getTable("ship").getInstance().getDataframe());
		MsdxSpan vParameters = ship.select("Name", "Column", "LowerBound", "UpperBound");
		
		OperatorWithOneArgument makeVariable= new OperatorWithOneArgument() {
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Column")
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply
			
		};//makeVariable
				
		MsdxSpan variables = vParameters.map(
			makeVariable/* .withResultSchema(vParameters.getSchema()) */);
		
		MsdxSpan balance_shipFrom = factory.create(
			mosdex.getTable("balance_shipFrom").getInstance().getDataframe());
		
		MsdxSpan joined= balance_shipFrom.leftJoin(
			variables, 
			"Column", 
			MsdxSpan.merge());
					
		OperatorWithOneArgument multiply= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(joined.getSchema())
					.removeItem("Coefficient")
					.removeItem("Name")
					.removeItem("Variable")
					.addItem("Expression", Expression.class)
					.build();
				// termSchema= {Row, Column, Expression};
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> term) {
				Expression product= Expression.multiply(
					term.get("Coefficient").toString(), 
					(Variable) term.get("Variable"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(term, "Row")
					.copyItem(term, "Column")
					.addItem("Expression", product)
					.build();
			}//apply
			
		};//multiply
				
		MsdxSpan terms = joined.map(multiply);
		
		OperatorWithTwoArguments add= new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(MsdxContainer<Class<?>> sumSchema,
					String sumKeyFieldName, MsdxContainer<Class<?>> termSchema, String termKeyFieldName) 
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= sumSchema;
				this.leftKeyFieldName= sumKeyFieldName;
				this.rightInputSchema= termSchema;
				this.rightKeyFieldName= termKeyFieldName;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(termSchema, "Row")
					.addItem("Expression", Expression.class)
					.build();
					//expressionSchema= {Row, Expression}
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> sum, MsdxContainer<Object> term) {
				Expression sumPlusTerm= ((Expression) sum.get("Expression")).add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum, "Row")
					.addItem("Expression", sumPlusTerm)
					.build();
			}

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> sum, Optional<MsdxContainer<Object>> term) {
				if(!sum.isPresent())
					throw new IllegalStateException("Empty sum");
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(sum.get(), "Row")
					.copyItem(sum.get(), "Expression")	//nothing added
					.build();
			}
				
		};//add
		
		MsdxSpan reduced = terms.reduceByKey("Row", add);

		MsdxSpan balance = factory.create(
			mosdex.getTable("balance").getInstance().getDataframe());
		MsdxSpan cParameters = balance.select("Name", "Row", "Sense", "RHS");
		
		OperatorWithOneArgument makeConstraint= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(inputSchema, "Name")
					.copyItem(inputSchema, "Row")
					.addItem("Constraint", Constraint.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint((String) parameter.get("Row"), "");
				constraint.setSenseAndRHS(
					(String) parameter.get("Sense"), 
					parameter.get("RHS").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.addItem("Expression", new Expression())
					.build();		
			}//apply
			
		}/*makeConstraint*/;/*return*/
		
		MsdxSpan constraints = cParameters.map(makeConstraint);
		
		OperatorWithTwoArguments combiner= new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> constraintsSchema, 
				String constraintsKeyFieldName, 
				MsdxContainer<Class<?>> reducedSchema,
				String reducedSchemaKeyFieldName)
			{
				if(this.resultSchema != null)
					throw new IllegalStateException("Result schema has already been defined");
				this.leftInputSchema= constraintsSchema;
				this.leftKeyFieldName= constraintsKeyFieldName;
				this.rightInputSchema= reducedSchema;
				this.rightKeyFieldName= reducedSchemaKeyFieldName;
				this.resultSchema= constraintsSchema.merge(reducedSchema.delete(reducedSchemaKeyFieldName));
				// join2Schema= {Row, Constraint, Expression};
				return this;
			}

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> constraint, MsdxContainer<Object> term) {
				Expression expression= (Expression) constraint.get("Expression");
				Expression sum;
				if(expression==null)
					sum= new Expression();
				sum= expression.add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(constraint, "Name")
					.copyItem(constraint, "Row")
					.addItem("Expression", sum)
					.copyItem(constraint, "Constraint")
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> constraint, Optional<MsdxContainer<Object>> term) {
				if(!constraint.isPresent())
					throw new IllegalStateException("Empty constraint");
				return constraint.get();	//nothing attached to current constraint
			}
			
		};//combiner
			
		MsdxSpan joined2= constraints.leftJoin(reduced, "Row", combiner);	
		joined2.show("Balance Constraints joined with Reduced Balance_shipFrom Terms", MsdxOutputDestination.toStream(dst));
		//End Test Code
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//joinTest2
	
	static class Variable {
		
		String columnId;
		String bounds;
	
		Variable(String columnId, String bounds) {
			super();
			this.columnId = columnId;
			this.bounds = bounds;
		}

		String getColumnId() {
			return columnId;
		}

		void setColumnId(String name) {
			this.columnId = name;
		}

		String getBounds() {
			return bounds;
		}

		void setBounds(String lb, String ub) {
			StringBuffer bnds= new StringBuffer();
			bnds.append(lb);
			bnds.append(" <= ");
			bnds.append(this.getColumnId());
			bnds.append(" <= ");
			bnds.append(ub); 
			this.bounds = bnds.toString();
		}

		@Override
		public String toString() {
			return bounds;
		}

	}//class Variable

	static class Constraint {
		
		String rowId;
		String senseAndRHS;
	
		Constraint(String rowId, String senseAndRHS) {
			super();
			this.rowId = rowId;
			this.senseAndRHS = senseAndRHS;
		}

		String getRowId() {
			return rowId;
		}

		void setRowId(String name) {
			this.rowId = name;
		}

		String getSenseAndRHS() {
			return senseAndRHS;
		}

		void setSenseAndRHS(String sense, String RHS) {
			StringBuffer sns = new StringBuffer();
			sns.append(sense);
			sns.append(" ");
			sns.append(RHS);
			this.senseAndRHS = sns.toString();		
		}

		@Override
		public String toString() {
			return senseAndRHS;
		}
	
	}//class Constraint

	static class Objective {
		
		String rowId;
		String sense;
		String constant;
	
		Objective(String rowId, String sense, Double constant) {
			super();
			this.rowId = rowId;
			this.sense = sense;
			this.setConstant(constant);
		}

		String getRowId() {
			return rowId;
		}

		void setRowId(String name) {
			this.rowId = name;
		}

		String getSense() {
			return sense;
		}

		void setSense(String sense) {
			this.sense = sense;		
		}

		String getConstant() {
			return constant;
		}

		void setConstant(Double constant) {
			this.constant = (Math.abs(constant) < 2.0 * Double.MIN_VALUE) ? 
				"" :								//constant == 0
				constant.toString();				//constant != 0
			if(constant > 2.0 * Double.MIN_VALUE)	//constant > 0
				this.constant= '+' + this.constant;
		}

		@Override
		public String toString() {
			StringBuffer sns = new StringBuffer();
			sns.append(sense);
			sns.append(" ");
			sns.append(constant);
			return sns.toString();
		}
		
	}//class Objective
	
	static class Expression {
		
		StringBuilder contents;
	
		public Expression(StringBuilder contents) {
			this.contents= contents;
		}

		public Expression() {
			this(new StringBuilder());
		}

		public Expression(Expression expression) {
			this(expression.get());
		}

		public Expression(String contents) {
			this(new StringBuilder(contents));
		}

		public Expression append(String coefficient) {
			return new Expression(contents.append(coefficient));
		}
		
		public Expression append(char c) {
			return new Expression(contents.append(c));		
		}
		
		public Expression append(Expression expr) {
			return new Expression(contents.append(expr.get()));
		}
		
		public Expression append(StringBuilder str) {
			return new Expression(contents.append(str));
		}
		
		public static Expression multiply(String coefficient, Variable variable) {
			return (new Expression())
				.append(coefficient)
				.append('*')
				.append(variable.getColumnId());	
		}

		public static Expression multiply(String coefficient, Variable variable, Variable variable2) {
			return (new Expression())
				.append(coefficient)
				.append('*')
				.append(variable.getColumnId())
				.append((variable2.getColumnId().equals(variable.getColumnId()) ?
						"^2" :
						"*" + variable2.getColumnId()));
		}

		public Expression add(Expression terms) {
			return new Expression(this
				.append(this.length()==0 || terms.get().charAt(0)=='-' ? "" : " + ")
				.append(terms.get()));
		}

		public int length() {
			return contents.length();
		}

		StringBuilder get() {
			return contents;
		}

		void set(StringBuilder contents) {
			this.contents = contents;
		}

		public String toString() {
			return contents.length()>0 ? contents.toString() : "empty";
		}
	
	}//class Expression
	
 
}//class MsdxSpanOperations
