/**
 * 
 */
package io.github.JeremyBloom.mosdex2.span;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxSchema;

/**
 * Tests the MsxdContainer class.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxContainerOperations {

	/**
	 * 
	 */
	public MsdxContainerOperations() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Msdx.GLOBAL.setDisplayTitle("Container Demo");
		Msdx.GLOBAL.showDisplay();
		
		Msdx.GLOBAL.out.println("Reading");
		reading();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Building");
		building();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Equality");
		equality();
		Msdx.GLOBAL.out.println();
		
		Msdx.GLOBAL.out.println("Inequality");
		inequality();
		Msdx.GLOBAL.out.println();		

		Msdx.GLOBAL.out.println("Selecting");
		selecting();
		Msdx.GLOBAL.out.println();		

		Msdx.GLOBAL.out.println("Deleting");
		deleting();
		Msdx.GLOBAL.out.println();		

		Msdx.GLOBAL.out.println("Getting");
		getting();
		Msdx.GLOBAL.out.println();		

		Msdx.GLOBAL.out.println("Merging");
		merging();
		Msdx.GLOBAL.out.println();

		Msdx.GLOBAL.out.println("Filtering");
		filtering();
		Msdx.GLOBAL.out.println();

		Msdx.GLOBAL.out.println("Renaming");
		renaming();
		Msdx.GLOBAL.out.println();

		Msdx.GLOBAL.out.println("Mismatched");
		mismatched();
		Msdx.GLOBAL.out.println();

	}//main
	
	public static void reading() {
		
		String mosdexFile= 
			/*'''
	{
        "SCHEMA": {
	        "FIELDS":
	          ["Row",         "Column",       "Coefficient"],
	        "TYPES":
	          ["STRING",      "STRING",       "DOUBLE"]
      	}
	}				
			'''*/
	"	{\r\n" + 
	"        \"SCHEMA\": {\r\n" + 
	"        \"FIELDS\":\r\n" + 
	"          [\"Row\",         \"Column\",       \"Coefficient\"],\r\n" + 
	"        \"TYPES\":\r\n" + 
	"          [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n" + 
	"      	}\r\n" + 
	"	}				\r\n" + 
	"";
		
		MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
		JsonNode file= null;
		try {
			file= Msdx.GLOBAL.mapper.readTree(src.getStream());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}		
		MsdxContainer<Class<?>> schema= MsdxSchema.fromNode(file.get("SCHEMA"));
		MsdxSchema.show(schema, Msdx.GLOBAL.out);
	}//reading
	
	@Test
	public void readingTest() {
		
		String mosdexFile= 
			/*'''
	{
        "SCHEMA": {
	        "FIELDS":
	          ["Row",         "Column",       "Coefficient"],
	        "TYPES":
	          ["STRING",      "STRING",       "DOUBLE"]
      	}
	}				
			'''*/
	"	{\r\n" + 
	"        \"SCHEMA\": {\r\n" + 
	"        \"FIELDS\":\r\n" + 
	"          [\"Row\",         \"Column\",       \"Coefficient\"],\r\n" + 
	"        \"TYPES\":\r\n" + 
	"          [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n" + 
	"      	}\r\n" + 
	"	}				\r\n" + 
	"";
		
		MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
		JsonNode file= null;
		try {
			file= Msdx.GLOBAL.mapper.readTree(src.getStream());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}		
		MsdxContainer<Class<?>> actual= MsdxSchema.fromNode(file.get("SCHEMA"));
		
		MsdxContainer<Class<?>> expected= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		assertEquals(expected, actual);
	}//readingTest
	
	public static void building() {
		MsdxContainer<Class<?>> schema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		
		MsdxContainer<Object> data= MsdxRecord.builder(schema)	
			.addItem("Row", "rowOne")
			.addItem("Column", "columnOne")
			.addItem("Coefficient", 1.0)
			.build();
		
		MsdxSchema.show(schema, Msdx.GLOBAL.out);
		MsdxRecord.showAsNode(data, schema, Msdx.GLOBAL.out);	
	}//building

	@Test
	public void buildingTest() throws JSONException {
		MsdxContainer<Class<?>> schema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		
		MsdxContainer<Object> data= MsdxRecord.builder(schema)	
			.addItem("Row", "rowOne")
			.addItem("Column", "columnOne")
			.addItem("Coefficient", 1.0)
			.build();
		
		String expectedSchema= 
				/*'''
		{
	        "FIELDS":
	          ["Row",         "Column",       "Coefficient"],
	        "TYPES":
	          ["STRING",      "STRING",       "DOUBLE"]
		}				
				'''*/
		"{\r\n"
		+ "	        \"FIELDS\":\r\n"
		+ "	          [\"Row\",         \"Column\",       \"Coefficient\"],\r\n"
		+ "	        \"TYPES\":\r\n"
		+ "	          [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n"
		+ "		}";
			
		String expectedData= 
				/*'''
		["rowOne", "columnOne", 1.0]				
				'''*/
		"[\"rowOne\", \"columnOne\", 1.0]";
		
		JSONAssert.assertEquals(expectedSchema, MsdxSchema.toNode(schema).toString(), JSONCompareMode.LENIENT);
		JSONAssert.assertEquals(expectedData, MsdxRecord.toNode(data, schema).toString(), JSONCompareMode.LENIENT);
	}//buildingTest

	public static void equality() {
		MsdxContainer<Class<?>> firstSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> first= MsdxRecord.create(firstSchema, "rowOne", "columnOne", 1.0);
			
		MsdxContainer<Class<?>> secondSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> second= MsdxRecord.create(secondSchema, "rowOne", "columnOne", 1.0);
			
		Msdx.GLOBAL.out.println(first.equals(second));			
	}//equality
	
	@Test
	public void equalityTest() {
		MsdxContainer<Class<?>> firstSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> first= MsdxRecord.create(firstSchema, "rowOne", "columnOne", 1.0);
			
		MsdxContainer<Class<?>> secondSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> second= MsdxRecord.create(secondSchema, "rowOne", "columnOne", 1.0);
			
		assertEquals(first, second);			
	}//equalityTest
	
	public static void inequality() {
		MsdxContainer<Class<?>> firstSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> first= MsdxRecord.create(firstSchema, "rowOne", "columnOne", 1.0);
			
		MsdxContainer<Class<?>> secondSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("ColumnId", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> second= MsdxRecord.create(secondSchema, "rowOne", "columnOne", 1.0);
			
		MsdxContainer<Class<?>> thirdSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> third= MsdxRecord.create(thirdSchema, "rowOne", "columnOne", 2.0);
			
		Msdx.GLOBAL.out.println("first equals second: " + first.equals(second) + " ");		
		Msdx.GLOBAL.out.println("first equals third: " + first.equals(third));		
		
	}//inequality
	
	@Test
	public void inequalityTest() {
		MsdxContainer<Class<?>> firstSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> first= MsdxRecord.create(firstSchema, "rowOne", "columnOne", 1.0);
			
		MsdxContainer<Class<?>> secondSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("ColumnId", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> second= MsdxRecord.create(secondSchema, "rowOne", "columnOne", 1.0);
			
		MsdxContainer<Class<?>> thirdSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> third= MsdxRecord.create(thirdSchema, "rowOne", "columnOne", 2.0);
			
		assertTrue(!first.equals(second));		
		assertTrue(!first.equals(third));			
	}//inequality
	
	public static void selecting() {	
		MsdxContainer<Class<?>> actualSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> actual= MsdxRecord.create(actualSchema, "rowOne", "columnOne", 1.0)
			.select("Row", "Coefficient");
		
		MsdxRecord.showAsNode(actual, actualSchema.select("Row", "Coefficient"), Msdx.GLOBAL.out);		
	}//selecting

	@Test
	public void selectingTest() throws JSONException {	
		MsdxContainer<Class<?>> actualSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> actual= MsdxRecord.create(actualSchema, "rowOne", "columnOne", 1.0)
			.select("Row", "Coefficient");
		
		String expectedData= 
				/*'''
		["rowOne", 1.0]				
				'''*/
		"[\"rowOne\", 1.0]";
		
		JSONAssert.assertEquals(expectedData, MsdxRecord.toNode(actual, actualSchema.delete("Column")).toString(), JSONCompareMode.LENIENT);
	}//selectingTest
	
	public static void deleting() {	
		MsdxContainer<Class<?>> actualSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> actual= MsdxRecord.create(actualSchema, "rowOne", "columnOne", 1.0)
			.delete("Column");
		
		MsdxRecord.showAsNode(actual, actualSchema.delete("Column"), Msdx.GLOBAL.out);		
	}//deleting
	
	@Test
	public void deletingTest() throws JSONException {	
		MsdxContainer<Class<?>> actualSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> actual= MsdxRecord.create(actualSchema, "rowOne", "columnOne", 1.0)
			.delete("Column");
		
		String expectedData= 
				/*'''
		["rowOne", 1.0]				
				'''*/
		"[\"rowOne\", 1.0]";
		
		JSONAssert.assertEquals(expectedData, MsdxRecord.toNode(actual, actualSchema.delete("Column")).toString(), JSONCompareMode.LENIENT);
	}//deletingTest
	
	public static void getting() {
		MsdxContainer<Class<?>> actualSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> actual= MsdxRecord.create(actualSchema, "rowOne", "columnOne", 1.0);
		
		Msdx.GLOBAL.out.println("Row: {" + actual.get("Row").toString() + ": " + 
				actualSchema.get("Row").getSimpleName() + 
				"} is instance " + String.class.isInstance(actual.get("Row")));		
	}//getting
	
	@Test
	public void gettingTest() {
		MsdxContainer<Class<?>> actualSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Row", String.class)
			.addItem("Column", String.class)
			.addItem("Coefficient", Double.class)
			.build();
		MsdxContainer<Object> actual= MsdxRecord.create(actualSchema, "rowOne", "columnOne", 1.0);
		
		assertTrue(String.class.isInstance(actual.get("Row")));
		assertEquals("rowOne", actual.get("Row"));
	}//gettingTest
	
	public static void merging() {
		
		String mosdexFile= 
				/*'''
		{
	        "SCHEMA": {
		        "FIELDS":
		          ["Row",         "Column",       "Coefficient"],
		        "TYPES":
		          ["STRING",      "STRING",       "DOUBLE"]
	      	}
		}				
				'''*/
		"	{\r\n" + 
		"        \"SCHEMA\": {\r\n" + 
		"        \"FIELDS\":\r\n" + 
		"          [\"Row\",         \"Column\",       \"Coefficient\"],\r\n" + 
		"        \"TYPES\":\r\n" + 
		"          [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n" + 
		"      	}\r\n" + 
		"	}				\r\n" + 
		"";
			
			MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
			JsonNode file= null;
			try {
				file= Msdx.GLOBAL.mapper.readTree(src.getStream());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		MsdxContainer<Class<?>> termSchema= MsdxSchema.fromNode(file.get("SCHEMA"));
		MsdxContainer<Object> term= MsdxRecord.create(termSchema, "balanceRow", "shipColumn", 1.0);
		Msdx.GLOBAL.out.println("term");
		MsdxSchema.show(termSchema, Msdx.GLOBAL.out);
		MsdxRecord.showAsNode(term, termSchema, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> variableSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Column", String.class)
			.addItem("Variable", String.class)
			.build();
		MsdxContainer<Object> variable= MsdxRecord.create(variableSchema, "shipColumn", "ship");
		Msdx.GLOBAL.out.println("variable");
		MsdxSchema.show(variableSchema, Msdx.GLOBAL.out);
		MsdxRecord.showAsNode(variable, variableSchema, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> expressionSchema= termSchema.merge(variableSchema);
		MsdxContainer<Object> expression= term.merge(variable);	
		Msdx.GLOBAL.out.println("expression");
		MsdxSchema.show(expressionSchema, Msdx.GLOBAL.out);	
		MsdxRecord.showAsNode(expression, expressionSchema, Msdx.GLOBAL.out);		
	}//merging
	
	@Test
	public void mergingTest() throws JSONException {
		
		String mosdexFile= 
				/*'''
		{
	        "SCHEMA": {
		        "FIELDS": ["Row",         "Column",       "Coefficient"],
		        "TYPES":  ["STRING",      "STRING",       "DOUBLE"]
	      	}
		}				
				'''*/
		"		{\r\n"
		+ "	        \"SCHEMA\": {\r\n"
		+ "		        \"FIELDS\": [\"Row\",         \"Column\",       \"Coefficient\"],\r\n"
		+ "		        \"TYPES\":  [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n"
		+ "	      	}\r\n"
		+ "		}\r\n";
			
			MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
			JsonNode file= null;
			try {
				file= Msdx.GLOBAL.mapper.readTree(src.getStream());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		MsdxContainer<Class<?>> termSchema= MsdxSchema.fromNode(file.get("SCHEMA"));
		MsdxContainer<Object> term= MsdxRecord.create(termSchema, "balanceRow", "shipColumn", 1.0);
		Msdx.GLOBAL.out.println("term");
		MsdxSchema.show(termSchema, Msdx.GLOBAL.out);
		MsdxRecord.show(term, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> variableSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Column", String.class)
			.addItem("Variable", String.class)
			.build();
		MsdxContainer<Object> variable= MsdxRecord.create(variableSchema, "shipColumn", "ship");
		Msdx.GLOBAL.out.println("variable");
		MsdxSchema.show(variableSchema, Msdx.GLOBAL.out);
		MsdxRecord.show(variable, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> expressionSchema= termSchema.merge(variableSchema);
		MsdxContainer<Object> expression= term.merge(variable);	

		String expectedSchema= 
				/*'''
		{
		  "FIELDS": ["Row", "Column", "Coefficient", "Variable"],
		  "TYPES": ["STRING", "STRING", "DOUBLE", "STRING"]
		}				
				'''*/
		"		{\r\n"
		+ "		  \"FIELDS\": [\"Row\", \"Column\", \"Coefficient\", \"Variable\"],\r\n"
		+ "		  \"TYPES\": [\"STRING\", \"STRING\", \"DOUBLE\", \"STRING\"]\r\n"
		+ "		}\r\n";
			
		String expectedData= 
				/*'''
		["balanceRow", "shipColumn", 1.0, "ship"]				
				'''*/
		"[\"balanceRow\", \"shipColumn\", 1.0, \"ship\"]";
		
		JSONAssert.assertEquals(expectedSchema, MsdxSchema.toNode(expressionSchema).toString(), JSONCompareMode.LENIENT);
		JSONAssert.assertEquals(expectedData, MsdxRecord.toNode(expression, expressionSchema).toString(), JSONCompareMode.LENIENT);
	}//mergingTest
	
	public static void filtering() {
		
		MsdxContainer<Class<?>> schema= MsdxContainer.<Class<?>>builder()
			.addItem("Name", String.class)
			.addItem("Type", String.class)
			.addItem("origin", String.class)
			.addItem("destination", String.class)
			.addItem("Column", String.class)
			.addItem("LowerBound", Double.class)
			.addItem("UpperBound", Double.class)
			.addItem("Value", Double.class)
			.build();
		MsdxRecord variable= MsdxRecord.create(schema, "Ship", "LINEAR", "SFO", "JFK", "ShipColumn", 0.0, 250.0, null);
		
		MsdxContainer<Class<?>> filteredSchema= schema.delete("Name", "Type", "origin", "destination");		
		MsdxContainer<Object> filtered= variable.delete("Name", "Type", "origin", "destination");		
		MsdxSchema.show(filteredSchema, Msdx.GLOBAL.out);			
		MsdxRecord.showAsNode(filtered, filteredSchema, Msdx.GLOBAL.out);			
		
	}//filtering
	
	@Test
	public void filteringTest() throws JSONException {
		
		MsdxContainer<Class<?>> schema= MsdxContainer.<Class<?>>builder()
			.addItem("Name", String.class)
			.addItem("Type", String.class)
			.addItem("origin", String.class)
			.addItem("destination", String.class)
			.addItem("Column", String.class)
			.addItem("LowerBound", Double.class)
			.addItem("UpperBound", Double.class)
			.addItem("Value", Double.class)
			.build();
		MsdxRecord variable= MsdxRecord.create(schema, "Ship", "LINEAR", "SFO", "JFK", "ShipColumn", 0.0, 250.0, null);
		
		MsdxContainer<Class<?>> filteredSchema= schema.delete("Name", "Type", "origin", "destination");		
		MsdxContainer<Object> filtered= variable.delete("Name", "Type", "origin", "destination");		

		String expectedSchema= 
				/*'''
		{
		  "FIELDS": ["Column", "LowerBound", "UpperBound", "Value"],
		  "TYPES": ["STRING", "DOUBLE", "DOUBLE", "DOUBLE"]
		}				
				'''*/
		"		{\r\n"
		+ "		  \"FIELDS\": [\"Column\", \"LowerBound\", \"UpperBound\", \"Value\"],\r\n"
		+ "		  \"TYPES\": [\"STRING\", \"DOUBLE\", \"DOUBLE\", \"DOUBLE\"]\r\n"
		+ "		}\r\n";
			
		String expectedData= 
				/*'''
		["ShipColumn", 0.0, 250.0, null]				
				'''*/
		"[\"ShipColumn\", 0.0, 250.0, null]\r\n";
		
//		System.out.println(expectedSchema);
//		System.out.println(MsdxSchema.toNode(filteredSchema).toString());
		JSONAssert.assertEquals(expectedSchema, MsdxSchema.toNode(filteredSchema).toString(), JSONCompareMode.LENIENT);
//		System.out.println(expectedData);
//		System.out.println(MsdxRecord.toNode(filtered, filteredSchema).toString());
		JSONAssert.assertEquals(expectedData, MsdxRecord.toNode(filtered, filteredSchema).toString(), JSONCompareMode.LENIENT);
	}//filteringTest
	
	public static void renaming() {
		
		String mosdexFile= 
				/*'''
		{
	        "SCHEMA": {
		        "FIELDS":
		          ["Row",         "Column",       "Coefficient"],
		        "TYPES":
		          ["STRING",      "STRING",       "DOUBLE"]
	      	}
		}				
				'''*/
		"	{\r\n" + 
		"        \"SCHEMA\": {\r\n" + 
		"        \"FIELDS\":\r\n" + 
		"          [\"Row\",         \"Column\",       \"Coefficient\"],\r\n" + 
		"        \"TYPES\":\r\n" + 
		"          [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n" + 
		"      	}\r\n" + 
		"	}				\r\n" + 
		"";
			
			MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
			JsonNode file= null;
			try {
				file= Msdx.GLOBAL.mapper.readTree(src.getStream());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		MsdxContainer<Class<?>> termSchema= MsdxSchema.fromNode(file.get("SCHEMA"));
		MsdxContainer<Object> term= MsdxRecord.create(termSchema, "balanceRow", "shipColumn", 1.0);
		Msdx.GLOBAL.out.println("term");
		MsdxSchema.show(termSchema, Msdx.GLOBAL.out);
		MsdxRecord.showAsNode(term, termSchema, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> variableSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Column", String.class)
			.addItem("Variable", String.class)
			.build();
		MsdxContainer<Object> variable= MsdxRecord.create(variableSchema, "shipColumn", "ship");
		Msdx.GLOBAL.out.println("variable");
		MsdxSchema.show(variableSchema, Msdx.GLOBAL.out);
		MsdxRecord.showAsNode(variable, variableSchema, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> expressionSchema= termSchema.merge(variableSchema.renameField("Variable", "Variable1"));
		MsdxContainer<Object> expression= term.merge(variable.renameField("Variable", "Variable1"));
		Msdx.GLOBAL.out.println("expression");
		MsdxSchema.show(expressionSchema, Msdx.GLOBAL.out);	
		MsdxRecord.showAsNode(expression, expressionSchema, Msdx.GLOBAL.out);		
	}//renaming
	
	@Test
	public void renamingTest() throws JSONException {
		
		String mosdexFile= 
				/*'''
		{
	        "SCHEMA": {
		        "FIELDS":
		          ["Row",         "Column",       "Coefficient"],
		        "TYPES":
		          ["STRING",      "STRING",       "DOUBLE"]
	      	}
		}				
				'''*/
		"	{\r\n" + 
		"        \"SCHEMA\": {\r\n" + 
		"        \"FIELDS\":\r\n" + 
		"          [\"Row\",         \"Column\",       \"Coefficient\"],\r\n" + 
		"        \"TYPES\":\r\n" + 
		"          [\"STRING\",      \"STRING\",       \"DOUBLE\"]\r\n" + 
		"      	}\r\n" + 
		"	}				\r\n" + 
		"";
			
			MsdxInputSource src= MsdxInputSource.fromString(mosdexFile);
			JsonNode file= null;
			try {
				file= Msdx.GLOBAL.mapper.readTree(src.getStream());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			
		MsdxContainer<Class<?>> termSchema= MsdxSchema.fromNode(file.get("SCHEMA"));
		MsdxContainer<Object> term= MsdxRecord.create(termSchema, "balanceRow", "shipColumn", 1.0);
		Msdx.GLOBAL.out.println("term");
		MsdxSchema.show(termSchema, Msdx.GLOBAL.out);
		MsdxRecord.show(term, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> variableSchema= MsdxContainer.<Class<?>>builder()
			.addItem("Column", String.class)
			.addItem("Variable", String.class)
			.build();
		MsdxContainer<Object> variable= MsdxRecord.create(variableSchema, "shipColumn", "ship");
		Msdx.GLOBAL.out.println("variable");
		MsdxSchema.show(variableSchema, Msdx.GLOBAL.out);
		MsdxRecord.show(variable, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> expressionSchema= termSchema.merge(variableSchema.renameField("Variable", "Variable1"));
		MsdxContainer<Object> expression= term.merge(variable.renameField("Variable", "Variable1"));

		String expectedSchema= 
				/*'''
		{
		  "FIELDS": ["Row", "Column", "Coefficient", "Variable1"],
		  "TYPES": ["STRING", "STRING", "DOUBLE", "STRING"]
		}				
				'''*/
		"		{\r\n"
		+ "		  \"FIELDS\": [\"Row\", \"Column\", \"Coefficient\", \"Variable1\"],\r\n"
		+ "		  \"TYPES\": [\"STRING\", \"STRING\", \"DOUBLE\", \"STRING\"]\r\n"
		+ "		}\r\n";
			
		String expectedData= 
				/*'''
		["balanceRow", "shipColumn", 1.0, "ship"]				
				'''*/
		"		"
		+ "[\"balanceRow\", \"shipColumn\", 1.0, \"ship\"]\r\n";
		
		JSONAssert.assertEquals(expectedSchema, MsdxSchema.toNode(expressionSchema).toString(), JSONCompareMode.LENIENT);
		JSONAssert.assertEquals(expectedData, MsdxRecord.toNode(expression, expressionSchema).toString(), JSONCompareMode.LENIENT);
		Msdx.GLOBAL.out.println("expression");
		MsdxSchema.show(expressionSchema, Msdx.GLOBAL.out);	
		MsdxRecord.show(expression, Msdx.GLOBAL.out);		
	}//renamingTest
	
	public static void mismatched() {
		MsdxContainer<Class<?>> firstSchema= MsdxContainer.<Class<?>>builder()
				.addItem("Row", String.class)
				.addItem("Column", String.class)
				.addItem("Coefficient", Double.class)
				.addItem("Expression", String.class)
				.build();
		MsdxSchema.show(firstSchema, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> secondSchema= MsdxContainer.<Class<?>>builder()
				.addItem("Row", String.class)
				.addItem("Column", Integer.class)
				.addItem("Coefficient", Double.class)
				.addItem("Formula", String.class)
				.build();
		MsdxSchema.show(secondSchema, Msdx.GLOBAL.out);
		
		Set<Map.Entry<String, Class<?>>> mismatchedItems= firstSchema.mismatchedItems(secondSchema);
		
		Msdx.GLOBAL.out.println("Mismatched Items");
		mismatchedItems.forEach(entry -> Msdx.GLOBAL.out.println(entry.getKey() + ": " + entry.getValue().getSimpleName()));	
	}//mismatched
	
	@Test
	public void mismatchedTest() {
		MsdxContainer<Class<?>> firstSchema= MsdxContainer.<Class<?>>builder()
				.addItem("Row", String.class)
				.addItem("Column", String.class)
				.addItem("Coefficient", Double.class)
				.addItem("Expression", String.class)
				.build();
		MsdxSchema.show(firstSchema, Msdx.GLOBAL.out);
		
		MsdxContainer<Class<?>> secondSchema= MsdxContainer.<Class<?>>builder()
				.addItem("Row", String.class)
				.addItem("Column", Integer.class)
				.addItem("Coefficient", Double.class)
				.addItem("Formula", String.class)
				.build();
		MsdxSchema.show(secondSchema, Msdx.GLOBAL.out);
		
		Set<Map.Entry<String, Class<?>>> mismatchedItems= firstSchema.mismatchedItems(secondSchema);
		Set<Map.Entry<String,Class<?>>> expected= 
			Set.<Map.Entry<String,Class<?>>>of(
				MsdxContainer.newItem("Column", String.class),
				MsdxContainer.newItem("Expression", String.class),
				MsdxContainer.newItem("Column", Integer.class),
				MsdxContainer.newItem("Formula", String.class));

		assertEquals(expected, mismatchedItems);
	}//mismatchedTest
	

}//class MsdxContainerOperations
