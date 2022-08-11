/**
 * 
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxApplication;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;

/**
 * Tests MOSDEX using a transshipment problem in query form.
 * Solves with CPLEX and returns solution values.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class SolveQueryFormApplication {

	/**
	 * 
	 */
	public SolveQueryFormApplication() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("Transshipment MOSDEX Query-Form Demo: CPLEX");
		Msdx.GLOBAL.showDisplay();
		
		MsdxApplication application= new MsdxApplication(
			"Transshipment")
			.addFile(transshipment(), MsdxOutputDestination.toStream(Msdx.GLOBAL.out))
			.addSolverResults(MsdxOutputDestination.toStream(Msdx.GLOBAL.out), true) 
			.useSparkDataframes()
			.useJavaSpans()
			.useCplex();
		application.run();
		
	}//main

	@org.junit.Test
	public void Test() throws JSONException {
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		
		MsdxApplication cplex= new MsdxApplication(
			"Transshipment")
			.addFile(transshipment(), MsdxOutputDestination.toStream(out)) 
			.addSolverResults(MsdxOutputDestination.toStream(System.out), true) 
			.useSparkDataframes()
			.useJavaSpans()
			.useCplex();
		cplex.run();	
		
		JSONAssert.assertEquals(expectedOutput(), out.toString(), JSONCompareMode.LENIENT);

	}//Test

	/**
	 * Setup the transshipment model.
	 */
	public static MsdxInputSource transshipment() {
		
		String mosdexFile= 
			/*'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json",
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
      "KIND": "MODEL",
      "HEADING": {
        "DESCRIPTION": [
          "General Transshipment Problem",
          "query form",
          "MOSDEX 2-0 Syntax"
        ],
        "VERSION": "net1a 2-1",
        "REFERENCE": ["https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod"],
        "AUTHOR": ["Jeremy A. Bloom (jeremyblmca@gmail.com)"],
        "NOTICES": ["Copyright 2019 Jeremy A. Bloom"],
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
          "INSTANCE": [[ "totalCost", "totalCost",  0.0,       "MINIMIZE",  "ObjectiveValue(totalCost)"]]
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
		+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json\",\r\n"
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
		+ "      \"KIND\": \"MODEL\",\r\n"
		+ "      \"HEADING\": {\r\n"
		+ "        \"DESCRIPTION\": [\r\n"
		+ "          \"General Transshipment Problem\",\r\n"
		+ "          \"query form\",\r\n"
		+ "          \"MOSDEX 2-0 Syntax\"\r\n"
		+ "        ],\r\n"
		+ "        \"VERSION\": \"net1a 2-1\",\r\n"
		+ "        \"REFERENCE\": [\"https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod\"],\r\n"
		+ "        \"AUTHOR\": [\"Jeremy A. Bloom (jeremyblmca@gmail.com)\"],\r\n"
		+ "        \"NOTICES\": [\"Copyright 2019 Jeremy A. Bloom\"],\r\n"
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
		
		MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
		return src;			
	}//transshipment

	public static String expectedOutput() {
			String expectedOutput=
				/*'''
	{
	  "SYNTAX": "MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json",
	  "MODULES": [
	    {
	      "NAME": "data",
	      "CLASS": "DATA",
	      "HEADING": {
	        "DESCRIPTION": [
	          "Data for General Transshipment Problem"
	        ]
	      },
	      "TABLES": [
	        {
	          "NAME": "cities",
	          "CLASS": "DATA",
	          "KIND": "INPUT",
	          "SCHEMA": {
	            "FIELDS": [ "city", "supply", "demand" ],
	            "TYPES": [ "STRING", "DOUBLE", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "PITT", 450.0, 0.0 ],
	            [ "NE", 0.0, 0.0 ],
	            [ "SE", 0.0, 0.0 ],
	            [ "BOS", 0.0, 90.0 ],
	            [ "EWR", 0.0, 120.0 ],
	            [ "BWI", 0.0, 120.0 ],
	            [ "ATL", 0.0, 70.0 ],
	            [ "MCO", 0.0, 50.0 ]
	          ]
	        },
	        {
	          "NAME": "routes",
	          "CLASS": "DATA",
	          "KIND": "INPUT",
	          "SCHEMA": {
	            "FIELDS": [ "origin", "destination", "cost", "capacity" ],
	            "TYPES": [ "STRING", "STRING", "DOUBLE", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "PITT", "NE", 2.5, 250.0 ],
	            [ "PITT", "SE", 3.5, 250.0 ],
	            [ "NE", "BOS", 1.7, 100.0 ],
	            [ "NE", "EWR", 0.7, 100.0 ],
	            [ "NE", "BWI", 1.3, 100.0 ],
	            [ "SE", "EWR", 1.3, 100.0 ],
	            [ "SE", "BWI", 0.8, 100.0 ],
	            [ "SE", "ATL", 0.2, 100.0 ],
	            [ "SE", "MCO", 2.1, 100.0 ]
	          ]
	        }
	      ]
	    },
	    {
	      "NAME": "modelingObjects",
	      "KIND": "MODEL",
	      "HEADING": {
	        "DESCRIPTION": [
	          "General Transshipment Problem",
	          "query form",
	          "MOSDEX 2-0 Syntax"
	        ],
	        "VERSION": "net1a 2-1",
	        "REFERENCE": [
	          "https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod"
	        ],
	        "AUTHOR": [
	          "Jeremy A. Bloom (jeremyblmca@gmail.com)"
	        ],
	        "NOTICES": [
	          "Copyright 2019 Jeremy A. Bloom"
	        ],
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
	          "NAME": "ship",
	          "CLASS": "VARIABLE",
	          "KIND": "CONTINUOUS",
	          "SCHEMA": {
	            "FIELDS": [ "Name", "origin", "destination", "Column", "LowerBound", "UpperBound", "value" ],
	            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "ship", "PITT", "NE", "ship_PITT_NE", 0.0, 250.0, 250.0 ],
	            [ "ship", "PITT", "SE", "ship_PITT_SE", 0.0, 250.0, 200.0 ],
	            [ "ship", "NE", "BOS", "ship_NE_BOS", 0.0, 100.0, 90.0 ],
	            [ "ship", "NE", "EWR", "ship_NE_EWR", 0.0, 100.0, 100.0 ],
	            [ "ship", "NE", "BWI", "ship_NE_BWI", 0.0, 100.0, 60.0 ],
	            [ "ship", "SE", "EWR", "ship_SE_EWR", 0.0, 100.0, 20.0 ],
	            [ "ship", "SE", "BWI", "ship_SE_BWI", 0.0, 100.0, 60.0 ],
	            [ "ship", "SE", "ATL", "ship_SE_ATL", 0.0, 100.0, 70.0 ],
	            [ "ship", "SE", "MCO", "ship_SE_MCO", 0.0, 100.0, 50.0 ]
	          ]
	        },
	        {
	          "NAME": "balance",
	          "CLASS": "CONSTRAINT",
	          "KIND": "LINEAR",
	          "SCHEMA": {
	            "FIELDS": [ "Name", "city", "Row", "Sense", "RHS", "dual" ],
	            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "balance", "NE", "balance_NE", "EQ", 0.0, 0.5 ],
	            [ "balance", "SE", "balance_SE", "EQ", 0.0, 0.0 ],
	            [ "balance", "BOS", "balance_BOS", "EQ", -90.0, -1.2 ],
	            [ "balance", "EWR", "balance_EWR", "EQ", -120.0, -1.3 ],
	            [ "balance", "BWI", "balance_BWI", "EQ", -120.0, -0.8 ],
	            [ "balance", "ATL", "balance_ATL", "EQ", -70.0, -0.2 ],
	            [ "balance", "MCO", "balance_MCO", "EQ", -50.0, -2.1 ],
	            [ "balance", "PITT", "balance_PITT", "EQ", 450.0, 3.5 ]
	          ]
	        },
	        {
	          "NAME": "totalCost",
	          "CLASS": "OBJECTIVE",
	          "KIND": "LINEAR",
	          "SCHEMA": {
	            "FIELDS": [ "Name", "Row", "Constant", "Sense", "cost" ],
	            "TYPES": [ "STRING", "STRING", "DOUBLE", "STRING", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "totalCost", "totalCost", 0.0, "MINIMIZE", 1819.0 ]
	          ]
	        },
	        {
	          "NAME": "balance_shipFrom",
	          "CLASS": "TERM",
	          "KIND": "LINEAR",
	          "SCHEMA": {
	            "FIELDS": [ "Row", "Column", "Coefficient" ],
	            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "balance_PITT", "ship_PITT_NE", 1.0 ],
	            [ "balance_PITT", "ship_PITT_SE", 1.0 ],
	            [ "balance_NE", "ship_NE_BOS", 1.0 ],
	            [ "balance_NE", "ship_NE_EWR", 1.0 ],
	            [ "balance_NE", "ship_NE_BWI", 1.0 ],
	            [ "balance_SE", "ship_SE_EWR", 1.0 ],
	            [ "balance_SE", "ship_SE_BWI", 1.0 ],
	            [ "balance_SE", "ship_SE_ATL", 1.0 ],
	            [ "balance_SE", "ship_SE_MCO", 1.0 ]
	          ]
	        },
	        {
	          "NAME": "balance_shipTo",
	          "CLASS": "TERM",
	          "KIND": "LINEAR",
	          "SCHEMA": {
	            "FIELDS": [ "Row", "Column", "Coefficient" ],
	            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "balance_NE", "ship_PITT_NE", -1.0 ],
	            [ "balance_SE", "ship_PITT_SE", -1.0 ],
	            [ "balance_BOS", "ship_NE_BOS", -1.0 ],
	            [ "balance_EWR", "ship_NE_EWR", -1.0 ],
	            [ "balance_BWI", "ship_NE_BWI", -1.0 ],
	            [ "balance_EWR", "ship_SE_EWR", -1.0 ],
	            [ "balance_BWI", "ship_SE_BWI", -1.0 ],
	            [ "balance_ATL", "ship_SE_ATL", -1.0 ],
	            [ "balance_MCO", "ship_SE_MCO", -1.0 ]
	          ]
	        },
	        {
	          "NAME": "total_ship",
	          "CLASS": "TERM",
	          "KIND": "LINEAR",
	          "SCHEMA": {
	            "FIELDS": [ "Row", "Column", "Coefficient" ],
	            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "totalCost", "ship_PITT_NE", 2.5 ],
	            [ "totalCost", "ship_PITT_SE", 3.5 ],
	            [ "totalCost", "ship_NE_BOS", 1.7 ],
	            [ "totalCost", "ship_NE_EWR", 0.7 ],
	            [ "totalCost", "ship_NE_BWI", 1.3 ],
	            [ "totalCost", "ship_SE_EWR", 1.3 ],
	            [ "totalCost", "ship_SE_BWI", 0.8 ],
	            [ "totalCost", "ship_SE_ATL", 0.2 ],
	            [ "totalCost", "ship_SE_MCO", 2.1 ]
	          ]
	        }
	      ]
	    },
	    {
	      "NAME": "results",
	      "KIND": "MODULE",
	      "HEADING": {
	        "DESCRIPTION": [
	          "Results from General Transshipment Problem"
	        ]
	      },
	      "TABLES": [
	        {
	          "NAME": "shipments",
	          "CLASS": "DATA",
	          "KIND": "OUTPUT",
	          "SCHEMA": {
	            "FIELDS": [ "origin", "destination", "value" ],
	            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ "PITT", "NE", 250.0 ],
	            [ "PITT", "SE", 200.0 ],
	            [ "NE", "BOS", 90.0 ],
	            [ "NE", "EWR", 100.0 ],
	            [ "NE", "BWI", 60.0 ],
	            [ "SE", "EWR", 20.0 ],
	            [ "SE", "BWI", 60.0 ],
	            [ "SE", "ATL", 70.0 ],
	            [ "SE", "MCO", 50.0 ]
	          ]
	        },
	        {
	          "NAME": "objective",
	          "CLASS": "DATA",
	          "KIND": "OUTPUT",
	          "SCHEMA": {
	            "FIELDS": [ "cost" ],
	            "TYPES": [ "DOUBLE" ]
	          },
	          "INSTANCE": [
	            [ 1819.0 ]
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
				+ "    {\r\n"
				+ "      \"NAME\": \"data\",\r\n"
				+ "      \"CLASS\": \"DATA\",\r\n"
				+ "      \"HEADING\": {\r\n"
				+ "        \"DESCRIPTION\": [\r\n"
				+ "          \"Data for General Transshipment Problem\"\r\n"
				+ "        ]\r\n"
				+ "      },\r\n"
				+ "      \"TABLES\": [\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"cities\",\r\n"
				+ "          \"CLASS\": \"DATA\",\r\n"
				+ "          \"KIND\": \"INPUT\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"city\", \"supply\", \"demand\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"PITT\", 450.0, 0.0 ],\r\n"
				+ "            [ \"NE\", 0.0, 0.0 ],\r\n"
				+ "            [ \"SE\", 0.0, 0.0 ],\r\n"
				+ "            [ \"BOS\", 0.0, 90.0 ],\r\n"
				+ "            [ \"EWR\", 0.0, 120.0 ],\r\n"
				+ "            [ \"BWI\", 0.0, 120.0 ],\r\n"
				+ "            [ \"ATL\", 0.0, 70.0 ],\r\n"
				+ "            [ \"MCO\", 0.0, 50.0 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"routes\",\r\n"
				+ "          \"CLASS\": \"DATA\",\r\n"
				+ "          \"KIND\": \"INPUT\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"origin\", \"destination\", \"cost\", \"capacity\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"PITT\", \"NE\", 2.5, 250.0 ],\r\n"
				+ "            [ \"PITT\", \"SE\", 3.5, 250.0 ],\r\n"
				+ "            [ \"NE\", \"BOS\", 1.7, 100.0 ],\r\n"
				+ "            [ \"NE\", \"EWR\", 0.7, 100.0 ],\r\n"
				+ "            [ \"NE\", \"BWI\", 1.3, 100.0 ],\r\n"
				+ "            [ \"SE\", \"EWR\", 1.3, 100.0 ],\r\n"
				+ "            [ \"SE\", \"BWI\", 0.8, 100.0 ],\r\n"
				+ "            [ \"SE\", \"ATL\", 0.2, 100.0 ],\r\n"
				+ "            [ \"SE\", \"MCO\", 2.1, 100.0 ]\r\n"
				+ "          ]\r\n"
				+ "        }\r\n"
				+ "      ]\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"NAME\": \"modelingObjects\",\r\n"
				+ "      \"KIND\": \"MODEL\",\r\n"
				+ "      \"HEADING\": {\r\n"
				+ "        \"DESCRIPTION\": [\r\n"
				+ "          \"General Transshipment Problem\",\r\n"
				+ "          \"query form\",\r\n"
				+ "          \"MOSDEX 2-0 Syntax\"\r\n"
				+ "        ],\r\n"
				+ "        \"VERSION\": \"net1a 2-1\",\r\n"
				+ "        \"REFERENCE\": [\r\n"
				+ "          \"https://ampl.com/BOOK/EXAMPLES/EXAMPLES2/net1.mod\"\r\n"
				+ "        ],\r\n"
				+ "        \"AUTHOR\": [\r\n"
				+ "          \"Jeremy A. Bloom (jeremyblmca@gmail.com)\"\r\n"
				+ "        ],\r\n"
				+ "        \"NOTICES\": [\r\n"
				+ "          \"Copyright 2019 Jeremy A. Bloom\"\r\n"
				+ "        ],\r\n"
				+ "        \"MATH\": [\r\n"
				+ "          \"var Ship {(i,j) in ROUTES} >= 0, <= capacity[i,j]; # packages to be shipped\",\r\n"
				+ "          \"minimize Total_Cost: sum {(i,j) in ROUTES} cost[i,j] * Ship[i,j];\",\r\n"
				+ "          \"subject to\",\r\n"
				+ "          \"Balance {k in CITIES}: \",\r\n"
				+ "          \"sum {(k,j) in ROUTES} Ship[k,j] - sum {(i,k) in ROUTES} Ship[i,k] = supply[k] - demand[k];\"\r\n"
				+ "        ]\r\n"
				+ "      },\r\n"
				+ "      \"TABLES\": [\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"ship\",\r\n"
				+ "          \"CLASS\": \"VARIABLE\",\r\n"
				+ "          \"KIND\": \"CONTINUOUS\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"Name\", \"origin\", \"destination\", \"Column\", \"LowerBound\", \"UpperBound\", \"value\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"ship\", \"PITT\", \"NE\", \"ship_PITT_NE\", 0.0, 250.0, 250.0 ],\r\n"
				+ "            [ \"ship\", \"PITT\", \"SE\", \"ship_PITT_SE\", 0.0, 250.0, 200.0 ],\r\n"
				+ "            [ \"ship\", \"NE\", \"BOS\", \"ship_NE_BOS\", 0.0, 100.0, 90.0 ],\r\n"
				+ "            [ \"ship\", \"NE\", \"EWR\", \"ship_NE_EWR\", 0.0, 100.0, 100.0 ],\r\n"
				+ "            [ \"ship\", \"NE\", \"BWI\", \"ship_NE_BWI\", 0.0, 100.0, 60.0 ],\r\n"
				+ "            [ \"ship\", \"SE\", \"EWR\", \"ship_SE_EWR\", 0.0, 100.0, 20.0 ],\r\n"
				+ "            [ \"ship\", \"SE\", \"BWI\", \"ship_SE_BWI\", 0.0, 100.0, 60.0 ],\r\n"
				+ "            [ \"ship\", \"SE\", \"ATL\", \"ship_SE_ATL\", 0.0, 100.0, 70.0 ],\r\n"
				+ "            [ \"ship\", \"SE\", \"MCO\", \"ship_SE_MCO\", 0.0, 100.0, 50.0 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"balance\",\r\n"
				+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
				+ "          \"KIND\": \"LINEAR\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"Name\", \"city\", \"Row\", \"Sense\", \"RHS\", \"dual\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"balance\", \"NE\", \"balance_NE\", \"EQ\", 0.0, 0.5 ],\r\n"
				+ "            [ \"balance\", \"SE\", \"balance_SE\", \"EQ\", 0.0, 0.0 ],\r\n"
				+ "            [ \"balance\", \"BOS\", \"balance_BOS\", \"EQ\", -90.0, -1.2 ],\r\n"
				+ "            [ \"balance\", \"EWR\", \"balance_EWR\", \"EQ\", -120.0, -1.3 ],\r\n"
				+ "            [ \"balance\", \"BWI\", \"balance_BWI\", \"EQ\", -120.0, -0.8 ],\r\n"
				+ "            [ \"balance\", \"ATL\", \"balance_ATL\", \"EQ\", -70.0, -0.2 ],\r\n"
				+ "            [ \"balance\", \"MCO\", \"balance_MCO\", \"EQ\", -50.0, -2.1 ],\r\n"
				+ "            [ \"balance\", \"PITT\", \"balance_PITT\", \"EQ\", 450.0, 3.5 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"totalCost\",\r\n"
				+ "          \"CLASS\": \"OBJECTIVE\",\r\n"
				+ "          \"KIND\": \"LINEAR\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Constant\", \"Sense\", \"cost\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"STRING\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"totalCost\", \"totalCost\", 0.0, \"MINIMIZE\", 1819.0 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"balance_shipFrom\",\r\n"
				+ "          \"CLASS\": \"TERM\",\r\n"
				+ "          \"KIND\": \"LINEAR\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"balance_PITT\", \"ship_PITT_NE\", 1.0 ],\r\n"
				+ "            [ \"balance_PITT\", \"ship_PITT_SE\", 1.0 ],\r\n"
				+ "            [ \"balance_NE\", \"ship_NE_BOS\", 1.0 ],\r\n"
				+ "            [ \"balance_NE\", \"ship_NE_EWR\", 1.0 ],\r\n"
				+ "            [ \"balance_NE\", \"ship_NE_BWI\", 1.0 ],\r\n"
				+ "            [ \"balance_SE\", \"ship_SE_EWR\", 1.0 ],\r\n"
				+ "            [ \"balance_SE\", \"ship_SE_BWI\", 1.0 ],\r\n"
				+ "            [ \"balance_SE\", \"ship_SE_ATL\", 1.0 ],\r\n"
				+ "            [ \"balance_SE\", \"ship_SE_MCO\", 1.0 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"balance_shipTo\",\r\n"
				+ "          \"CLASS\": \"TERM\",\r\n"
				+ "          \"KIND\": \"LINEAR\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"balance_NE\", \"ship_PITT_NE\", -1.0 ],\r\n"
				+ "            [ \"balance_SE\", \"ship_PITT_SE\", -1.0 ],\r\n"
				+ "            [ \"balance_BOS\", \"ship_NE_BOS\", -1.0 ],\r\n"
				+ "            [ \"balance_EWR\", \"ship_NE_EWR\", -1.0 ],\r\n"
				+ "            [ \"balance_BWI\", \"ship_NE_BWI\", -1.0 ],\r\n"
				+ "            [ \"balance_EWR\", \"ship_SE_EWR\", -1.0 ],\r\n"
				+ "            [ \"balance_BWI\", \"ship_SE_BWI\", -1.0 ],\r\n"
				+ "            [ \"balance_ATL\", \"ship_SE_ATL\", -1.0 ],\r\n"
				+ "            [ \"balance_MCO\", \"ship_SE_MCO\", -1.0 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"total_ship\",\r\n"
				+ "          \"CLASS\": \"TERM\",\r\n"
				+ "          \"KIND\": \"LINEAR\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"totalCost\", \"ship_PITT_NE\", 2.5 ],\r\n"
				+ "            [ \"totalCost\", \"ship_PITT_SE\", 3.5 ],\r\n"
				+ "            [ \"totalCost\", \"ship_NE_BOS\", 1.7 ],\r\n"
				+ "            [ \"totalCost\", \"ship_NE_EWR\", 0.7 ],\r\n"
				+ "            [ \"totalCost\", \"ship_NE_BWI\", 1.3 ],\r\n"
				+ "            [ \"totalCost\", \"ship_SE_EWR\", 1.3 ],\r\n"
				+ "            [ \"totalCost\", \"ship_SE_BWI\", 0.8 ],\r\n"
				+ "            [ \"totalCost\", \"ship_SE_ATL\", 0.2 ],\r\n"
				+ "            [ \"totalCost\", \"ship_SE_MCO\", 2.1 ]\r\n"
				+ "          ]\r\n"
				+ "        }\r\n"
				+ "      ]\r\n"
				+ "    },\r\n"
				+ "    {\r\n"
				+ "      \"NAME\": \"results\",\r\n"
				+ "      \"KIND\": \"MODULE\",\r\n"
				+ "      \"HEADING\": {\r\n"
				+ "        \"DESCRIPTION\": [\r\n"
				+ "          \"Results from General Transshipment Problem\"\r\n"
				+ "        ]\r\n"
				+ "      },\r\n"
				+ "      \"TABLES\": [\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"shipments\",\r\n"
				+ "          \"CLASS\": \"DATA\",\r\n"
				+ "          \"KIND\": \"OUTPUT\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"origin\", \"destination\", \"value\" ],\r\n"
				+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ \"PITT\", \"NE\", 250.0 ],\r\n"
				+ "            [ \"PITT\", \"SE\", 200.0 ],\r\n"
				+ "            [ \"NE\", \"BOS\", 90.0 ],\r\n"
				+ "            [ \"NE\", \"EWR\", 100.0 ],\r\n"
				+ "            [ \"NE\", \"BWI\", 60.0 ],\r\n"
				+ "            [ \"SE\", \"EWR\", 20.0 ],\r\n"
				+ "            [ \"SE\", \"BWI\", 60.0 ],\r\n"
				+ "            [ \"SE\", \"ATL\", 70.0 ],\r\n"
				+ "            [ \"SE\", \"MCO\", 50.0 ]\r\n"
				+ "          ]\r\n"
				+ "        },\r\n"
				+ "        {\r\n"
				+ "          \"NAME\": \"objective\",\r\n"
				+ "          \"CLASS\": \"DATA\",\r\n"
				+ "          \"KIND\": \"OUTPUT\",\r\n"
				+ "          \"SCHEMA\": {\r\n"
				+ "            \"FIELDS\": [ \"cost\" ],\r\n"
				+ "            \"TYPES\": [ \"DOUBLE\" ]\r\n"
				+ "          },\r\n"
				+ "          \"INSTANCE\": [\r\n"
				+ "            [ 1819.0 ]\r\n"
				+ "          ]\r\n"
				+ "        }\r\n"
				+ "      ]\r\n"
				+ "    }\r\n"
				+ "  ]\r\n"
				+ "}\r\n";
			
			return expectedOutput;
		}//expectedOutput
	
	
}//class SolveQueryFormApplication
