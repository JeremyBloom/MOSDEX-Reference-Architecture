/**
 * 
 */
package io.github.JeremyBloom.mosdex2.dataframe;

import java.util.Map;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxFile;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery.Resolver;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxTable;

/**
 * Tests building query strings and creating a schema from a query.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class GenerateQueries {

	static MsdxSparkDataframe.Factory dfFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
	
	/**
	 * 
	 */
	public GenerateQueries() {
		// TODO Auto-generated constructor stub
	}
	
	public static MsdxFile queries() {
		
		String json= 
/*'''
{
  "MODULES": [
    {
      "NAME": "instanceTables",
      "TABLES": [
        {
          "NAME": "cities",
          "SCHEMA": {
            "FIELDS": [ "city",   "supply", "demand"],
            "TYPES": [  "STRING", "DOUBLE", "DOUBLE"]
          },
          "INSTANCE": [
            [           "NYC",  1000.0,     0.0],
            [           "PIT",  0.0,        0.0],
            [           "DEN",  0.0,        0.0],
            [           "LAX",  0.0,        1000.0]
          ]
        },
        {
          "NAME": "routes",
          "SCHEMA": {
            "FIELDS": [ "origin", "destination",  "cost"],
            "TYPES": [  "STRING", "STRING",       "DOUBLE"]
          },
          "INSTANCE": [
            [           "NYC",    "PIT",          10.0],
            [           "NYC",    "DEN",          15.0],
            [           "NYC",    "LAX",          25.0],
            [           "PIT",    "DEN",          12.0],
            [           "PIT",    "LAX",          20.0],
            [           "DEN",    "LAX",          10.0]
          ]
        },
        {
          "NAME": "objective",
          "SCHEMA": {
            "FIELDS": [ "Name",       "Row",        "Sense"],
            "TYPES": [  "STRING",     "STRING",     "STRING"]
          },
          "INSTANCE":
                  [[    "objective",  "objective",  "minimize"]]
        }
      ]
    },
    {
      "NAME": "queryTables",
      "TABLES": [
        {
          "NAME": "traffic",
          "QUERY": {
            "SELECT": [
              "'traffic' AS Name -- STRING",
              "routes.origin AS origin -- STRING",
              "routes.destination AS destination -- STRING",
              "CONCAT('traffic', '_', origin, '_', destination) AS Column -- STRING",
			  "'PrimalValue(Column)' AS Value -- DOUBLE_FUNCTION"              
            ],
            "FROM": "routes"
          }
        },
        {
          "NAME": "balance",
          "QUERY": {
            "SELECT": [
              "'balance' AS Name -- STRING",
              "cities.city AS city -- STRING",
              "CONCAT('balance', '_', city) AS Row -- STRING",
              "(cities.supply-cities.demand) AS RHS -- DOUBLE",
			  "'Slack(Row)' AS Slack -- DOUBLE_FUNCTION"              
            ],
            "FROM": "cities"
          }
        },
        {
          "NAME": "balance_trafficOut",
          "QUERY": {
            "SELECT": [
            	"balance.Row AS Row -- STRING", 
            	"traffic.Column AS Column -- STRING", 
            	"1.0 AS Coefficient -- DOUBLE"
        	],
            "FROM": "balance",
            "JOIN": "traffic", "ON": "balance.city = traffic.origin"
          }
        },
        {
          "NAME": "balance_trafficIn",
          "QUERY": {
            "SELECT": [
            	"balance.Row AS Row -- STRING", 
            	"traffic.Column AS Column -- STRING", 
            	"-1.0 AS Coefficient -- DOUBLE"
            ],
            "FROM": "balance",
            "JOIN": "traffic", "ON": "balance.city = traffic.destination"
          }
        },
        {
          "NAME": "objective_traffic",
          "QUERY": {
            "SELECT": [
            	"objective.Row AS Row -- STRING", 
            	"traffic.Column AS Column -- STRING", 
            	"cost AS Coefficient -- DOUBLE"
            ],
            "FROM": "objective",
            "CROSS JOIN": "traffic"
          }
        }
      ]
    },
    {
      "NAME": "outputTables",
      "TABLES": [
        {
          "NAME": "shipments",
          "QUERY": {
            "SELECT": [
              "'shipments' AS Name -- STRING",
              "traffic.origin AS origin -- STRING",
              "traffic.destination AS destination -- STRING",
			  "traffic.Value' AS Value -- DOUBLE"              
            ],
            "FROM": "traffic"
          }
        },
        {
          "NAME": "cost",
          "QUERY": {
            "SELECT": [
              "'cost' AS Name -- STRING",
              "objective.Value AS Value -- DOUBLE"
            ],
            "FROM": "objective"
          }
        }
      ]
    }
  ]
}
'''*/			
"{\r\n"
+ "  \"MODULES\": [\r\n"
+ "    {\r\n"
+ "      \"NAME\": \"instanceTables\",\r\n"
+ "      \"TABLES\": [\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"cities\",\r\n"
+ "          \"SCHEMA\": {\r\n"
+ "            \"FIELDS\": [ \"city\",   \"supply\", \"demand\"],\r\n"
+ "            \"TYPES\": [  \"STRING\", \"DOUBLE\", \"DOUBLE\"]\r\n"
+ "          },\r\n"
+ "          \"INSTANCE\": [\r\n"
+ "            [           \"NYC\",  1000.0,     0.0],\r\n"
+ "            [           \"PIT\",  0.0,        0.0],\r\n"
+ "            [           \"DEN\",  0.0,        0.0],\r\n"
+ "            [           \"LAX\",  0.0,        1000.0]\r\n"
+ "          ]\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"routes\",\r\n"
+ "          \"SCHEMA\": {\r\n"
+ "            \"FIELDS\": [ \"origin\", \"destination\",  \"cost\"],\r\n"
+ "            \"TYPES\": [  \"STRING\", \"STRING\",       \"DOUBLE\"]\r\n"
+ "          },\r\n"
+ "          \"INSTANCE\": [\r\n"
+ "            [           \"NYC\",    \"PIT\",          10.0],\r\n"
+ "            [           \"NYC\",    \"DEN\",          15.0],\r\n"
+ "            [           \"NYC\",    \"LAX\",          25.0],\r\n"
+ "            [           \"PIT\",    \"DEN\",          12.0],\r\n"
+ "            [           \"PIT\",    \"LAX\",          20.0],\r\n"
+ "            [           \"DEN\",    \"LAX\",          10.0]\r\n"
+ "          ]\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"objective\",\r\n"
+ "          \"SCHEMA\": {\r\n"
+ "            \"FIELDS\": [ \"Name\",       \"Row\",        \"Sense\"],\r\n"
+ "            \"TYPES\": [  \"STRING\",     \"STRING\",     \"STRING\"]\r\n"
+ "          },\r\n"
+ "          \"INSTANCE\":\r\n"
+ "                  [[    \"objective\",  \"objective\",  \"minimize\"]]\r\n"
+ "        }\r\n"
+ "      ]\r\n"
+ "    },\r\n"
+ "    {\r\n"
+ "      \"NAME\": \"queryTables\",\r\n"
+ "      \"TABLES\": [\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"traffic\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "              \"'traffic' AS Name -- STRING\",\r\n"
+ "              \"routes.origin AS origin -- STRING\",\r\n"
+ "              \"routes.destination AS destination -- STRING\",\r\n"
+ "              \"CONCAT('traffic', '_', origin, '_', destination) AS Column -- STRING\",\r\n"
+ "			  \"'PrimalValue(Column)' AS Value -- DOUBLE_FUNCTION\"              \r\n"
+ "            ],\r\n"
+ "            \"FROM\": \"routes\"\r\n"
+ "          }\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"balance\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "              \"'balance' AS Name -- STRING\",\r\n"
+ "              \"cities.city AS city -- STRING\",\r\n"
+ "              \"CONCAT('balance', '_', city) AS Row -- STRING\",\r\n"
+ "              \"(cities.supply-cities.demand) AS RHS -- DOUBLE\",\r\n"
+ "			  \"'Slack(Row)' AS Slack -- DOUBLE_FUNCTION\"              \r\n"
+ "            ],\r\n"
+ "            \"FROM\": \"cities\"\r\n"
+ "          }\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"balance_trafficOut\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "            	\"balance.Row AS Row -- STRING\", \r\n"
+ "            	\"traffic.Column AS Column -- STRING\", \r\n"
+ "            	\"1.0 AS Coefficient -- DOUBLE\"\r\n"
+ "        	],\r\n"
+ "            \"FROM\": \"balance\",\r\n"
+ "            \"JOIN\": \"traffic\", \"ON\": \"balance.city = traffic.origin\"\r\n"
+ "          }\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"balance_trafficIn\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "            	\"balance.Row AS Row -- STRING\", \r\n"
+ "            	\"traffic.Column AS Column -- STRING\", \r\n"
+ "            	\"-1.0 AS Coefficient -- DOUBLE\"\r\n"
+ "            ],\r\n"
+ "            \"FROM\": \"balance\",\r\n"
+ "            \"JOIN\": \"traffic\", \"ON\": \"balance.city = traffic.destination\"\r\n"
+ "          }\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"objective_traffic\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "            	\"objective.Row AS Row -- STRING\", \r\n"
+ "            	\"traffic.Column AS Column -- STRING\", \r\n"
+ "            	\"cost AS Coefficient -- DOUBLE\"\r\n"
+ "            ],\r\n"
+ "            \"FROM\": \"objective\",\r\n"
+ "            \"CROSS JOIN\": \"traffic\"\r\n"
+ "          }\r\n"
+ "        }\r\n"
+ "      ]\r\n"
+ "    },\r\n"
+ "    {\r\n"
+ "      \"NAME\": \"outputTables\",\r\n"
+ "      \"TABLES\": [\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"shipments\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "              \"'shipments' AS Name -- STRING\",\r\n"
+ "              \"traffic.origin AS origin -- STRING\",\r\n"
+ "              \"traffic.destination AS destination -- STRING\",\r\n"
+ "			  \"traffic.Value' AS Value -- DOUBLE\"              \r\n"
+ "            ],\r\n"
+ "            \"FROM\": \"traffic\"\r\n"
+ "          }\r\n"
+ "        },\r\n"
+ "        {\r\n"
+ "          \"NAME\": \"cost\",\r\n"
+ "          \"QUERY\": {\r\n"
+ "            \"SELECT\": [\r\n"
+ "              \"'cost' AS Name -- STRING\",\r\n"
+ "              \"objective.Value AS Value -- DOUBLE\"\r\n"
+ "            ],\r\n"
+ "            \"FROM\": \"objective\"\r\n"
+ "          }\r\n"
+ "        }\r\n"
+ "      ]\r\n"
+ "    }\r\n"
+ "  ]\r\n"
+ "}\r\n";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
				dfFactory, 
				Msdx.GLOBAL.mapper, 
				false);
				
		MsdxInputSource src= MsdxInputSource.fromString(json);
		MsdxFile queries= factory.readFile(src);
		return queries;
	}//moreQueries

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Msdx.GLOBAL.setDisplayTitle("Query Tests");
		Msdx.GLOBAL.showDisplay();
		
		makeSchema();
		makeSQL();
		findDependencies();
		findResolutionOrder();

	}//main
	
	static void makeSchema() {
		MsdxFile queries= queries();
		Map<String, MsdxTable> tables= queries.getTables();
		MsdxQuery.Resolver resolver= new MsdxQuery.Resolver(tables);
		resolver.findDependencies();
		
		Msdx.GLOBAL.out.println("In GenerateQueries.tests1");
		MsdxContainer<Class<?>> schema;
		for(MsdxTable table: tables.values()) {
			if(table.hasField("QUERY") || table.hasField("INITIALIZE")) {
				schema= table.getQuery().getSchema();
				Msdx.GLOBAL.out.println(table.getName() + " SCHEMA : {");
				Msdx.GLOBAL.out.println(MsdxContainer.toString(schema.itemNames(), "  ", "FIELDS", item -> String.valueOf(item)));
				Msdx.GLOBAL.out.println(MsdxContainer.toString(schema.getContent(), "  ", "TYPES", item -> item.getSimpleName()));
				Msdx.GLOBAL.out.println("}");	
			}
		}
		Msdx.GLOBAL.out.println();		
	}//makeSchema

	static void makeSQL() {
		MsdxFile queries= queries();
		Map<String, MsdxTable> tables= queries.getTables();
		MsdxQuery.Resolver resolver= new MsdxQuery.Resolver(tables);
		resolver.findDependencies();
		
		Msdx.GLOBAL.out.println("In GenerateQueries.tests2");
		for(MsdxTable table: tables.values()) {
			if(table.hasField("QUERY") || table.hasField("INITIALIZE")) {
				Msdx.GLOBAL.out.println(table.getName() + " SQL:");
				Msdx.GLOBAL.out.println(table.getQuery().toSQL());
			}
		}
		Msdx.GLOBAL.out.println();		
	}//makeSQL

	static void findDependencies() {
		MsdxFile queries= queries();
		Map<String, MsdxTable> tables= queries.getTables();
		MsdxQuery.Resolver resolver= new MsdxQuery.Resolver(tables);
		resolver.findDependencies();
		
		for(MsdxTable table: tables.values()) {
			if(table.hasField("QUERY") || table.hasField("INITIALIZE")) {
				Msdx.GLOBAL.out.println(table.getName() + " dependencies= " + table.getQuery().getDependencies().toString());
			}
		}
		
	}//findDependencies
	
	static void findResolutionOrder() {
		MsdxFile queries= queries();
		Map<String, MsdxTable> tables= queries.getTables();
		MsdxQuery.Resolver resolver= new MsdxQuery.Resolver(tables);
		resolver.findDependencies();		
		
		resolver.resolveDependencies();
		Msdx.GLOBAL.out.println("Resolution order= " + resolver.getResolutionOrder().toString());
		Msdx.GLOBAL.out.println();
		
	}//findResolutionOrder
	

}//GenerateQueries
