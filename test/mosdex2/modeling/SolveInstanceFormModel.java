/**
 * 
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.util.Map;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxSparkDataframe;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxFile;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxTable;
import io.github.JeremyBloom.mosdex2.span.MsdxJavaSpan;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;

/**
 * Tests MOSDEX using a transshipment problem in instance form.
 * Includes retrieved solution values
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class SolveInstanceFormModel {

	static MsdxSparkDataframe.Factory dfFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
	
	/**
	 * 
	 */
	public SolveInstanceFormModel() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("Transshipment MOSDEX Instance-Form Demo: CPLEX");
		Msdx.GLOBAL.showDisplay();
		
		Msdx.GLOBAL.out.println("CPLEX Solve Test");
		cplex();
		Msdx.GLOBAL.out.println();
		
	}//main

	/**
	 * Setup the transshipment model.
	 */
	public static MsdxFile transshipment() {
		
		String mosdexFile= 
			/*'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json",
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
        "NOTICES": ["Copyright 2019 Jeremy A. Bloom"],
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
    }
  ]
}
			'''*/
		"{\r\n"
		+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json\",\r\n"
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
		+ "        \"NOTICES\": [\"Copyright 2019 Jeremy A. Bloom\"],\r\n"
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
		+ "    }\r\n"
		+ "  ]\r\n"
		+ "}\r\n"
		+ "\r\n";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
				dfFactory, 
				Msdx.GLOBAL.mapper, 
				false);
				
		MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
		MsdxFile mosdex= factory.readFile(src);
		return mosdex;			
	}//transshipment
	
	public static void cplex() {
		MsdxFile mosdex= transshipment();
		MsdxSpan.Factory spanFactory = new MsdxJavaSpan.Factory();
		MsdxSolverModelingFactory modelingFactory= null;
		try {
			modelingFactory = new MsdxCplexModelingFactory(new IloCplex(), dfFactory);
		} catch (IloException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		MsdxModel model= new MsdxModel("Transshipment", modelingFactory, spanFactory, dfFactory);
		model.createModelingObjects(mosdex.getTables());
		
		Msdx.GLOBAL.out.println("MOSDEX Input for " + model.getModelName());
		Map<String, MsdxTable> tables= mosdex.getTables();
		MsdxTable table;
		for(String tableName: tables.keySet()) {
			Msdx.GLOBAL.out.println("SolveInstanceFormModel.cplex: table - " + tableName);
			table=  mosdex.getTable(tableName);
//			table.getInstance().showSchema(Msdx.GLOBAL.out);
//			table.getInstance().showRecords(Msdx.GLOBAL.out);
			table.getInstance().getSchema().show(Msdx.GLOBAL.out);
			table.getInstance().show(Msdx.GLOBAL.out);
			Msdx.GLOBAL.out.println();
		}		
		
		Msdx.GLOBAL.out.println();
		
		modelingFactory.withName(model.getModelName());
		
		Msdx.GLOBAL.out.println("CPLEX LP file for " + modelingFactory.getName());
		modelingFactory.generate(model, Msdx.GLOBAL.out);
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("CPLEX Solution for " + modelingFactory.getName());
		modelingFactory.solve(model, Msdx.GLOBAL.out);
		Msdx.GLOBAL.out.println();
		
		model.createSolutionObjects(mosdex.getTables());
		
		Msdx.GLOBAL.out.println("MOSDEX Output for " + model.getModelName());
		mosdex.write(MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		Msdx.GLOBAL.out.println();
	}//cplex
	
	
}//class SolveInstanceFormModel
