/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxFunctionCall;
import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter.Generator;

/**
 * The base class for all MOSDEX objects.
 * MOSDEX objects in general are represented as nodes of the Jackson Tree Model, and 
 * are accessed by calls to JsonNode methods, usually as get(fieldName) or as the iterators fields and elements.
 * <p> 
 * However, certain objects also create Java objects. These fields (module, table, 
 * schema, query, instance, etc.) require special handling to create the corresponding objects. 
 * In particular, instance data is held in dataframes, because the node representation 
 * would be too cumbersome.
 * <p>   
 * The objects represented by Java classes provide alternative access to certain fields 
 * of their parent objects.
 * <p>
 * The static member class Factory provides methods to read and write MOSDEX JSON.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxObject {
	
	/**The JSON Tree Model node that corresponds to this object.*/
	protected JsonNode thisNode;
	
	/**The parent object of this object; null for an MsdxFile object.*/
	protected MsdxObject parent;
	
	/**The factory that created this object.*/
	protected MsdxObject.Factory factory;

	/**
	 * Creates a new MOSDEX Object instance.
     * @param thisNode Tree Model representation of this object
     * @param parent object containing this object
     * @param factory that created this object
	 */
	public MsdxObject(JsonNode thisNode, MsdxObject parent, MsdxObject.Factory factory) {
		super();
		this.thisNode = thisNode;
		this.parent = parent;
		this.factory = factory;
	}

	/*@return the JSON Tree Model node representing this Object.
	 */
	public JsonNode getAsNode() {
		if(thisNode==null)
			throw new IllegalArgumentException("This node is not defined");
		return thisNode;
	}

	/**@return true if this node represents a JSON array*/
	public final boolean nodeIsArray() {
		return this.getAsNode().isArray();
	}

	/**@return true if this node represents a JSON array*/
	public final boolean nodeIsObject() {
		return this.getAsNode().isObject();
	}

	/**@return true if this node represents a JSON object*/
	public JsonNode getFieldAsNode(String fieldName) {
		return this.getAsNode().get(fieldName);
	}

	/**Get the factory that created this object.*/
	public MsdxObject.Factory getFactory() {
		return factory;
	}

	/**Get the parent of this object.*/
	public MsdxObject getParent() {
		if(this.parent==null)
			throw new IllegalArgumentException("Parent node is not defined");
		return this.parent;
	}

	/**@return true if this object has the given field*/
	public boolean hasField(String fieldName) {
		return this.getAsNode().has(fieldName);
	}

	/**@return an iterator over the field names of this object*/
	public Iterator<String> fieldNames() {
		return this.getAsNode().fieldNames();
	}
	
	/**@return the type of this object's node representation*/
	public JsonNodeType getNodeType() {
		return this.getAsNode().getNodeType();
	}

	/**@return true if this object has a null node (not the same as a Java null)*/
	public final boolean isNull() {
		return this.getAsNode().isNull();
	}

	/**@return an iterator over the child elements of this object*/
	public Iterator<JsonNode> elements() {
		return this.getAsNode().elements();
	}

	/**@return an iterator over the field name: field value pairs of this object (applicable only to JSON object nodes)*/
	public Iterator<Entry<String, JsonNode>> fields() {
		return this.getAsNode().fields();
	}

	/**
	 * Applies an action to each child element of this object.*/
	public void forEach(Consumer<? super JsonNode> action) {
		this.getAsNode().forEach(action);
	}

	/**@return a spliterator over the child elements of this object*/
	public Spliterator<JsonNode> spliterator() {
		return this.getAsNode().spliterator();
	}
	
	/**@return a stream of the child elements of this object*/
	public Stream<JsonNode> toNodeStream() {
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	/**
	 * Adds a field to this object (applicable only to JSON objects).
	 * @param fieldName
	 * @param value
	 * @return this object as a node
	 */
	public JsonNode setField(String fieldName, JsonNode value) {
		if(!this.getNodeType().equals(JsonNodeType.OBJECT))
			throw new UnsupportedOperationException("Not an object node");
		return ((ObjectNode) this.getAsNode()).set(fieldName, value);
	}

	/**
	 * Adds an element to this object (applicable only to JSON arrays).
	 * @param value
	 * @return this object as a node
	 */
	public ArrayNode addElement(JsonNode value) {
		if(!this.getAsNode().getNodeType().equals(JsonNodeType.ARRAY))
			throw new UnsupportedOperationException("Not an array node");
		return ((ArrayNode) this.getAsNode()).add(value);
	}

	/**
	 * Creates a simple string representation of the JSON representation of this object. 
	 * It has no indentation or pretty printing, and instances show as a single representative record.
	 * To create a pretty JSON string, use one of the write methods of the factory or 
	 * one of the class-specific show methods.
	 */
	@Override
	public String toString() {
		return thisNode.toString();
	}

	/**
	 * This class provides methods for reading and writing MOSDEX JSON. 
	 * The primary methods are readFile and writeFile.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Factory {
	
		/** Provides a representation of Instance data.*/
		private MsdxDataframe.Factory dataframeFactory;
		
		/**Provides the basic functionality for reading and writing MOSDEX as JSON.*/
		private ObjectMapper mapper;
		
		/**
		 * Creates a new MsdxObject.Factory instance.
		 * @param dataframeFactory creates representations of Instance data
		 * @param mapper used for reading and writing MOSDEX as JSON
		 * @param validate True if the MOSDEX File is to be validated against the MOSDEX schema;
		 * false otherwise (not implemented yet)
		 */
		public Factory(MsdxDataframe.Factory dataframeFactory, ObjectMapper mapper, boolean validate) {
			super();
			this.dataframeFactory = dataframeFactory;
			this.mapper = mapper;	
		}
	
		/**@return the dataframe factory*/
		public MsdxDataframe.Factory getDataframeFactory() {
			return dataframeFactory;
		}

		/**
		 * Creates a JSON parser for reading from an input source.
		 * Uses the Jackson parser.
		 * 
		 * @param src
		 * @return a parser
		 */
		public JsonParser createParser(MsdxInputSource src) {
			JsonParser parser= null;
			try {
				parser = this.mapper.getFactory().createParser(src.getStream());
				parser.nextToken();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			return parser;
		}//createParser

		/**
		 * Reads a MOSDEX JSON file from an input source and 
		 * creates the Java objects representing the MOSDEX objects.
		 * Each MOSDEX file generates a MsdxFile instance and its subsidiary objects, 
		 * Modules, Tables, etc. The Application handles multiple files as necessary.
		 * <p>
		 * This method uses a MsdxReader instance to handle the actual deserialization of JSON. 
		 * The reader handles standard JSON elements (e.g. objects, arrays, and values) but permits 
		 * customization through the specialHandling method, to deal with JSON fields that create Java objects 
		 * in addition to nodes of the tree model,
		 * in particular INSTANCE arrays that are read directly into Dataframes.
		 * 
		 * @param src a MsdxInputSource wraps various kinds of sources 
		 * (file system Files, input streams, urls) in a uniform way.
		 * @return a new instance of the MsdxFile class
		 */
		public MsdxFile readFile(MsdxInputSource src) {
	     	MsdxFile file= new MsdxFile();
	     	file.factory= this;    	
	     	file.modules= new LinkedHashMap<String, MsdxModule>();

		    JsonParser parser= this.createParser(src);
		    	
	    	MsdxReader reader= new MsdxReader(parser) {
				/**
				 * Modules are processed in two stages. In the first, this reader reads the MODULES field of the File 
				 * as a JSON Array. In the second stage, the readModules method reads each element of the array as a Module;
				 * this stage is recognized by the synthetic keyword MODULE passed in the call to the array parser 
				 * in readModules. 
				 */
				@Override 
				protected JsonNode specialHandling(String keyword) throws IOException {
					MsdxModule module;					
					if(keyword!=null && keyword.equals("MODULES")) {
						return this.arrayFromJson("MODULE");
					}
					else if(keyword!=null && keyword.equals("MODULE")) {
						module= readModule(parser);
						module.parent = file/* .modules */;
						if(file.modules.containsKey(module.getName()))
							throw new IllegalArgumentException("Duplicate module name " + module.getName());
						file.modules.put(module.getName(), module);
						return module.getAsNode();
					}
					else 
						return null;
				}//specialHandling
	    	}/*MsdxReader*/;
		    	
 			try {
				file.thisNode= reader.objectFromJson(); //Captures the JSON nodes
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
	    	return file;
	    }//readFile
		
		/**
		 * Reads a MOSDEX Module as a JSON object.
		 * Creates Table objects and puts them in the tables array in the Module.
		 * Reads other Module components, if any, as nodes of the tree model.
		 * 
		 * @param parser
		 * @return a new Module object
		 */
		protected MsdxModule readModule(JsonParser parser) {
			MsdxModule module= new MsdxModule();	//parent object is set by caller
			module.factory = this;
	     	module.tables= new LinkedHashMap<String, MsdxTable>();

			MsdxReader reader= new MsdxReader(parser) {
				/**
				 * Tables are processed in two stages. In the first, this reader reads the TABLES field of the module 
				 * as a JSON Array. In the second stage, the readTables method reads each element of the array as a Table;
				 * this stage is recognized by the synthetic keyword TABLE passed in the call to the array parser 
				 * in readTables. 
				 */
				@Override 
				protected JsonNode specialHandling(String keyword) throws IOException {
					MsdxTable table;					
					if(keyword!=null && keyword.equals("TABLES")) {
						return this.arrayFromJson("TABLE");
					}
					else if(keyword!=null && keyword.equals("TABLE")) {
						table= readTable(parser);
						table.parent = module;
						if(module.tables.containsKey(table.getName()))
							throw new IllegalArgumentException("Duplicate table name " + table.getName());
						module.tables.put(table.getName(), table);
						return table.getAsNode();
					}
					else
						return null;
				}//specialHandling
	    	}/*MsdxReader*/;
		    	
	     	try {
	 			module.thisNode= reader.objectFromJson();
	 			if(!module.getAsNode().has("NAME") || module.getAsNode().get("NAME").asText().isBlank())
	 				throw new JsonParseException(parser, "Missing module name", parser.getCurrentLocation());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}     	
	    	return module;
	    }//readModule
	    
		/**
		 * Reads a MOSDEX Table as a JSON object.
		 * Creates Schema, Query, and Instance objects and puts them in the table object.
		 * Reads other Table components as nodes of the Tree Model.
		 * 
		 * @param parser
		 * @return a new Table object
		 */
		protected MsdxTable readTable(JsonParser parser) {
			MsdxTable table= new MsdxTable();	//parent object is set by caller
			table.factory= this;

			MsdxReader reader= new MsdxReader(parser) {
				@Override 
				protected JsonNode specialHandling(String keyword) throws IOException {
					JsonNode value;
					MsdxSchema schema;
					MsdxQuery query;
					MsdxInstance instance;
					
					if(keyword==null)
						return null;
					if(keyword.equals("NAME")) {
						value= this.valueFromJson(parser.getCurrentToken(), null);
						table.setField("NAME", value);
						return value;						
					}
					else if(keyword.equals("CLASS")) {		
						value= this.valueFromJson(parser.getCurrentToken(), null);
						table.setField("CLASS", value);
						return value;
					}
					else if(keyword.equals("SCHEMA")) {
						schema= readSchema(parser);
						schema.parent= table;
						table.schema = schema;
						table.setField("SCHEMA", schema.getAsNode());
						return schema.getAsNode();
					}
					else if(keyword.equals("INSTANCE")) {
						instance= readInstance(table.getName(), table.getSchema(), parser);
						instance.parent= table;
						table.instance = instance;
						//need to set instance.thisNode as representative record
						table.setField("INSTANCE", instance.getAsNode());
						return instance.getAsNode();
					}
					else if(
						keyword.equals("QUERY") || 
						keyword.equals("INITIALIZE") ||
						keyword.equals("APPEND") ||
						keyword.equals("REVISE")) 
					{
						query= readQuery(parser);
						query.parent= table;
						if(keyword.equals("QUERY") || keyword.equals("INITIALIZE"))
							table.query = query;
						else if(keyword.equals("APPEND"))
							table.append = query;
						else /*keyword.equals("REVISE")*/
							table.revise = query;							
						table.setField(keyword, query.getAsNode());
						return query.getAsNode();
					}
					else
						return null;					
				}//specialHandling    		
	    	}/*MsdxReader*/;
		    	
	     	try {
	 			table.thisNode= reader.objectFromJson();		
	 			if(!table.getAsNode().has("NAME") || table.getAsNode().get("NAME").asText().isBlank())
	 				throw new JsonParseException(parser, "Missing table name", parser.getCurrentLocation());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			return table;
		}//readTable

	    /**
		 * Reads a table schema from MOSDEX JSON.
		 * The JSON consists of parallel arrays for the field names and types, 
		 * which is not convenient for use with record containers.
		 * This method creates a schema Container.
		 * 
		 * @param parser
		 * @return a new Schema object
		 */
		protected MsdxSchema readSchema(JsonParser parser) {
			MsdxReader reader= new MsdxReader(parser);
				
			JsonNode schemaNode= null;	
	     	try {
				schemaNode= reader.objectFromJson();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
	    	MsdxSchema schema= new MsdxSchema(schemaNode, null, this, MsdxSchema.fromNode(schemaNode));	//parent object is set by caller
			return schema;
		}//readSchema
	   
		/**
		 * Reads a MOSDEX Query as a JSON object.
		 * The query itself is accessed as a node of the tree model, 
		 * but the query object provides additional functionality.
		 * 
		 * @param parser
		 * @return a new Query object
		 */
		protected MsdxQuery readQuery(JsonParser parser) {
			MsdxQuery query= new MsdxQuery(null);	//parent object is set by caller
			try {
				query.thisNode= new MsdxReader(parser).objectFromJson();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			return query;		
		}//readQuery
		
		/**
		 * Reads a MOSDEX Instance array as a JSON object.
		 * Due to the potentially large size of Instance arrays, MOSDEX does not 
		 * create a new node of the Tree Model. Instead, an Instance is read directly into a Dataframe.
		 * This method creates a stream of Records which are used to populate a Dataframe. 
		 * In order to maintain consistency with the Tree Model, this method creates a representative node 
		 * consisting of a single record, which is used when validating MOSDEX against its JSON schema.
		 * 
		 * @param tableName
		 * @param tableSchema
		 * @param parser
		 * @return a new Instance object
		 */
		protected MsdxInstance readInstance(String tableName, MsdxSchema tableSchema, JsonParser parser) {
			MsdxInstance instance = new MsdxInstance();		//parent object is set by caller
			instance.factory= this;
			instance.thisNode= MsdxReader.createArrayNode();
			
			if(tableSchema==null)
				throw new IllegalArgumentException("Undefined table schema");
			instance.tableSchema = tableSchema;
			
			MsdxReader reader= new MsdxReader(parser);
			Stream<MsdxRecord> records= null;
			try {
				records= reader.streamFromJson(null)
					.peek(node -> {
						if(instance.getAsNode().size()==0)
							((ArrayNode) instance.getAsNode()).add(node);  //Add a representative record for validation against the MOSDEX Schema
					})
					.map(node -> readRecord(node, tableSchema, parser));		
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			instance.dataframe= this.dataframeFactory.create(
				tableName, 
				records.map(record -> (MsdxContainer<Object>)record), 
				tableSchema.asContainer());			
 			return instance;
		}//readInstance
		
		/**
		 * Reads a MOSDEX Record as a JSON array. 
		 * 
		 * @param node
		 * @param tableSchema
		 * @param parser
		 * @return a new Record object
		 */
		protected MsdxRecord readRecord(JsonNode node, MsdxSchema tableSchema, JsonParser parser) {
			return MsdxRecord.fromNode(node, tableSchema.asContainer());
		}//readRecord

		/**
		 * Creates a JSON generator for writing to an output destination.
		 * Does not use the Jackson generator, due to restrictions on output format.
		 * The generator provides pretty printed output for the covenience of a human reader.
		 * 
		 * @param dst MsdxOutputDestination wraps various kinds of destinations 
		 * (file system Files, output streams, urls) in a uniform way. 
		 * @return a new Generator object
		 */
		public JsonGenerator createGenerator(MsdxOutputDestination dst) {
			JsonGenerator generator=null;
			try {
				generator= this.mapper.getFactory()
					.createGenerator(dst.getStream());
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			return generator;
		}//createGenerator
		
		/**
		 * Writes a MOSDEX File as a JSON object. 
		 * Most of the MOSDEX Objects are written from their node representations in the Tree Model. 
		 * However, MOSDEX Instances require a special write method because 
		 * their records are stored in a Dataframe, not as nodes of the Tree Model.
		 * <p>
		 * This method uses a MsdxWrite instance to handle the actual serialization to JSON. 
		 * The writer handles standard JSON elements (e.g. objects, arrays, and values) but permits 
		 * customization through the specialHandling method, to deal with Java objects that represent MOSDEX elements,
		 * such as INSTANCE arrays that are written directly from Dataframes, not as nodes of the Tree Model.
		 * <p>
		 * You can specify a subset of the tables that will be output, 
		 * which may be helpful when dealing with large MOSDEX files. (This feature is not fully implement yet, 
		 * so be careful in using it.) Assumes all Modules and Tables have unique names.		 *
		 *  
		 * @param file the MOSDEX Object to be written
		 * @param objectsToShow set of Modules (and their Tables) to include in the output (include all if empty)
		 * @param dst MsdxOutputDestination wraps various kinds of destinations 
		 * (file system Files, output streams, urls) in a uniform way. 
		 */
		public void writeFile(
			MsdxFile file, 
			Map<String, Set<String>> objectsToShow, 
			MsdxOutputDestination dst) 
		{
			if(file==null)
				throw new IllegalArgumentException("A MOSDEX File has not been created");
			MsdxWriter.Generator generator= MsdxWriter.Generator.create(dst);
			
			Map<String, MsdxModule> modules= file.getModules();
			Set<String> modulesToShow= modules.keySet().stream()
				.filter(name -> objectsToShow.isEmpty() || objectsToShow.containsKey(name))
				.collect(Collectors.toSet());
			
	    	MsdxWriter writer= new MsdxWriter(generator) {
				@Override
				protected boolean specialHandling(JsonNode node, String keyword) 
					throws IOException 
				{
					if(keyword.equals("MODULES")) {
						MsdxModule module;
						for(Iterator<JsonNode> moduleNodes= node.elements(); moduleNodes.hasNext(); ) {
							module= modules.get(moduleNodes.next().get("NAME").asText());
							if(!(modulesToShow.contains(module.getName())))
								continue;
							generator.writeStartObject(false);
							if(objectsToShow.isEmpty()) {
								writeModule(
									module, 
									generator);	
							}
							else {
								writeModule(
									module, 
									objectsToShow.get(module.getName()), 
									generator);
							}
					    	generator.writeEndObject(false);
					    	
					    	if(moduleNodes.hasNext())
					    		generator.writeArrayValueSeparator(false);
						}
						return true;
					}
					return false;
				}//specialHandling
	    	}/*MsdxWriter*/;
	    	
			try {
				generator.writeStartObject(file.getAsNode().size()==0);
		    	writer.objectToJson(file.getAsNode());
		    	generator.writeEndObject(file.getAsNode().size()==0);	
		    	generator.linefeed();
				generator.flush();	    	
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}			
		}//writeFile
		
		/**
		 * Writes a MOSDEX File object with all its Modules and Tables.
		 * @param file
		 * @param dst
		 */
		public void writeFile(MsdxFile file, MsdxOutputDestination dst) {
			writeFile(file, Collections.emptyMap(), dst);
		}
		
		/**
		 * Writes a MOSDEX Module as a JSON object. 
		 * 
		 * @param module
		 * @param objectsToShow tables of this module to show or, if empty, show all
		 * @param generator
		 * @throws IOException
		 */
		public void writeModule(
			MsdxModule module, 
			Set<String> objectsToShow,	//tables to show or, if empty, show all  
			MsdxWriter.Generator generator) 
			throws IOException 
		{
			Map<String, MsdxTable> tables= module.getTables();
			Set<String> tablesToShow= tables.keySet().stream()
				.filter(name -> objectsToShow.isEmpty() || objectsToShow.contains(name))
				.collect(Collectors.toSet());
			
	    	MsdxWriter writer= new MsdxWriter(generator) {
				@Override
				protected boolean specialHandling(JsonNode node, String keyword) 
					throws IOException 
				{
					MsdxTable table;
					if(keyword.equals("TABLES")) {
						for(Iterator<JsonNode> tableNodes= node.elements(); tableNodes.hasNext(); ) {
							table= tables.get(tableNodes.next().get("NAME").asText());
							if(!(tablesToShow.contains(table.getName())))
								continue;
							generator.writeStartObject(false);
							writeTable(table, generator);
					    	generator.writeEndObject(false);
					    	
					    	if(tableNodes.hasNext())
					    		generator.writeArrayValueSeparator(false);
						}
						return true;
					}
					return false;
				}//specialHandling
	    	}/*MsdxWriter*/;

	    	writer.objectToJson(module.getAsNode());
		}//writeModule
		
		/**
		 * Writes a MOSDEX Module object with all its Tables.
		 * @param module
		 * @param generator
		 * @throws IOException
		 */
		public void writeModule(
			MsdxModule module, 
			MsdxWriter.Generator generator) 
			throws IOException 
		{
			writeModule(module, Set.of(), generator);
		}
		
		/**
		 * Writes a MOSDEX Table as a JSON object. 
		 * 
		 * @param table
		 * @param generator
		 * @throws IOException
		 */
		protected void writeTable(MsdxTable table, Generator generator) throws IOException {
	    	MsdxWriter writer= new MsdxWriter(generator) {

				@Override
				protected boolean specialHandling(JsonNode node, String keyword) throws IOException {
					if(keyword.equals("SCHEMA")) {
						writeSchema(table.getSchema().asContainer(), generator);
						return true;
					}
					else if(keyword.equals("INSTANCE")) {
						writeRecords(
							table.instance.getDataframe().toStream().map(record -> (MsdxContainer<Object>)record), 
							table.getSchema().asContainer()/*fields*/, 
							generator);
						return true;
					}
					return false;
				}
	    	}/*MsdxWriter*/;
	    	
			writer.objectToJson(table.getAsNode());
		}//writeTable
		
		/**
		 * Writes a MOSDEX Schema as a JSON object. 
		 * @param tableSchema a Schema Java object holding the actual fields and types information
		 * @param generator
		 * 
		 * @throws IOException
		 */
		public void writeSchema(
			MsdxContainer<Class<?>> tableSchema, 
			MsdxWriter.Generator generator) throws IOException 
		{
			MsdxWriter writer= new MsdxWriter(generator) {
				@Override
				protected boolean specialHandling(JsonNode node, String keyword) 
					throws IOException 
				{
					Class<?> type;
					if(keyword.equals("FIELDS")) {
						for(Iterator<Map.Entry<String, Class<?>>> fields= 
								tableSchema.getItems().entrySet().iterator(); 
								fields.hasNext(); ) 
							{
								generator.writeString(fields.next().getKey());
								if(fields.hasNext())
									generator.writeArrayValueSeparator(true);
							}//for fields
						return true;
					}
					else if(keyword.equals("TYPES")) {
						for(Iterator<Map.Entry<String, Class<?>>> fields= 
								tableSchema.getItems().entrySet().iterator(); 
								fields.hasNext(); ) 
							{
								type= fields.next().getValue();
								if(MsdxFunctionCall.class.isAssignableFrom(type))
									generator.writeString(type.getSimpleName().toUpperCase() + "_FUNCTION");
								else
									generator.writeString(type.getSimpleName().toUpperCase());
								if(fields.hasNext())
									generator.writeArrayValueSeparator(true);
							}//for fields
						return true;
					}
					return false;
				}//specialHandling
	    	}/*MsdxWriter*/;
	    	
			writer.objectToJson(MsdxSchema.toNode(tableSchema));
		}//writeSchema
		
		/**
		 * Writes a MOSDEX Instance as a JSON array.
		 * Instance requires a special write method because its records are not 
		 * stored as nodes of the Tree Model.
		 * 
		 * @param records
		 * @param recordSchema
		 * @param generator
		 * @throws IOException
		 */
		public void writeRecords(
			Stream<MsdxContainer<Object>> records, 
			MsdxContainer<Class<?>> recordSchema, 
			MsdxWriter.Generator generator) 
			throws IOException 
		{
			MsdxContainer<Object> record;
			for(Iterator<MsdxContainer<Object>> containers= records.iterator(); containers.hasNext(); ) {
				record= containers.next();
				generator.writeStartArray(true);
			 	writeRecord(record, recordSchema, generator);
				generator.writeEndArray(true);
				
				if(containers.hasNext())
					generator.writeArrayValueSeparator(false);				
			}//for containers		
		}//writeInstance
		
		/**
		 * Writes a MOSDEX Record as a JSON array.
		 * Record requires a special write method because it is not 
		 * stored as a node of the Tree Model. 
		 * 
		 * @param record to be written
		 * @param recordSchema
		 * @param generator
		 * @throws IOException 
		 */
		protected void writeRecord(
			MsdxContainer<Object> record, 
			MsdxContainer<Class<?>> recordSchema, 
			MsdxWriter.Generator generator) 
			throws IOException 
		{
			Map.Entry<String, Object> entry;
			for(Iterator<Map.Entry<String, Object>> items= record.toStream().iterator(); items.hasNext(); ) {
				entry= items.next();
				writeItem(
					entry.getKey(), 					//field name
					entry.getValue(), 					//item
					recordSchema.get(entry.getKey()),	//field type 
					generator);
				
				if(items.hasNext())
					generator.writeArrayValueSeparator(true);
			}//for items
		}//writeRecord
		
		/**
		 * Writes a MOSDEX Record item as a JSON value.
		 * Record requires a special write method because it is not 
		 * stored as a node of the Tree Model. 
		 * 
		 * @param fieldName
		 * @param item the value to be written
		 * @param fieldType
		 * @param generator
		 * @throws IOException 
		 */
		protected void writeItem(
			String fieldName, 
			Object item, 
			Class<?>fieldType, 
			MsdxWriter.Generator generator) throws IOException 
		{
			if(item == null)
				generator.writeNull();
			else if(MsdxFunctionCall.class.isAssignableFrom(fieldType))
				generator.writeString(((MsdxFunctionCall)item).getCallString());		
			else if (item instanceof IEEEDouble)
				generator.writeString(((IEEEDouble)item).toHexString());
			else if(item instanceof Double) {
				if(!((Double) item).isInfinite())
					generator.writeNumber((Double)item);
				else
					generator.writeString(String.valueOf((Double)item));
			}
			else if(item instanceof Integer) {
				if(((Integer)item).equals(Integer.MAX_VALUE))
					generator.writeString("Infinity");
				else if(((Integer)item).equals(Integer.MIN_VALUE))
					generator.writeString("-Infinity");
				else
					generator.writeNumber((Integer)item);
			}
			else if(item instanceof String)
				generator.writeString((String)item);
			//These cases arise only when writing MsdxSpan, not when writing MsdxObject
			else if(item instanceof Class<?>)	
				generator.writeString(((Class<?>) item).getSimpleName());
			else
				generator.writeString(String.valueOf(item));
		}//writeItem
				
	}//class MsdxObject.Factory


}//class MsdxObject
