/**
 * 
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxJavaDataframe;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxSparkDataframe;

/**
 * This class offers tests of the MOSDEX Object Model classes.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxObjectModel {
	
	static MsdxSparkDataframe.Factory dfFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
	
	/**
	 * The main method is used to run the display (non-test) methods of this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("MOSDEX Object Model Tests");
		Msdx.GLOBAL.showDisplay();
		
		Msdx.GLOBAL.out.println("MsdxFile test");
		displayFile();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxModules test");
		displayModules();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxModule test");
		displayModule();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxTables test");
		displayTables();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxTable test");
		displayTable();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxSchema test");
		displaySchema();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxInstance Java test");
		displayJavaInstance();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxInstance Spark test");
		displaySparkInstance();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("MsdxQuery test");
		displayQuery();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Model test");
		displayModel();
		Msdx.GLOBAL.out.println();
		
	}//main
	
	public static void displayFile() {
		
		String mosdex= 
/*			'''
{
  "SYNTAX": "url",
  "MODULES": []
}			'''
*/
		"{\r\n"
		+ "  \"SYNTAX\": \"url\",\r\n"
		+ "  \"MODULES\": []\r\n"
		+ "}"
		;
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//fileDisplay

	@Test
	public void fileTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "url",
			  "MODULES": [  ]
			}
			'''
*/
			"			{\r\n"
			+ "			  \"SYNTAX\": \"url\",\r\n"
			+ "			  \"MODULES\": [  ]\r\n"
			+ "			}\r\n";
			
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "url",
			  "MODULES": []
			}			
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"url\",\r\n"
			+ "  \"MODULES\": []\r\n"
			+ "}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//fileTest
	
	public static void displayModules() {
		
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "url",
	  "MODULES": [
  		{"NAME": "Module1"},
   		{"NAME": "Module2"}
	  ]
	}			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"url\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\"},\r\n"
			+ "   		{\"NAME\": \"Module2\"}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displayModules
	
	@Test
	public void modulesTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "url",
			  "MODULES": [
			    {
			      "NAME": "Module1"
			    },
			    {
			      "NAME": "Module2"
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"url\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module1\"\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module2\"\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "url",
	  "MODULES": [
  		{"NAME": "Module1"},
   		{"NAME": "Module2"}
	  ]
	}
			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"url\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\"},\r\n"
			+ "   		{\"NAME\": \"Module2\"}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//modulesTest
	
	public static void displayModule() {
		
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "",
	  "MODULES": [
  		{"NAME": "Module1", "TABLES" : []},
   		{"NAME": "Module2", "TABLES" : []}
	  ]
	}			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \"TABLES\" : []},\r\n"
			+ "   		{\"NAME\": \"Module2\", \"TABLES\" : []}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displayModule
	
	@Test
	public void moduleTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
			    {
			      "NAME": "Module1",
			      "TABLES": [  ]
			    },
			    {
			      "NAME": "Module2",
			      "TABLES": [  ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module1\",\r\n"
			+ "      \"TABLES\": [  ]\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module2\",\r\n"
			+ "      \"TABLES\": [  ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
		  		{"NAME": "Module1", "TABLES" : []},
		   		{"NAME": "Module2", "TABLES" : []}
			  ]
			}
			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \"TABLES\" : []},\r\n"
			+ "   		{\"NAME\": \"Module2\", \"TABLES\" : []}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//moduleTest
	
	public static void displayTables() {
		
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "",
	  "MODULES": [
  		{"NAME": "Module1", 
  		 "TABLES" : [
  		  {"NAME" : "TableA"}, 
  		  {"NAME" : "TableB"}
  		 ]
  		},
   		{"NAME": "Module2", 
  		 "TABLES" : [
  		  {"NAME" : "TableC"}, 
  		  {"NAME" : "TableD"}
  		 ]
   		}
	  ]
	}			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\"NAME\" : \"TableA\"}, \r\n"
			+ "  		  {\"NAME\" : \"TableB\"}\r\n"
			+ "  		 ]\r\n"
			+ "  		},\r\n"
			+ "   		{\"NAME\": \"Module2\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\"NAME\" : \"TableC\"}, \r\n"
			+ "  		  {\"NAME\" : \"TableD\"}\r\n"
			+ "  		 ]\r\n"
			+ "   		}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displayTables
	
	@Test
	public void tablesTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
			    {
			      "NAME": "Module1",
			      "TABLES": [
			        {
			          "NAME": "TableA"
			        },
			        {
			          "NAME": "TableB"
			        }
			      ]
			    },
			    {
			      "NAME": "Module2",
			      "TABLES": [
			        {
			          "NAME": "TableC"
			        },
			        {
			          "NAME": "TableD"
			        }
			      ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module1\",\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableA\"\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableB\"\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module2\",\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableC\"\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableD\"\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n"
			+ "\r\n";
		
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
		  		{"NAME": "Module1", 
		  		 "TABLES" : [
		  		  {"NAME" : "TableA"}, 
		  		  {"NAME" : "TableB"}
		  		 ]
		  		},
		   		{"NAME": "Module2", 
		  		 "TABLES" : [
		  		  {"NAME" : "TableC"}, 
		  		  {"NAME" : "TableD"}
		  		 ]
		   		}
			  ]
			}
			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\"NAME\" : \"TableA\"}, \r\n"
			+ "  		  {\"NAME\" : \"TableB\"}\r\n"
			+ "  		 ]\r\n"
			+ "  		},\r\n"
			+ "   		{\"NAME\": \"Module2\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\"NAME\" : \"TableC\"}, \r\n"
			+ "  		  {\"NAME\" : \"TableD\"}\r\n"
			+ "  		 ]\r\n"
			+ "   		}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//tablesTest
	
	public static void displayTable() {
		
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "",
	  "MODULES": [
  		{"NAME": "Module1", 
  		 "TABLES" : [
  		  {
  		  	"NAME" : "TableA",
  		  	"SCHEMA": {}
	  	  }, 
  		  {
  		  	"NAME" : "TableB",
  		  	"SCHEMA": {}
  		  }
  		 ]
  		},
   		{"NAME": "Module2", 
  		 "TABLES" : [
  		  {
  		  	"NAME" : "TableC",
  		  	"SCHEMA": {}
  		  }, 
  		  {
  		  	"NAME" : "TableD",
  		  	"SCHEMA": {}
  		  }
  		 ]
   		}
	  ]
	}			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableA\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "	  	  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableB\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "  		},\r\n"
			+ "   		{\"NAME\": \"Module2\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableC\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "  		  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableD\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "   		}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displayTable
	
	@Test
	public void tableTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
			    {
			      "NAME": "Module1",
			      "TABLES": [
			        {
			          "NAME": "TableA",
			          "SCHEMA": { }
			        },
			        {
			          "NAME": "TableB",
			          "SCHEMA": { }
			        }
			      ]
			    },
			    {
			      "NAME": "Module2",
			      "TABLES": [
			        {
			          "NAME": "TableC",
			          "SCHEMA": { }
			        },
			        {
			          "NAME": "TableD",
			          "SCHEMA": { }
			        }
			      ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module1\",\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableA\",\r\n"
			+ "          \"SCHEMA\": { }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableB\",\r\n"
			+ "          \"SCHEMA\": { }\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module2\",\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableC\",\r\n"
			+ "          \"SCHEMA\": { }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableD\",\r\n"
			+ "          \"SCHEMA\": { }\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n"
			+ "\r\n";
		
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
		  		{"NAME": "Module1", 
		  		 "TABLES" : [
		  		  {
		  		  	"NAME" : "TableA",
		  		  	"SCHEMA": {}
			  	  }, 
		  		  {
		  		  	"NAME" : "TableB",
		  		  	"SCHEMA": {}
		  		  }
		  		 ]
		  		},
		   		{"NAME": "Module2", 
		  		 "TABLES" : [
		  		  {
		  		  	"NAME" : "TableC",
		  		  	"SCHEMA": {}
		  		  }, 
		  		  {
		  		  	"NAME" : "TableD",
		  		  	"SCHEMA": {}
		  		  }
		  		 ]
		   		}
			  ]
			}
			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableA\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "	  	  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableB\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "  		},\r\n"
			+ "   		{\"NAME\": \"Module2\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableC\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "  		  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableD\",\r\n"
			+ "  		  	\"SCHEMA\": {}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "   		}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//tableTest
	
	public static void displaySchema() {
		
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "",
	  "MODULES": [
  		{"NAME": "Module1", 
  		 "TABLES" : [
  		  {
  		  	"NAME" : "TableA",
  		  	"SCHEMA": {
  		  		"FIELDS": ["Label", "Number"],
   		  		"TYPES": ["STRING", "DOUBLE"]
 		  	}
	  	  }, 
  		  {
  		  	"NAME" : "TableB",
  		  	"SCHEMA": {
  		  		"FIELDS": ["Label", "Integer"],
   		  		"TYPES": ["STRING", "INTEGER"]
 		  	}
  		  }
  		 ]
  		},
   		{"NAME": "Module2", 
  		 "TABLES" : [
  		  {
  		  	"NAME" : "TableC",
  		  	"SCHEMA": {
  		  		"FIELDS": ["Label", "Number"],
   		  		"TYPES": ["STRING", "IEEEDOUBLE"]
 		  	}
  		  }, 
  		  {
  		  	"NAME" : "TableD",
  		  	"SCHEMA": {
  		  		"FIELDS": ["Label", "FunctionCall"],
   		  		"TYPES": ["STRING", "DOUBLE_FUNCTION"]
 		  	}
  		  }
  		 ]
   		}
	  ]
	}			'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableA\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"Number\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"DOUBLE\"]\r\n"
			+ " 		  	}\r\n"
			+ "	  	  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableB\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"Integer\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"INTEGER\"]\r\n"
			+ " 		  	}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "  		},\r\n"
			+ "   		{\"NAME\": \"Module2\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableC\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"Number\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"IEEEDOUBLE\"]\r\n"
			+ " 		  	}\r\n"
			+ "  		  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableD\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"FunctionCall\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"DOUBLE_FUNCTION\"]\r\n"
			+ " 		  	}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "   		}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displaySchema
	

	@Test
	public void schemaTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "",
			  "MODULES": [
			    {
			      "NAME": "Module1",
			      "TABLES": [
			        {
			          "NAME": "TableA",
			          "SCHEMA": {
			            "FIELDS": [ "Label", "Number" ],
			            "TYPES": [ "STRING", "DOUBLE" ]
			          }
			        },
			        {
			          "NAME": "TableB",
			          "SCHEMA": {
			            "FIELDS": [ "Label", "Integer" ],
			            "TYPES": [ "STRING", "INTEGER" ]
			          }
			        }
			      ]
			    },
			    {
			      "NAME": "Module2",
			      "TABLES": [
			        {
			          "NAME": "TableC",
			          "SCHEMA": {
			            "FIELDS": [ "Label", "Number" ],
			            "TYPES": [ "STRING", "IEEEDOUBLE" ]
			          }
			        },
			        {
			          "NAME": "TableD",
			          "SCHEMA": {
			            "FIELDS": [ "Label", "FunctionCall" ],
			            "TYPES": [ "STRING", "DOUBLE_FUNCTION" ]
			          }
			        }
			      ]
			    }
			  ]
			}
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module1\",\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableA\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Label\", \"Number\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"DOUBLE\" ]\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableB\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Label\", \"Integer\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"INTEGER\" ]\r\n"
			+ "          }\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    },\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"Module2\",\r\n"
			+ "      \"TABLES\": [\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableC\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Label\", \"Number\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"IEEEDOUBLE\" ]\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"TableD\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Label\", \"FunctionCall\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"DOUBLE_FUNCTION\" ]\r\n"
			+ "          }\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n";
		
		String mosdex= 
/*			'''
				{
				  "SYNTAX": "",
				  "MODULES": [
			  		{"NAME": "Module1", 
			  		 "TABLES" : [
			  		  {
			  		  	"NAME" : "TableA",
			  		  	"SCHEMA": {
			  		  		"FIELDS": ["Label", "Number"],
			   		  		"TYPES": ["STRING", "DOUBLE"]
			 		  	}
				  	  }, 
			  		  {
			  		  	"NAME" : "TableB",
			  		  	"SCHEMA": {
			  		  		"FIELDS": ["Label", "Integer"],
			   		  		"TYPES": ["STRING", "INTEGER"]
			 		  	}
			  		  }
			  		 ]
			  		},
			   		{"NAME": "Module2", 
			  		 "TABLES" : [
			  		  {
			  		  	"NAME" : "TableC",
			  		  	"SCHEMA": {
			  		  		"FIELDS": ["Label", "Number"],
			   		  		"TYPES": ["STRING", "IEEEDOUBLE"]
			 		  	}
			  		  }, 
			  		  {
			  		  	"NAME" : "TableD",
			  		  	"SCHEMA": {
			  		  		"FIELDS": ["Label", "FunctionCall"],
			   		  		"TYPES": ["STRING", "DOUBLE_FUNCTION"]
			 		  	}
			  		  }
			  		 ]
			   		}
				  ]
				}
				'''
*/
			"	{\r\n"
			+ "	  \"SYNTAX\": \"\",\r\n"
			+ "	  \"MODULES\": [\r\n"
			+ "  		{\"NAME\": \"Module1\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableA\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"Number\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"DOUBLE\"]\r\n"
			+ " 		  	}\r\n"
			+ "	  	  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableB\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"Integer\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"INTEGER\"]\r\n"
			+ " 		  	}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "  		},\r\n"
			+ "   		{\"NAME\": \"Module2\", \r\n"
			+ "  		 \"TABLES\" : [\r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableC\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"Number\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"IEEEDOUBLE\"]\r\n"
			+ " 		  	}\r\n"
			+ "  		  }, \r\n"
			+ "  		  {\r\n"
			+ "  		  	\"NAME\" : \"TableD\",\r\n"
			+ "  		  	\"SCHEMA\": {\r\n"
			+ "  		  		\"FIELDS\": [\"Label\", \"FunctionCall\"],\r\n"
			+ "   		  		\"TYPES\": [\"STRING\", \"DOUBLE_FUNCTION\"]\r\n"
			+ " 		  	}\r\n"
			+ "  		  }\r\n"
			+ "  		 ]\r\n"
			+ "   		}\r\n"
			+ "	  ]\r\n"
			+ "	}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//schemaTest
	
	public static void displayJavaInstance() {
		
		String mosdex= 
/*			'''
{
  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
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
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			new MsdxJavaDataframe.Factory(), 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		Msdx.GLOBAL.out.println();
		Msdx.GLOBAL.out.println("cities");
		file
			/*.getModules()*/.getModule("data")
			/*.getTables()*/.getTable("cities")
				.getInstance()/* .getDataframe() */
//			.showRecords("cities", Msdx.GLOBAL.out, null);
			.show(Msdx.GLOBAL.out, null);
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displayJavaInstance
	
	@Test
	public void javaInstanceTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
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
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			new MsdxJavaDataframe.Factory(), 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//javaInstanceTest
	
	public static void displaySparkInstance() {
			
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
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
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}";
			
			MsdxObject.Factory factory= new MsdxObject.Factory(
				dfFactory, 
				Msdx.GLOBAL.mapper, 
				false);
			
			MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
			Msdx.GLOBAL.out.println();
			Msdx.GLOBAL.out.println("cities");
			file
				/*.getModules()*/.getModule("data")
				/*.getTables()*/.getTable("cities")
				.getInstance()/* .getDataframe() */
//				.showRecords("cities", Msdx.GLOBAL.out, null);
				.show(Msdx.GLOBAL.out, null);
			factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
			
		}//displaySparkInstance

	@Test
	public void sparkInstanceTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
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
			+ "        }\r\n"
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
		
	}//sparkInstanceTest
	
	public static void displayQuery() {
			
		String mosdex= 
/*			'''
	{
	  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
	  "MODULES": [
	    {
	      "NAME": "results",
	      "CLASS": "DATA",
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
	'''
*/
				"{\r\n"
				+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
				+ "  \"MODULES\": [\r\n"
				+ "    {\r\n"
				+ "      \"NAME\": \"results\",\r\n"
				+ "      \"CLASS\": \"DATA\",\r\n"
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
				+ "}";
			
			MsdxObject.Factory factory= new MsdxObject.Factory(
				new MsdxJavaDataframe.Factory(), 
				Msdx.GLOBAL.mapper, 
				false);
			
			MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
			Msdx.GLOBAL.out.println();
			factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
			
		}//displayQuery

	@Test
	public void queryTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "results",
			      "CLASS": "DATA",
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
			            "SELECT": [
			              "totalCost.cost AS cost -- DOUBLE"
			            ],
			            "FROM": "totalCost"
			          }
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
			+ "      \"NAME\": \"results\",\r\n"
			+ "      \"CLASS\": \"DATA\",\r\n"
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
			+ "            \"SELECT\": [\r\n"
			+ "              \"totalCost.cost AS cost -- DOUBLE\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"totalCost\"\r\n"
			+ "          }\r\n"
			+ "        }\r\n"
			+ "      ]\r\n"
			+ "    }\r\n"
			+ "  ]\r\n"
			+ "}\r\n"
			+ "\r\n";
		
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
			    {
			      "NAME": "results",
			      "CLASS": "DATA",
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
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
			+ "    {\r\n"
			+ "      \"NAME\": \"results\",\r\n"
			+ "      \"CLASS\": \"DATA\",\r\n"
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
			+ "}";
			
		MsdxObject.Factory factory= new MsdxObject.Factory(
			null, 
			Msdx.GLOBAL.mapper, 
			false);
			
		ByteArrayOutputStream dst= new ByteArrayOutputStream();
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		factory.writeFile(file, MsdxOutputDestination.toStream(dst));
		String actual= dst.toString();
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		
	}//queryTest
	
	public static void displayModel() {
		
		String mosdex= 
/*			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
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
			      "NAME": "results",
			      "CLASS": "DATA",
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
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
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
			+ "           \"NAME\":\"ship\",\r\n"
			+ "           \"CLASS\": \"VARIABLE\",\r\n"
			+ "           \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"'ship' AS Name                                          -- STRING\",\r\n"
			+ "              \"routes.origin AS origin                                 -- STRING\",\r\n"
			+ "              \"routes.destination AS destination                       -- STRING\",\r\n"
			+ "              \"CONCAT('ship', '_', origin, '_', destination) AS Column -- STRING\",\r\n"
			+ "              \"CAST(0.0 AS DOUBLE) AS LowerBound                       -- DOUBLE\",\r\n"
			+ "              \"routes.capacity AS UpperBound                           -- DOUBLE\",\r\n"
			+ "			  \"'PrimalValue(Column)' AS value                          -- DOUBLE_FUNCTION\"\r\n"
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
			+ "			  \"'DualValue(Row)' AS dual                                -- DOUBLE_FUNCTION\"\r\n"
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
			+ "      \"NAME\": \"results\",\r\n"
			+ "      \"CLASS\": \"DATA\",\r\n"
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
			+ "}";
		
		MsdxObject.Factory factory= new MsdxObject.Factory(
			new MsdxJavaDataframe.Factory(), 
			Msdx.GLOBAL.mapper, 
			false);
		
		MsdxFile file= factory.readFile(MsdxInputSource.fromString(mosdex));
		Msdx.GLOBAL.out.println();
		factory.writeFile(file, MsdxOutputDestination.toStream(Msdx.GLOBAL.out));
		
	}//displayModel
	
	@Test
	public void modelTest() throws JSONException {
		String expected= 
/*
			'''
			{
			  "SYNTAX": "MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json",
			  "MODULES": [
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
			            "FIELDS": [ "Name", "Row", "Constant", "Sense", "cost" ],
			            "TYPES": [ "STRING", "STRING", "DOUBLE", "STRING", "DOUBLE_FUNCTION" ]
			          },
			          "INSTANCE": [
			            [ "totalCost", "totalCost", 0.0, "MINIMIZE", "ObjectiveValue(Row)" ]
			          ]
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
			            "ON": "balance.city = ship.destination"
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
			      "NAME": "results",
			      "CLASS": "DATA",
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
			            "SELECT": [
			              "totalCost.cost AS cost -- DOUBLE"
			            ],
			            "FROM": "totalCost"
			          }
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
			+ "      \"NAME\": \"modelingObjects\",\r\n"
			+ "      \"CLASS\": \"MODEL\",\r\n"
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
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"'ship' AS Name                                          -- STRING\",\r\n"
			+ "              \"routes.origin AS origin                                 -- STRING\",\r\n"
			+ "              \"routes.destination AS destination                       -- STRING\",\r\n"
			+ "              \"CONCAT('ship', '_', origin, '_', destination) AS Column -- STRING\",\r\n"
			+ "              \"CAST(0.0 AS DOUBLE) AS LowerBound                       -- DOUBLE\",\r\n"
			+ "              \"routes.capacity AS UpperBound                           -- DOUBLE\",\r\n"
			+ "              \"'PrimalValue(Column)' AS value                          -- DOUBLE_FUNCTION\"\r\n"
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
			+ "              \"'DualValue(Row)' AS dual                                -- DOUBLE_FUNCTION\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"cities\"\r\n"
			+ "          }\r\n"
			+ "        },\r\n"
			+ "        {\r\n"
			+ "          \"NAME\": \"totalCost\",\r\n"
			+ "          \"CLASS\": \"OBJECTIVE\",\r\n"
			+ "          \"KIND\": \"LINEAR\",\r\n"
			+ "          \"SCHEMA\": {\r\n"
			+ "            \"FIELDS\": [ \"Name\", \"Row\", \"Constant\", \"Sense\", \"cost\" ],\r\n"
			+ "            \"TYPES\": [ \"STRING\", \"STRING\", \"DOUBLE\", \"STRING\", \"DOUBLE_FUNCTION\" ]\r\n"
			+ "          },\r\n"
			+ "          \"INSTANCE\": [\r\n"
			+ "            [ \"totalCost\", \"totalCost\", 0.0, \"MINIMIZE\", \"ObjectiveValue(Row)\" ]\r\n"
			+ "          ]\r\n"
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
			+ "          }\r\n"
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
			+ "            \"ON\": \"balance.city = ship.destination\"\r\n"
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
			+ "      \"NAME\": \"results\",\r\n"
			+ "      \"CLASS\": \"DATA\",\r\n"
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
			+ "            \"SELECT\": [\r\n"
			+ "              \"totalCost.cost AS cost -- DOUBLE\"\r\n"
			+ "            ],\r\n"
			+ "            \"FROM\": \"totalCost\"\r\n"
			+ "          }\r\n"
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
			      "NAME": "results",
			      "CLASS": "DATA",
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
			'''
*/
			"{\r\n"
			+ "  \"SYNTAX\": \"MOSDEX/MOSDEX v2/MOSDEXSchemaV2-0.json\",\r\n"
			+ "  \"MODULES\": [\r\n"
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
			+ "           \"NAME\":\"ship\",\r\n"
			+ "           \"CLASS\": \"VARIABLE\",\r\n"
			+ "           \"KIND\": \"CONTINUOUS\",\r\n"
			+ "          \"QUERY\": {\r\n"
			+ "            \"SELECT\": [\r\n"
			+ "              \"'ship' AS Name                                          -- STRING\",\r\n"
			+ "              \"routes.origin AS origin                                 -- STRING\",\r\n"
			+ "              \"routes.destination AS destination                       -- STRING\",\r\n"
			+ "              \"CONCAT('ship', '_', origin, '_', destination) AS Column -- STRING\",\r\n"
			+ "              \"CAST(0.0 AS DOUBLE) AS LowerBound                       -- DOUBLE\",\r\n"
			+ "              \"routes.capacity AS UpperBound                           -- DOUBLE\",\r\n"
			+ "			  \"'PrimalValue(Column)' AS value                          -- DOUBLE_FUNCTION\"\r\n"
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
			+ "			  \"'DualValue(Row)' AS dual                                -- DOUBLE_FUNCTION\"\r\n"
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
			+ "      \"NAME\": \"results\",\r\n"
			+ "      \"CLASS\": \"DATA\",\r\n"
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
		
	}//modelTest
	

}//class MsdxObjectModel
