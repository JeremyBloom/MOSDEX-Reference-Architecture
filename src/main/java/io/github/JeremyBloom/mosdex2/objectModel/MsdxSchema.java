/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxFunctionCall;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;

/**
 * Represents a MOSDEX SCHEMA in Java. A Table Schema defines the names and datatypes of the fields in the Instance.
 * <p>
 * Schema wraps Container of Class and serves a fundamental role in accessing the general purpose data carrier 
 * Record in Instance, Dataframe, and Span. Record holds heterogeneous objects 
 * that are used the bridging stream operations for creating solver-specific modeling objects, providing 
 * uniformity in handling those streams while avoiding creating many context-specific classes. 
 * Data items in a Record cannot be used without its accompanying Schema, 
 * the fields of which specify the class identity of each item. In this respect, 
 * it follows the concept of a typesafe heterogeneous container developed by J. Bloch in his book 
 * <a href="https://www.oreilly.com/library/view/effective-java/9780134686097/"> Effective Java (item 33)</a>.
 * Without this concept, MOSDEX would require the user to create Java classes for each table
 * and solver-specific modeling object, a significant programming burden. 
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * Copyright © 2022 Jeremy A. Bloom
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxSchema extends MsdxObject {

    /**
     * Holds the class identity of each field.
     */
    public MsdxContainer<Class<?>> fields;
    
    /**
	 * Constructs a new Schema object.
	 * Using a Schema constructor is strongly discouraged.
	 * The preferred way to create a Schema is to use the MsdxContainer Builder.
	 * 
     * @param thisNode Tree Model representation of this object
     * @param parent Table object containing this Schema
     * @param factory that created this object
	 * @param schema container
	 */
	protected MsdxSchema(JsonNode thisNode, MsdxObject parent, MsdxObject.Factory factory, MsdxContainer<Class<?>> schema) {
		super(thisNode, parent, factory);
		this.fields = new MsdxContainer<Class<?>>(schema);
	}

	protected MsdxSchema() {
		this(MsdxReader.createObjectNode(), null, null, new MsdxContainer<Class<?>>());
	}
	
	/**
	 * Replaces content of this Schema by the argument. 
	 * Replaces the node consistent with the new content.
	 * Used in MsdxTable when replacing the Instance as part of retrieving thesolution from the solver, 
	 * when function calls are replaced by their values.
	 * 
	 * @param recordSchema replacement
	 * @return a new Schema object
	 */
	protected MsdxSchema replace(MsdxContainer<Class<?>> recordSchema) {
		this.fields= new MsdxContainer<Class<?>>(recordSchema);
		
		ObjectNode schemaNode= MsdxSchema.toNode(recordSchema);
		((ObjectNode) this.thisNode).replace("FIELDS", schemaNode.get("FIELDS"));
		((ObjectNode) this.thisNode).replace("TYPES", schemaNode.get("TYPES"));
		return this;
	}//replace

	/**Applies an action to each field of this Schema.*/
	public void forEach(BiConsumer<? super String, ? super Class<?>> action) {
		fields.forEach(action);
	}

	/**@return an unwrapped Container of this Schema
	 */
	public MsdxContainer<Class<?>> asContainer() {
		return fields;
	}
	
	/**@return the field names*/
	public Set<String> getFieldNames() {
		return fields.itemNames();
	}

	/**@return true if this Schema contains the field name, false otherwise*/
	public boolean containsField(String fieldName) {
		return fields.containsField(fieldName);
	}

	/**@return the number of fields in this Schema*/
	public int size() {
		return fields.size();
	}

	/**@return true if this Schema has no fields, false otherwise*/
	public boolean isEmpty() {
		return fields.isEmpty();
	}

	/**
	 * Creates a schema Container from its JSON Tree Model node representation.
	 * 
	 * @param schemaNode
	 * @return a new schema Container
	 */
	public static MsdxContainer<Class<?>> fromNode(JsonNode schemaNode) {
		
		if(!schemaNode.getNodeType().equals(JsonNodeType.OBJECT))
			throw new IllegalArgumentException("Expected object node but got " + schemaNode.getNodeType().name());
		
		if(schemaNode.size()==0) {
	 		return new MsdxContainer<Class<?>>(); //empty schema
	 	};
			
		Iterator<JsonNode> fieldNames= schemaNode.get("FIELDS").iterator();
		Iterator<JsonNode> fieldTypes= schemaNode.get("TYPES").iterator();
		
		if(fieldNames==null || fieldTypes==null)
			throw new IllegalArgumentException("Missing " +  
				fieldNames==null ? "FIELDS " : "" + 
				fieldTypes==null ? "TYPES" : "");
		
		List<String> duplicates= MsdxReader.findDuplicates(
			StreamSupport.stream(schemaNode.get("FIELDS").spliterator(), false)
			.map(fieldNode -> fieldNode.textValue()));
		if(!duplicates.isEmpty())
			throw new IllegalArgumentException("Duplicate field(s) " + duplicates.toString());
	
		MsdxContainer.Builder<Class<?>> builder= MsdxContainer.<Class<?>>builder();
		while(fieldNames.hasNext()) {
			if(!fieldTypes.hasNext())
				throw new IllegalArgumentException("Length mismatch between FIELDS and TYPES");
			builder.addItem(
				fieldNames.next().textValue(), 
				MsdxSchema.typeOf(fieldTypes.next().textValue())
			);
		}
		if(fieldTypes.hasNext())
			throw new IllegalArgumentException("Length mismatch between FIELDS and TYPES");
		
		return builder.build();
	}//fromNode

	/**
	 * Maps MOSDEX data types to Java classes.
	 * Used in the fromNode method.
	 * 
	 * @param fieldType in MOSDEX
	 * @return the corresponding Java class
	 */
	protected static Class<?> typeOf(String fieldType) {
		if(fieldType.equals("STRING"))
			return String.class;
		else if(fieldType.equals("INTEGER"))
			return Integer.class;
		else if(fieldType.equals("DOUBLE"))
			return Double.class;
		else if(fieldType.equals("IEEEDOUBLE"))
			return IEEEDouble.class;
		else if(fieldType.equals("DOUBLE_FUNCTION"))
			return MsdxFunctionCall.DOUBLE.class;
		else if(fieldType.equals("IEEEDOUBLE_FUNCTION"))
			return MsdxFunctionCall.IEEEDOUBLE.class;
		else if(fieldType.equals("INTEGER_FUNCTION"))
			return MsdxFunctionCall.INTEGER.class;
		else if(fieldType.equals("STRING_FUNCTION"))
			return MsdxFunctionCall.STRING.class;
		else
			throw new IllegalArgumentException("Unsupported type " + fieldType);					
	}//typeOf

	/**
	 * Creates a JSON Tree Model node representation from a schema Container. 
	 * 
	 * @param schema
	 * @return a JSON node
	 */
	public static ObjectNode toNode(MsdxContainer<Class<?>> schema) {
		
		ArrayNode fieldsNode= MsdxReader.createArrayNode();
		ArrayNode typesNode= MsdxReader.createArrayNode();
		Class<?> fieldType;
		String typeName;
		for(String fieldName: schema.itemNames()) {
			fieldsNode.add((String)fieldName);
			fieldType= schema.get(fieldName);
			if(MsdxFunctionCall.class.isAssignableFrom(fieldType))
				typeName= fieldType.getSimpleName().toUpperCase() + "_FUNCTION";
			else
				typeName= fieldType.getSimpleName().toUpperCase();			
			typesNode.add(typeName);
		}//for fieldName
		
		ObjectNode schemaNode= MsdxReader.createObjectNode();
		schemaNode.set("FIELDS", fieldsNode);
		schemaNode.set("TYPES", typesNode);
		return schemaNode;		
	}//toNode

	/**@return a stream of field name: field type pairs*/
	public Stream<Entry<String, Class<?>>> toStream() {
		return fields.toStream();
	}

	/**@return a simple string representation of this Schema's fields (use the show method for a pretty-print version)*/
	@Override
	public String toString() {
		return fields.toString();
	}
	
	/**
	 * Outputs a readable JSON representation of a Schema or Container.
	 * 
	 * @param schema
	 * @param out destination
	 */
	public static void show(MsdxContainer<Class<?>> schema, MsdxOutputDestination out) {
		MsdxWriter.Generator generator= MsdxWriter.Generator.create(out);
		MsdxWriter writer= new MsdxWriter(generator);
		try {
			generator.writeStartObject(false);
			generator.writeFieldName("SCHEMA");
			generator.writeStartObject(schema.isEmpty());
			writer.objectToJson(MsdxSchema.toNode(schema));
			generator.writeEndObject(schema.isEmpty());
			generator.writeEndObject(false);
			generator.linefeed();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}	
	}//showAsNode

	/**
	 * Outputs a readable JSON representation of a Schema or Container.
	 * 
	 * @param schema
	 * @param out destination
	 */
	public static void show(MsdxContainer<Class<?>> schema, PrintStream out) {
		show(schema, MsdxOutputDestination.toStream(out));
	}//showAsNode

	/**
	 * Outputs a readable JSON representation of a Schema or Container.
	 * 
	 * @param out destination
	 */
	public void show(MsdxOutputDestination out) {
		MsdxSchema.show(this.fields, out);	
	}

	/**
	 * Outputs a readable JSON representation of this Schema.
	 * 
	 * @param out destination
	 */
	public void show(PrintStream out) {
		MsdxSchema.show(this.fields, out);	
	}
	

}//class MsdxSchema
