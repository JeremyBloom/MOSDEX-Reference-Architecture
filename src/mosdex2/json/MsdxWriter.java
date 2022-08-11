/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;

/**
 * The base class for all MOSDEX object writers.
 * The primary methods in this class writes objects, arrays, and values as JSON. 
 * They write nodes of the JSON Tree Model, which is the primary representation of most 
 * of the components of the MOSDEX object model.
 * Customizations are applied through the specialHanding method. 
 * In particular, class-specific overrides of the specialHanding method are used to write Java objects for 
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
 * A JSON Object is always written so that the individual field entries are placed on separate lines and indented.
 * A JSON Array may be written inline, with all entries on the same line, if the individual values are primitives that are not too long; 
 * otherwise, it is written so that the individual entries are placed on separate lines and indented.
 * <p>
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxWriter {
	
	/**
	 * An instance of the JSON generator. This class does not use the Jackson  generator, in order to provide MOSDEX-specific pretty printing format. 
	 * A generator always has a specific output destination, set when it is created. 
	 * Calls to the generator always start from the current position in the output destination.
	 */
	protected MsdxWriter.Generator generator;
	
	/**
	 * Create a new Writer instance.
	 * @param generator 
	 */
	public MsdxWriter(MsdxWriter.Generator generator) {
		super();
		this.generator = generator;
	}

	/**
	 * This method customizes writing of the JSON element for specific MOSDEX keywords. 
	 * It should be overridden in any implementation of the write method of a MOSDEX object 
	 * that contains elements requiring special handling; the default implementation does not invoke any special handling.  
	 * Normally and by default, a Writer reproduces nodes of the JSON tree model corresponding to 
	 * MOSDEX objects. However, certain MOSDEX objects also have representations as Java classes. 
	 * The specialHandling method provides a means to write those Java objects as JSON.
	 * If the JSON element in question is a field of a JSON object, the keyword is the corresponding field name; 
	 * however, if the element is a JSON array component or value, the keyword is a synthetic identifier 
	 * that is supplied by the caller.
	 * <p>
	 * Note that this method actually writes a JSON element from 
	 * some instance field of the Java object being written. 
	 * The return value from this method is an 
	 * indicator of whether or not the element was in fact processed by the method. If it was not, 
	 * a false value is returned, indicating that a default processor should be used.
	 * 
	 * @param node corresponding the the current element
	 * @param keyword of the element requiring customized generation 
	 * @return true if the element is one of the ones requiring special handling, false if not; 
	 * typically if false is returned, the writer uses a default generation for the element
	 * @throws IOException if a JSON generation exception occurs
	 */
	protected boolean specialHandling(JsonNode node, String keyword) throws IOException {
		return false;
	}//specialHandling
	
	/**
	 * Flags the next member to be written as an inline array (no linefeeds between values).
	 * An array can be written inline if all the values are primitives (number or short string).
	 * If any value is an object or array or if it is a long string, the array will be written 
	 * with linefeed and indent separating the values.
	 * 
	 * @param member
	 * @return true if the element can be written inline, false otherwise
	 */
	public boolean canWriteInline(JsonNode member) {
		if(member.isObject())
			return false;
		else
			return (member.size() == 0) || (hasAllPrimitives(member) && hasAllShortStrings(member));
	}//canWriteInline

	/**
	 * Checks whether an array has all primitive values (numbers or strings).
	 * 
	 * @param member the array to be checked for privative values
	 * @return true if all values are primitives, false if any value is an object or array
	 */
	protected static boolean hasAllPrimitives(JsonNode member) {
		if(!member.isArray())
			return false;
		Stream<JsonNode> nodes= StreamSupport.stream(Spliterators.spliteratorUnknownSize(member.elements(), 0), false);
		return nodes.allMatch(node -> node.isValueNode());
	}

	/**
	 * Checks whether an array has any long strings (25 or more characters).
	 * Adjust the criterion for long strings here.
	 * 
	 * @param member the array to be checked for privative values
	 * @return true if any values is a long string, false otherwise
	 */
	protected static boolean hasAllShortStrings(JsonNode member) {
		if(!member.isArray())
			return false;
		Stream<JsonNode> nodes= StreamSupport.stream(Spliterators.spliteratorUnknownSize(member.elements(), 0), false);
		return nodes
			.filter(node -> node.isTextual())
			.allMatch(node -> node.asText().length() <= 25);
	}

	/**
	 * This method recursively generates JSON objects. It first verifies that the
	 * node is an object. It then alternately writes the
	 * field name and then generates the JSON object, array, or value associated
	 * with that field.
	 * <p>
	 * Since this method is used whenever the writer encounters a JSON object,
	 * customized code is placed within it to deal with certain specific kinds of
	 * elements that arise in MOSDEX: these situations are handled by the
	 * specialHandling method.
	 * 
	 * 
	 * @param node to be written
	 * @throws IOException if a JSON generation exception occurs
	 */
	public void objectToJson(JsonNode node) throws IOException {
		if(!node.isObject())
			throw new IllegalArgumentException("Not a JSON object");
		
		Map.Entry<String,JsonNode> field= null;
		
		for(Iterator<Map.Entry<String,JsonNode>> fields= node.fields(); fields.hasNext(); ) {
			field= fields.next();
	
			if(field.getValue().isObject()) {
				this.generator.writeFieldName(field.getKey());
				this.generator.writeStartObject(field.getValue().size()==0);
				
				if(!specialHandling(field.getValue(), field.getKey())) {
					//Default processing of the element
					objectToJson(field.getValue());
				}
				//else no further action
				this.generator.writeEndObject(field.getValue().size()==0);
			}
			else if(field.getValue().isArray()) {
				this.generator.writeFieldName(field.getKey());
				this.generator.writeStartArray(canWriteInline(field.getValue()));
				if(!specialHandling(field.getValue(), field.getKey())) {
					//Default processing of the element
					arrayToJson(field.getValue(), field.getKey());
				}
				//else no further action
				this.generator.writeEndArray(canWriteInline(field.getValue()));
			}
			else if(field.getValue().isValueNode()) {
				if(!specialHandling(field.getValue(), field.getKey())) {
					//Default processing of the element
					this.generator.writeFieldName(field.getKey());
					valueToJson(field.getValue(), field.getKey());
				}
				//else no further action
			}				
			else
				throw new IllegalArgumentException("Unsupported type " + node.getNodeType().toString());
			
			if(fields.hasNext())
				this.generator.writeObjectEntrySeparator();
			
		}//for fields	
	}//objectToJson

	/**
	 * This method recursively writes JSON arrays. It first verifies that the
	 * node is an array. It then writes each element (JSON object, array, or value).
	 * <p>
	 * Since this method is used whenever the writer encounters a JSON array,
	 * customized code is placed within it to deal with certain specific kinds of
	 * elements that arise in MOSDEX: these situations are handled by the
	 * specialHandling method.
	 * 
	 * @param node to be written
	 * @param syntheticKey indicates the kind of array element being processed
	 * @throws IOException if a JSON generation exception occurs
	 */
	public void arrayToJson(JsonNode node, String syntheticKey) throws IOException {
		if(!node.isArray())
			throw new IllegalArgumentException("Not a JSON array");
	
		JsonNode element;
		for (Iterator<JsonNode> elements= node.elements(); elements.hasNext(); ) {
			element= elements.next();
			
			if(element.isObject()) {
				this.generator.writeStartObject(element.size()==0);
				if(!specialHandling(element, syntheticKey)) {
					//Default processing of the element
					objectToJson(element);
				}
				//else no further action
				this.generator.writeEndObject(element.size()==0);
			}
			else if(element.isArray()) {
				this.generator.writeStartArray(canWriteInline(element));
				if(!specialHandling(element, syntheticKey)) {
					//Default processing of the element
					arrayToJson(element, syntheticKey);
				}
				//else no further action
				this.generator.writeEndArray(canWriteInline(element));
			}
			else if(element.isValueNode()) {
				if(!specialHandling(element, syntheticKey)) {
					//Default processing of the element
					valueToJson(element, syntheticKey);
				}
				//else no further action
			}				
			else
				throw new IllegalArgumentException("Unsupported type " + node.getNodeType().toString());
			
			if(elements.hasNext())
				this.generator.writeArrayValueSeparator(canWriteInline(node));
			
		}//for elements
	}//arrayToJson
	
	/**
	 * This method writes JSON values.
	 * It identifies the kind of value from the JSON node and calls the appropriate 
	 * mapper method to write the node.
	 * The kinds of values supported are null, floating point numbers, integers, and strings.
	 * Since this method is used whenever the writer encounters a JSON value, 
	 * customized code is placed within it to deal with certain specific kinds of elements that arise in MOSDEX:
	 * these situations are handled by the specialHandling method.
	 * These special handling customizations are provided to the element processing, as in the 
	 * object parser, by the use of a synthetic keyword in place of an object field name.
	 * <p>
	 * This method applies only to value fields in a MOSDEX object or array;
	 * it does not write the data items in a record, 
	 * which are instead handled by the MsdxObject.Factory.writeItem method. 
	 * Thus, no schema is applied when parsing valueFromJson.
	 * 
	 * @param node
	 * @param syntheticKey indicates the kind of value element being processed
	 * @throws IOException if a JSON generation exception occurs
	 */
	public void valueToJson(JsonNode node, String syntheticKey) throws IOException {
		if(!node.isValueNode())
			throw new IllegalArgumentException("Not a JSON value");

		if(node.isNull()) {
			if(!specialHandling(node, syntheticKey)) {
				//Default processing of the element
				this.generator.writeNull();
			}
			//else no further action
		}
		else if(node.isDouble()) {
			if(!specialHandling(node, syntheticKey)) {
				//Default processing of the element
				this.generator.writeNumber(node.asDouble());
			}
			//else no further action
		}
		else if(node.isInt()) {
			if(!specialHandling(node, syntheticKey)) {
				//Default processing of the element
				this.generator.writeNumber(node.asInt());
			}
			//else no further action
		}
		else if(node.isTextual()) {
			if(!specialHandling(node, syntheticKey)) {
				//Default processing of the element
				this.generator.writeString(node.asText());
			}
			//else no further action
		}
		else
			throw new IllegalArgumentException("Unsupported type " + node.getNodeType().toString());
		return;
	}//valueToJson

	/**
	 * Writes MOSDEX elements as JSON to an output destination.
	 * Behaves like the Jackson JsonGenerator but with less restrictions on pretty printing. 
	 * The pretty printing functions are incorporated into the write methods of this class.
	 * <p>
	 * A JSON Object is always generated so that the individual field entries are placed on separate lines and indented.
	 * A JSON Array may be generated inline, with all entries on the same line, if the individual values are primitives that are not too long; 
	 * otherwise, it is generated so that the individual entries are placed on separate lines and indented.
	 * <p>
	 * Note that this class does not use the Jackson generator or pretty printer, 
	 * which proved too difficult to customize; nevertheless, it generally follows 
	 * their conventions. 
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Generator {
		
		/**Used to separate values.*/
	    public final static String VALUE_SEPARATOR = " ";
	    
		/**Used to insert a newline.*/
		private static final String LINEFEED = System.getProperty("line.separator");
		
		/**Used to insert two spaces for each level of indent.*/
		private static final String WHITESPACE= "  ";
		
		/**The destination for JSON output.*/
		private MsdxOutputDestination dst;
		
		/**The current indent level.*/
		private int level;
	
		/**
		 * Creates a new Generator instance.
		 * Prefer using the static create method.
		 * @param dst destination for JSON output
		 */
		protected Generator(MsdxOutputDestination dst) {
			super();
			this.dst = dst;
			this.level = 0;
		}
		
		/**
		 * @param dst destination for JSON output
		 * @return a new Generator instance 
		 */
		public static MsdxWriter.Generator create(MsdxOutputDestination dst) {
			return new MsdxWriter.Generator(dst);
		}
		
		/**
		 * Generates an indent at the current level.
		 * @throws IOException if a JSON generation exception occurs
		 */
		private void indent() throws IOException {
			if(level<0)
				throw new IllegalStateException("Negative level");
			writeRaw(WHITESPACE.repeat(level));
		}
		
		/**
		 * Generates a new line.
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void linefeed() throws IOException {
			writeRaw(LINEFEED);		
		}
		
		/**
		 * Writes debugging information.
		 * Uncomment the wrteRaw line to use.
		 * 
		 * @param text message to be printed
		 * @throws IOException if a JSON generation exception occurs
		 */
		protected void annotate(String text) throws IOException {
			String annotation= "<" + text + level + ">";
//			writeRaw(annotation);
		}
		
		/**
		 * Generates a single character.
		 * @param c
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeRaw(char c) throws IOException {
			this.dst.write(c);
		}

		/**
		 * Generates a string.
		 * @param text
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeRaw(String text) throws IOException {
			this.dst.write(text.getBytes());
		}

		/**
		 * Generates separator between JSON elements.
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeRootValueSeparator() throws IOException {
			writeRaw(VALUE_SEPARATOR);
		}

		/**
		 * Generates the JSON array start character '['.
		 * @param writeInline true if no new line is generated after the array start 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeStartArray(boolean writeInline) throws IOException {
			annotate("SA" + (writeInline ? "L" : ""));
			writeRaw("[");
			beforeArrayValues(writeInline);
		}
	
		/**
		 * Generates a leading new line and indent before the first array value.
		 * @param writeInline true if no new line and indent is generated after the array start 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void beforeArrayValues(boolean writeInline) throws IOException {
			annotate("BV" + (writeInline ? "L" : ""));
			if(!writeInline) {
				linefeed();
				level++;
				indent();
			}
			else {
				writeRootValueSeparator();
			}
		}

		/**
		 * Generates a new line and indent between array values.
		 * @param writeInline true if no new line and indent is generated between array values 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeArrayValueSeparator(boolean writeInline) throws IOException {
			writeRaw(",");
			if(!writeInline) {
				annotate("AS");
				linefeed();
				indent();
			}
			else {
				writeRootValueSeparator();
				annotate("ASL");
			}
		}

		/**
		 * Generates the JSON array end character ']'.
		 * @param writeInline true if no new line is generated before the array end 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeEndArray(boolean writeInline) throws IOException {
			if(!writeInline) {
				linefeed();
				level--;
				indent();
			}
			else {
				writeRootValueSeparator();
			}
			writeRaw("]");
			annotate("EA" + (writeInline ? "L" : ""));
		}
	
		/**
		 * Generates the JSON object start character '{'.
		 * @param empty true if the object is empty, in which case it is written inline 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeStartObject(boolean empty) throws IOException {
			annotate("SO");
			writeRaw("{");
			if(!empty) {
				beforeObjectEntries();
			}
		}

		/**
		 * Generates a leading new line and indent before the first object entry.
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void beforeObjectEntries() throws IOException {
			linefeed();
			level++;
			indent();
		}

		/**
		 * Generates a field name surrounded by '"'.
		 * @param name of the field
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeFieldName(String name) throws IOException {
			writeRaw("\"");
			writeRaw(name);
			writeRaw("\"");
			writeObjectFieldValueSeparator();
		}

		/**
		 * Generates a ':' separator following a field name.
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeObjectFieldValueSeparator() throws IOException {
			annotate("FS");
			writeRaw(": ");
		}

		/**
		 * Generates a new line and indent between object entries.
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeObjectEntrySeparator() throws IOException {
			writeRaw(",");
			annotate("ES");			
			linefeed();
			indent();
		}

		/**
		 * Generates the JSON object end character '}'.
		 * @param empty true if the object is empty, in which case it is written inline 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeEndObject(boolean empty) throws IOException {
			if(!empty) {
				linefeed();
				level--;
				indent();
			}
			else
				writeRootValueSeparator();
			writeRaw("}");
			annotate("EO");	        
		}
	
		/**
		 * Generates a string surrounded by '"'.
		 * @param text
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeString(String text) throws IOException {
			writeRaw("\"");
			writeRaw(text);
			writeRaw("\"");
		}
	
		/**
		 * Generates an integer number.
		 * @param v
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(int v) throws IOException {
			writeRaw(String.valueOf(v));
		}
	
		/**
		 * Generates an integer number.
		 * @param v
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(long v) throws IOException {
			writeRaw(String.valueOf(v));
		}
	
		/**
		 * Generates an integer number.
		 * @param v
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(BigInteger v) throws IOException {
			writeRaw(String.valueOf(v));
		}
	
		/**
		 * Generates a floating point number.
		 * @param v
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(double v) throws IOException {
			writeRaw(String.valueOf(v));
		}
	
		/**
		 * Generates a floating point number.
		 * @param v
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(float v) throws IOException {
			writeRaw(String.valueOf(v));
		}
	
		/**
		 * Generates a floating point number.
		 * @param v
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(BigDecimal v) throws IOException {
			writeRaw(String.valueOf(v));
		}
	
		/**
		 * Generates a number.
		 * @param encodedValue string representation of a number
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNumber(String encodedValue) throws IOException {
			writeRaw(encodedValue);
		}
	
		/**
		 * Generates a boolean value (not used in MOSDEX).
		 * @param state
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeBoolean(boolean state) throws IOException {
			writeRaw(String.valueOf(state));
		}
	
		/**
		 * Generates the value "null";
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void writeNull() throws IOException {
			writeRaw("null");
		}
	
		/**
		 * Flushes the output destination and forces any buffered output bytes to be written out.
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void flush() throws IOException {
			dst.flush();
		}
	
		/**
		 * Closes the output destination and releases any associated system resources. 
		 * @throws IOException if a JSON generation exception occurs
		 */
		public void close() throws IOException {
			dst.close();
		}

	}//class MsdxWriter.Generator


}//class MsdxWriter
