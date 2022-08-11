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
 * Uses a full-size data set, and a separate file for sales demand data.
 * Solves with CPLEX and returns selected output tables showing the solution values.
 * Warehousing is a facility location problem.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class WarehousingLarge {
	
	/**
	 * The display title of the example. 
	 * The first word should be the short name of the example.
	 */
	static String title= "Warehousing with Full-Size Dataset MOSDEX Query-Form Demo Using CPLEX"; 
	
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
	 * The name of the MOSDEX JSON data file (except sales data) in the example files directory.
	 */
	static String dataFileName= "warehousingData_2-0.json"; 
	
	/**
	 * The name of the MOSDEX JSON sales demand data file in the example files directory.
	 */
	static String salesFileName= "warehousingSalesData_2-0.json";
	
	/**
	 * Output expected from the solution.
	 * Note, this string is provided in two forms: <br>
	 * - as a Java text block (for use in Java 13 and above)<br>
	 * - as a traditional Java string with the necessary escaped characters.<br>
	 * Because of the size of the input data set, it is not reproduced in the output file; 
	 * instead, only the (non-zero) solution elements are displayed.
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
          "NAME": "objectives",
          "CLASS": "DATA",
          "KIND": "OUTPUT",
          "SCHEMA": {
            "FIELDS": [ "problem", "capitalCost", "operatingCost", "totalCost" ],
            "TYPES": [ "STRING", "DOUBLE", "DOUBLE", "DOUBLE" ]
          },
          "INSTANCE": [
            [ "Warehousing", 6373620.0, 4580688.49, 1.095430849E7 ]
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
            [ "New-York-NY", 1, 3961.0 ],
            [ "Lawrenceville-GA", 1, 1146.0 ],
            [ "Chicago-IL", 1, 2720.0 ],
            [ "Dallas-TX", 1, 1395.0 ],
            [ "Denver-CO", 1, 874.0 ],
            [ "Los-Angeles-CA", 1, 5581.0 ],
            [ "San-Francisco-CA", 1, 2388.0 ]
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
            [ "New-York-NY", "Malden-MA", 104.0 ],
            [ "New-York-NY", "Medford-MA", 50.0 ],
            [ "New-York-NY", "Quincy-MA", 25.0 ],
            [ "New-York-NY", "Brockton-MA", 28.0 ],
            [ "New-York-NY", "Bristol-CT", 28.0 ],
            [ "New-York-NY", "Manchester-CT", 80.0 ],
            [ "New-York-NY", "Milford-CT", 103.0 ],
            [ "New-York-NY", "New-Haven-CT", 80.0 ],
            [ "New-York-NY", "Stamford-CT", 73.0 ],
            [ "New-York-NY", "Bayonne-NJ", 58.0 ],
            [ "New-York-NY", "Passaic-NJ", 64.0 ],
            [ "New-York-NY", "Union-City-NJ", 34.0 ],
            [ "New-York-NY", "West-New-York-NJ", 66.0 ],
            [ "New-York-NY", "Irvington-NJ", 27.0 ],
            [ "New-York-NY", "Jersey-City-NJ", 29.0 ],
            [ "New-York-NY", "Wayne-NJ", 27.0 ],
            [ "New-York-NY", "Lakewood-NJ", 102.0 ],
            [ "New-York-NY", "Toms-River-NJ", 31.0 ],
            [ "New-York-NY", "Piscataway-NJ", 51.0 ],
            [ "New-York-NY", "New-York-NY", 53.0 ],
            [ "New-York-NY", "Staten-Island-NY", 41.0 ],
            [ "New-York-NY", "Bronx-NY", 40.0 ],
            [ "New-York-NY", "Yonkers-NY", 29.0 ],
            [ "New-York-NY", "Brooklyn-NY", 52.0 ],
            [ "New-York-NY", "Flushing-NY", 37.0 ],
            [ "New-York-NY", "Corona-NY", 72.0 ],
            [ "New-York-NY", "Jackson-Heights-NY", 77.0 ],
            [ "New-York-NY", "Elmhurst-NY", 123.0 ],
            [ "New-York-NY", "Forest-Hills-NY", 33.0 ],
            [ "New-York-NY", "Woodside-NY", 113.0 ],
            [ "New-York-NY", "Ridgewood-NY", 179.0 ],
            [ "New-York-NY", "Jamaica-NY", 27.0 ],
            [ "New-York-NY", "Hempstead-NY", 25.0 ],
            [ "New-York-NY", "Far-Rockaway-NY", 113.0 ],
            [ "New-York-NY", "Bay-Shore-NY", 25.0 ],
            [ "New-York-NY", "Huntington-Station-NY", 87.0 ],
            [ "New-York-NY", "Massapequa-NY", 54.0 ],
            [ "New-York-NY", "Troy-NY", 25.0 ],
            [ "New-York-NY", "Newburgh-NY", 52.0 ],
            [ "New-York-NY", "Buffalo-NY", 45.0 ],
            [ "New-York-NY", "Ithaca-NY", 94.0 ],
            [ "New-York-NY", "Greensburg-PA", 94.0 ],
            [ "New-York-NY", "Carlisle-PA", 53.0 ],
            [ "New-York-NY", "Mechanicsburg-PA", 87.0 ],
            [ "New-York-NY", "York-PA", 27.0 ],
            [ "New-York-NY", "Lancaster-PA", 51.0 ],
            [ "New-York-NY", "Allentown-PA", 60.0 ],
            [ "New-York-NY", "Bensalem-PA", 25.0 ],
            [ "New-York-NY", "Morrisville-PA", 25.0 ],
            [ "New-York-NY", "Philadelphia-PA", 149.0 ],
            [ "New-York-NY", "West-Chester-PA", 24.0 ],
            [ "New-York-NY", "Newark-DE", 84.0 ],
            [ "New-York-NY", "New-Castle-DE", 46.0 ],
            [ "New-York-NY", "Washington-DC", 27.0 ],
            [ "New-York-NY", "Fort-Washington-MD", 87.0 ],
            [ "New-York-NY", "Potomac-MD", 52.0 ],
            [ "New-York-NY", "Gaithersburg-MD", 27.0 ],
            [ "New-York-NY", "Silver-Spring-MD", 114.0 ],
            [ "New-York-NY", "Pasadena-MD", 96.0 ],
            [ "New-York-NY", "Baltimore-MD", 32.0 ],
            [ "New-York-NY", "Parkville-MD", 33.0 ],
            [ "New-York-NY", "Hagerstown-MD", 69.0 ],
            [ "New-York-NY", "Annandale-VA", 96.0 ],
            [ "New-York-NY", "Dale-City-VA", 28.0 ],
            [ "New-York-NY", "Stafford-VA", 26.0 ],
            [ "New-York-NY", "Chesapeake-VA", 57.0 ],
            [ "New-York-NY", "Virginia-Beach-VA", 36.0 ],
            [ "Lawrenceville-GA", "Goose-Creek-SC", 24.0 ],
            [ "Lawrenceville-GA", "Decatur-GA", 55.0 ],
            [ "Lawrenceville-GA", "Lawrenceville-GA", 105.0 ],
            [ "Lawrenceville-GA", "Lilburn-GA", 55.0 ],
            [ "Lawrenceville-GA", "Lithonia-GA", 71.0 ],
            [ "Lawrenceville-GA", "Marietta-GA", 33.0 ],
            [ "Lawrenceville-GA", "Duluth-GA", 72.0 ],
            [ "Lawrenceville-GA", "Atlanta-GA", 77.0 ],
            [ "Lawrenceville-GA", "Augusta-GA", 27.0 ],
            [ "Lawrenceville-GA", "Martinez-GA", 27.0 ],
            [ "Lawrenceville-GA", "Columbus-GA", 89.0 ],
            [ "Lawrenceville-GA", "Orange-Park-FL", 24.0 ],
            [ "Lawrenceville-GA", "Jacksonville-FL", 28.0 ],
            [ "Lawrenceville-GA", "Winter-Park-FL", 50.0 ],
            [ "Lawrenceville-GA", "Hialeah-FL", 37.0 ],
            [ "Lawrenceville-GA", "Hollywood-FL", 30.0 ],
            [ "Lawrenceville-GA", "Lighthouse-Point-FL", 24.0 ],
            [ "Lawrenceville-GA", "Pompano-Beach-FL", 24.0 ],
            [ "Lawrenceville-GA", "Perrine-FL", 29.0 ],
            [ "Lawrenceville-GA", "Miami-FL", 28.0 ],
            [ "Lawrenceville-GA", "Fort-Lauderdale-FL", 94.0 ],
            [ "Lawrenceville-GA", "Clarksville-TN", 28.0 ],
            [ "Lawrenceville-GA", "Nashville-TN", 86.0 ],
            [ "Lawrenceville-GA", "Memphis-TN", 29.0 ],
            [ "Chicago-IL", "Newark-OH", 27.0 ],
            [ "Chicago-IL", "Westerville-OH", 50.0 ],
            [ "Chicago-IL", "Lancaster-OH", 53.0 ],
            [ "Chicago-IL", "Marion-OH", 73.0 ],
            [ "Chicago-IL", "Zanesville-OH", 112.0 ],
            [ "Chicago-IL", "Elyria-OH", 102.0 ],
            [ "Chicago-IL", "Mentor-OH", 31.0 ],
            [ "Chicago-IL", "Painesville-OH", 74.0 ],
            [ "Chicago-IL", "Lakewood-OH", 28.0 ],
            [ "Chicago-IL", "Cleveland-OH", 66.0 ],
            [ "Chicago-IL", "Hamilton-OH", 47.0 ],
            [ "Chicago-IL", "Huber-Heights-OH", 25.0 ],
            [ "Chicago-IL", "Chillicothe-OH", 26.0 ],
            [ "Chicago-IL", "Findlay-OH", 25.0 ],
            [ "Chicago-IL", "Indianapolis-IN", 47.0 ],
            [ "Chicago-IL", "West-Lafayette-IN", 88.0 ],
            [ "Chicago-IL", "Roseville-MI", 25.0 ],
            [ "Chicago-IL", "Ann-Arbor-MI", 82.0 ],
            [ "Chicago-IL", "Taylor-MI", 106.0 ],
            [ "Chicago-IL", "Westland-MI", 77.0 ],
            [ "Chicago-IL", "Canton-MI", 79.0 ],
            [ "Chicago-IL", "Ypsilanti-MI", 25.0 ],
            [ "Chicago-IL", "Detroit-MI", 31.0 ],
            [ "Chicago-IL", "Wyoming-MI", 60.0 ],
            [ "Chicago-IL", "Apple-Valley-MN", 24.0 ],
            [ "Chicago-IL", "Arlington-Heights-IL", 113.0 ],
            [ "Chicago-IL", "Des-Plaines-IL", 78.0 ],
            [ "Chicago-IL", "Mount-Prospect-IL", 52.0 ],
            [ "Chicago-IL", "Palatine-IL", 25.0 ],
            [ "Chicago-IL", "Waukegan-IL", 29.0 ],
            [ "Chicago-IL", "Bartlett-IL", 101.0 ],
            [ "Chicago-IL", "Elgin-IL", 27.0 ],
            [ "Chicago-IL", "Lombard-IL", 25.0 ],
            [ "Chicago-IL", "Wheaton-IL", 58.0 ],
            [ "Chicago-IL", "Berwyn-IL", 27.0 ],
            [ "Chicago-IL", "Chicago-Heights-IL", 55.0 ],
            [ "Chicago-IL", "Shorewood-IL", 53.0 ],
            [ "Chicago-IL", "Lockport-IL", 24.0 ],
            [ "Chicago-IL", "Oak-Lawn-IL", 28.0 ],
            [ "Chicago-IL", "Tinley-Park-IL", 104.0 ],
            [ "Chicago-IL", "Aurora-IL", 26.0 ],
            [ "Chicago-IL", "Chicago-IL", 51.0 ],
            [ "Chicago-IL", "Cicero-IL", 91.0 ],
            [ "Chicago-IL", "Quincy-IL", 25.0 ],
            [ "Chicago-IL", "Ballwin-MO", 26.0 ],
            [ "Chicago-IL", "Florissant-MO", 50.0 ],
            [ "Chicago-IL", "Saint-Louis-MO", 99.0 ],
            [ "Chicago-IL", "Jennings-MO", 25.0 ],
            [ "Chicago-IL", "O-Fallon-MO", 27.0 ],
            [ "Chicago-IL", "Saint-Peters-MO", 36.0 ],
            [ "Chicago-IL", "Olathe-KS", 82.0 ],
            [ "Dallas-TX", "Springfield-MO", 52.0 ],
            [ "Dallas-TX", "Kenner-LA", 51.0 ],
            [ "Dallas-TX", "Marrero-LA", 55.0 ],
            [ "Dallas-TX", "New-Orleans-LA", 75.0 ],
            [ "Dallas-TX", "Conway-AR", 25.0 ],
            [ "Dallas-TX", "Plano-TX", 25.0 ],
            [ "Dallas-TX", "Grand-Prairie-TX", 25.0 ],
            [ "Dallas-TX", "Mesquite-TX", 27.0 ],
            [ "Dallas-TX", "Dallas-TX", 95.0 ],
            [ "Dallas-TX", "North-Richland-Hills-TX", 83.0 ],
            [ "Dallas-TX", "Houston-TX", 30.0 ],
            [ "Dallas-TX", "Sugar-Land-TX", 82.0 ],
            [ "Dallas-TX", "San-Antonio-TX", 31.0 ],
            [ "Dallas-TX", "McAllen-TX", 77.0 ],
            [ "Dallas-TX", "Brownsville-TX", 161.0 ],
            [ "Dallas-TX", "Edinburg-TX", 35.0 ],
            [ "Dallas-TX", "Harlingen-TX", 66.0 ],
            [ "Dallas-TX", "Mission-TX", 159.0 ],
            [ "Dallas-TX", "Pharr-TX", 64.0 ],
            [ "Dallas-TX", "Weslaco-TX", 49.0 ],
            [ "Dallas-TX", "San-Marcos-TX", 102.0 ],
            [ "Dallas-TX", "Austin-TX", 26.0 ],
            [ "Denver-CO", "El-Paso-TX", 119.0 ],
            [ "Denver-CO", "Aurora-CO", 56.0 ],
            [ "Denver-CO", "Littleton-CO", 110.0 ],
            [ "Denver-CO", "Denver-CO", 96.0 ],
            [ "Denver-CO", "Longmont-CO", 81.0 ],
            [ "Denver-CO", "Colorado-Springs-CO", 74.0 ],
            [ "Denver-CO", "Kearns-UT", 30.0 ],
            [ "Denver-CO", "Provo-UT", 70.0 ],
            [ "Denver-CO", "Albuquerque-NM", 73.0 ],
            [ "Denver-CO", "Rio-Rancho-NM", 24.0 ],
            [ "Denver-CO", "Santa-Fe-NM", 88.0 ],
            [ "Denver-CO", "Roswell-NM", 53.0 ],
            [ "Los-Angeles-CA", "Phoenix-AZ", 34.0 ],
            [ "Los-Angeles-CA", "Mesa-AZ", 78.0 ],
            [ "Los-Angeles-CA", "Chandler-AZ", 29.0 ],
            [ "Los-Angeles-CA", "Scottsdale-AZ", 70.0 ],
            [ "Los-Angeles-CA", "Tempe-AZ", 47.0 ],
            [ "Los-Angeles-CA", "Glendale-AZ", 87.0 ],
            [ "Los-Angeles-CA", "Yuma-AZ", 31.0 ],
            [ "Los-Angeles-CA", "Tucson-AZ", 62.0 ],
            [ "Los-Angeles-CA", "Henderson-NV", 115.0 ],
            [ "Los-Angeles-CA", "Las-Vegas-NV", 84.0 ],
            [ "Los-Angeles-CA", "Los-Angeles-CA", 90.0 ],
            [ "Los-Angeles-CA", "Bell-CA", 103.0 ],
            [ "Los-Angeles-CA", "Compton-CA", 24.0 ],
            [ "Los-Angeles-CA", "Hawthorne-CA", 122.0 ],
            [ "Los-Angeles-CA", "Huntington-Park-CA", 157.0 ],
            [ "Los-Angeles-CA", "Lynwood-CA", 32.0 ],
            [ "Los-Angeles-CA", "South-Gate-CA", 46.0 ],
            [ "Los-Angeles-CA", "Cypress-CA", 26.0 ],
            [ "Los-Angeles-CA", "La-Habra-CA", 31.0 ],
            [ "Los-Angeles-CA", "Montebello-CA", 35.0 ],
            [ "Los-Angeles-CA", "Norwalk-CA", 156.0 ],
            [ "Los-Angeles-CA", "Pico-Rivera-CA", 31.0 ],
            [ "Los-Angeles-CA", "Cerritos-CA", 60.0 ],
            [ "Los-Angeles-CA", "Bellflower-CA", 32.0 ],
            [ "Los-Angeles-CA", "Paramount-CA", 26.0 ],
            [ "Los-Angeles-CA", "San-Pedro-CA", 61.0 ],
            [ "Los-Angeles-CA", "Wilmington-CA", 66.0 ],
            [ "Los-Angeles-CA", "Carson-CA", 26.0 ],
            [ "Los-Angeles-CA", "Long-Beach-CA", 118.0 ],
            [ "Los-Angeles-CA", "Pacoima-CA", 47.0 ],
            [ "Los-Angeles-CA", "Reseda-CA", 31.0 ],
            [ "Los-Angeles-CA", "Sylmar-CA", 36.0 ],
            [ "Los-Angeles-CA", "North-Hills-CA", 26.0 ],
            [ "Los-Angeles-CA", "Granada-Hills-CA", 100.0 ],
            [ "Los-Angeles-CA", "Canyon-Country-CA", 25.0 ],
            [ "Los-Angeles-CA", "Panorama-City-CA", 28.0 ],
            [ "Los-Angeles-CA", "North-Hollywood-CA", 27.0 ],
            [ "Los-Angeles-CA", "Azusa-CA", 76.0 ],
            [ "Los-Angeles-CA", "Baldwin-Park-CA", 39.0 ],
            [ "Los-Angeles-CA", "Chino-Hills-CA", 64.0 ],
            [ "Los-Angeles-CA", "Chino-CA", 111.0 ],
            [ "Los-Angeles-CA", "Rancho-Cucamonga-CA", 79.0 ],
            [ "Los-Angeles-CA", "El-Monte-CA", 61.0 ],
            [ "Los-Angeles-CA", "La-Puente-CA", 81.0 ],
            [ "Los-Angeles-CA", "Hacienda-Heights-CA", 90.0 ],
            [ "Los-Angeles-CA", "Ontario-CA", 78.0 ],
            [ "Los-Angeles-CA", "Pomona-CA", 124.0 ],
            [ "Los-Angeles-CA", "Rosemead-CA", 94.0 ],
            [ "Los-Angeles-CA", "Alhambra-CA", 49.0 ],
            [ "Los-Angeles-CA", "Chula-Vista-CA", 146.0 ],
            [ "Los-Angeles-CA", "National-City-CA", 50.0 ],
            [ "Los-Angeles-CA", "Spring-Valley-CA", 60.0 ],
            [ "Los-Angeles-CA", "El-Cajon-CA", 124.0 ],
            [ "Los-Angeles-CA", "Oceanside-CA", 81.0 ],
            [ "Los-Angeles-CA", "San-Marcos-CA", 25.0 ],
            [ "Los-Angeles-CA", "Santee-CA", 27.0 ],
            [ "Los-Angeles-CA", "Vista-CA", 28.0 ],
            [ "Los-Angeles-CA", "San-Diego-CA", 132.0 ],
            [ "Los-Angeles-CA", "Fontana-CA", 108.0 ],
            [ "Los-Angeles-CA", "Hesperia-CA", 74.0 ],
            [ "Los-Angeles-CA", "Rialto-CA", 33.0 ],
            [ "Los-Angeles-CA", "Victorville-CA", 120.0 ],
            [ "Los-Angeles-CA", "San-Bernardino-CA", 69.0 ],
            [ "Los-Angeles-CA", "Riverside-CA", 126.0 ],
            [ "Los-Angeles-CA", "Rubidoux-CA", 30.0 ],
            [ "Los-Angeles-CA", "Moreno-Valley-CA", 96.0 ],
            [ "Los-Angeles-CA", "Costa-Mesa-CA", 57.0 ],
            [ "Los-Angeles-CA", "Lake-Forest-CA", 51.0 ],
            [ "Los-Angeles-CA", "Huntington-Beach-CA", 30.0 ],
            [ "Los-Angeles-CA", "Laguna-Niguel-CA", 60.0 ],
            [ "Los-Angeles-CA", "Westminster-CA", 129.0 ],
            [ "Los-Angeles-CA", "Santa-Ana-CA", 115.0 ],
            [ "Los-Angeles-CA", "Fountain-Valley-CA", 28.0 ],
            [ "Los-Angeles-CA", "Tustin-CA", 47.0 ],
            [ "Los-Angeles-CA", "Anaheim-CA", 113.0 ],
            [ "Los-Angeles-CA", "Placentia-CA", 25.0 ],
            [ "Los-Angeles-CA", "Corona-CA", 52.0 ],
            [ "Los-Angeles-CA", "Oxnard-CA", 36.0 ],
            [ "Los-Angeles-CA", "Simi-Valley-CA", 30.0 ],
            [ "Los-Angeles-CA", "Porterville-CA", 27.0 ],
            [ "Los-Angeles-CA", "Tulare-CA", 93.0 ],
            [ "Los-Angeles-CA", "Bakersfield-CA", 28.0 ],
            [ "Los-Angeles-CA", "Lancaster-CA", 49.0 ],
            [ "Los-Angeles-CA", "Palmdale-CA", 137.0 ],
            [ "San-Francisco-CA", "Fresno-CA", 27.0 ],
            [ "San-Francisco-CA", "Daly-City-CA", 30.0 ],
            [ "San-Francisco-CA", "South-San-Francisco-CA", 28.0 ],
            [ "San-Francisco-CA", "Sunnyvale-CA", 30.0 ],
            [ "San-Francisco-CA", "San-Francisco-CA", 37.0 ],
            [ "San-Francisco-CA", "Alameda-CA", 100.0 ],
            [ "San-Francisco-CA", "Antioch-CA", 117.0 ],
            [ "San-Francisco-CA", "Fairfield-CA", 76.0 ],
            [ "San-Francisco-CA", "Fremont-CA", 62.0 ],
            [ "San-Francisco-CA", "Hayward-CA", 58.0 ],
            [ "San-Francisco-CA", "Livermore-CA", 154.0 ],
            [ "San-Francisco-CA", "Martinez-CA", 24.0 ],
            [ "San-Francisco-CA", "Napa-CA", 84.0 ],
            [ "San-Francisco-CA", "Pittsburg-CA", 37.0 ],
            [ "San-Francisco-CA", "Union-City-CA", 31.0 ],
            [ "San-Francisco-CA", "Oakland-CA", 53.0 ],
            [ "San-Francisco-CA", "San-Pablo-CA", 24.0 ],
            [ "San-Francisco-CA", "Cupertino-CA", 26.0 ],
            [ "San-Francisco-CA", "Milpitas-CA", 89.0 ],
            [ "San-Francisco-CA", "Santa-Clara-CA", 111.0 ],
            [ "San-Francisco-CA", "Watsonville-CA", 61.0 ],
            [ "San-Francisco-CA", "San-Jose-CA", 134.0 ],
            [ "San-Francisco-CA", "Stockton-CA", 113.0 ],
            [ "San-Francisco-CA", "Merced-CA", 101.0 ],
            [ "San-Francisco-CA", "Modesto-CA", 25.0 ],
            [ "San-Francisco-CA", "Tracy-CA", 28.0 ],
            [ "San-Francisco-CA", "Carmichael-CA", 28.0 ],
            [ "San-Francisco-CA", "Davis-CA", 81.0 ],
            [ "San-Francisco-CA", "Vacaville-CA", 28.0 ],
            [ "San-Francisco-CA", "Sacramento-CA", 103.0 ],
            [ "San-Francisco-CA", "Beaverton-OR", 69.0 ],
            [ "San-Francisco-CA", "Salem-OR", 105.0 ],
            [ "San-Francisco-CA", "Albany-OR", 52.0 ],
            [ "San-Francisco-CA", "Kent-WA", 84.0 ],
            [ "San-Francisco-CA", "Redmond-WA", 99.0 ],
            [ "San-Francisco-CA", "Bellingham-WA", 79.0 ]
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
		+ "          \"NAME\": \"objectives\",\r\n"
		+ "          \"CLASS\": \"DATA\",\r\n"
		+ "          \"KIND\": \"OUTPUT\",\r\n"
		+ "          \"SCHEMA\": {\r\n"
		+ "            \"FIELDS\": [ \"problem\", \"capitalCost\", \"operatingCost\", \"totalCost\" ],\r\n"
		+ "            \"TYPES\": [ \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
		+ "          },\r\n"
		+ "          \"INSTANCE\": [\r\n"
		+ "            [ \"Warehousing\", 6373620.0, 4580688.49, 1.095430849E7 ]\r\n"
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
		+ "            [ \"New-York-NY\", 1, 3961.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", 1, 1146.0 ],\r\n"
		+ "            [ \"Chicago-IL\", 1, 2720.0 ],\r\n"
		+ "            [ \"Dallas-TX\", 1, 1395.0 ],\r\n"
		+ "            [ \"Denver-CO\", 1, 874.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", 1, 5581.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", 1, 2388.0 ]\r\n"
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
		+ "            [ \"New-York-NY\", \"Malden-MA\", 104.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Medford-MA\", 50.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Quincy-MA\", 25.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Brockton-MA\", 28.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Bristol-CT\", 28.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Manchester-CT\", 80.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Milford-CT\", 103.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"New-Haven-CT\", 80.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Stamford-CT\", 73.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Bayonne-NJ\", 58.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Passaic-NJ\", 64.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Union-City-NJ\", 34.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"West-New-York-NJ\", 66.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Irvington-NJ\", 27.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Jersey-City-NJ\", 29.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Wayne-NJ\", 27.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Lakewood-NJ\", 102.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Toms-River-NJ\", 31.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Piscataway-NJ\", 51.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"New-York-NY\", 53.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Staten-Island-NY\", 41.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Bronx-NY\", 40.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Yonkers-NY\", 29.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Brooklyn-NY\", 52.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Flushing-NY\", 37.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Corona-NY\", 72.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Jackson-Heights-NY\", 77.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Elmhurst-NY\", 123.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Forest-Hills-NY\", 33.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Woodside-NY\", 113.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Ridgewood-NY\", 179.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Jamaica-NY\", 27.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Hempstead-NY\", 25.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Far-Rockaway-NY\", 113.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Bay-Shore-NY\", 25.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Huntington-Station-NY\", 87.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Massapequa-NY\", 54.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Troy-NY\", 25.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Newburgh-NY\", 52.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Buffalo-NY\", 45.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Ithaca-NY\", 94.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Greensburg-PA\", 94.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Carlisle-PA\", 53.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Mechanicsburg-PA\", 87.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"York-PA\", 27.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Lancaster-PA\", 51.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Allentown-PA\", 60.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Bensalem-PA\", 25.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Morrisville-PA\", 25.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Philadelphia-PA\", 149.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"West-Chester-PA\", 24.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Newark-DE\", 84.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"New-Castle-DE\", 46.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Washington-DC\", 27.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Fort-Washington-MD\", 87.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Potomac-MD\", 52.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Gaithersburg-MD\", 27.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Silver-Spring-MD\", 114.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Pasadena-MD\", 96.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Baltimore-MD\", 32.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Parkville-MD\", 33.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Hagerstown-MD\", 69.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Annandale-VA\", 96.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Dale-City-VA\", 28.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Stafford-VA\", 26.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Chesapeake-VA\", 57.0 ],\r\n"
		+ "            [ \"New-York-NY\", \"Virginia-Beach-VA\", 36.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Goose-Creek-SC\", 24.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Decatur-GA\", 55.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Lawrenceville-GA\", 105.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Lilburn-GA\", 55.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Lithonia-GA\", 71.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Marietta-GA\", 33.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Duluth-GA\", 72.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Atlanta-GA\", 77.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Augusta-GA\", 27.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Martinez-GA\", 27.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Columbus-GA\", 89.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Orange-Park-FL\", 24.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Jacksonville-FL\", 28.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Winter-Park-FL\", 50.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Hialeah-FL\", 37.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Hollywood-FL\", 30.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Lighthouse-Point-FL\", 24.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Pompano-Beach-FL\", 24.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Perrine-FL\", 29.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Miami-FL\", 28.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Fort-Lauderdale-FL\", 94.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Clarksville-TN\", 28.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Nashville-TN\", 86.0 ],\r\n"
		+ "            [ \"Lawrenceville-GA\", \"Memphis-TN\", 29.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Newark-OH\", 27.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Westerville-OH\", 50.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Lancaster-OH\", 53.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Marion-OH\", 73.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Zanesville-OH\", 112.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Elyria-OH\", 102.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Mentor-OH\", 31.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Painesville-OH\", 74.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Lakewood-OH\", 28.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Cleveland-OH\", 66.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Hamilton-OH\", 47.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Huber-Heights-OH\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Chillicothe-OH\", 26.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Findlay-OH\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Indianapolis-IN\", 47.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"West-Lafayette-IN\", 88.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Roseville-MI\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Ann-Arbor-MI\", 82.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Taylor-MI\", 106.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Westland-MI\", 77.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Canton-MI\", 79.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Ypsilanti-MI\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Detroit-MI\", 31.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Wyoming-MI\", 60.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Apple-Valley-MN\", 24.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Arlington-Heights-IL\", 113.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Des-Plaines-IL\", 78.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Mount-Prospect-IL\", 52.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Palatine-IL\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Waukegan-IL\", 29.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Bartlett-IL\", 101.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Elgin-IL\", 27.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Lombard-IL\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Wheaton-IL\", 58.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Berwyn-IL\", 27.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Chicago-Heights-IL\", 55.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Shorewood-IL\", 53.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Lockport-IL\", 24.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Oak-Lawn-IL\", 28.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Tinley-Park-IL\", 104.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Aurora-IL\", 26.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Chicago-IL\", 51.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Cicero-IL\", 91.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Quincy-IL\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Ballwin-MO\", 26.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Florissant-MO\", 50.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Saint-Louis-MO\", 99.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Jennings-MO\", 25.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"O-Fallon-MO\", 27.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Saint-Peters-MO\", 36.0 ],\r\n"
		+ "            [ \"Chicago-IL\", \"Olathe-KS\", 82.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Springfield-MO\", 52.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Kenner-LA\", 51.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Marrero-LA\", 55.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"New-Orleans-LA\", 75.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Conway-AR\", 25.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Plano-TX\", 25.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Grand-Prairie-TX\", 25.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Mesquite-TX\", 27.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Dallas-TX\", 95.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"North-Richland-Hills-TX\", 83.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Houston-TX\", 30.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Sugar-Land-TX\", 82.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"San-Antonio-TX\", 31.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"McAllen-TX\", 77.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Brownsville-TX\", 161.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Edinburg-TX\", 35.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Harlingen-TX\", 66.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Mission-TX\", 159.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Pharr-TX\", 64.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Weslaco-TX\", 49.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"San-Marcos-TX\", 102.0 ],\r\n"
		+ "            [ \"Dallas-TX\", \"Austin-TX\", 26.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"El-Paso-TX\", 119.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Aurora-CO\", 56.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Littleton-CO\", 110.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Denver-CO\", 96.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Longmont-CO\", 81.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Colorado-Springs-CO\", 74.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Kearns-UT\", 30.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Provo-UT\", 70.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Albuquerque-NM\", 73.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Rio-Rancho-NM\", 24.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Santa-Fe-NM\", 88.0 ],\r\n"
		+ "            [ \"Denver-CO\", \"Roswell-NM\", 53.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Phoenix-AZ\", 34.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Mesa-AZ\", 78.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Chandler-AZ\", 29.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Scottsdale-AZ\", 70.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Tempe-AZ\", 47.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Glendale-AZ\", 87.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Yuma-AZ\", 31.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Tucson-AZ\", 62.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Henderson-NV\", 115.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Las-Vegas-NV\", 84.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Los-Angeles-CA\", 90.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Bell-CA\", 103.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Compton-CA\", 24.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Hawthorne-CA\", 122.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Huntington-Park-CA\", 157.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Lynwood-CA\", 32.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"South-Gate-CA\", 46.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Cypress-CA\", 26.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"La-Habra-CA\", 31.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Montebello-CA\", 35.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Norwalk-CA\", 156.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Pico-Rivera-CA\", 31.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Cerritos-CA\", 60.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Bellflower-CA\", 32.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Paramount-CA\", 26.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"San-Pedro-CA\", 61.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Wilmington-CA\", 66.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Carson-CA\", 26.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Long-Beach-CA\", 118.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Pacoima-CA\", 47.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Reseda-CA\", 31.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Sylmar-CA\", 36.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"North-Hills-CA\", 26.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Granada-Hills-CA\", 100.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Canyon-Country-CA\", 25.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Panorama-City-CA\", 28.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"North-Hollywood-CA\", 27.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Azusa-CA\", 76.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Baldwin-Park-CA\", 39.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Chino-Hills-CA\", 64.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Chino-CA\", 111.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Rancho-Cucamonga-CA\", 79.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"El-Monte-CA\", 61.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"La-Puente-CA\", 81.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Hacienda-Heights-CA\", 90.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Ontario-CA\", 78.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Pomona-CA\", 124.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Rosemead-CA\", 94.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Alhambra-CA\", 49.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Chula-Vista-CA\", 146.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"National-City-CA\", 50.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Spring-Valley-CA\", 60.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"El-Cajon-CA\", 124.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Oceanside-CA\", 81.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"San-Marcos-CA\", 25.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Santee-CA\", 27.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Vista-CA\", 28.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"San-Diego-CA\", 132.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Fontana-CA\", 108.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Hesperia-CA\", 74.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Rialto-CA\", 33.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Victorville-CA\", 120.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"San-Bernardino-CA\", 69.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Riverside-CA\", 126.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Rubidoux-CA\", 30.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Moreno-Valley-CA\", 96.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Costa-Mesa-CA\", 57.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Lake-Forest-CA\", 51.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Huntington-Beach-CA\", 30.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Laguna-Niguel-CA\", 60.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Westminster-CA\", 129.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Santa-Ana-CA\", 115.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Fountain-Valley-CA\", 28.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Tustin-CA\", 47.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Anaheim-CA\", 113.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Placentia-CA\", 25.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Corona-CA\", 52.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Oxnard-CA\", 36.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Simi-Valley-CA\", 30.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Porterville-CA\", 27.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Tulare-CA\", 93.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Bakersfield-CA\", 28.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Lancaster-CA\", 49.0 ],\r\n"
		+ "            [ \"Los-Angeles-CA\", \"Palmdale-CA\", 137.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Fresno-CA\", 27.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Daly-City-CA\", 30.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"South-San-Francisco-CA\", 28.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Sunnyvale-CA\", 30.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"San-Francisco-CA\", 37.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Alameda-CA\", 100.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Antioch-CA\", 117.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Fairfield-CA\", 76.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Fremont-CA\", 62.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Hayward-CA\", 58.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Livermore-CA\", 154.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Martinez-CA\", 24.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Napa-CA\", 84.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Pittsburg-CA\", 37.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Union-City-CA\", 31.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Oakland-CA\", 53.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"San-Pablo-CA\", 24.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Cupertino-CA\", 26.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Milpitas-CA\", 89.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Santa-Clara-CA\", 111.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Watsonville-CA\", 61.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"San-Jose-CA\", 134.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Stockton-CA\", 113.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Merced-CA\", 101.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Modesto-CA\", 25.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Tracy-CA\", 28.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Carmichael-CA\", 28.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Davis-CA\", 81.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Vacaville-CA\", 28.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Sacramento-CA\", 103.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Beaverton-OR\", 69.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Salem-OR\", 105.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Albany-OR\", 52.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Kent-WA\", 84.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Redmond-WA\", 99.0 ],\r\n"
		+ "            [ \"San-Francisco-CA\", \"Bellingham-WA\", 79.0 ]\r\n"
		+ "          ]\r\n"
		+ "        }\r\n"
		+ "      ]\r\n"
		+ "    }\r\n"
		+ "  ]\r\n"
		+ "}\r\n";
	
	/**
	 * 
	 */
	public WarehousingLarge() {
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
		MsdxInputSource salesSource= MsdxInputSource.fromFile(Path.of(path.toString(), salesFileName).toFile());
		
		MsdxApplication application= new MsdxApplication(
			title.split(" (\\b[^\\s]+\\b)")[0]) //first word of title
			.addFile(modelSource, MsdxOutputDestination.toStream(
				Msdx.GLOBAL.out), expectedOutput )
			.addFile(dataSource)
			.addFile(salesSource)
			.show("warehouseModel", "objectives", "openWarehouses", "shipments")
			.addSolverResults(MsdxOutputDestination.toStream(System.out), false)
			.useSparkDataframes()
			.useJavaSpans()
			.useCplex();
		application.run();
		
	}//main


}//WarehousingLarge
