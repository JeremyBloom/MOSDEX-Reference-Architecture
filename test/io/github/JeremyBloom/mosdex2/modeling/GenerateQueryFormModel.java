/**
 * 
 */
package io.github.JeremyBloom.mosdex2.modeling;


import java.util.Map;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxSparkDataframe;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxFile;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxTable;
import io.github.JeremyBloom.mosdex2.span.MsdxJavaSpan;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;

/**
 * Tests MOSDEX using a transshipment problem with query tables.
 * Produces .lp and .mps files.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class GenerateQueryFormModel {

	static MsdxSparkDataframe.Factory dfFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
	
	/**
	 * 
	 */
	public GenerateQueryFormModel() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("Transshipment MOSDEX Query-Form Demo: Spark, LP, MPS");
		Msdx.GLOBAL.showDisplay();
		
		Msdx.GLOBAL.out.println("MOSDEX Input File");
		json();
		Msdx.GLOBAL.out.println();

		Msdx.GLOBAL.out.println("Spark Dataframes");
		spark();
		Msdx.GLOBAL.out.println();

		Msdx.GLOBAL.out.println("LP Output File");
		lp();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MPS Output File");
		mps();
		
		
	}//main
	
	/**
		 * Setup the transshipment model.
		 */
		public static MsdxFile transshipment() {
			
			String mosdexFile= 
				/*'''
	{
	  "SYNTAX": "MOSDEX/MOSDEX v1-2/MOSDEXSchemaV1-2.json",
	  "MODULES": [
	    {
	      "NAME": "data",
	      "CLASS": "DATA",
	      "HEADING": {
	        "DESCRIPTION": ["Data for General Transshipment Problem"]
	      },
	      "TABLES": [
	        {
	        "NAME": "cities",
	        "CLASS": "DATA",
	        "KIND": "INPUT",
	        "SCHEMA": {
	          "FIELDS": [ "city",   "supply",  "demand"],
	          "TYPES": [  "STRING", "DOUBLE",  "DOUBLE"]
	        },
	        "INSTANCE": [
	          [           "PITT",   450.0,     0.0    ],
	          [           "NE",     0.0,       0.0    ],
	          [           "SE",     0.0,       0.0    ],
	          [           "BOS",    0.0,       90.0   ],
	          [           "EWR",    0.0,       120.0  ],
	          [           "BWI",    0.0,       120.0  ],
	          [           "ATL",    0.0,       70.0   ],
	          [           "MCO",    0.0,       50.0   ]
	        ]
	        },
	        {
	          "NAME": "routes",
	          "CLASS": "DATA",
	          "KIND": "INPUT",
	          "SCHEMA": {
	            "FIELDS": [ "origin",  "destination", "cost",   "capacity" ],
	            "TYPES": [  "STRING",  "STRING",      "DOUBLE", "DOUBLE"]
	          },
	          "INSTANCE": [
	            [           "PITT",    "NE",           2.5,     250.0     ],
	            [           "PITT",    "SE",           3.5,     250.0     ],
	            [           "NE",      "BOS",          1.7,     100.0     ],
	            [           "NE",      "EWR",          0.7,     100.0     ],
	            [           "NE",      "BWI",          1.3,     100.0     ],
	            [           "SE",      "EWR",          1.3,     100.0     ],
	            [           "SE",      "BWI",          0.8,     100.0     ],
	            [           "SE",      "ATL",          0.2,     100.0     ],
	            [           "SE",      "MCO",          2.1,     100.0     ]
	          ]
	        }
	      ]
	    },
	    {
	      "NAME": "modelingObjects",
	      "CLASS": "MODEL",
	      "HEADING": {
	        "DESCRIPTION": [
	          "General Transshipment Problem",
	          "query form",
	          "MOSDEX 2-0 Syntax"
	        ],
	        "VERSION": "net1a 2-1",
	        "REFERENCE": ["https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod"],
	        "AUTHOR": ["Jeremy A. Bloom (jeremyblmca@gmail.com)"],
	        "NOTICES": ["Copyright © 2019 Jeremy A. Bloom"],
	        "MATH": [
	          "var Ship {(i,j) in ROUTES} >= 0, <= capacity[i,j]; # packages to be shipped",
	          "minimize Total_Cost: sum {(i,j) in ROUTES} cost[i,j] * Ship[i,j];",
	          "subject to",
	            "Balance {k in CITIES}: ",
	              "sum {(k,j) in ROUTES} Ship[k,j] - sum {(i,k) in ROUTES} Ship[i,k] = supply[k] - demand[k];"
	        ]
	      },
	      "TABLES": [
	        {
	          "NAME":"ship",
	          "CLASS": "VARIABLE",
	          "KIND": "CONTINUOUS",
	          "QUERY": {
	            "SELECT": [
	              "'ship' AS Name                                          -- STRING",
	              "routes.origin AS origin                                 -- STRING",
	              "routes.destination AS destination                       -- STRING",
	              "CONCAT('ship', '_', origin, '_', destination) AS Column -- STRING",
	              "CAST(0.0 AS DOUBLE) AS LowerBound                       -- DOUBLE",
	              "routes.capacity AS UpperBound                           -- DOUBLE",
				  "'PrimalValue(Column)' AS value                          -- DOUBLE_FUNCTION"
	            ],
	            "FROM": "routes"
	          }
	        },
	        {
	          "NAME": "balance",
	          "CLASS": "CONSTRAINT",
	          "KIND": "LINEAR",
	          "QUERY": {
	            "SELECT": [
	              "'balance' AS Name                                       -- STRING",
	              "cities.city AS city                                     -- STRING",
	              "CONCAT('balance', '_', city) AS Row                     -- STRING",
	              "'EQ' AS Sense                                           -- STRING",
	              "(cities.supply-cities.demand) AS RHS                    -- DOUBLE",
				  "'DualValue(Row)' AS dual                                -- DOUBLE_FUNCTION"
	            ],
	            "FROM": "cities"
	          }
	        },
	        {
	          "NAME": "totalCost",
	          "CLASS": "OBJECTIVE",
	          "KIND": "LINEAR",
	          "SCHEMA": {
	            "FIELDS": [  "Name",      "Row",       "Constant", "Sense",     "cost"],
	            "TYPES": [   "STRING",    "STRING",    "DOUBLE",   "STRING",    "DOUBLE_FUNCTION"]
	          },
	          "INSTANCE": [[ "totalCost", "totalCost",  0.0,       "MINIMIZE",  "ObjectiveValue(Row)"]]
	        },
	        {
	          "NAME": "balance_shipFrom",
	          "CLASS": "TERM",
	          "KIND": "LINEAR",
	          "QUERY": {
	            "SELECT": [
	              "balance.Row AS Row -- STRING",
	              "ship.Column AS Column -- STRING",
	              "CAST(1.0 AS DOUBLE) AS Coefficient -- DOUBLE"
	            ],
	            "FROM": "balance",
	            "JOIN": "ship",
	            "ON": "balance.city = ship.origin"
	            }
	        },
	        {
	          "NAME": "balance_shipTo",
	          "CLASS": "TERM",
	          "KIND": "LINEAR",
	          "QUERY": {
	            "SELECT": [
	              "balance.Row AS Row -- STRING",
	              "ship.Column AS Column -- STRING",
	              "CAST(-1.0 AS DOUBLE) AS Coefficient -- DOUBLE"
	            ],
	            "FROM": "balance",
	            "JOIN": "ship",
	            "ON":  "balance.city = ship.destination"
	          }
	        },
	        {
	          "NAME": "total_ship",
	          "CLASS": "TERM",
	          "KIND": "LINEAR",
	          "QUERY": {
	            "SELECT": [
	              "totalCost.Row AS Row -- STRING",
	              "ship.Column AS Column -- STRING",
	              "routes.cost AS Coefficient -- DOUBLE"
	            ],
	            "FROM": "totalCost",
	            "CROSS JOIN": "ship",
	            "JOIN": "routes",
	            "ON": "routes.origin = ship.origin AND routes.destination = ship.destination"
	          }
	        }
	      ]
	    },
	    {
	      "NAME": "results",
	      "KIND": "MODULE",
	      "HEADING": {
	        "DESCRIPTION": ["Results from General Transshipment Problem"]
	      },
	      "TABLES": [
	        {
	          "NAME": "shipments",
	          "CLASS": "DATA",
	          "KIND": "OUTPUT",
	          "QUERY": {
	            "SELECT": [
	              "ship.origin AS origin           -- STRING",
	              "ship.destination AS destination -- STRING",
	              "ship.value AS value             -- DOUBLE"
	            ],
	            "FROM": "ship"
	          }
	        },
	        {
	          "NAME": "objective",
	          "CLASS": "DATA",
	          "KIND": "OUTPUT",
	          "QUERY": {
	            "SELECT": ["totalCost.cost AS cost -- DOUBLE"],
	            "FROM": "totalCost"
	          }
	        }
	      ]
	    }
	  ]
	}
				'''*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v1-2/MOSDEXSchemaV1-2.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"data\",\r\n"
			+ "      \"CLASS\": \"DATA\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\"Data for General Transshipment Problem\"]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "        \"NAME\": \"cities\",\r\n"
			+ "        \"CLASS\": \"DATA\",\r\n"
			+ "        \"KIND\": \"INPUT\",\r\n"
			+ "        \"SCHEMA\": {\r\n"
			+ "          \"FIELDS\": [ \"city\",   \"supply\",  \"demand\"],\r\n"
			+ "          \"TYPES\": [  \"STRING\", \"DOUBLE\",  \"DOUBLE\"]\r\n"
			+ "        },\r\n"
			+ "        \"INSTANCE\": [\r\n"
			+ "          [           \"PITT\",   450.0,     0.0    ],\r\n"
			+ "          [           \"NE\",     0.0,       0.0    ],\r\n"
			+ "          [           \"SE\",     0.0,       0.0    ],\r\n"
			+ "          [           \"BOS\",    0.0,       90.0   ],\r\n"
			+ "          [           \"EWR\",    0.0,       120.0  ],\r\n"
			+ "          [           \"BWI\",    0.0,       120.0  ],\r\n"
			+ "          [           \"ATL\",    0.0,       70.0   ],\r\n"
			+ "          [           \"MCO\",    0.0,       50.0   ]\r\n"
			+ "        ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"routes\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"INPUT\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"origin\",  \"destination\", \"cost\",   \"capacity\" ],\r\n"
			+ "            \"TYPES\": [  \"STRING\",  \"STRING\",      \"DOUBLE\", \"DOUBLE\"]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [           \"PITT\",    \"NE\",           2.5,     250.0     ],\r\n"
			+ "            [           \"PITT\",    \"SE\",           3.5,     250.0     ],\r\n"
			+ "            [           \"NE\",      \"BOS\",          1.7,     100.0     ],\r\n"
			+ "            [           \"NE\",      \"EWR\",          0.7,     100.0     ],\r\n"
			+ "            [           \"NE\",      \"BWI\",          1.3,     100.0     ],\r\n"
			+ "            [           \"SE\",      \"EWR\",          1.3,     100.0     ],\r\n"
			+ "            [           \"SE\",      \"BWI\",          0.8,     100.0     ],\r\n"
			+ "            [           \"SE\",      \"ATL\",          0.2,     100.0     ],\r\n"
			+ "            [           \"SE\",      \"MCO\",          2.1,     100.0     ]\r\n"
			+ "          ]\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"modelingObjects\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\r\n"
			+ "          \"General Transshipment Problem\",\r\n"
			+ "          \"query form\",\r\n"
			+ "          \"MOSDEX 2-0 Syntax\"\r\n"
			+ "        ],\r\n"
			+ "        \"VERSION\": \"net1a 2-1\",\r\n"
			+ "        \"REFERENCE\": [\"https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod\"],\r\n"
			+ "        \"AUTHOR\": [\"Jeremy A. Bloom (jeremyblmca@gmail.com)\"],\r\n"
			+ "        \"NOTICES\": [\"Copyright © 2019 Jeremy A. Bloom\"],\r\n"
			+ "        \"MATH\": [\r\n"
			+ "          \"var Ship {(i,j) in ROUTES} >= 0, <= capacity[i,j]; # packages to be shipped\",\r\n"
			+ "          \"minimize Total_Cost: sum {(i,j) in ROUTES} cost[i,j] * Ship[i,j];\",\r\n"
			+ "          \"subject to\",\r\n"
			+ "            \"Balance {k in CITIES}: \",\r\n"
			+ "              \"sum {(k,j) in ROUTES} Ship[k,j] - sum {(i,k) in ROUTES} Ship[i,k] = supply[k] - demand[k];\"\r\n"
			+ "        ]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\":\"ship\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"'ship' AS Name                                          -- STRING\",\r\n"
			+ "              \"routes.origin AS origin                                 -- STRING\",\r\n"
			+ "              \"routes.destination AS destination                       -- STRING\",\r\n"
			+ "              \"CONCAT('ship', '_', origin, '_', destination) AS Column -- STRING\",\r\n"
			+ "              \"CAST(0.0 AS DOUBLE) AS LowerBound                       -- DOUBLE\",\r\n"
			+ "              \"routes.capacity AS UpperBound                           -- DOUBLE\",\r\n"
			+ "			  	 \"'PrimalValue(Column)' AS value                          -- DOUBLE_FUNCTION\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"routes\"\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"balance\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"'balance' AS Name                                       -- STRING\",\r\n"
			+ "              \"cities.city AS city                                     -- STRING\",\r\n"
			+ "              \"CONCAT('balance', '_', city) AS Row                     -- STRING\",\r\n"
			+ "              \"'EQ' AS Sense                                           -- STRING\",\r\n"
			+ "              \"(cities.supply-cities.demand) AS RHS                    -- DOUBLE\",\r\n"
			+ "			  	 \"'DualValue(Row)' AS dual                                -- DOUBLE_FUNCTION\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"cities\"\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"totalCost\",\r\n"
			+ "          \"CLASS\": \"OBJECTIVE\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [  \"Name\",      \"Row\",       \"Constant\", \"Sense\",     \"cost\"],\r\n"
			+ "            \"TYPES\": [   \"STRING\",    \"STRING\",    \"DOUBLE\",   \"STRING\",    \"DOUBLE_FUNCTION\"]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [[ \"totalCost\", \"totalCost\",  0.0,       \"MINIMIZE\",  \"ObjectiveValue(Row)\"]]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"balance_shipFrom\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"balance.Row AS Row -- STRING\",\r\n"
			+ "              \"ship.Column AS Column -- STRING\",\r\n"
			+ "              \"CAST(1.0 AS DOUBLE) AS Coefficient -- DOUBLE\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"balance\",\r\n"
			+ "            \"JOIN\": \"ship\",\r\n"
			+ "            \"ON\": \"balance.city = ship.origin\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"balance_shipTo\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"balance.Row AS Row -- STRING\",\r\n"
			+ "              \"ship.Column AS Column -- STRING\",\r\n"
			+ "              \"CAST(-1.0 AS DOUBLE) AS Coefficient -- DOUBLE\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"balance\",\r\n"
			+ "            \"JOIN\": \"ship\",\r\n"
			+ "            \"ON\":  \"balance.city = ship.destination\"\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"total_ship\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"totalCost.Row AS Row -- STRING\",\r\n"
			+ "              \"ship.Column AS Column -- STRING\",\r\n"
			+ "              \"routes.cost AS Coefficient -- DOUBLE\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"totalCost\",\r\n"
			+ "            \"CROSS JOIN\": \"ship\",\r\n"
			+ "            \"JOIN\": \"routes\",\r\n"
			+ "            \"ON\": \"routes.origin = ship.origin AND routes.destination = ship.destination\"\r\n"
			+ "          }\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"results\",\r\n"
			+ "      \"KIND\": \"MODULE\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\"Results from General Transshipment Problem\"]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"shipments\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"OUTPUT\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"ship.origin AS origin           -- STRING\",\r\n"
			+ "              \"ship.destination AS destination -- STRING\",\r\n"
			+ "              \"ship.value AS value             -- DOUBLE\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"ship\"\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"objective\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"OUTPUT\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\"totalCost.cost AS cost -- DOUBLE\"],\r\n"
			+ "            \"FROM\": \"totalCost\"\r\n"
			+ "          }\r\n"
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

		public static void json() {
			MsdxFile mosdex= transshipment();
			MsdxOutputDestination dst= MsdxOutputDestination.toStream(Msdx.GLOBAL.out);
			mosdex.write(dst);
		}//json

		public static void spark() {
			MsdxFile mosdex= transshipment();
			Map<String, MsdxTable> tables= mosdex.getTables();
			MsdxQuery.Resolver resolver= new MsdxQuery.Resolver(tables);
			resolver.findDependencies();
			resolver.resolveDependencies();
			
			MsdxTable table;
			for(String tableName: resolver.getResolutionOrder()) {
				table= tables.get(tableName);
				if(table.getTableClass().equals("DATA") && table.getTableType().equals("OUTPUT"))
					continue;	//skip output data tables for now, 
								//since they depend on solution values yet to be computed
				if(table.hasField("QUERY") || table.hasField("INITIALIZE")) {
					table.createInstance();
				}
			}//for tableName

			for(String tableName: tables.keySet()) {
				table= tables.get(tableName);
				if(table.getTableClass().equals("DATA") && table.getTableType().equals("OUTPUT"))
					continue;	//skip output data tables for now, 
								//since they depend on solution values yet to be computed
				Msdx.GLOBAL.out.println("Table: " + tableName);
//				table.getInstance().showSchema(Msdx.GLOBAL.out);
//				table.getInstance().showRecords(Msdx.GLOBAL.out);
				table.getInstance().getSchema().show(Msdx.GLOBAL.out);
				table.getInstance().show(Msdx.GLOBAL.out);
				Msdx.GLOBAL.out.println();
			}		
		}//spark
		
		/**
		 * Generates a .lp file from MOSDEX.
		 */
		public static void lp() {
			MsdxFile mosdex= transshipment();
			MsdxSpan.Factory spanFactory = new MsdxJavaSpan.Factory();
			MsdxSolverModelingFactory modelingFactory= new MsdxLpTextModelingFactory(dfFactory);
			
			MsdxModel model= new MsdxModel("Transshipment", modelingFactory, spanFactory, dfFactory);
			model.createModelingObjects(mosdex.getTables());
			
			modelingFactory.generate(model, Msdx.GLOBAL.out);
			
		}//lp
		
		/**
		 * Generates a .mps file from MOSDEX.
		 */
		public static void mps() {
			MsdxFile mosdex= transshipment();
			MsdxSpan.Factory spanFactory = new MsdxJavaSpan.Factory();
			MsdxSolverModelingFactory modelingFactory= new MsdxMpsTextModelingFactory(dfFactory);
			
			MsdxModel model= new MsdxModel("Transshipment", modelingFactory, spanFactory, dfFactory);
			model.createModelingObjects(mosdex.getTables());
			
			modelingFactory.generate(model, Msdx.GLOBAL.out);
			
		}//mps
		
	
}//class GenerateQueryFormModel
