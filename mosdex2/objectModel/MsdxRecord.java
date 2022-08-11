/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxFunctionCall;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;

/**
 * Represents a Record in MOSDEX. 
 * Record has no visibility in the MOSDEX object model, and thus, it does not inherit from MsdxObject and 
 * has no parent reference.
 * <p>
 * Instead, Record inherits from Container of Object and serves a fundamental role as a general purpose data carrier 
 * in Dataframe and Span as well as Instance. It holds heterogeneous objects 
 * that are used the bridging stream operations for creating solver-specific modeling objects. This class provides 
 * uniformity in handling those streams while avoiding creating many context-specific classes.
 * <p>
 * Data items in a Record cannot be used without its accompanying Schema, 
 * the fields of which specify the class identity of each item. In this respect, 
 * it follows the concept of a typesafe heterogeneous container developed by J. Bloch in his book 
 * <a href="https://www.oreilly.com/library/view/effective-java/9780134686097/"> Effective Java (item 33)</a>.
 * Without this concept, MOSDEX would require the user to create Java classes for each table
 * and solver-specific modeling object, a significant programming burden. 
 * <p>
 * Usually, a Record does not have its own Schema but instead references 
 * the Schema of the Table, Instance, Dataframe, Span, or stream that 
 * contains it. This avoids duplication of objects and prevents Records of the same 
 * containing structure from having different schemas.
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxRecord extends MsdxContainer<Object> {
	//Note MsdxRecord does not inherit from MsdxObject, and hence has no parent object.

	private static final long serialVersionUID = -8316440229258565102L;
	
	/**
	 * Defines the names and data types of this Record's fields.
	 * Note: Record uses MsdxContainer<Object> rather than MsdxSchema so that 
	 * it can be used in MsdxSpan and related classes that do not inherit from MsdxObject.
	 */
	private MsdxContainer<Class<?>> recordSchema;

	/**
	 * Constructs a new Record object.
	 * Using a Record constructor is strongly discouraged.
	 * The preferred way to create a Record is to use the Builder.
	 * 
	 * @param items the Record content
	 * @param tableSchema of the containing Table, Instance, Dataframe, or Span
	 */
	protected MsdxRecord(MsdxContainer<Object> items, MsdxContainer<Class<?>> tableSchema) {
		super(items);
		if(tableSchema==null || tableSchema.isEmpty())
			throw new IllegalArgumentException("Table schema has not been defined");
		this.recordSchema = tableSchema;
	}

	protected MsdxRecord(Map<String, Object> items, MsdxContainer<Class<?>> recordSchema) {
		this(new MsdxContainer<Object>(items), recordSchema);
	}

	/**Constructs an empty Record with a null Schema.*/
	protected MsdxRecord() {
		this(Collections.emptyMap(), null);
	}

	/**@return the Schema of this Record*/
	public MsdxContainer<Class<?>> getSchema() {
		return recordSchema;
	}

	/**@return a new Record Builder using the given Schema*/
	public static MsdxRecord.Builder builder(MsdxContainer<Class<?>> recordSchema) {
		return new MsdxRecord.Builder(recordSchema);
	}
	
	/**@return a new Record Builder using the given Schema*/
	public static MsdxRecord.Builder builder(MsdxSchema tableSchema) {
		return new MsdxRecord.Builder(tableSchema);
	}
	
	/**@return a new Record from the given JSON node using the given Schema*/
	public static MsdxRecord fromNode(JsonNode recordNode, MsdxContainer<Class<?>> recordSchema) {
		if(!recordNode.getNodeType().equals(JsonNodeType.ARRAY))
			throw new IllegalArgumentException("Expected array node but got " + recordNode.getNodeType().name());
		if(recordNode.size()==0) {
	 		return new MsdxRecord(); //empty record
	 	};
	 	
		MsdxRecord.Builder record= MsdxRecord.builder(recordSchema);
		String fieldName;
		Iterator<String> fieldNames= recordSchema.itemNames().iterator();
		Iterator<JsonNode> items= recordNode.elements();
		while(items.hasNext()) {
			if(!fieldNames.hasNext())
				throw new IllegalArgumentException("Record has more items than its schema has fields");
			fieldName= fieldNames.next();
			record.addItem(fieldName, MsdxRecord.readItem(items.next(), recordSchema.get(fieldName)));			
		}
		if(fieldNames.hasNext())
			throw new IllegalArgumentException("Schema has more fields than record has items");
		return record.build();
	}//fromNode
	
	/**
	 * Matches a valid double as a string.
	 * Used in the readItem method.
	 */
	protected static final Pattern DOUBLE_PATTERN = Pattern.compile(
	    "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
	    "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|" +
	    "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
	    "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*"
	    );
	
	/**Defines various ways to represent infinity as a string; used in the readItem method.*/
	protected static final Set<String> INFINITY= Set.of("Infinity", "INFINITY", "infinity");

	/**Defines various ways to represent -infinity as a string; used in the readItem method.*/
	protected static final Set<String> NEGATIVE_INFINITY= Set.of("-Infinity", "-INFINITY", "-infinity");

	/**
	 * Parses the items comprising a Record from JSON.
	 * Assigns each item to its appropriate type according to the schema.
	 * <p>
	 * Converts a string representing a nonstandard numeric value to a numeric value if called for by the Schema. 
	 * JSON can parse certain primitive values, notably strings, integers, and floating point numbers, 
	 * but it does not handle several types that are needed by MOSDEX. These include infinite values for integers and doubles and 
	 * hex strings representing floating point values. This method provides the needed conversions. 
	 * In particular:
	 * The quoted strings "Infinity" or "-Infinity" (or uncapitalized or all capitals versions) 
	 * become Integer.MAX_VALUE or Integer.MIN_VALUE for integers and 
	 * become Double.POSITIVE_INFINITY or Double.NEGATIVE_INFINITY for doubles.
	 * A quoted string that parses as an IEEE formatted floating number becomes an IEEEDouble or an ordinary double.
	 * Unquoted strings that do not parse as a JSON primitive cause a parser exception.
	 * <p> This method also creates function calls from call strings.
	 * 
	 * @param itemNode 
	 * @param fieldType class of the item in the Schema
	 * @return a Java object with underlying identity of the appropriate Class
	 * @throws NumberFormatException on a Double or IEEEDouble field if the item string is not valid
	 */
	public static Object readItem(JsonNode itemNode, Class<?> fieldType) {
		Object item= null;
		if(itemNode.isNull())
			item= null;			
		else if(fieldType.equals(Integer.class)) {
			if(INFINITY.contains(itemNode.asText()))
				item= Integer.MAX_VALUE; 
			else if(NEGATIVE_INFINITY.contains(itemNode.asText()))
				item= Integer.MIN_VALUE;		
			else	
				item= Integer.valueOf(itemNode.asInt());
		}
		//Note: IEEEDouble is a convenience class that is identical to ordinary Double 
		//but which on output is formatted as hexadecimal in conformance with IEEE Standard
		else if(fieldType.equals(IEEEDouble.class)) {
			if(INFINITY.contains(itemNode.asText()))
				item= Double.POSITIVE_INFINITY; 
			else if(NEGATIVE_INFINITY.contains(itemNode.asText()))
				item= Double.NEGATIVE_INFINITY;		
			else if(DOUBLE_PATTERN.matcher(itemNode.asText()).matches()) {
				item= IEEEDouble.valueOf(itemNode.asText());	//correctly parses Infinity and -Infinity without enclosing quotes
			}
			else
				throw new NumberFormatException("Invalid as IEEE double " + itemNode.asText());
		}
		else if(fieldType.equals(Double.class)) {
			if(INFINITY.contains(itemNode.asText()))
				item= Double.POSITIVE_INFINITY; 
			else if(NEGATIVE_INFINITY.contains(itemNode.asText()))
				item= Double.NEGATIVE_INFINITY;		
			else if(DOUBLE_PATTERN.matcher(itemNode.asText()).matches()) {
				item= Double.valueOf(itemNode.asText());	//correctly parses Infinity and -Infinity
			}
			else
				throw new NumberFormatException("Invalid as double " + itemNode.asText());
		}
		else if(fieldType.equals(String.class)) { 
			item= new String(itemNode.asText());						
		}
		else if(MsdxFunctionCall.class.isAssignableFrom(fieldType)) {
			item= MsdxFunctionCall.create(itemNode.asText(), MsdxFunctionCall.getResultTypeFor(fieldType));
		}
		else
			throw new IllegalArgumentException("Unsupported type " + itemNode.asText());
		return item; 
	}//readItem

	/**@return a JSON Tree model node representation of the given record Container with the given Schema*/
	public static ArrayNode toNode(MsdxContainer<Object> record, MsdxContainer<Class<?>> recordSchema) {
		ArrayNode recordNode= MsdxReader.createArrayNode();
		Class<?> fieldType;
		for(String fieldName: recordSchema.itemNames()) {
			fieldType= recordSchema.get(fieldName);
			if(fieldType.equals(String.class))		
				recordNode.add((String)record.get(fieldName));		
			else if(fieldType.equals(Integer.class))		
				recordNode.add((Integer)record.get(fieldName));		
			else if(fieldType.equals(Double.class))		
				recordNode.add((Double)record.get(fieldName));		
			else if(fieldType.equals(IEEEDouble.class))	
				recordNode.add((String)((IEEEDouble)record.get(fieldName)).toHexString());
			else if(MsdxFunctionCall.class.isAssignableFrom(fieldType)) {
				MsdxFunctionCall functionCall= (MsdxFunctionCall) record.get(fieldName);
				recordNode.add((String)functionCall.getCallString());
			}
			else 
				throw new IllegalArgumentException(fieldType.getSimpleName() + " is not a supported type");			
		}//for fieldName
		return recordNode;	
	}//toNode

	/**
	 * Creates a single line string representation of this Record.
	 * For output purposes, prefer the show methods or showAsNode.
	 */
	@Override
	public String toString() { 
		StringBuilder result= new StringBuilder();
		Iterator<Object> items= this.getItems().values().iterator();
		result.append("[");
		while(items.hasNext()) {
			result.append(String.valueOf(items.next()));
			if(items.hasNext())
				result.append(", ");
		}
		result.append("]");
		return result.toString();
	}//toString
	
	/**
	 * Outputs a readable, single-line representation of a Record or Container.
	 * 
	 * @param record
	 * @param out destination
	 */
	public static void show (MsdxContainer<Object> record, PrintStream out) {
		out.println(MsdxContainer.toString(record.getContent(), "  ", "", item -> String.valueOf(item)));
	}
	
	/**
	 * Outputs a readable, single-line representation of this Record.
	 * 
	 * @param out destination
	 */
	public void show(PrintStream out) {
		show(this, out);	
	}

	/**
	 * Outputs a readable, single-line JSON representation of a Record or Container.
	 * 
	 * @param content
	 * @param schema
	 * @param out destination
	 */
	public static void showAsNode(MsdxContainer<Object> content, MsdxContainer<Class<?>> schema, PrintStream out) {
		MsdxWriter.Generator generator= MsdxWriter.Generator.create(MsdxOutputDestination.toStream(out));
		MsdxWriter writer= new MsdxWriter(generator);
		try {
			generator.writeStartArray(content.isEmpty());
			writer.arrayToJson(MsdxRecord.toNode(content, schema), null);
			generator.writeEndArray(content.isEmpty());
			out.println();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}	
	}//showAsNode
	
	/**
	 * A static factory to create a new Record instance from a Schema and data items.
	 * Uses the Builder.
	 * 
	 * @param recordSchema
	 * @param items
	 * @return a new Record object
	 */
	public static MsdxRecord create(MsdxContainer<Class<?>> recordSchema, Object... items) {
		if(recordSchema.size() != items.length)
			throw new IllegalArgumentException("Size mismatch between the schema and the items");
		MsdxRecord.Builder builder= MsdxRecord.builder(recordSchema);
		Iterator<String> fieldNames= recordSchema.itemNames().iterator();
		for(Object item: items) {
			builder.addItem(fieldNames.next(), item);
		}
		return builder.build();
	}
	
	/**
	 * This class enables building a Record item by item. It also permits copying one or more 
	 * items from another Container and for removing unnecessary copies. When the Record is built, 
	 * it validates the items against the Schema. You can also build a Record with all null items, 
	 * which is useful when an Operator needs to return a Record with missing items.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 */
	public static class Builder extends MsdxContainer.Builder<Object> {
		
		/**Defines the names and data types of this Record's fields.*/
		private MsdxContainer<Class<?>> recordSchema;
	
		/**
		 * Creates a new Record Builder.
		 * @param tableSchema the names and data types of this Record's fields
		 */
		public Builder(MsdxContainer<Class<?>> tableSchema) {
			super();
			this.recordSchema = tableSchema;
		}

		/**
		 * Creates a new Record Builder.
		 * @param tableSchema the names and data types of this Record's fields
		 */
		public Builder(MsdxSchema tableSchema) {
			this(tableSchema.asContainer());
		}
		
		/**
		 * Adds the item to this container.
		 * 
		 * @param itemName
		 * @param value
		 * @return this Builder
		 * @throws IllegalArgumentException if the type of the item does not match the Schema
		 * @throws IllegalArgumentException if the item name duplicates a field already in the Container 
		 * and the content is not equal to the existing field content (allows duplicate fields to be replaced).
		 */
		@Override
		public MsdxRecord.Builder addItem(String itemName, Object value) {
			if(value!=null && !recordSchema.get(itemName).isInstance(value))
				throw new IllegalArgumentException("Type mismatch on field " + itemName 
					+ ": actual= " + value.getClass().getName() 
					+ " but expected= " + recordSchema.get(itemName).getSimpleName());
			super.addItem(itemName, value);
			return this; 
		}
		
		/**
		 * Adds a item only if the selector is true.
		 * 
		 * @param selector boolean expression
		 * @param itemName
		 * @param value
		 * @return this Builder
		 */
		@Override
		public MsdxRecord.Builder addItemIf(boolean selector, String itemName, Object value) {
			return !selector ? this : this.addItem(itemName, value); 
		}
		
		/**
		 * Copies an item from the other Container if it is present.
		 * 
		 * @param other
		 * @param fieldName
		 * @return this Builder
		 */
		public MsdxRecord.Builder copyItem(MsdxContainer<Object> other, String fieldName) {
			if(other.containsField(fieldName)) 
				this.addItem(fieldName, other.get(fieldName));
			return this;
		}
	
		/**
		 * Copies a group of items from the other Container.
		 * 
		 * @param other
		 * @param itemNames
		 * @return this Builder
		 */
		public MsdxRecord.Builder copyItems(MsdxContainer<Object> other, Collection<String> itemNames) {
			for(String itemName: itemNames)
				this.copyItem(other, itemName);
			return this;
		}

		/**
		 * Copies a group of items from the other Container.
		 * 
		 * @param other
		 * @param itemNames
		 * @return this Builder
		 */
		public MsdxRecord.Builder copyItems(MsdxContainer<Object> other, String... itemNames) {
			return copyItems(other, Arrays.asList(itemNames));
		}

		/**
		 * Copies all items from the other Container.
		 * 
		 * @param other
		 * @return this Builder
		 */
		public MsdxRecord.Builder copyItems(MsdxContainer<Object> other) {
			return copyItems(other, other.itemNames());
		}
		
		/**
		 * Removes the item if it is present.
		 * 
		 * @param itemName
		 * @return this Builder
		 */
		@Override
		public MsdxRecord.Builder removeItem(String itemName) {
			super.removeItem(itemName);
			return this; 
		}
	
		/**
		 * Removes the item if it is present and the selector is true
		 * 
		 * @param selector boolean expression
		 * @param itemName
		 * @return this Builder
		 */
		@Override
		public MsdxRecord.Builder removeItemIf(boolean selector, String itemName) {
			super.removeItemIf(selector, itemName);
			return this; 
		}
	
		/**
		 * Concludes the build process.
		 * 
		 * @return the new Record
		 * @throws IllegalStateException if the new Record is missing or has extra fields 
		 * with respect to the Schema
		 */
		@Override
		public MsdxRecord build() {
			Set<String> missing= recordSchema
				.itemNames().stream()
				.filter(itemName -> !this.items.containsKey(itemName))
				.collect(Collectors.toSet());
			if(!missing.isEmpty())
				throw new IllegalStateException("Missing field(s) " + missing.toString());
			Set<String> extra= recordSchema
				.itemNames().stream()
				.filter(name -> !recordSchema.containsField(name))
				.collect(Collectors.toSet());
			if(!extra.isEmpty())
				throw new IllegalStateException("Name(s) not in schema " + extra.toString());
			return new MsdxRecord(super.build(), recordSchema);
		}//build
		
		/**@return a new Record with null values for each field specified in the Schema*/
		public MsdxRecord buildNull() {
			Map<String, Object> result= new LinkedHashMap<String, Object>();
			this.recordSchema.itemNames().stream()
				.forEach(name -> result.put(name, null));
			return new MsdxRecord(result, recordSchema);
		}//buildNull
			
	}//class MsdxRecord.Builder
	

}//class MsdxRecord
