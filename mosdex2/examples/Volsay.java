/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.examples;

import io.github.JeremyBloom.mosdex2.MsdxApplication;

/**
 * Demonstrates MOSDEX using the Volsay problem in instance form.
 * Solves with CPLEX and returns solution values.
 * <p>
 * Volsay is a simple 3 constraint, 2 variable problem.
 * MOSDEX is probably overkill for it, but you don't need a solver for this problem either.
 * MOSDEX is intended for very large problems encountered in real-world applications
 * where query form provides a very compact problem representation.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class Volsay {
	
	/**
	 * Output expected from the solution.
	 * Note, this string is provided in two forms: <br>
	 * - as a Java text block (for use in Java 13 and above)<br>
	 * - as a traditional Java string with the necessary escaped characters.
	 * <p>
	 * The volsay problem is stated in the MATH element of the MOSDEX file.
	 */
	static String expectedOutput= 
			/*'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json",
  "MODULES": [
    {
      "NAME": "volsay",
      "CLASS": "MODULE",
      "KIND": "MODEL",
      "HEADING": {
        "DESCRIPTION": [
          "Linear program in instance form",
          "MOSDEX is probably overkill for this simple 3 constraint, 2 variable problem",
          "but you don't need a solver for this problem either.",
          "MOSDEX is intended for very large problems encountered in real-world applications",
          "and in such cases, recipe form provides a very compact problem representation.",
          "MOSDEX 2-0 Syntax"
        ],
        "VERSION": [ "2-0" ],
        "REFERENCE": [
          "https://www.ibm.com/support/knowledgecenter/en/SSSA5P_12.5.0/ilog.odms.ide.help/OPL_Studio/opllanguser/topics/opl_languser_shortTour_LP_prodplanning.html"
        ],
        "AUTHOR": [
          "Jeremy A. Bloom (jeremyblmca@gmail.com)"
        ],
        "NOTICES": [
          "Copyright 2019 Jeremy A. Bloom"
        ],
        "MATH": [
          "maximize   40 * Gas + 50 * Chloride;",
          "subject to {",
          "ctMaxTotal: Gas + Chloride <= 50;",
          "ctMaxTotal2: 3 * Gas + 4 * Chloride <= 180;",
          "ctMaxChloride: Chloride <= 40;",
          "}"
        ]
      },
      "TABLES": [
        {
          "NAME": "gas",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "gas", "gas", 0.0, "Infinity", 20.0 ]
          ]
        },
        {
          "NAME": "chloride",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "chloride", "chloride", 0.0, "Infinity", 30.0 ]
          ]
        },
        {
          "NAME": "ctMaxTotal",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Sense", "RHS", "Dual" ],
            "TYPES": [ "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxTotal", "ctMaxTotal", "<=", 50.0, 10.0 ]
          ]
        },
        {
          "NAME": "ctMaxTotal2",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Sense", "RHS", "Dual" ],
            "TYPES": [ "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxTotal2", "ctMaxTotal2", "<=", 180.0, 10.0 ]
          ]
        },
        {
          "NAME": "ctMaxChloride",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Sense", "RHS", "Dual" ],
            "TYPES": [ "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxChloride", "ctMaxChloride", "<=", 40.0, -0.0 ]
          ]
        },
        {
          "NAME": "profit",
          "CLASS": "OBJECTIVE",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Constant", "Sense", "Value" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "profit", "profit", 0.0, "MAXIMIZE", 2300.0 ]
          ]
        },
        {
          "NAME": "profit_gas",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "profit", "gas", 40.0 ]
          ]
        },
        {
          "NAME": "profit_chloride",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "profit", "chloride", 50.0 ]
          ]
        },
        {
          "NAME": "ctMaxTotal_gas",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxTotal", "gas", 1.0 ]
          ]
        },
        {
          "NAME": "ctMaxTotal_chloride",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxTotal", "chloride", 1.0 ]
          ]
        },
        {
          "NAME": "ctMaxTotal2_gas",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxTotal2", "gas", 3.0 ]
          ]
        },
        {
          "NAME": "ctMaxTotal2_chloride",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxTotal2", "chloride", 4.0 ]
          ]
        },
        {
          "NAME": "ctMaxChloride_chloride",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctMaxChloride", "chloride", 1.0 ]
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
			+ "      \"NAME\": \"volsay\",\r\n"
			+ "      \"CLASS\": \"MODULE\",\r\n"
			+ "      \"KIND\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\r\n"
			+ "          \"Linear program in instance form\",\r\n"
			+ "          \"MOSDEX is probably overkill for this simple 3 constraint, 2 variable problem\",\r\n"
			+ "          \"but you don't need a solver for this problem either.\",\r\n"
			+ "          \"MOSDEX is intended for very large problems encountered in real-world applications\",\r\n"
			+ "          \"and in such cases, recipe form provides a very compact problem representation.\",\r\n"
			+ "          \"MOSDEX 2-0 Syntax\"\r\n"
			+ "        ],\r\n"
			+ "        \"VERSION\": [ \"2-0\" ],\r\n"
			+ "        \"REFERENCE\": [\r\n"
			+ "          \"https://www.ibm.com/support/knowledgecenter/en/SSSA5P_12.5.0/ilog.odms.ide.help/OPL_Studio/opllanguser/topics/opl_languser_shortTour_LP_prodplanning.html\"\r\n"
			+ "        ],\r\n"
			+ "        \"AUTHOR\": [\r\n"
			+ "          \"Jeremy A. Bloom (jeremyblmca@gmail.com)\"\r\n"
			+ "        ],\r\n"
			+ "        \"NOTICES\": [\r\n"
			+ "          \"Copyright 2019 Jeremy A. Bloom\"\r\n"
			+ "        ],\r\n"
			+ "        \"MATH\": [\r\n"
			+ "          \"maximize   40 * Gas + 50 * Chloride;\",\r\n"
			+ "          \"subject to {\",\r\n"
			+ "          \"ctMaxTotal: Gas + Chloride <= 50;\",\r\n"
			+ "          \"ctMaxTotal2: 3 * Gas + 4 * Chloride <= 180;\",\r\n"
			+ "          \"ctMaxChloride: Chloride <= 40;\",\r\n"
			+ "          \"}\"\r\n"
			+ "        ]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"gas\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"gas\", \"gas\", 0.0, \"Infinity\", 20.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"chloride\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"chloride\", \"chloride\", 0.0, \"Infinity\", 30.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxTotal\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Sense\", \"RHS\", \"Dual\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxTotal\", \"ctMaxTotal\", \"<=\", 50.0, 10.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxTotal2\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Sense\", \"RHS\", \"Dual\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxTotal2\", \"ctMaxTotal2\", \"<=\", 180.0, 10.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxChloride\",\r\n"
			+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Sense\", \"RHS\", \"Dual\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxChloride\", \"ctMaxChloride\", \"<=\", 40.0, -0.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"profit\",\r\n"
			+ "          \"CLASS\": \"OBJECTIVE\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Constant\", \"Sense\", \"Value\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"profit\", \"profit\", 0.0, \"MAXIMIZE\", 2300.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"profit_gas\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"profit\", \"gas\", 40.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"profit_chloride\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"profit\", \"chloride\", 50.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxTotal_gas\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxTotal\", \"gas\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxTotal_chloride\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxTotal\", \"chloride\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxTotal2_gas\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxTotal2\", \"gas\", 3.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxTotal2_chloride\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxTotal2\", \"chloride\", 4.0 ]\r\n"
			+ "          ]\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ctMaxChloride_chloride\",\r\n"
			+ "          \"CLASS\": \"TERM\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ctMaxChloride\", \"chloride\", 1.0 ]\r\n"
			+ "          ]\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n"
			+ "\r\n";
	
	public Volsay() {
		super();
	}

	/**
	 * The main method sets up and runs the example Application.
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		MsdxApplication.example(
			"Volsay MOSDEX Instance-Form Demo Using CPLEX", 
			"volsay_2-0.json", 
			expectedOutput)
		.run();
	}

	
}//Volsay
