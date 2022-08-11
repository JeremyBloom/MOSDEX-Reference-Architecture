/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.examples;

import java.nio.file.Path;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxApplication;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;

/**
 * Demonstrates MOSDEX using a warehouse location problem in query form.
 * Uses a small data set for testing, which includes sales demand data.
 * Solves with CPLEX and returns solution values.
 * Warehousing is a facility location problem.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class WarehousingSmall {

	/**
	 * The display title of the example. 
	 * The first word should be the short name of the example.
	 */
	static String title= "Warehousing with Small Dataset MOSDEX Query-Form Demo Using CPLEX"; 
	
	/**
	 * The name of the directory containing the MOSDEX JSON files of the examples.
	 * This directory should be a folder directly under the mosdex2 project: 
	 * /mosdex2/exampleFiles
	 */
	static String exampleFiles= "exampleFiles";
	
	/**
	 * The name of the MOSDEX JSON model file in the example files directory.
	 */
	static String fileName= "warehousing_2-0.json"; //model file
	
	/**
	 * The name of the MOSDEX JSON data file in the example files directory.
	 */
	static String dataFileName= "warehousingTestData_2-0.json"; 
	
	/**
	 * Output expected from the solution.
	 * Note, this string is provided in two forms: <br>
	 * - as a Java text block (for use in Java 13 and above)<br>
	 * - as a traditional Java string with the necessary escaped characters.<br>
	 * <p>
	 * The warehousing problem is stated in the MATH element of the MOSDEX file.
	 */
	static String expectedOutput= 
		/*'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2-0/MOSDEXSchemaV2-0.json",
  "MODULES": [
    {
      "NAME": "warehouseModel",
      "CLASS": "MODULE",
      "KIND": "MODEL",
      "HEADING": {
        "DESCRIPTION": [
          "Warehouse location problem",
          "MOSDEX 2-0 Syntax"
        ],
        "VERSION": [ "2-0" ],
        "REFERENCE": [
          "https://github.com/JeremyBloom/Optimization---Sample-Notebooks/blob/master/Optimization%2BModeling%2Band%2BRelational%2BData%2Bpub.ipynb"
        ],
        "AUTHOR": [
          "Jeremy A. Bloom (jeremyblmca@gmail.com)"
        ],
        "NOTICES": [
          "Copyright 2019 Jeremy A. Bloom"
        ],
        "MATH": [
          "dexpr float capitalCost= sum(w in warehouses) (w.fixedCost*open[w] + w.capacityCost*capacity[w]);",
          "dexpr float operatingCost= sum(r in routes) r.shippingCost*demand[r]*ship[r];",
          "",
          "minimize totalCost == capitalCost + operatingCost; // $/yr",
          "subject to {",
          "",
          "forall(w in warehouses)",
          "//Cannot ship more out of a warehouse than its capacity",
          "ctCapacity[w]: capacity[w] >= sum(r in routes: r.location==w.location) demand[r]*ship[r];",
          "",
          "forall(s in stores)",
          "//Must ship at least 100% of each store's demand",
          "ctDemand[s]: sum(r in routes: r.store==s.store) ship[r] >= 1.0;",
          "",
          "forall(r in routes)",
          "//Can only ship along a supply route if its warehouse is open",
          "ctSupply[r]: ship[r] - open[r.location]<= 0.0",
          "",
          "}"
        ]
      },
      "TABLES": [
        {
          "NAME": "open",
          "CLASS": "VARIABLE",
          "KIND": "BINARY",
          "SCHEMA": {
            "FIELDS": [ "Name", "location", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "STRING", "INTEGER", "INTEGER", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "open", "Brockton-MA", "open_Brockton-MA", 0, 1, 1.0 ],
            [ "open", "Bristol-CT", "open_Bristol-CT", 0, 1, 0.0 ]
          ]
        },
        {
          "NAME": "capacity",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "location", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "capacity", "Brockton-MA", "capacity_Brockton-MA", 0.0, "Infinity", 571.0 ],
            [ "capacity", "Bristol-CT", "capacity_Bristol-CT", 0.0, "Infinity", 0.0 ]
          ]
        },
        {
          "NAME": "ship",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "location", "store", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ship", "Brockton-MA", "Malden-MA", "ship_Brockton-MA_Malden-MA", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Medford-MA", "ship_Brockton-MA_Medford-MA", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Quincy-MA", "ship_Brockton-MA_Quincy-MA", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Brockton-MA", "ship_Brockton-MA_Brockton-MA", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Bristol-CT", "ship_Brockton-MA_Bristol-CT", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Manchester-CT", "ship_Brockton-MA_Manchester-CT", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Milford-CT", "ship_Brockton-MA_Milford-CT", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "New-Haven-CT", "ship_Brockton-MA_New-Haven-CT", 0.0, 1.0, 1.0 ],
            [ "ship", "Brockton-MA", "Stamford-CT", "ship_Brockton-MA_Stamford-CT", 0.0, 1.0, 1.0 ],
            [ "ship", "Bristol-CT", "Malden-MA", "ship_Bristol-CT_Malden-MA", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Medford-MA", "ship_Bristol-CT_Medford-MA", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Quincy-MA", "ship_Bristol-CT_Quincy-MA", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Brockton-MA", "ship_Bristol-CT_Brockton-MA", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Bristol-CT", "ship_Bristol-CT_Bristol-CT", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Manchester-CT", "ship_Bristol-CT_Manchester-CT", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Milford-CT", "ship_Bristol-CT_Milford-CT", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "New-Haven-CT", "ship_Bristol-CT_New-Haven-CT", 0.0, 1.0, 0.0 ],
            [ "ship", "Bristol-CT", "Stamford-CT", "ship_Bristol-CT_Stamford-CT", 0.0, 1.0, 0.0 ]
          ]
        },
        {
          "NAME": "ctCapacity",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "location", "Row", "Sense", "RHS" ],
            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctCapacity", "Brockton-MA", "ctCapacity_Brockton-MA", ">=", 0.0 ],
            [ "ctCapacity", "Bristol-CT", "ctCapacity_Bristol-CT", ">=", 0.0 ]
          ]
        },
        {
          "NAME": "ctCapacity_capacity",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctCapacity_Brockton-MA", "capacity_Brockton-MA", 1.0 ],
            [ "ctCapacity_Bristol-CT", "capacity_Bristol-CT", 1.0 ]
          ]
        },
        {
          "NAME": "ctCapacity_ship",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Malden-MA", -104.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Medford-MA", -50.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Quincy-MA", -25.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Brockton-MA", -28.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Bristol-CT", -28.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Manchester-CT", -80.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Milford-CT", -103.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_New-Haven-CT", -80.0 ],
            [ "ctCapacity_Brockton-MA", "ship_Brockton-MA_Stamford-CT", -73.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Malden-MA", -104.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Medford-MA", -50.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Quincy-MA", -25.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Brockton-MA", -28.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Bristol-CT", -28.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Manchester-CT", -80.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Milford-CT", -103.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_New-Haven-CT", -80.0 ],
            [ "ctCapacity_Bristol-CT", "ship_Bristol-CT_Stamford-CT", -73.0 ]
          ]
        },
        {
          "NAME": "ctDemand",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "store", "Row", "Sense", "RHS" ],
            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctDemand", "Malden-MA", "ctDemand_Malden-MA", ">=", 1.0 ],
            [ "ctDemand", "Medford-MA", "ctDemand_Medford-MA", ">=", 1.0 ],
            [ "ctDemand", "Quincy-MA", "ctDemand_Quincy-MA", ">=", 1.0 ],
            [ "ctDemand", "Brockton-MA", "ctDemand_Brockton-MA", ">=", 1.0 ],
            [ "ctDemand", "Bristol-CT", "ctDemand_Bristol-CT", ">=", 1.0 ],
            [ "ctDemand", "Manchester-CT", "ctDemand_Manchester-CT", ">=", 1.0 ],
            [ "ctDemand", "Milford-CT", "ctDemand_Milford-CT", ">=", 1.0 ],
            [ "ctDemand", "New-Haven-CT", "ctDemand_New-Haven-CT", ">=", 1.0 ],
            [ "ctDemand", "Stamford-CT", "ctDemand_Stamford-CT", ">=", 1.0 ]
          ]
        },
        {
          "NAME": "ctDemand_ship",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctDemand_Malden-MA", "ship_Brockton-MA_Malden-MA", 1.0 ],
            [ "ctDemand_Medford-MA", "ship_Brockton-MA_Medford-MA", 1.0 ],
            [ "ctDemand_Quincy-MA", "ship_Brockton-MA_Quincy-MA", 1.0 ],
            [ "ctDemand_Brockton-MA", "ship_Brockton-MA_Brockton-MA", 1.0 ],
            [ "ctDemand_Bristol-CT", "ship_Brockton-MA_Bristol-CT", 1.0 ],
            [ "ctDemand_Manchester-CT", "ship_Brockton-MA_Manchester-CT", 1.0 ],
            [ "ctDemand_Milford-CT", "ship_Brockton-MA_Milford-CT", 1.0 ],
            [ "ctDemand_New-Haven-CT", "ship_Brockton-MA_New-Haven-CT", 1.0 ],
            [ "ctDemand_Stamford-CT", "ship_Brockton-MA_Stamford-CT", 1.0 ],
            [ "ctDemand_Malden-MA", "ship_Bristol-CT_Malden-MA", 1.0 ],
            [ "ctDemand_Medford-MA", "ship_Bristol-CT_Medford-MA", 1.0 ],
            [ "ctDemand_Quincy-MA", "ship_Bristol-CT_Quincy-MA", 1.0 ],
            [ "ctDemand_Brockton-MA", "ship_Bristol-CT_Brockton-MA", 1.0 ],
            [ "ctDemand_Bristol-CT", "ship_Bristol-CT_Bristol-CT", 1.0 ],
            [ "ctDemand_Manchester-CT", "ship_Bristol-CT_Manchester-CT", 1.0 ],
            [ "ctDemand_Milford-CT", "ship_Bristol-CT_Milford-CT", 1.0 ],
            [ "ctDemand_New-Haven-CT", "ship_Bristol-CT_New-Haven-CT", 1.0 ],
            [ "ctDemand_Stamford-CT", "ship_Bristol-CT_Stamford-CT", 1.0 ]
          ]
        },
        {
          "NAME": "ctSupply",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "location", "store", "Row", "Sense", "RHS" ],
            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctSupply", "Brockton-MA", "Malden-MA", "ctSupply_Brockton-MA_Malden-MA", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Medford-MA", "ctSupply_Brockton-MA_Medford-MA", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Quincy-MA", "ctSupply_Brockton-MA_Quincy-MA", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Brockton-MA", "ctSupply_Brockton-MA_Brockton-MA", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Bristol-CT", "ctSupply_Brockton-MA_Bristol-CT", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Manchester-CT", "ctSupply_Brockton-MA_Manchester-CT", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Milford-CT", "ctSupply_Brockton-MA_Milford-CT", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "New-Haven-CT", "ctSupply_Brockton-MA_New-Haven-CT", "<=", 0.0 ],
            [ "ctSupply", "Brockton-MA", "Stamford-CT", "ctSupply_Brockton-MA_Stamford-CT", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Malden-MA", "ctSupply_Bristol-CT_Malden-MA", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Medford-MA", "ctSupply_Bristol-CT_Medford-MA", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Quincy-MA", "ctSupply_Bristol-CT_Quincy-MA", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Brockton-MA", "ctSupply_Bristol-CT_Brockton-MA", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Bristol-CT", "ctSupply_Bristol-CT_Bristol-CT", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Manchester-CT", "ctSupply_Bristol-CT_Manchester-CT", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Milford-CT", "ctSupply_Bristol-CT_Milford-CT", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "New-Haven-CT", "ctSupply_Bristol-CT_New-Haven-CT", "<=", 0.0 ],
            [ "ctSupply", "Bristol-CT", "Stamford-CT", "ctSupply_Bristol-CT_Stamford-CT", "<=", 0.0 ]
          ]
        },
        {
          "NAME": "ctSupply_open",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctSupply_Brockton-MA_Malden-MA", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Medford-MA", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Quincy-MA", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Brockton-MA", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Bristol-CT", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Manchester-CT", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Milford-CT", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_New-Haven-CT", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Brockton-MA_Stamford-CT", "open_Brockton-MA", -1.0 ],
            [ "ctSupply_Bristol-CT_Malden-MA", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Medford-MA", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Quincy-MA", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Brockton-MA", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Bristol-CT", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Manchester-CT", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Milford-CT", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_New-Haven-CT", "open_Bristol-CT", -1.0 ],
            [ "ctSupply_Bristol-CT_Stamford-CT", "open_Bristol-CT", -1.0 ]
          ]
        },
        {
          "NAME": "ctSupply_ship",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "ctSupply_Brockton-MA_Malden-MA", "ship_Brockton-MA_Malden-MA", 1.0 ],
            [ "ctSupply_Brockton-MA_Medford-MA", "ship_Brockton-MA_Medford-MA", 1.0 ],
            [ "ctSupply_Brockton-MA_Quincy-MA", "ship_Brockton-MA_Quincy-MA", 1.0 ],
            [ "ctSupply_Brockton-MA_Brockton-MA", "ship_Brockton-MA_Brockton-MA", 1.0 ],
            [ "ctSupply_Brockton-MA_Bristol-CT", "ship_Brockton-MA_Bristol-CT", 1.0 ],
            [ "ctSupply_Brockton-MA_Manchester-CT", "ship_Brockton-MA_Manchester-CT", 1.0 ],
            [ "ctSupply_Brockton-MA_Milford-CT", "ship_Brockton-MA_Milford-CT", 1.0 ],
            [ "ctSupply_Brockton-MA_New-Haven-CT", "ship_Brockton-MA_New-Haven-CT", 1.0 ],
            [ "ctSupply_Brockton-MA_Stamford-CT", "ship_Brockton-MA_Stamford-CT", 1.0 ],
            [ "ctSupply_Bristol-CT_Malden-MA", "ship_Bristol-CT_Malden-MA", 1.0 ],
            [ "ctSupply_Bristol-CT_Medford-MA", "ship_Bristol-CT_Medford-MA", 1.0 ],
            [ "ctSupply_Bristol-CT_Quincy-MA", "ship_Bristol-CT_Quincy-MA", 1.0 ],
            [ "ctSupply_Bristol-CT_Brockton-MA", "ship_Bristol-CT_Brockton-MA", 1.0 ],
            [ "ctSupply_Bristol-CT_Bristol-CT", "ship_Bristol-CT_Bristol-CT", 1.0 ],
            [ "ctSupply_Bristol-CT_Manchester-CT", "ship_Bristol-CT_Manchester-CT", 1.0 ],
            [ "ctSupply_Bristol-CT_Milford-CT", "ship_Bristol-CT_Milford-CT", 1.0 ],
            [ "ctSupply_Bristol-CT_New-Haven-CT", "ship_Bristol-CT_New-Haven-CT", 1.0 ],
            [ "ctSupply_Bristol-CT_Stamford-CT", "ship_Bristol-CT_Stamford-CT", 1.0 ]
          ]
        },
        {
          "NAME": "capitalCost",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "capitalCost", "capitalCost", 0.0, "Infinity", 634508.0 ]
          ]
        },
        {
          "NAME": "deCapitalCost",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Sense", "RHS" ],
            "TYPES": [ "STRING", "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deCapitalCost", "deCapitalCost", "==", 0.0 ]
          ]
        },
        {
          "NAME": "deCapitalCost_open",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deCapitalCost", "open_Brockton-MA", 550000.0 ],
            [ "deCapitalCost", "open_Bristol-CT", 600000.0 ]
          ]
        },
        {
          "NAME": "deCapitalCost_capacity",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deCapitalCost", "capacity_Brockton-MA", 148.0 ],
            [ "deCapitalCost", "capacity_Bristol-CT", 148.0 ]
          ]
        },
        {
          "NAME": "deCapitalCost_capitalCost",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deCapitalCost", "capitalCost", -1.0 ]
          ]
        },
        {
          "NAME": "operatingCost",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS": [ "Name", "Column", "LowerBound", "UpperBound", "Value" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "operatingCost", "operatingCost", 0.0, "Infinity", 81781.9 ]
          ]
        },
        {
          "NAME": "deOperatingCost",
          "CLASS": "CONSTRAINT",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Name", "Row", "Sense", "RHS" ],
            "TYPES": [ "STRING", "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deOperatingCost", "deOperatingCost", "==", 0.0 ]
          ]
        },
        {
          "NAME": "deOperatingCost_ship",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deOperatingCost", "ship_Brockton-MA_Malden-MA", 4465.76 ],
            [ "deOperatingCost", "ship_Brockton-MA_Medford-MA", 2071.5 ],
            [ "deOperatingCost", "ship_Brockton-MA_Quincy-MA", 465.25 ],
            [ "deOperatingCost", "ship_Brockton-MA_Brockton-MA", 0.0 ],
            [ "deOperatingCost", "ship_Brockton-MA_Bristol-CT", 5209.400000000001 ],
            [ "deOperatingCost", "ship_Brockton-MA_Manchester-CT", 11529.6 ],
            [ "deOperatingCost", "ship_Brockton-MA_Milford-CT", 22398.38 ],
            [ "deOperatingCost", "ship_Brockton-MA_New-Haven-CT", 16124.0 ],
            [ "deOperatingCost", "ship_Brockton-MA_Stamford-CT", 19518.010000000002 ],
            [ "deOperatingCost", "ship_Bristol-CT_Malden-MA", 20558.72 ],
            [ "deOperatingCost", "ship_Bristol-CT_Medford-MA", 9554.5 ],
            [ "deOperatingCost", "ship_Bristol-CT_Quincy-MA", 4778.0 ],
            [ "deOperatingCost", "ship_Bristol-CT_Brockton-MA", 5209.400000000001 ],
            [ "deOperatingCost", "ship_Bristol-CT_Bristol-CT", 0.0 ],
            [ "deOperatingCost", "ship_Bristol-CT_Manchester-CT", 3356.8 ],
            [ "deOperatingCost", "ship_Bristol-CT_Milford-CT", 5837.01 ],
            [ "deOperatingCost", "ship_Bristol-CT_New-Haven-CT", 3563.2 ],
            [ "deOperatingCost", "ship_Bristol-CT_Stamford-CT", 6910.179999999999 ]
          ]
        },
        {
          "NAME": "deOperatingCost_operatingCost",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "deOperatingCost", "operatingCost", -1.0 ]
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
            [ "totalCost", "totalCost", 0.0, "MINIMIZE", 716289.9 ]
          ]
        },
        {
          "NAME": "totalCost_capitalCost",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "totalCost", "capitalCost", 1.0 ]
          ]
        },
        {
          "NAME": "totalCost_operatingCost",
          "CLASS": "TERM",
          "KIND": "LINEAR",
          "SCHEMA": {
            "FIELDS": [ "Row", "Column", "Coefficient" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "totalCost", "operatingCost", 1.0 ]
          ]
        },
        {
          "NAME": "objectives",
          "CLASS": "DATA",
          "KIND": "OUTPUT",
          "SCHEMA": {
            "FIELDS": [ "problem", "capitalCost", "operatingCost", "totalCost" ],
            "TYPES": [ "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "Warehousing", 634508.0, 81781.9, 716289.9 ]
          ]
        },
        {
          "NAME": "openWarehouses",
          "CLASS": "DATA",
          "KIND": "OUTPUT",
          "SCHEMA": {
            "FIELDS": [ "location", "open", "capacity" ],
            "TYPES": [ "STRING", "INTEGER", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "Brockton-MA", 1, 571.0 ]
          ]
        },
        {
          "NAME": "shipments",
          "CLASS": "DATA",
          "KIND": "OUTPUT",
          "SCHEMA": {
            "FIELDS": [ "location", "store", "amount" ],
            "TYPES": [ "STRING", "STRING", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "Brockton-MA", "Malden-MA", 104.0 ],
            [ "Brockton-MA", "Medford-MA", 50.0 ],
            [ "Brockton-MA", "Quincy-MA", 25.0 ],
            [ "Brockton-MA", "Brockton-MA", 28.0 ],
            [ "Brockton-MA", "Bristol-CT", 28.0 ],
            [ "Brockton-MA", "Manchester-CT", 80.0 ],
            [ "Brockton-MA", "Milford-CT", 103.0 ],
            [ "Brockton-MA", "New-Haven-CT", 80.0 ],
            [ "Brockton-MA", "Stamford-CT", 73.0 ]
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
		+ "      \"NAME\": \"warehouseModel\",\r\n"
		+ "      \"CLASS\": \"MODULE\",\r\n"
		+ "      \"KIND\": \"MODEL\",\r\n"
		+ "      \"HEADING\": {\r\n"
		+ "        \"DESCRIPTION\": [\r\n"
		+ "          \"Warehouse location problem\",\r\n"
		+ "          \"MOSDEX 2-0 Syntax\"\r\n"
		+ "        ],\r\n"
		+ "        \"VERSION\": [ \"2-0\" ],\r\n"
		+ "        \"REFERENCE\": [\r\n"
		+ "          \"https://github.com/JeremyBloom/Optimization---Sample-Notebooks/blob/master/Optimization%2BModeling%2Band%2BRelational%2BData%2Bpub.ipynb\"\r\n"
		+ "        ],\r\n"
		+ "        \"AUTHOR\": [\r\n"
		+ "          \"Jeremy A. Bloom (jeremyblmca@gmail.com)\"\r\n"
		+ "        ],\r\n"
		+ "        \"NOTICES\": [\r\n"
		+ "          \"Copyright 2019 Jeremy A. Bloom\"\r\n"
		+ "        ],\r\n"
		+ "        \"MATH\": [\r\n"
		+ "          \"dexpr float capitalCost= sum(w in warehouses) (w.fixedCost*open[w] + w.capacityCost*capacity[w]);\",\r\n"
		+ "          \"dexpr float operatingCost= sum(r in routes) r.shippingCost*demand[r]*ship[r];\",\r\n"
		+ "          \"\",\r\n"
		+ "          \"minimize totalCost == capitalCost + operatingCost; // $/yr\",\r\n"
		+ "          \"subject to {\",\r\n"
		+ "          \"\",\r\n"
		+ "          \"forall(w in warehouses)\",\r\n"
		+ "          \"//Cannot ship more out of a warehouse than its capacity\",\r\n"
		+ "          \"ctCapacity[w]: capacity[w] >= sum(r in routes: r.location==w.location) demand[r]*ship[r];\",\r\n"
		+ "          \"\",\r\n"
		+ "          \"forall(s in stores)\",\r\n"
		+ "          \"//Must ship at least 100% of each store's demand\",\r\n"
		+ "          \"ctDemand[s]: sum(r in routes: r.store==s.store) ship[r] >= 1.0;\",\r\n"
		+ "          \"\",\r\n"
		+ "          \"forall(r in routes)\",\r\n"
		+ "          \"//Can only ship along a supply route if its warehouse is open\",\r\n"
		+ "          \"ctSupply[r]: ship[r] - open[r.location]<= 0.0\",\r\n"
		+ "          \"\",\r\n"
		+ "          \"}\"\r\n"
		+ "        ]\r\n"
		+ "      },\r\n"
		+ "      \"TABLES\": [\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"open\",\r\n"
		+ "          \"CLASS\": \"VARIABLE\",\r\n"
		+ "          \"KIND\": \"BINARY\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"location\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"INTEGER\", \"INTEGER\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"open\", \"Brockton-MA\", \"open_Brockton-MA\", 0, 1, 1.0 ],\r\n"
		+ "            [ \"open\", \"Bristol-CT\", \"open_Bristol-CT\", 0, 1, 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"capacity\",\r\n"
		+ "          \"CLASS\": \"VARIABLE\",\r\n"
		+ "          \"KIND\": \"CONTINUOUS\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"location\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"capacity\", \"Brockton-MA\", \"capacity_Brockton-MA\", 0.0, \"Infinity\", 571.0 ],\r\n"
		+ "            [ \"capacity\", \"Bristol-CT\", \"capacity_Bristol-CT\", 0.0, \"Infinity\", 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ship\",\r\n"
		+ "          \"CLASS\": \"VARIABLE\",\r\n"
		+ "          \"KIND\": \"CONTINUOUS\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"location\", \"store\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Malden-MA\", \"ship_Brockton-MA_Malden-MA\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Medford-MA\", \"ship_Brockton-MA_Medford-MA\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Quincy-MA\", \"ship_Brockton-MA_Quincy-MA\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Brockton-MA\", \"ship_Brockton-MA_Brockton-MA\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Bristol-CT\", \"ship_Brockton-MA_Bristol-CT\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Manchester-CT\", \"ship_Brockton-MA_Manchester-CT\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Milford-CT\", \"ship_Brockton-MA_Milford-CT\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"New-Haven-CT\", \"ship_Brockton-MA_New-Haven-CT\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Brockton-MA\", \"Stamford-CT\", \"ship_Brockton-MA_Stamford-CT\", 0.0, 1.0, 1.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Malden-MA\", \"ship_Bristol-CT_Malden-MA\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Medford-MA\", \"ship_Bristol-CT_Medford-MA\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Quincy-MA\", \"ship_Bristol-CT_Quincy-MA\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Brockton-MA\", \"ship_Bristol-CT_Brockton-MA\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Bristol-CT\", \"ship_Bristol-CT_Bristol-CT\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Manchester-CT\", \"ship_Bristol-CT_Manchester-CT\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Milford-CT\", \"ship_Bristol-CT_Milford-CT\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"New-Haven-CT\", \"ship_Bristol-CT_New-Haven-CT\", 0.0, 1.0, 0.0 ],\r\n"
		+ "            [ \"ship\", \"Bristol-CT\", \"Stamford-CT\", \"ship_Bristol-CT_Stamford-CT\", 0.0, 1.0, 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctCapacity\",\r\n"
		+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"location\", \"Row\", \"Sense\", \"RHS\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctCapacity\", \"Brockton-MA\", \"ctCapacity_Brockton-MA\", \">=\", 0.0 ],\r\n"
		+ "            [ \"ctCapacity\", \"Bristol-CT\", \"ctCapacity_Bristol-CT\", \">=\", 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctCapacity_capacity\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"capacity_Brockton-MA\", 1.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"capacity_Bristol-CT\", 1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctCapacity_ship\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Malden-MA\", -104.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Medford-MA\", -50.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Quincy-MA\", -25.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Brockton-MA\", -28.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Bristol-CT\", -28.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Manchester-CT\", -80.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Milford-CT\", -103.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_New-Haven-CT\", -80.0 ],\r\n"
		+ "            [ \"ctCapacity_Brockton-MA\", \"ship_Brockton-MA_Stamford-CT\", -73.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Malden-MA\", -104.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Medford-MA\", -50.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Quincy-MA\", -25.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Brockton-MA\", -28.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Bristol-CT\", -28.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Manchester-CT\", -80.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Milford-CT\", -103.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_New-Haven-CT\", -80.0 ],\r\n"
		+ "            [ \"ctCapacity_Bristol-CT\", \"ship_Bristol-CT_Stamford-CT\", -73.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctDemand\",\r\n"
		+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"store\", \"Row\", \"Sense\", \"RHS\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctDemand\", \"Malden-MA\", \"ctDemand_Malden-MA\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Medford-MA\", \"ctDemand_Medford-MA\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Quincy-MA\", \"ctDemand_Quincy-MA\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Brockton-MA\", \"ctDemand_Brockton-MA\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Bristol-CT\", \"ctDemand_Bristol-CT\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Manchester-CT\", \"ctDemand_Manchester-CT\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Milford-CT\", \"ctDemand_Milford-CT\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"New-Haven-CT\", \"ctDemand_New-Haven-CT\", \">=\", 1.0 ],\r\n"
		+ "            [ \"ctDemand\", \"Stamford-CT\", \"ctDemand_Stamford-CT\", \">=\", 1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctDemand_ship\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctDemand_Malden-MA\", \"ship_Brockton-MA_Malden-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Medford-MA\", \"ship_Brockton-MA_Medford-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Quincy-MA\", \"ship_Brockton-MA_Quincy-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Brockton-MA\", \"ship_Brockton-MA_Brockton-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Bristol-CT\", \"ship_Brockton-MA_Bristol-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Manchester-CT\", \"ship_Brockton-MA_Manchester-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Milford-CT\", \"ship_Brockton-MA_Milford-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_New-Haven-CT\", \"ship_Brockton-MA_New-Haven-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Stamford-CT\", \"ship_Brockton-MA_Stamford-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Malden-MA\", \"ship_Bristol-CT_Malden-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Medford-MA\", \"ship_Bristol-CT_Medford-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Quincy-MA\", \"ship_Bristol-CT_Quincy-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Brockton-MA\", \"ship_Bristol-CT_Brockton-MA\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Bristol-CT\", \"ship_Bristol-CT_Bristol-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Manchester-CT\", \"ship_Bristol-CT_Manchester-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Milford-CT\", \"ship_Bristol-CT_Milford-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_New-Haven-CT\", \"ship_Bristol-CT_New-Haven-CT\", 1.0 ],\r\n"
		+ "            [ \"ctDemand_Stamford-CT\", \"ship_Bristol-CT_Stamford-CT\", 1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctSupply\",\r\n"
		+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"location\", \"store\", \"Row\", \"Sense\", \"RHS\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Malden-MA\", \"ctSupply_Brockton-MA_Malden-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Medford-MA\", \"ctSupply_Brockton-MA_Medford-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Quincy-MA\", \"ctSupply_Brockton-MA_Quincy-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Brockton-MA\", \"ctSupply_Brockton-MA_Brockton-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Bristol-CT\", \"ctSupply_Brockton-MA_Bristol-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Manchester-CT\", \"ctSupply_Brockton-MA_Manchester-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Milford-CT\", \"ctSupply_Brockton-MA_Milford-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"New-Haven-CT\", \"ctSupply_Brockton-MA_New-Haven-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Brockton-MA\", \"Stamford-CT\", \"ctSupply_Brockton-MA_Stamford-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Malden-MA\", \"ctSupply_Bristol-CT_Malden-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Medford-MA\", \"ctSupply_Bristol-CT_Medford-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Quincy-MA\", \"ctSupply_Bristol-CT_Quincy-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Brockton-MA\", \"ctSupply_Bristol-CT_Brockton-MA\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Bristol-CT\", \"ctSupply_Bristol-CT_Bristol-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Manchester-CT\", \"ctSupply_Bristol-CT_Manchester-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Milford-CT\", \"ctSupply_Bristol-CT_Milford-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"New-Haven-CT\", \"ctSupply_Bristol-CT_New-Haven-CT\", \"<=\", 0.0 ],\r\n"
		+ "            [ \"ctSupply\", \"Bristol-CT\", \"Stamford-CT\", \"ctSupply_Bristol-CT_Stamford-CT\", \"<=\", 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctSupply_open\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Malden-MA\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Medford-MA\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Quincy-MA\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Brockton-MA\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Bristol-CT\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Manchester-CT\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Milford-CT\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_New-Haven-CT\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Stamford-CT\", \"open_Brockton-MA\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Malden-MA\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Medford-MA\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Quincy-MA\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Brockton-MA\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Bristol-CT\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Manchester-CT\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Milford-CT\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_New-Haven-CT\", \"open_Bristol-CT\", -1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Stamford-CT\", \"open_Bristol-CT\", -1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"ctSupply_ship\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Malden-MA\", \"ship_Brockton-MA_Malden-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Medford-MA\", \"ship_Brockton-MA_Medford-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Quincy-MA\", \"ship_Brockton-MA_Quincy-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Brockton-MA\", \"ship_Brockton-MA_Brockton-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Bristol-CT\", \"ship_Brockton-MA_Bristol-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Manchester-CT\", \"ship_Brockton-MA_Manchester-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Milford-CT\", \"ship_Brockton-MA_Milford-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_New-Haven-CT\", \"ship_Brockton-MA_New-Haven-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Brockton-MA_Stamford-CT\", \"ship_Brockton-MA_Stamford-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Malden-MA\", \"ship_Bristol-CT_Malden-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Medford-MA\", \"ship_Bristol-CT_Medford-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Quincy-MA\", \"ship_Bristol-CT_Quincy-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Brockton-MA\", \"ship_Bristol-CT_Brockton-MA\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Bristol-CT\", \"ship_Bristol-CT_Bristol-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Manchester-CT\", \"ship_Bristol-CT_Manchester-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Milford-CT\", \"ship_Bristol-CT_Milford-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_New-Haven-CT\", \"ship_Bristol-CT_New-Haven-CT\", 1.0 ],\r\n"
		+ "            [ \"ctSupply_Bristol-CT_Stamford-CT\", \"ship_Bristol-CT_Stamford-CT\", 1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"capitalCost\",\r\n"
		+ "          \"CLASS\": \"VARIABLE\",\r\n"
		+ "          \"KIND\": \"CONTINUOUS\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"capitalCost\", \"capitalCost\", 0.0, \"Infinity\", 634508.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deCapitalCost\",\r\n"
		+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Sense\", \"RHS\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deCapitalCost\", \"deCapitalCost\", \"==\", 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deCapitalCost_open\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deCapitalCost\", \"open_Brockton-MA\", 550000.0 ],\r\n"
		+ "            [ \"deCapitalCost\", \"open_Bristol-CT\", 600000.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deCapitalCost_capacity\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deCapitalCost\", \"capacity_Brockton-MA\", 148.0 ],\r\n"
		+ "            [ \"deCapitalCost\", \"capacity_Bristol-CT\", 148.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deCapitalCost_capitalCost\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deCapitalCost\", \"capitalCost\", -1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"operatingCost\",\r\n"
		+ "          \"CLASS\": \"VARIABLE\",\r\n"
		+ "          \"KIND\": \"CONTINUOUS\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"operatingCost\", \"operatingCost\", 0.0, \"Infinity\", 81781.9 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deOperatingCost\",\r\n"
		+ "          \"CLASS\": \"CONSTRAINT\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Sense\", \"RHS\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deOperatingCost\", \"deOperatingCost\", \"==\", 0.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deOperatingCost_ship\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Malden-MA\", 4465.76 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Medford-MA\", 2071.5 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Quincy-MA\", 465.25 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Brockton-MA\", 0.0 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Bristol-CT\", 5209.400000000001 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Manchester-CT\", 11529.6 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Milford-CT\", 22398.38 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_New-Haven-CT\", 16124.0 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Brockton-MA_Stamford-CT\", 19518.010000000002 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Malden-MA\", 20558.72 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Medford-MA\", 9554.5 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Quincy-MA\", 4778.0 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Brockton-MA\", 5209.400000000001 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Bristol-CT\", 0.0 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Manchester-CT\", 3356.8 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Milford-CT\", 5837.01 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_New-Haven-CT\", 3563.2 ],\r\n"
		+ "            [ \"deOperatingCost\", \"ship_Bristol-CT_Stamford-CT\", 6910.179999999999 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"deOperatingCost_operatingCost\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"deOperatingCost\", \"operatingCost\", -1.0 ]\r\n"
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
		+ "            [ \"totalCost\", \"totalCost\", 0.0, \"MINIMIZE\", 716289.9 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"totalCost_capitalCost\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"totalCost\", \"capitalCost\", 1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"totalCost_operatingCost\",\r\n"
		+ "          \"CLASS\": \"TERM\",\r\n"
		+ "          \"KIND\": \"LINEAR\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"Row\", \"Column\", \"Coefficient\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"totalCost\", \"operatingCost\", 1.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"objectives\",\r\n"
		+ "          \"CLASS\": \"DATA\",\r\n"
		+ "          \"KIND\": \"OUTPUT\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"problem\", \"capitalCost\", \"operatingCost\", \"totalCost\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"Warehousing\", 634508.0, 81781.9, 716289.9 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"openWarehouses\",\r\n"
		+ "          \"CLASS\": \"DATA\",\r\n"
		+ "          \"KIND\": \"OUTPUT\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"location\", \"open\", \"capacity\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"INTEGER\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"Brockton-MA\", 1, 571.0 ]\r\n"
		+ "          ]\r\n"
		+ "        },\r\n"
		+ "        {\r\n"
		+ "          \"NAME\": \"shipments\",\r\n"
		+ "          \"CLASS\": \"DATA\",\r\n"
		+ "          \"KIND\": \"OUTPUT\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"location\", \"store\", \"amount\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"Brockton-MA\", \"Malden-MA\", 104.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Medford-MA\", 50.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Quincy-MA\", 25.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Brockton-MA\", 28.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Bristol-CT\", 28.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Manchester-CT\", 80.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Milford-CT\", 103.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"New-Haven-CT\", 80.0 ],\r\n"
		+ "            [ \"Brockton-MA\", \"Stamford-CT\", 73.0 ]\r\n"
		+ "          ]\r\n"
		+ "        }\r\n"
		+ "      ]\r\n"
		+ "    }\r\n"
		+ "  ]\r\n"
		+ "}\r\n"
		+ "\r\n";
	
	public WarehousingSmall() {
		super();
	}

	/**
	 * The main method sets up and runs the Application. 
	 * It does not use the example method, but instead, specifies all of the 
	 * components of a full Application.
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		if(		title == null || 
				fileName == null || 
				dataFileName == null ||
				expectedOutput == null)
			throw new IllegalArgumentException("Uninitialized field(s)");
		
		Msdx.GLOBAL.setDisplayTitle(title);
		Msdx.GLOBAL.showDisplay();
		
//		Getting the example file from the mosdex2/exampleFiles folder
		Path path= Path.of(MsdxApplication.findProject().toString(), exampleFiles);
		MsdxInputSource modelSource= MsdxInputSource.fromFile(Path.of(path.toString(), fileName).toFile());
		MsdxInputSource dataSource= MsdxInputSource.fromFile(Path.of(path.toString(), dataFileName).toFile());
		
		MsdxApplication application= new MsdxApplication(
			title.split(" (\\b[^\\s]+\\b)")[0]) //first word of title
			.addFile(modelSource, MsdxOutputDestination.toStream(Msdx.GLOBAL.out), expectedOutput)
			.addFile(dataSource)
			.addSolverResults(MsdxOutputDestination.toStream(System.out), true)
			.useSparkDataframes()
			.useJavaSpans()
			.useCplex();
		application.run();
		
	}//main


}//class WarehousingSmall
