/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;

/**
 * The base class for all MOSDEX JSON object readers.
 * The primary methods in this class parse objects, arrays, and values from JSON. 
 * They create nodes of the JSON Tree Model, which is the primary representation of most 
 * of the components of the MOSDEX object model.
 * Customizations are applied through the specialHanding method. 
 * In particular, class-specific overrides of the specialHanding method are used to create Java objects for 
 * the components of the MOSDEX object model that need a more functional representation 
 * than the Tree Model alone can provide: 
 * <ul style="list-style-type:bullet">
 * <li> File</li>
 * <li> Module</li>
 * <li> Table</li>
 * <li> Schema</li>
 * <li> Instance</li>
 * <li> Query</li>
 * </ul> 
 * <p>
 * The element parsers always assume 
 * that the JSON parser is positioned at the starting token of the element; 
 * for elements that are fields of an object, the field name is regarded as part of the parent object, and 
 * the JSON parser is positioned at the token following the field name.
 * This class also provides a number of utility methods for creating various kinds of nodes, 
 * verifying the type of token encountered and determining whether duplicate field names occur in an object.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxReader {
	
	/**
	 * An instance of the Jackson parser. A parser always has a specific input source, set when it is created. 
	 * Calls to the parser always start at the current position in the input source.
	 */
	protected JsonParser parser;
	
	/**
	 * An optional field used to accumulate elements in the specialHanding method.
	 * Should be initialized as an array node or object node in an initialization block.
	 * Can be returned by the getAccumulator method.
	 */
	protected JsonNode accumulator;

	/**
	 * Create a new Reader instance.
	 * @param parser
	 */
	public MsdxReader(JsonParser parser) {
		super();
		this.parser = parser;
	}
	
	/**
	 * Creates a parser with this given input source.
	 * @param src for JSON input
	 * @return a new parser instance
	 */
	public static JsonParser createParser(MsdxInputSource src) {
		JsonParser parser= null;
		try {
			parser = Msdx.GLOBAL.mapper.getFactory().createParser(src.getStream());
			parser.nextToken();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return parser;
	}//createParser

	/**
	 * A string that represents "true".
	 */
	public static final String TRUE_VALUE= Boolean.valueOf(true).toString();

	/**
	 * Gets the current token and checks whether it is expected at that point in the parsing process.
	 * 
	 * @param expected token(s) expected at the parse point
	 * @return the token at the current parse point
	 * @throws IOException if a JSON parsing exception occurs
	 * @see com.fasterxml.jackson.core.JsonParser#nextToken()
	 */
	public JsonToken getCurrentToken(JsonToken... expected) throws IOException {
		JsonToken given= this.parser.getCurrentToken();
		if(given == null)
			throw new JsonParseException(parser, "Unexpected end of input", this.parser.getCurrentLocation());	
		if(!isOneOf(given, expected).equals(TRUE_VALUE))
			throw new JsonParseException(parser, "Expected " + isOneOf(given, expected) + " but got " + given, this.parser.getCurrentLocation());
		return given;
	}//getCurrentToken

	/**
	 * Moves the parse point to the next token and checks whether it is expected at that point in the parsing process.
	 * 
	 * @param expected token()s expected at the parse point
	 * @return the next token at the parse point
	 * @throws IOException if a JSON parsing exception occurs
	 * @throws JsonParseException if a JSON parsing exception occurs
	 * @see com.fasterxml.jackson.core.JsonParser#nextToken()
	 */
	public JsonToken nextToken(JsonToken... expected) throws IOException {
		JsonToken given= this.parser.nextToken();
		if(given == null)
			throw new JsonParseException(parser, "Unexpected end of input", this.parser.getCurrentLocation());	
		if(!isOneOf(given, expected).equals(TRUE_VALUE))
			throw new JsonParseException(parser, "Expected " + isOneOf(given, expected) + " but got " + given, this.parser.getCurrentLocation());
		return given;
	}//nextToken

	/**
	 * Determines whether a given token is one of the expected tokens at a point during the parsing process.
	 * 
	 * @param given the token to be tested
	 * @param expected token(s) at the parse point
	 * @return if the string value of boolean true if the given token is one of the expected tokens 
	 * or else the list of expected tokens as a string (for use in an error message)
	 */
	public static String isOneOf(JsonToken given, JsonToken... expected) {
		boolean matchFound= false;
		StringBuilder expectedAsString= new StringBuilder();
		JsonToken token;
		Iterator<JsonToken> expectedTokens= Arrays.asList(expected).iterator();
		while(!matchFound && expectedTokens.hasNext()) {
			token= expectedTokens.next();
			expectedAsString.append(token.toString());
			if(expectedTokens.hasNext())
				expectedAsString.append(" or ");
			matchFound|= given.equals(token);
		}
		return matchFound ? TRUE_VALUE : expectedAsString.toString();
	}//	isOneOf
	
	/**
	 * Finds duplicate names.
	 * 
	 * @param names items a JSON Object or Array
	 * @param unique a pre-existing set of unique items (empty if absent)
	 * @return a list of duplicate items
	 */
	public static List<String> findDuplicates(Stream<String> names, Set<String> unique) {
		//Defensive copy
		Set<String> uniqueNames= !unique.isEmpty() ? 
			new HashSet<String>(unique) : 
			new HashSet<String>();
		return names
            // Set.add() returns false if the element was already present in the set. 
            // Hence filter such elements 
			.filter(name -> !uniqueNames.add(name))
			.collect(Collectors.toList());
	}
	
	/**
	 * Finds duplicate names.
	 * 
	 * @param items of a JSON Object or Array
	 * @return a list of duplicate items
	 */
	public static List<String> findDuplicates(Stream<String> items) {
		return findDuplicates(items, Collections.emptySet());
	}
	

	/**
	 * This method customizes reading of the JSON element for specific MOSDEX
	 * keywords. It should be overridden in any implementation of the read method of
	 * a MOSDEX object that contains elements requiring special handling; the
	 * default implementation does not invoke any special handling. Normally and by
	 * default, a Reader produces nodes of the JSON tree model corresponding to
	 * MOSDEX objects. However, certain MOSDEX objects also have representations as
	 * Java classes. The specialHandling method provides a means to create those
	 * Java objects as well as their Tree Model nodes. If the JSON element in
	 * question is a field of a JSON object, the keyword is the corresponding field
	 * name; however, if the element is a JSON array component or value, the keyword
	 * is a synthetic identifier that is supplied by the caller. Note that this
	 * method actually reads a JSON element and stores the result in some instance
	 * field of the Java object being constructed. The return value from this method
	 * is an indicator of whether or not the element was in fact processed by the
	 * method. If it was not, a null value is returned, indication that a default
	 * processor should be used.
	 * <p>
	 * Normally, the overridden version of this method processes a single JSON element. 
	 * However, in some cases it may be desirable to accumulate the elements processed 
	 * over multiple invocations (for example, when the individual elements are components of a list). 
	 * You can define an optional accumulator field in the specialHandling method 
	 * to accumulate elements over multiple invocations. You should initialized it 
	 * as an array node or object node in an initialization block.
	 * The accumulator can be returned by the getAccumulator method.
	 * 
	 * @param keyWord of the element requiring customized parsing
	 * @return a node if the element is one of the ones requiring special handling,
	 *   null if not; typically if null is returned, the reader uses a default
	 *   parsing for the element
	 * @throws IOException if a JSON parsing exception occurs
	 */
	protected JsonNode specialHandling(String keyWord) throws IOException {
		return null;
	}//specialHandling
	
	/**@return a copy of the accumulator field*/
	public JsonNode copyAccumulator() {
		return accumulator.deepCopy();
	}

	/**
	 * This method recursively parses JSON objects. It first verifies that the
	 * parser is positioned at a object start token. It then alternately gets the
	 * field name and then parses the the JSON object, array, or value associated
	 * with that field.
	 * <p>
	 * Since this method is used whenever the parser encounters a JSON object,
	 * customized code is placed within it to deal with certain specific kinds of
	 * elements that arise in MOSDEX: these situations are handled by the
	 * specialHandling method.
	 * 
	 * @return a JSON object node
	 * @throws IOException if a JSON parsing exception occurs
	 */
	public ObjectNode objectFromJson() throws IOException {
		getCurrentToken(JsonToken.START_OBJECT);
		ObjectNode node= createObjectNode();
		JsonNode special;
		JsonToken token;
		String fieldName;
		
		token= nextToken(JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
		while(!this.parser.getCurrentToken().equals(JsonToken.END_OBJECT)) {
			fieldName= this.parser.getCurrentName();
			token= nextToken(JsonToken.START_OBJECT, 
				JsonToken.START_ARRAY, 
				JsonToken.VALUE_NULL, 
				JsonToken.VALUE_NUMBER_FLOAT,
				JsonToken.VALUE_NUMBER_INT,
				JsonToken.VALUE_STRING,
				JsonToken.END_OBJECT);
			// This section of the code customizes the behavior of the parser 
			// depending upon what kind of element is being processed.
			special= specialHandling(fieldName);			
			if(special != null) {
				node.set(fieldName, special);
			}
			//Default processing of the element
			else if(token.equals(JsonToken.START_OBJECT)) {
				node.set(fieldName, objectFromJson());
			}
			else if(token.equals(JsonToken.START_ARRAY)) {
				node.set(fieldName, arrayFromJson(fieldName));
			}
			else if(!token.equals(JsonToken.END_OBJECT))
				node.set(fieldName, valueFromJson(token, fieldName));
			
			token= nextToken(JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
		}//while	
		return node;
	}//objectFromJson
	
	/**
	 * This method recursively parses a JSON array, returning the array as an
	 * iterator of JSON nodes. It first verifies that the parser is positioned at an
	 * array start token. It then parses each array element as a JSON object, array,
	 * or value. 
	 * <p>
	 * Since this method is used whenever the parser encounters a JSON
	 * array, customized code is placed within it to deal with certain specific
	 * kinds of elements that arise in MOSDEX: these situations are handled by the
	 * specialHandling method. These special handling customizations are provided to
	 * the element processing, as in the object parser, by the use of a synthetic
	 * keyword in place of an object field name.
	 * 
	 * @param syntheticKey indicates the kind of array element being processed
	 * @return an iterator over JSON nodes
	 * @throws IOException if a JSON parsing exception occurs
	 */
	public Iterator<JsonNode> iteratorFromJson(String syntheticKey) throws IOException {
		return new Iterator<JsonNode>() {
			JsonToken token;	
			
			{//Initialization
				getCurrentToken(JsonToken.START_ARRAY);
				token= nextToken(
					JsonToken.START_OBJECT, 
					JsonToken.START_ARRAY, 
					JsonToken.VALUE_NULL, 
					JsonToken.VALUE_NUMBER_FLOAT,
					JsonToken.VALUE_NUMBER_INT,
					JsonToken.VALUE_STRING,
					JsonToken.END_ARRAY);
			}				
	
			@Override
			public boolean hasNext() {
				return !token.equals(JsonToken.END_ARRAY);
			}//hasNext
	
			@Override
			public JsonNode next() {
				JsonNode node = null;
				try {
					JsonNode special= specialHandling(syntheticKey);
					if(special != null) {
						node= special;
					}
					//Default processing of the element
					else if(token.equals(JsonToken.START_OBJECT))
						node= objectFromJson();
					else if(token.equals(JsonToken.START_ARRAY))
						node= arrayFromJson(null);
					else if(!token.equals(JsonToken.END_ARRAY))
						node= valueFromJson(token, null);
					
					token= nextToken(JsonToken.START_OBJECT, 
						JsonToken.START_ARRAY, 
						JsonToken.VALUE_NULL, 
						JsonToken.VALUE_NUMBER_FLOAT,
						JsonToken.VALUE_NUMBER_INT,
						JsonToken.VALUE_STRING,
						JsonToken.END_ARRAY);			
				} catch (IOException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return node;
			}//next
			
		}/*Iterator*/;//nodes
	}//iteratorFromJson
	
	/**
	 * This method returns an array node by accumulating the nodes of an iterator. 
	 * <p>
	 * This method recursively parses a JSON array, returning the array as an
	 * iterator of JSON nodes. It first verifies that the parser is positioned at an
	 * array start token. It then parses each array element as a JSON object, array,
	 * or value. 
	 * <p>
	 * Since this method is used whenever the parser encounters a JSON
	 * array, customized code is placed within it to deal with certain specific
	 * kinds of elements that arise in MOSDEX: these situations are handled by the
	 * specialHandling method. These special handling customizations are provided to
	 * the element processing, as in the object parser, by the use of a synthetic
	 * keyword in place of an object field name.
	 * 
	 * @param syntheticKey indicates the kind of array element being processed
	 * @return a JSON array node
	 * @throws IOException if a JSON parsing exception occurs
	 */
	public ArrayNode arrayFromJson(String syntheticKey) throws IOException {
		ArrayNode node= createArrayNode();
		Iterator<JsonNode> nodes= iteratorFromJson(syntheticKey);
		while(nodes.hasNext()) {
			node.add(nodes.next());
		}//while	
		return node;	
	}//arrayFromJson

	/**
	 * Returns an array as a Java stream of JSON Nodes.
	 * <p>
	 * This method recursively parses a JSON array, returning the array as an
	 * iterator of JSON nodes. It first verifies that the parser is positioned at an
	 * array start token. It then parses each array element as a JSON object, array,
	 * or value. 
	 * <p>
	 * Since this method is used whenever the parser encounters a JSON
	 * array, customized code is placed within it to deal with certain specific
	 * kinds of elements that arise in MOSDEX: these situations are handled by the
	 * specialHandling method. These special handling customizations are provided to
	 * the element processing, as in the object parser, by the use of a synthetic
	 * keyword in place of an object field name.
	 * 
	 * @param syntheticKey indicates the kind of array element being processed
	 * @return a stream of JSON nodes
	 * @throws IOException if a JSON parsing exception occurs
	 */
	public Stream<JsonNode> streamFromJson(String syntheticKey) throws IOException {
		return StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(iteratorFromJson(syntheticKey), 0), 
			false);
	}//streamFromJson
	
	/**
	 * This method parses JSON values.
	 * It identifies the kind of value from the JSON token and calls the appropriate 
	 * mapper method to read the node.
	 * The kinds of values supported are null, floating point numbers, integers, and strings.
	 * Since this method is used whenever the parser encounters a JSON value, 
	 * customized code is placed within it to deal with certain specific kinds of elements that arise in MOSDEX:
	 * these situations are handled by the specialHandling method.
	 * These special handling customizations are provided to the element processing, as in the 
	 * object parser, by the use of a synthetic keyword in place of an object field name.
	 * <p>
	 * This method applies only to value fields in a MOSDEX object or array;
	 * it does not parse the data items in a Record, 
	 * which are instead handled by the MsdxRecord.readItem method. 
	 * Thus, no schema is applied when parsing valueFromJson.
	 * 
	 * @param valueToken
	 * @param syntheticKey indicates the kind of value element being processed
	 * @return a JSON node
	 * @throws IOException if a JSON parsing exception occurs
	 */
	public JsonNode valueFromJson(JsonToken valueToken, String syntheticKey) throws IOException {
		JsonNode special;
		getCurrentToken(JsonToken.VALUE_NULL, 				
			JsonToken.VALUE_NUMBER_FLOAT,
			JsonToken.VALUE_NUMBER_INT,
			JsonToken.VALUE_STRING
		);
		special= specialHandling(syntheticKey);
		if(special != null) {
			return special;
		}
		//Default processing of the element
		if(valueToken.equals(JsonToken.VALUE_NULL))
			return NullNode.getInstance();
		if(valueToken.equals(JsonToken.VALUE_NUMBER_FLOAT))
			return createNumberNode(this.parser.getDoubleValue());
		if(valueToken.equals(JsonToken.VALUE_NUMBER_INT))
			return createNumberNode(this.parser.getIntValue());
		if(valueToken.equals(JsonToken.VALUE_STRING))
			return createTextNode(this.parser.getText());
		throw new IllegalArgumentException("Unsupported type " + valueToken.toString());
	}//valueFromJson
	
	/**@return a numeric integer node*/
	public static ValueNode createNumberNode(int value) {
		return Msdx.GLOBAL.mapper.getNodeFactory().numberNode(value);
	}

	/**@return a numeric double node*/
	public static ValueNode createNumberNode(double value) {
		return Msdx.GLOBAL.mapper.getNodeFactory().numberNode(value);
	}

	/**@return a string node*/
	public static TextNode createTextNode(String text) {
		return Msdx.GLOBAL.mapper.getNodeFactory().textNode(text);
	}

	/**@return a null node (not a Java null)*/
	public static NullNode createNullNode() {
		return Msdx.GLOBAL.mapper.getNodeFactory().nullNode();
	}

	/**@return an empty array node*/
	public static ArrayNode createArrayNode() {
		return Msdx.GLOBAL.mapper.getNodeFactory().arrayNode();
	}

	/**@return an empty object node*/
	public static ObjectNode createObjectNode() {
		return Msdx.GLOBAL.mapper.getNodeFactory().objectNode();
	}
	
	
}//class MsdxReader
