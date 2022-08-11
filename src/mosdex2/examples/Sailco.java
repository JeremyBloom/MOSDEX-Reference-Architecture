/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.examples;

import io.github.JeremyBloom.mosdex2.MsdxApplication;

/**
 * Demonstrates MOSDEX using the Sailco problem in query form.
 * Solves with CPLEX and returns solution values.
 * Sailco is an inventory problem with lagged decision variables.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class Sailco {
		
	/**
	 * Output expected from the solution.
	 * Note, this string is provided in two forms: <br>
	 * - as a Java text block (for use in Java 13 and above)<br>
	 * - as a traditional Java string with the necessary escaped characters.
	 * <p>
	 * The sailco problem is stated in the MATH element of the MOSDEX file.
	 */
	static String expectedOutput= 
			/*'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json",
  "MODULES": [
    {
      "NAME": "sailco",
      "CLASS": "MODULE",
      "KIND": "MODEL",
      "HEADING": {
        "DESCRIPTION": [
          "Inventory problem with lagged decision variables"
        ],
        "VERSION": [ "2-0" ],
        "REFERENCE": [
          "https://www.ibm.com/support/knowledgecenter/SSSA5P_12.7.1/ilog.odms.ide.help/OPL_Studio/opllanguser/topics/opl_languser_app_areas_pwl_inventory.html"
        ],
        "AUTHOR": [
          "Jeremy A. Bloom (jeremyblmca@gmail.com)"
        ],
        "NOTICES": [
          "Copyright 2019 Jeremy A. Bloom"
        ],
        "MATH": [
          "minimize",
          "sum( t in Periods ) ( RegularCost * Regular[t] ) +",
          "sum( t in Periods ) ( ExtraCost * Extra[t] ) +",
          "sum( t in Periods ) ( InventoryCost * Inventory[t] );",
          "subject to {",
          "forall( t in Periods )",
          "ctCapacity[t]: Regular[t] <= Capacity;",
          "forall( t in Periods )",
          "ctBoat[t]:",
          "if (t>1)",
          "Regular[t] + Extra[t] + Inventory[t-1] - Inventory[t] == Demand[t];",
          "else //t=1",
          "Regular[t] + Extra[t] - Inventory[t] == Demand[t] - InitialInventory;",
          "}"
        ]
      },
      "TABLES": [
        {
          "NAME": "periods",
          "CLASS": "DATA",
          "KIND": "INPUT",
          "SCHEMA": {
            "FIELDS": [ "period" ],
            "TYPES": [ "INTEGER" ]
          },
          "INSTANCE": [
            [ 1 ],
            [ 2 ],
            [ 3 ],
            [ 4 ]
          ]
        },
        {
          "NAME": "demands",
          "CLASS": "DATA",
          "KIND": "INPUT",
          "SCHEMA": {
            "FIELDS": [ "period", "demand" ],
            "TYPES": [ "INTEGER", "DOUBLE" ]
          },
          "INSTANCE": [
            [ 1, 40.0 ],
            [ 2, 60.0 ],
            [ 3, 75.0 ],
            [ 4, 25.0 ]
          ]
        },
        {
          "NAME": "parameters",
          "CLASS": "DATA",
          "KIND": "INPUT",
          "SCHEMA": {
            "FIELDS": [ "regularCost", "extraCost", "capacity", "initialInventory", "inventoryCost" ],
            "TYPES": [ "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ 400.0, 450.0, 40.0, 10.0, 20.0 ]
          ]
        },
        {
          "NAME": "regular",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "period", "Column", "LowerBound", "UpperBound", "value" ],
            "TYPES": [ "STRING", "INTEGER", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "regular", 1, "regular_1", 0.0, "Infinity", 40.0 ],
            [ "regular", 2, "regular_2", 0.0, "Infinity", 40.0 ],
            [ "regular", 3, "regular_3", 0.0, "Infinity", 40.0 ],
            [ "regular", 4, "regular_4", 0.0, "Infinity", 25.0 ]
          ]
        },
        {
          "NAME": "extra",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "period", "Column", "LowerBound", "UpperBound", "value" ],
            "TYPES": [ "STRING", "INTEGER", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "extra", 1, "extra_1", 0.0, "Infinity", 0.0 ],
            [ "extra", 2, "extra_2", 0.0, "Infinity", 10.0 ],
            [ "extra", 3, "extra_3", 0.0, "Infinity", 35.0 ],
            [ "extra", 4, "extra_4", 0.0, "Infinity", 0.0 ]
          ]
        },
        {
          "NAME": "inventory",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "period", "Column", "LowerBound", "UpperBound", "value" ],
            "TYPES": [ "STRING", "INTEGER", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "inventory", 1, "inventory_1", 0.0, "Infinity", 10.0 ],
            [ "inventory", 2, "inventory_2", 0.0, "Infinity", 0.0 ],
            [ "inventory", 3, "inventory_3", 0.0, "Infinity", 0.0 ],
            [ "inventory", 4, "inventory_4", 0.0, "Infinity", 0.0 ]
          ]
        },
        {
          "NAME": "ctCapacity",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "period", "Row", "Sense", "RHS", "dual" ],
            "TYPES": [ "STRING", "INTEGER", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctCapacity", 1, "ctCapacity_1", "<=", 40.0, -30.0 ],
            [ "ctCapacity", 2, "ctCapacity_2", "<=", 40.0, -50.0 ],
            [ "ctCapacity", 3, "ctCapacity_3", "<=", 40.0, -50.0 ],
            [ "ctCapacity", 4, "ctCapacity_4", "<=", 40.0, 0.0 ]
          ]
        },
        {
          "NAME": "ctBoat",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "period", "Row", "Sense", "RHS", "dual" ],
            "TYPES": [ "STRING", "INTEGER", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctBoat", 2, "ctBoat_2", "==", 60.0, 450.0 ],
            [ "ctBoat", 3, "ctBoat_3", "==", 75.0, 450.0 ],
            [ "ctBoat", 4, "ctBoat_4", "==", 25.0, 400.0 ]
          ]
        },
        {
          "NAME": "ctBoat_1",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "period", "Row", "Sense", "RHS", "dual" ],
            "TYPES": [ "STRING", "INTEGER", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctBoat_1", 1, "ctBoat_1", "==", 30.0, 430.0 ]
          ]
        },
        {
          "NAME": "totalCost",
          "CLASS": "OBJECTIVE",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Constant", "Sense", "Value" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "totalCost", "totalCost", 0.0, "MINIMIZE", 78450.0 ]
          ]
        },
        {
          "NAME": "ctCapacity_regular",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctCapacity_1", "regular_1", 1.0 ],
            [ "ctCapacity_2", "regular_2", 1.0 ],
            [ "ctCapacity_3", "regular_3", 1.0 ],
            [ "ctCapacity_4", "regular_4", 1.0 ]
          ]
        },
        {
          "NAME": "ctBoat_regular",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctBoat_1", "regular_1", 1.0 ],
            [ "ctBoat_2", "regular_2", 1.0 ],
            [ "ctBoat_3", "regular_3", 1.0 ],
            [ "ctBoat_4", "regular_4", 1.0 ]
          ]
        },
        {
          "NAME": "ctBoat_extra",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctBoat_1", "extra_1", 1.0 ],
            [ "ctBoat_2", "extra_2", 1.0 ],
            [ "ctBoat_3", "extra_3", 1.0 ],
            [ "ctBoat_4", "extra_4", 1.0 ]
          ]
        },
        {
          "NAME": "ctBoat_inventory",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctBoat_1", "inventory_1", -1.0 ],
            [ "ctBoat_2", "inventory_2", -1.0 ],
            [ "ctBoat_3", "inventory_3", -1.0 ],
            [ "ctBoat_4", "inventory_4", -1.0 ]
          ]
        },
        {
          "NAME": "ctBoat_lagged_inventory",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctBoat_2", "inventory_1", 1.0 ],
            [ "ctBoat_3", "inventory_2", 1.0 ],
            [ "ctBoat_4", "inventory_3", 1.0 ]
          ]
        },
        {
          "NAME": "totalCost_regular",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "totalCost", "regular_1", 400.0 ],
            [ "totalCost", "regular_2", 400.0 ],
            [ "totalCost", "regular_3", 400.0 ],
            [ "totalCost", "regular_4", 400.0 ]
          ]
        },
        {
          "NAME": "totalCost_extra",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "totalCost", "extra_1", 450.0 ],
            [ "totalCost", "extra_2", 450.0 ],
            [ "totalCost", "extra_3", 450.0 ],
            [ "totalCost", "extra_4", 450.0 ]
          ]
        },
        {
          "NAME": "totalCost_inventory",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "totalCost", "inventory_1", 20.0 ],
            [ "totalCost", "inventory_2", 20.0 ],
            [ "totalCost", "inventory_3", 20.0 ],
            [ "totalCost", "inventory_4", 20.0 ]
          ]
        },
        {
          "NAME": "production",
          "CLASS": "DATA",
          "KIND": "OUTPUT",
          "SCHEMA": {
            "FIELDS": [ "period", "regular", "extra", "inventory", "marginalCapacityValue" ],
            "TYPES": [ "INTEGER", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ 1, 40.0, 0.0, 10.0, -30.0 ],
            [ 2, 40.0, 10.0, 0.0, -50.0 ],
            [ 3, 40.0, 35.0, 0.0, -50.0 ],
            [ 4, 25.0, 0.0, 0.0, 0.0 ]
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
			+ "      \"NAME\": \"sailco\",\r\n"
			+ "      \"CLASS\": \"MODULE\",\r\n"
			+ "      \"KIND\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\r\n"
			+ "          \"Inventory problem with lagged decision variables\"\r\n"
			+ "        ],\r\n"
			+ "        \"VERSION\": [ \"2-0\" ],\r\n"
			+ "        \"REFERENCE\": [\r\n"
			+ "          \"https://www.ibm.com/support/knowledgecenter/SSSA5P_12.7.1/ilog.odms.ide.help/OPL_Studio/opllanguser/topics/opl_languser_app_areas_pwl_inventory.html\"\r\n"
			+ "        ],\r\n"
			+ "        \"AUTHOR\": [\r\n"
			+ "          \"Jeremy A. Bloom (jeremyblmca@gmail.com)\"\r\n"
			+ "        ],\r\n"
			+ "        \"NOTICES\": [\r\n"
			+ "          \"Copyright 2019 Jeremy A. Bloom\"\r\n"
			+ "        ],\r\n"
			+ "        \"MATH\": [\r\n"
			+ "          \"minimize\",\r\n"
			+ "          \"sum( t in Periods ) ( RegularCost * Regular[t] ) +\",\r\n"
			+ "          \"sum( t in Periods ) ( ExtraCost * Extra[t] ) +\",\r\n"
			+ "          \"sum( t in Periods ) ( InventoryCost * Inventory[t] );\",\r\n"
			+ "          \"subject to {\",\r\n"
			+ "          \"forall( t in Periods )\",\r\n"
			+ "          \"ctCapacity[t]: Regular[t] <= Capacity;\",\r\n"
			+ "          \"forall( t in Periods )\",\r\n"
			+ "          \"ctBoat[t]:\",\r\n"
			+ "          \"if (t>1)\",\r\n"
			+ "          \"Regular[t] + Extra[t] + Inventory[t-1] - Inventory[t] == Demand[t];\",\r\n"
			+ "          \"else //t=1\",\r\n"
			+ "          \"Regular[t] + Extra[t] - Inventory[t] == Demand[t] - InitialInventory;\",\r\n"
			+ "          \"}\"\r\n"
			+ "        ]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"periods\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"INPUT\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"period\" ],\r\n"
			+ "            \"TYPES\": [ \"INTEGER\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ 1 ],\r\n"
			+ "            [ 2 ],\r\n"
			+ "            [ 3 ],\r\n"
			+ "            [ 4 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"demands\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"INPUT\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"period\", \"demand\" ],\r\n"
			+ "            \"TYPES\": [ \"INTEGER\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ 1, 40.0 ],\r\n"
			+ "            [ 2, 60.0 ],\r\n"
			+ "            [ 3, 75.0 ],\r\n"
			+ "            [ 4, 25.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"parameters\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"INPUT\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"regularCost\", \"extraCost\", \"capacity\", \"initialInventory\", \"inventoryCost\" ],\r\n"
			+ "            \"TYPES\": [ \"DOUBLE\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ 400.0, 450.0, 40.0, 10.0, 20.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"regular\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"period\", \"Column\", \"LowerBound\", \"UpperBound\", \"value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"regular\", 1, \"regular_1\", 0.0, \"Infinity\", 40.0 ],\r\n"
			+ "            [ \"regular\", 2, \"regular_2\", 0.0, \"Infinity\", 40.0 ],\r\n"
			+ "            [ \"regular\", 3, \"regular_3\", 0.0, \"Infinity\", 40.0 ],\r\n"
			+ "            [ \"regular\", 4, \"regular_4\", 0.0, \"Infinity\", 25.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"extra\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"period\", \"Column\", \"LowerBound\", \"UpperBound\", \"value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"extra\", 1, \"extra_1\", 0.0, \"Infinity\", 0.0 ],\r\n"
			+ "            [ \"extra\", 2, \"extra_2\", 0.0, \"Infinity\", 10.0 ],\r\n"
			+ "            [ \"extra\", 3, \"extra_3\", 0.0, \"Infinity\", 35.0 ],\r\n"
			+ "            [ \"extra\", 4, \"extra_4\", 0.0, \"Infinity\", 0.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"inventory\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"period\", \"Column\", \"LowerBound\", \"UpperBound\", \"value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"inventory\", 1, \"inventory_1\", 0.0, \"Infinity\", 10.0 ],\r\n"
			+ "            [ \"inventory\", 2, \"inventory_2\", 0.0, \"Infinity\", 0.0 ],\r\n"
			+ "            [ \"inventory\", 3, \"inventory_3\", 0.0, \"Infinity\", 0.0 ],\r\n"
			+ "            [ \"inventory\", 4, \"inventory_4\", 0.0, \"Infinity\", 0.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctCapacity\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"period\", \"Row\", \"Sense\", \"RHS\", \"dual\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctCapacity\", 1, \"ctCapacity_1\", \"<=\", 40.0, -30.0 ],\r\n"
			+ "            [ \"ctCapacity\", 2, \"ctCapacity_2\", \"<=\", 40.0, -50.0 ],\r\n"
			+ "            [ \"ctCapacity\", 3, \"ctCapacity_3\", \"<=\", 40.0, -50.0 ],\r\n"
			+ "            [ \"ctCapacity\", 4, \"ctCapacity_4\", \"<=\", 40.0, 0.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctBoat\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"period\", \"Row\", \"Sense\", \"RHS\", \"dual\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctBoat\", 2, \"ctBoat_2\", \"==\", 60.0, 450.0 ],\r\n"
			+ "            [ \"ctBoat\", 3, \"ctBoat_3\", \"==\", 75.0, 450.0 ],\r\n"
			+ "            [ \"ctBoat\", 4, \"ctBoat_4\", \"==\", 25.0, 400.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctBoat_1\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"period\", \"Row\", \"Sense\", \"RHS\", \"dual\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctBoat_1\", 1, \"ctBoat_1\", \"==\", 30.0, 430.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"totalCost\",\r\n"
			+ "          \"CLASS\": \"OBJECTIVE\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Constant\", \"Sense\", \"Value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"totalCost\", \"totalCost\", 0.0, \"MINIMIZE\", 78450.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctCapacity_regular\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctCapacity_1\", \"regular_1\", 1.0 ],\r\n"
			+ "            [ \"ctCapacity_2\", \"regular_2\", 1.0 ],\r\n"
			+ "            [ \"ctCapacity_3\", \"regular_3\", 1.0 ],\r\n"
			+ "            [ \"ctCapacity_4\", \"regular_4\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctBoat_regular\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctBoat_1\", \"regular_1\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_2\", \"regular_2\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_3\", \"regular_3\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_4\", \"regular_4\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctBoat_extra\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctBoat_1\", \"extra_1\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_2\", \"extra_2\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_3\", \"extra_3\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_4\", \"extra_4\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctBoat_inventory\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctBoat_1\", \"inventory_1\", -1.0 ],\r\n"
			+ "            [ \"ctBoat_2\", \"inventory_2\", -1.0 ],\r\n"
			+ "            [ \"ctBoat_3\", \"inventory_3\", -1.0 ],\r\n"
			+ "            [ \"ctBoat_4\", \"inventory_4\", -1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctBoat_lagged_inventory\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctBoat_2\", \"inventory_1\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_3\", \"inventory_2\", 1.0 ],\r\n"
			+ "            [ \"ctBoat_4\", \"inventory_3\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"totalCost_regular\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"totalCost\", \"regular_1\", 400.0 ],\r\n"
			+ "            [ \"totalCost\", \"regular_2\", 400.0 ],\r\n"
			+ "            [ \"totalCost\", \"regular_3\", 400.0 ],\r\n"
			+ "            [ \"totalCost\", \"regular_4\", 400.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"totalCost_extra\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"totalCost\", \"extra_1\", 450.0 ],\r\n"
			+ "            [ \"totalCost\", \"extra_2\", 450.0 ],\r\n"
			+ "            [ \"totalCost\", \"extra_3\", 450.0 ],\r\n"
			+ "            [ \"totalCost\", \"extra_4\", 450.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"totalCost_inventory\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"totalCost\", \"inventory_1\", 20.0 ],\r\n"
			+ "            [ \"totalCost\", \"inventory_2\", 20.0 ],\r\n"
			+ "            [ \"totalCost\", \"inventory_3\", 20.0 ],\r\n"
			+ "            [ \"totalCost\", \"inventory_4\", 20.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"production\",\r\n"
			+ "          \"CLASS\": \"DATA\",\r\n"
			+ "          \"KIND\": \"OUTPUT\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"period\", \"regular\", \"extra\", \"inventory\", \"marginalCapacityValue\" ],\r\n"
			+ "            \"TYPES\": [ \"INTEGER\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ 1, 40.0, 0.0, 10.0, -30.0 ],\r\n"
			+ "            [ 2, 40.0, 10.0, 0.0, -50.0 ],\r\n"
			+ "            [ 3, 40.0, 35.0, 0.0, -50.0 ],\r\n"
			+ "            [ 4, 25.0, 0.0, 0.0, 0.0 ]\r\n"
			+ "          ]\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n"
			+ "\r\n";
	
	public Sailco() {
		super();
	}

	/**
	 * The main method sets up and runs the example Application.
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		MsdxApplication.example(
			"Sailco MOSDEX Query-Form Demo Using CPLEX", 
			"sailco_2-0.json", 
			expectedOutput)
			.run();
	}
	

}//class Sailco
