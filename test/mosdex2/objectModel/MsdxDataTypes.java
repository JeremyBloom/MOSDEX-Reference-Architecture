/**
 * 
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxSparkDataframe;
import io.github.JeremyBloom.mosdex2.span.MsdxJavaSpan;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;

/**
 * This class offers tests of MOSDEX reading and writing data of various types.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxDataTypes {
	
	static MsdxSparkDataframe.Factory dfFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
	
	/**
	 * The main method is used to run the display (non-test) methods of this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("MOSDEX Datatype Tests");
		Msdx.GLOBAL.showDisplay();
		
		Msdx.GLOBAL.out.println("MOSDEX Infinity test");
		displayInfinity();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MOSDEX IEEE Double test");
		displayIEEEDouble();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MOSDEX IEEE Double equality test");
		displayEquality();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MOSDEX IEEE Double comparison test");
		displayComparison();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MOSDEX Function Call test");
		displayFunctionCalls();
		Msdx.GLOBAL.out.println();
		
	}//main
	
	public static void displayInfinity() {
		
		String mosdex= 
/*			'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
  "MODULES": [
    {
      "NAME": "model",
      "CLASS": "MODEL",
      "HEADING": {
        "DESCRIPTION": ["Part of General Transshipment Problem"]
      },
      "TABLES": [
        { "NAME": "ship",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS":
              ["Name",  "origin",  "destination",  "Column",     "LowerBound", "UpperBound"],
            "TYPES":
              ["STRING","STRING",  "STRING",       "STRING",     "DOUBLE",     "DOUBLE"]
          },
          "INSTANCE": [
            ["ship", "PITT",    "NE",           "ship_PITT_NE",   0.0,          250.0   ],
            ["ship", "PITT",    "SE",           "ship_PITT_SE",   0.0,          250.0   ],
            ["ship", "NE",      "BOS",          "ship_NE_BOS",    0.0,          "Infinity"   ],
            ["ship", "NE",      "EWR",          "ship_NE_EWR",    0.0,          100.0   ],
            ["ship", "NE",      "BWI",          "ship_NE_BWI",    0.0,          100.0   ],
            ["ship", "SE",      "EWR",          "ship_SE_EWR",    0.0,          100.0   ],
            ["ship", "SE",      "BWI",          "ship_SE_BWI",    0.0,          "Infinity"   ],
            ["ship", "SE",      "ATL",          "ship_SE_ATL",    "-Infinity",          100.0   ],
            ["ship", "SE",      "MCO",          "ship_SE_MCO",    "-Infinity",          100.0   ]
          ]
      	}
      ]
    }
  ]
}			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"model\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\"Part of General Transshipment Problem\"]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        { \"NAME\": \"ship\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\":\r\n"
			+ "              [\"Name\",  \"origin\",  \"destination\",  \"Column\",     \"LowerBound\", \"UpperBound\"],\r\n"
			+ "            \"TYPES\":\r\n"
			+ "              [\"STRING\",\"STRING\",  \"STRING\",       \"STRING\",     \"DOUBLE\",     \"DOUBLE\"]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [\"ship\", \"PITT\",    \"NE\",           \"ship_PITT_NE\",   0.0,          250.0   ],\r\n"
			+ "            [\"ship\", \"PITT\",    \"SE\",           \"ship_PITT_SE\",   0.0,          250.0   ],\r\n"
			+ "            [\"ship\", \"NE\",      \"BOS\",          \"ship_NE_BOS\",    0.0,          \"Infinity\"   ],\r\n"
			+ "            [\"ship\", \"NE\",      \"EWR\",          \"ship_NE_EWR\",    0.0,          100.0   ],\r\n"
			+ "            [\"ship\", \"NE\",      \"BWI\",          \"ship_NE_BWI\",    0.0,          100.0   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"EWR\",          \"ship_SE_EWR\",    0.0,          100.0   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"BWI\",          \"ship_SE_BWI\",    0.0,          \"Infinity\"   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"ATL\",          \"ship_SE_ATL\",    \"-Infinity\",          100.0   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"MCO\",          \"ship_SE_MCO\",    \"-Infinity\",          100.0   ]\r\n"
			+ "          ]\r\n"
			+ "      	}\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			dfFactory, 
			Msdx.GLOBAL.mapper, 
			false);
				
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
				
	}//displayInfinity

	@Test
	public void infinityTest() throws JSONException {
		String expected= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "model",
			      "CLASS": "MODEL",
			      "HEADING": {
			        "DESCRIPTION": [
			          "Part of General Transshipment Problem"
			        ]
			      },
			      "TABLES": [
			        {
			          "NAME": "ship",
			          "CLASS": "VARIABLE",
			          "KIND": "CONTINUOUS",
			          "SCHEMA": {
			            "FIELDS": [ "Name", "origin", "destination", "Column", "LowerBound", "UpperBound" ],
			            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE", "DOUBLE" ]
			          },
			          "INSTANCE": [
			            [ "ship", "PITT", "NE", "ship_PITT_NE", 0.0, 250.0 ],
			            [ "ship", "PITT", "SE", "ship_PITT_SE", 0.0, 250.0 ],
			            [ "ship", "NE", "BOS", "ship_NE_BOS", 0.0, "Infinity" ],
			            [ "ship", "NE", "EWR", "ship_NE_EWR", 0.0, 100.0 ],
			            [ "ship", "NE", "BWI", "ship_NE_BWI", 0.0, 100.0 ],
			            [ "ship", "SE", "EWR", "ship_SE_EWR", 0.0, 100.0 ],
			            [ "ship", "SE", "BWI", "ship_SE_BWI", 0.0, "Infinity" ],
			            [ "ship", "SE", "ATL", "ship_SE_ATL", "-Infinity", 100.0 ],
			            [ "ship", "SE", "MCO", "ship_SE_MCO", "-Infinity", 100.0 ]
			          ]
			        }
			      ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"model\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\r\n"
			+ "          \"Part of General Transshipment Problem\"\r\n"
			+ "        ]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ship\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"origin\", \"destination\", \"Column\", \"LowerBound\", \"UpperBound\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ship\", \"PITT\", \"NE\", \"ship_PITT_NE\", 0.0, 250.0 ],\r\n"
			+ "            [ \"ship\", \"PITT\", \"SE\", \"ship_PITT_SE\", 0.0, 250.0 ],\r\n"
			+ "            [ \"ship\", \"NE\", \"BOS\", \"ship_NE_BOS\", 0.0, \"Infinity\" ],\r\n"
			+ "            [ \"ship\", \"NE\", \"EWR\", \"ship_NE_EWR\", 0.0, 100.0 ],\r\n"
			+ "            [ \"ship\", \"NE\", \"BWI\", \"ship_NE_BWI\", 0.0, 100.0 ],\r\n"
			+ "            [ \"ship\", \"SE\", \"EWR\", \"ship_SE_EWR\", 0.0, 100.0 ],\r\n"
			+ "            [ \"ship\", \"SE\", \"BWI\", \"ship_SE_BWI\", 0.0, \"Infinity\" ],\r\n"
			+ "            [ \"ship\", \"SE\", \"ATL\", \"ship_SE_ATL\", \"-Infinity\", 100.0 ],\r\n"
			+ "            [ \"ship\", \"SE\", \"MCO\", \"ship_SE_MCO\", \"-Infinity\", 100.0 ]\r\n"
			+ "          ]\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n";

		String mosdex= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "model",
			      "CLASS": "MODEL",
			      "HEADING": {
			        "DESCRIPTION": ["Part of General Transshipment Problem"]
			      },
			      "TABLES": [
			        { "NAME": "ship",
			          "CLASS": "VARIABLE",
			          "KIND": "CONTINUOUS",
			          "SCHEMA": {
			            "FIELDS":
			              ["Name",  "origin",  "destination",  "Column",     "LowerBound", "UpperBound"],
			            "TYPES":
			              ["STRING","STRING",  "STRING",       "STRING",     "DOUBLE",     "DOUBLE"]
			          },
			          "INSTANCE": [
			            ["ship", "PITT",    "NE",           "ship_PITT_NE",   0.0,          250.0   ],
			            ["ship", "PITT",    "SE",           "ship_PITT_SE",   0.0,          250.0   ],
			            ["ship", "NE",      "BOS",          "ship_NE_BOS",    0.0,          "Infinity"   ],
			            ["ship", "NE",      "EWR",          "ship_NE_EWR",    0.0,          100.0   ],
			            ["ship", "NE",      "BWI",          "ship_NE_BWI",    0.0,          100.0   ],
			            ["ship", "SE",      "EWR",          "ship_SE_EWR",    0.0,          100.0   ],
			            ["ship", "SE",      "BWI",          "ship_SE_BWI",    0.0,          "Infinity"   ],
			            ["ship", "SE",      "ATL",          "ship_SE_ATL",    "-Infinity",          100.0   ],
			            ["ship", "SE",      "MCO",          "ship_SE_MCO",    "-Infinity",          100.0   ]
			          ]
			      	}
			      ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"model\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\"Part of General Transshipment Problem\"]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        { \"NAME\": \"ship\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\":\r\n"
			+ "              [\"Name\",  \"origin\",  \"destination\",  \"Column\",     \"LowerBound\", \"UpperBound\"],\r\n"
			+ "            \"TYPES\":\r\n"
			+ "              [\"STRING\",\"STRING\",  \"STRING\",       \"STRING\",     \"DOUBLE\",     \"DOUBLE\"]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [\"ship\", \"PITT\",    \"NE\",           \"ship_PITT_NE\",   0.0,          250.0   ],\r\n"
			+ "            [\"ship\", \"PITT\",    \"SE\",           \"ship_PITT_SE\",   0.0,          250.0   ],\r\n"
			+ "            [\"ship\", \"NE\",      \"BOS\",          \"ship_NE_BOS\",    0.0,          \"Infinity\"   ],\r\n"
			+ "            [\"ship\", \"NE\",      \"EWR\",          \"ship_NE_EWR\",    0.0,          100.0   ],\r\n"
			+ "            [\"ship\", \"NE\",      \"BWI\",          \"ship_NE_BWI\",    0.0,          100.0   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"EWR\",          \"ship_SE_EWR\",    0.0,          100.0   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"BWI\",          \"ship_SE_BWI\",    0.0,          \"Infinity\"   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"ATL\",          \"ship_SE_ATL\",    \"-Infinity\",          100.0   ],\r\n"
			+ "            [\"ship\", \"SE\",      \"MCO\",          \"ship_SE_MCO\",    \"-Infinity\",          100.0   ]\r\n"
			+ "          ]\r\n"
			+ "      	}\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			dfFactory, 
			Msdx.GLOBAL.mapper, 
			false);
				
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
			
	}//infinityTest

	public static void displayIEEEDouble() {
			
		String mosdex= 
/*			'''
		{
		  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
		  "MODULES": [
		    {
		      "NAME": "model",
		      "CLASS": "MODEL",
		      "HEADING": {
		        "DESCRIPTION": ["Part of General Transshipment Problem"]
		      },
		      "TABLES": [
		        { "NAME": "ship",
		          "CLASS": "VARIABLE",
		          "KIND": "CONTINUOUS",
		          "SCHEMA": {
		            "FIELDS":
		              ["Name",  "origin",  "destination",  "Column",     "LowerBound", "UpperBound"],
		            "TYPES":
		              ["STRING","STRING",  "STRING",       "STRING",     "DOUBLE",     "IEEEDOUBLE"]
		          },
		          "INSTANCE": [
		            ["ship", "PITT",    "NE",           "ship_PITT_NE",   0.0,          "250.0"   ],
		            ["ship", "PITT",    "SE",           "ship_PITT_SE",   0.0,          "250.0"   ],
		            ["ship", "NE",      "BOS",          "ship_NE_BOS",    0.0,          "100.0"   ],
		            ["ship", "NE",      "EWR",          "ship_NE_EWR",    0.0,          "0x1.28p7"   ],
		            ["ship", "NE",      "BWI",          "ship_NE_BWI",    0.0,          "100.0"   ],
		            ["ship", "SE",      "EWR",          "ship_SE_EWR",    0.0,          "0x2.46p7"   ],
		            ["ship", "SE",      "BWI",          "ship_SE_BWI",    0.0,          "100.0"   ],
		            ["ship", "SE",      "ATL",          "ship_SE_ATL",    0.0,          "100.0"   ],
		            ["ship", "SE",      "MCO",          "ship_SE_MCO",    0.0,          "100.0"   ]
		          ]
		      	}
		      ]
		    }
		  ]
		}
				'''
*/
				"	{\r\n"
				+ "	  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
				+ "	  \"MODULES\": [\r\n"
				+ "	    {\r\n"
				+ "	      \"NAME\": \"model\",\r\n"
				+ "	      \"CLASS\": \"MODEL\",\r\n"
				+ "	      \"HEADING\": {\r\n"
				+ "	        \"DESCRIPTION\": [\"Part of General Transshipment Problem\"]\r\n"
				+ "	      },\r\n"
				+ "	      \"TABLES\": [\r\n"
				+ "	        { \"NAME\": \"ship\",\r\n"
				+ "	          \"CLASS\": \"VARIABLE\",\r\n"
				+ "	          \"KIND\": \"CONTINUOUS\",\r\n"
				+ "	          \"SCHEMA\": {\r\n"
				+ "	            \"FIELDS\":\r\n"
				+ "	              [\"Name\",  \"origin\",  \"destination\",  \"Column\",     \"LowerBound\", \"UpperBound\"],\r\n"
				+ "	            \"TYPES\":\r\n"
				+ "	              [\"STRING\",\"STRING\",  \"STRING\",       \"STRING\",     \"DOUBLE\",     \"IEEEDOUBLE\"]\r\n"
				+ "	          },\r\n"
				+ "	          \"INSTANCE\": [\r\n"
				+ "	            [\"ship\", \"PITT\",    \"NE\",           \"ship_PITT_NE\",   0.0,          \"250.0\"   ],\r\n"
				+ "	            [\"ship\", \"PITT\",    \"SE\",           \"ship_PITT_SE\",   0.0,          \"250.0\"   ],\r\n"
				+ "	            [\"ship\", \"NE\",      \"BOS\",          \"ship_NE_BOS\",    0.0,          \"100.0\"   ],\r\n"
				+ "	            [\"ship\", \"NE\",      \"EWR\",          \"ship_NE_EWR\",    0.0,          \"0x1.28p7\"   ],\r\n"
				+ "	            [\"ship\", \"NE\",      \"BWI\",          \"ship_NE_BWI\",    0.0,          \"100.0\"   ],\r\n"
				+ "	            [\"ship\", \"SE\",      \"EWR\",          \"ship_SE_EWR\",    0.0,          \"0x2.46p7\"   ],\r\n"
				+ "	            [\"ship\", \"SE\",      \"BWI\",          \"ship_SE_BWI\",    0.0,          \"100.0\"   ],\r\n"
				+ "	            [\"ship\", \"SE\",      \"ATL\",          \"ship_SE_ATL\",    0.0,          \"100.0\"   ],\r\n"
				+ "	            [\"ship\", \"SE\",      \"MCO\",          \"ship_SE_MCO\",    0.0,          \"100.0\"   ]\r\n"
				+ "	          ]\r\n"
				+ "	      	}\r\n"
				+ "	      ]\r\n"
				+ "	    }\r\n"
				+ "	  ]\r\n"
				+ "	}";
			
			MsdxObject.Factory factory= new MsdxObject.Factory(
				dfFactory, 
				Msdx.GLOBAL.mapper, 
				false);
					
			MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
			factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
					
		}//displayIEEEDouble

	@Test
	public void IEEEDoubleTest() throws JSONException {
		String expected= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "model",
			      "CLASS": "MODEL",
			      "HEADING": {
			        "DESCRIPTION": [
			          "Part of General Transshipment Problem"
			        ]
			      },
			      "TABLES": [
			        {
			          "NAME": "ship",
			          "CLASS": "VARIABLE",
			          "KIND": "CONTINUOUS",
			          "SCHEMA": {
			            "FIELDS": [ "Name", "origin", "destination", "Column", "LowerBound", "UpperBound" ],
			            "TYPES": [ "STRING", "STRING", "STRING", "STRING", "DOUBLE", "IEEEDOUBLE" ]
			          },
			          "INSTANCE": [
			            [ "ship", "PITT", "NE", "ship_PITT_NE", 0.0, "0x1.f4p7" ],
			            [ "ship", "PITT", "SE", "ship_PITT_SE", 0.0, "0x1.f4p7" ],
			            [ "ship", "NE", "BOS", "ship_NE_BOS", 0.0, "0x1.9p6" ],
			            [ "ship", "NE", "EWR", "ship_NE_EWR", 0.0, "0x1.28p7" ],
			            [ "ship", "NE", "BWI", "ship_NE_BWI", 0.0, "0x1.9p6" ],
			            [ "ship", "SE", "EWR", "ship_SE_EWR", 0.0, "0x1.23p8" ],
			            [ "ship", "SE", "BWI", "ship_SE_BWI", 0.0, "0x1.9p6" ],
			            [ "ship", "SE", "ATL", "ship_SE_ATL", 0.0, "0x1.9p6" ],
			            [ "ship", "SE", "MCO", "ship_SE_MCO", 0.0, "0x1.9p6" ]
			          ]
			        }
			      ]
			    }
			  ]
			}
			'''
*/
			"\r\n"
			+ "{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"model\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\r\n"
			+ "          \"Part of General Transshipment Problem\"\r\n"
			+ "        ]\r\n"
			+ "      },\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"ship\",\r\n"
			+ "          \"CLASS\": \"VARIABLE\",\r\n"
			+ "          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"origin\", \"destination\", \"Column\", \"LowerBound\", \"UpperBound\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"IEEEDOUBLE\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"ship\", \"PITT\", \"NE\", \"ship_PITT_NE\", 0.0, \"0x1.f4p7\" ],\r\n"
			+ "            [ \"ship\", \"PITT\", \"SE\", \"ship_PITT_SE\", 0.0, \"0x1.f4p7\" ],\r\n"
			+ "            [ \"ship\", \"NE\", \"BOS\", \"ship_NE_BOS\", 0.0, \"0x1.9p6\" ],\r\n"
			+ "            [ \"ship\", \"NE\", \"EWR\", \"ship_NE_EWR\", 0.0, \"0x1.28p7\" ],\r\n"
			+ "            [ \"ship\", \"NE\", \"BWI\", \"ship_NE_BWI\", 0.0, \"0x1.9p6\" ],\r\n"
			+ "            [ \"ship\", \"SE\", \"EWR\", \"ship_SE_EWR\", 0.0, \"0x1.23p8\" ],\r\n"
			+ "            [ \"ship\", \"SE\", \"BWI\", \"ship_SE_BWI\", 0.0, \"0x1.9p6\" ],\r\n"
			+ "            [ \"ship\", \"SE\", \"ATL\", \"ship_SE_ATL\", 0.0, \"0x1.9p6\" ],\r\n"
			+ "            [ \"ship\", \"SE\", \"MCO\", \"ship_SE_MCO\", 0.0, \"0x1.9p6\" ]\r\n"
			+ "          ]\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		String mosdex= 
/*			'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
  "MODULES": [
    {
      "NAME": "model",
      "CLASS": "MODEL",
      "HEADING": {
        "DESCRIPTION": ["Part of General Transshipment Problem"]
      },
      "TABLES": [
        { "NAME": "ship",
          "CLASS": "VARIABLE",
          "KIND": "CONTINUOUS",
          "SCHEMA": {
            "FIELDS":
              ["Name",  "origin",  "destination",  "Column",     "LowerBound", "UpperBound"],
            "TYPES":
              ["STRING","STRING",  "STRING",       "STRING",     "DOUBLE",     "IEEEDOUBLE"]
          },
          "INSTANCE": [
            ["ship", "PITT",    "NE",           "ship_PITT_NE",   0.0,          "250.0"   ],
            ["ship", "PITT",    "SE",           "ship_PITT_SE",   0.0,          "250.0"   ],
            ["ship", "NE",      "BOS",          "ship_NE_BOS",    0.0,          "100.0"   ],
            ["ship", "NE",      "EWR",          "ship_NE_EWR",    0.0,          "0x1.28p7"   ],
            ["ship", "NE",      "BWI",          "ship_NE_BWI",    0.0,          "100.0"   ],
            ["ship", "SE",      "EWR",          "ship_SE_EWR",    0.0,          "0x2.46p7"   ],
            ["ship", "SE",      "BWI",          "ship_SE_BWI",    0.0,          "100.0"   ],
            ["ship", "SE",      "ATL",          "ship_SE_ATL",    0.0,          "100.0"   ],
            ["ship", "SE",      "MCO",          "ship_SE_MCO",    0.0,          "100.0"   ]
          ]
      	}
      ]
    }
  ]
}			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "	    {\r\n"
			+ "	      \"NAME\": \"model\",\r\n"
			+ "	      \"CLASS\": \"MODEL\",\r\n"
			+ "	      \"HEADING\": {\r\n"
			+ "	        \"DESCRIPTION\": [\"Part of General Transshipment Problem\"]\r\n"
			+ "	      },\r\n"
			+ "	      \"TABLES\": [\r\n"
			+ "	        { \"NAME\": \"ship\",\r\n"
			+ "	          \"CLASS\": \"VARIABLE\",\r\n"
			+ "	          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "	          \"SCHEMA\": {\r\n"
			+ "	            \"FIELDS\":\r\n"
			+ "	              [\"Name\",  \"origin\",  \"destination\",  \"Column\",     \"LowerBound\", \"UpperBound\"],\r\n"
			+ "	            \"TYPES\":\r\n"
			+ "	              [\"STRING\",\"STRING\",  \"STRING\",       \"STRING\",     \"DOUBLE\",     \"IEEEDOUBLE\"]\r\n"
			+ "	          },\r\n"
			+ "	          \"INSTANCE\": [\r\n"
			+ "	            [\"ship\", \"PITT\",    \"NE\",           \"ship_PITT_NE\",   0.0,          \"250.0\"   ],\r\n"
			+ "	            [\"ship\", \"PITT\",    \"SE\",           \"ship_PITT_SE\",   0.0,          \"250.0\"   ],\r\n"
			+ "	            [\"ship\", \"NE\",      \"BOS\",          \"ship_NE_BOS\",    0.0,          \"100.0\"   ],\r\n"
			+ "	            [\"ship\", \"NE\",      \"EWR\",          \"ship_NE_EWR\",    0.0,          \"0x1.28p7\"   ],\r\n"
			+ "	            [\"ship\", \"NE\",      \"BWI\",          \"ship_NE_BWI\",    0.0,          \"100.0\"   ],\r\n"
			+ "	            [\"ship\", \"SE\",      \"EWR\",          \"ship_SE_EWR\",    0.0,          \"0x2.46p7\"   ],\r\n"
			+ "	            [\"ship\", \"SE\",      \"BWI\",          \"ship_SE_BWI\",    0.0,          \"100.0\"   ],\r\n"
			+ "	            [\"ship\", \"SE\",      \"ATL\",          \"ship_SE_ATL\",    0.0,          \"100.0\"   ],\r\n"
			+ "	            [\"ship\", \"SE\",      \"MCO\",          \"ship_SE_MCO\",    0.0,          \"100.0\"   ]\r\n"
			+ "	          ]\r\n"
			+ "	      	}\r\n"
			+ "	      ]\r\n"
			+ "	    }\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			dfFactory, 
			Msdx.GLOBAL.mapper, 
			false);
				
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
				
	}//IEEEDoubleTest
	
	public static void displayEquality() {
		
		IEEEDouble x= new IEEEDouble("123456789.987654321");
		IEEEDouble y= new IEEEDouble("123456789.987654320");	
		Msdx.GLOBAL.out.println("x= " + x.toString() + " y= " + y.toString() + " x.equals(y)= " + x.equals(y));

		y= new IEEEDouble("123456789.98765");
		Msdx.GLOBAL.out.println("x= " + x.toString() + " y= " + y.toString() + " x.equals(y)= " + x.equals(y));	
		
	}//displayEquality

	@Test
	public void equalityTest() {
		
		IEEEDouble x= new IEEEDouble("123456789.987654321");
		IEEEDouble y= new IEEEDouble("123456789.987654320");	
		assertTrue(x.equals(y));

		y= new IEEEDouble("123456789.98765");
		assertFalse(x.equals(y));	
		
	}//equalityTest
	
	public static void displayComparison() {
		
		IEEEDouble x= new IEEEDouble("123456789.987654321");
		IEEEDouble y= new IEEEDouble("123456789.987654320");	
		Msdx.GLOBAL.out.println("x= " + x.toString() + " y= " + y.toString() + " x.compareTo(y)= " + x.compareTo(y));

		y= new IEEEDouble("123456789.98765");
		Msdx.GLOBAL.out.println("x= " + x.toString() + " y= " + y.toString() + " x.compareTo(y)= " + x.compareTo(y));	
		
		y= new IEEEDouble("123456789.98766");
		Msdx.GLOBAL.out.println("x= " + x.toString() + " y= " + y.toString() + " x.compareTo(y)= " + x.compareTo(y));	
		
	}//displayComparison

	@Test
	public void comparisonTest() {
		
		IEEEDouble x= new IEEEDouble("123456789.987654321");
		IEEEDouble y= new IEEEDouble("123456789.987654320");	
		assertEquals(0, x.compareTo(y));

		y= new IEEEDouble("123456789.98765");
		assertEquals(1, x.compareTo(y));	
		
		y= new IEEEDouble("123456789.98766");
		assertEquals(-1, x.compareTo(y));	
		
	}//comparisonTest
	
	public static void displayFunctionCalls() {
			
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "model",
			      "CLASS": "MODEL",
			      "HEADING": {
			        "DESCRIPTION": ["Part of General Transshipment Problem"]
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
			     	}
			      ]
			    }
			  ]
			}
			'''
	*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"model\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\"Part of General Transshipment Problem\"]\r\n"
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
			+ "     	}\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
			
		MsdxObject.Factory factory= new MsdxObject.Factory(
			dfFactory, 
			Msdx.GLOBAL.mapper, 
			false);
				
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
				
	}//displayFunctionCalls

	@Test
	public void FunctionCallsTest() throws JSONException {
		String expected= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "model",
			      "CLASS": "MODEL",
			      "HEADING": {
			        "DESCRIPTION": [
			          "Part of General Transshipment Problem"
			        ]
			      },
			      "TABLES": [
			        {
			          "NAME": "ship",
			          "CLASS": "VARIABLE",
			          "KIND": "CONTINUOUS",
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
			      ]
			    }
			  ]
			}
			'''
*/
			"			{\r\n"
			+ "			  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "			  \"MODULES\": [\r\n"
			+ "			    {\r\n"
			+ "			      \"NAME\": \"model\",\r\n"
			+ "			      \"CLASS\": \"MODEL\",\r\n"
			+ "			      \"HEADING\": {\r\n"
			+ "			        \"DESCRIPTION\": [\r\n"
			+ "			          \"Part of General Transshipment Problem\"\r\n"
			+ "			        ]\r\n"
			+ "			      },\r\n"
			+ "			      \"TABLES\": [\r\n"
			+ "			        {\r\n"
			+ "			          \"NAME\": \"ship\",\r\n"
			+ "			          \"CLASS\": \"VARIABLE\",\r\n"
			+ "			          \"KIND\": \"CONTINUOUS\",\r\n"
			+ "			          \"SCHEMA\": {\r\n"
			+ "			            \"FIELDS\": [ \"Name\", \"origin\", \"destination\", \"Column\", \"LowerBound\", \"UpperBound\", \"Value\" ],\r\n"
			+ "			            \"TYPES\": [ \"STRING\", \"STRING\", \"STRING\", \"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE_FUNCTION\" ]\r\n"
			+ "			          },\r\n"
			+ "			          \"INSTANCE\": [\r\n"
			+ "			            [ \"ship\", \"PITT\", \"NE\", \"ship_PITT_NE\", 0.0, 250.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"PITT\", \"SE\", \"ship_PITT_SE\", 0.0, 250.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"NE\", \"BOS\", \"ship_NE_BOS\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"NE\", \"EWR\", \"ship_NE_EWR\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"NE\", \"BWI\", \"ship_NE_BWI\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"SE\", \"EWR\", \"ship_SE_EWR\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"SE\", \"BWI\", \"ship_SE_BWI\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"SE\", \"ATL\", \"ship_SE_ATL\", 0.0, 100.0, \"PrimalValue(Column)\" ],\r\n"
			+ "			            [ \"ship\", \"SE\", \"MCO\", \"ship_SE_MCO\", 0.0, 100.0, \"PrimalValue(Column)\" ]\r\n"
			+ "			          ]\r\n"
			+ "			        }\r\n"
			+ "			      ]\r\n"
			+ "			    }\r\n"
			+ "			  ]\r\n"
			+ "			}\r\n";
		
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "model",
			      "CLASS": "MODEL",
			      "HEADING": {
			        "DESCRIPTION": ["Part of General Transshipment Problem"]
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
			     	}
			      ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"model\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
			+ "      \"HEADING\": {\r\n"
			+ "        \"DESCRIPTION\": [\"Part of General Transshipment Problem\"]\r\n"
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
			+ "     	}\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
			
		MsdxObject.Factory factory= new MsdxObject.Factory(
			dfFactory, 
			Msdx.GLOBAL.mapper, 
			false);
				
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
				
	}//FunctionCallsTest
		

}//class MsdxDataTypes
