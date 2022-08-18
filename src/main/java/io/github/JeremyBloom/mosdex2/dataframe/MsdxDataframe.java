/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.dataframe;

import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;

/**
 * This interface provides a template for classes that represent a data set of
 * records that supports SQL queries. A record is a Container of Objects. A
 * Dataframe has a schema that defines the field names and types of the items in
 * each record. The schema is a Container of Classes. All records have the same
 * schema and only one schema instance exists for a Dataframe.
 * <p>
 * The primary use of Dataframe is to provide the content for the Instance field of a Table .
 * A Dataframe can be deserialized from MOSDEX JSON using the readInstance
 * method of MsdxObject.Factory or it can result from the execution of a query
 * using the create method of MsdxDataframe.Factory. MOSDEX converts Dataframes
 * to Spans as part of the bridges that connect MOSDEX input to solver-specific
 * modeling objects. The primary reason for separating Dataframe from Span
 * architecturally is that Spans can contain objects (e.g. classes of a solver
 * API) that do not map easily to primitive data types whereas, most
 * implementations of Dataframe limit content to objects representing primitive
 * data types (i.e. integer, double, or string). This interface defines (and
 * sometimes provides default methods for) basic operations on a Dataframe.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public interface MsdxDataframe {
	
	/**@return the schema of this Dataframe*/
	public MsdxContainer<Class<?>> getSchema();
	
	/**@return the contents of this Dataframe as a stream of records*/
	public Stream<MsdxContainer<Object>> toStream();
	
	/**
	 * Counts the records of this Dataframe.
	 * Use with caution, since counting a large Dataframe is an expensive operation and 
	 * is a terminal operation for a stream-based Dataframe.
	 * 
	 * @return the number of records in this Dataframe
	 */
	public long size();
	
	/**@return the first record in this Dataframe*/
	public MsdxContainer<Object> first();

	/**
	 * Performs an action for each record of this Dataframe.
	 * 
	 * @param action
	 */
	public void forEach(Consumer<MsdxContainer<Object>> action);
	
	/**
	 * Writes a Dataframe as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * Primarily used for displaying the contents of the Dataframe.
	 * Note: the various write operations are adapted from the corresponding operations 
	 * in MsdxObject.Factory.
	 * 
	 * @param name used in the display
	 * @param dst destination
	 * @param limit maximum number of records written (null for no limit)
	 */
	default void show(String name, MsdxOutputDestination dst, Long limit) {
		
		//Dummy nodes for use by the writer
		ObjectNode spanNode= MsdxReader.createObjectNode();
		if(name != null && !name.isBlank())
			spanNode.put("NAME", name);
		ObjectNode schemaNode= spanNode.putObject("SCHEMA");
		schemaNode.putArray("FIELDS");
		schemaNode.putArray("TYPES");
		ArrayNode instanceNode= spanNode.putArray("INSTANCE");
		instanceNode.add(new String("Dummy").repeat(10));	//so that the instance is not written in-line
		
		MsdxObject.Factory objectFactory= new MsdxObject.Factory(null, Msdx.GLOBAL.mapper, false);
		MsdxWriter.Generator generator= MsdxWriter.Generator.create(dst);
		MsdxWriter writer= new MsdxWriter(generator) {

			@Override
			protected boolean specialHandling(JsonNode node, String keyword) throws IOException {
				if(keyword.equals("SCHEMA")) {
					objectFactory.writeSchema(MsdxDataframe.this.getSchema(), generator);
					return true;
				}
				else if(keyword.equals("INSTANCE")) {
					objectFactory.writeRecords(
						MsdxDataframe.this.toStream() 
							.limit(limit!=null ? limit : Long.MAX_VALUE),
						MsdxDataframe.this.getSchema(), 
						generator);
					return true;
				}
				return false;
			}
    	}/*MsdxWriter*/;
	    	
		try {
			generator.writeStartObject(false);
	    	writer.objectToJson(spanNode);
	    	generator.writeEndObject(false);	
	    	generator.linefeed();
			generator.flush();	    	
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}				
	}//show
	
	/**
	 * Writes a Dataframe as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * 
	 * @param name used in the display
	 * @param dst destination
	 */
	default void show(String name, MsdxOutputDestination dst) {
		show(name, dst, null);
	}//show

	/**
	 * Writes a Dataframe as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * 
	 * @param name used in the display
	 * @param out destination
	 * @param limit maximum number of records written (null for no limit)
	 */
	default void show(String name, PrintStream out, Long limit) {
		show(name, MsdxOutputDestination.toStream(out), limit);
	}//show
	
	/**
	 * Writes a Dataframe as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * 
	 * @param name used in the display
	 * @param out destination
	 */
	default void show(String name, PrintStream out) {
		show(name, MsdxOutputDestination.toStream(out), null);
	}//show
	
	/**@return the factory that created this Dataframe*/
	public MsdxDataframe.Factory getFactory();
	
	/**
	 * The Dataframe Factory provides public methods for creating dataframes and executing queries.
	 * The Factory also enables registering SQL user-defined functions with the database engine at implements the Dataframe.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 */
	public static interface Factory {
		
		/**
		 * Creates a Dataframe from a stream of records.
		 * Does not create new instances of the records.
		 * 
		 * @param tableName
		 * @param records
		 * @param schema
		 * @return a new Dataframe
		 */
		public MsdxDataframe create(String tableName, Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> schema);
		
		/**
		 * Copies the records of a Span to a Dataframe.
		 * Creates a new record instance in the Dataframe from each record in the Span.
		 * 
		 * @param tableName
		 * @param records
		 * @return a new Dataframe
		 */
		default MsdxDataframe create(String tableName, MsdxSpan records) {
			return create(
				tableName, 
				records.apply().map(record -> new MsdxContainer<Object>(record)), 
				records.getSchema());
		}//create
		
		/**
		 * Creates a new Dataframe by executing a query.
		 * 
		 * @param tableName
		 * @param query
		 * @param schema (required even if the query specifies its schema, 
		 * in which case the two must be the same)
		 * @return a new Dataframe
		 */
		public MsdxDataframe create(String tableName, MsdxQuery query, MsdxContainer<Class<?>> schema);

		/**
		 * Creates set of functions <code>IDn</code> that make a row or column Id string of the form "tableName_key", 
		 * and registers it with the Dataframe Factory for use in queries as an SQL user-defined function.
		 * The functions created replace the calls to <code>"CONCAT('tableName', '_', key)"</code> in MOSDEX queries.
		 * Note, n, the number of keys specifies the corresponding function to be called 
		 * (currently it is restricted to between 2 and 5).
		 * 
		 * This method would be used in the MsdxDataframe.Factory. 
		 * Notice that the database-specific classes that implement the actual SQL calls 
		 * are not exposed.
		 * 
		 * @param numberOfKeyFields = n creates ID2,..., IDn
		 */
		public void registerStringIDFunction(int numberOfKeyFields);

		/**
		 * Currently unsupported. 
		 * Intended to create and register a set of functions IDn that make a row or column Id number.
		 * To be used with solvers that do not accept strings as row or column Ids.
		 * 
		 * @param numberOfKeyFields
		 */
		public void registerIntegerIDFunction(int numberOfKeyFields);
		
		/**
		 * Creates a function call string of the form <code>"functionName(arguments)"</code>, 
		 * and registers it with the Dataframe Factory for use in queries as an SQL user-defined function.
		 * The arguments string has the form <code>"tableName_key"</code>, and 
		 * would be constructed by calling one of the IDn functions.
		 * The functions created replace the strings <code>"'functionName(Column or Row)'"</code> in MOSDEX queries.
		 * <p>
		 * This method would be used in the MsdxSolverModelingFactory.initializeFunctionTable method, 
		 * called for each function defined there 
		 * (new functions added by a user, as discussed there, also need to be registered with this method).
		 * Note that this function does not actually get any data; it merely creates 
		 * a call string that is used by the MsdxModel.createSolutionObjects method 
		 * to capture data from the solver.
		 * <p>
		 * Notice also that the database-specific class that implements the actual SQL call is not exposed.
		 * <p>
		 * Note, nothing in this method restricts it registering only solver function calls, so 
		 * it can also include other user-defined SQL functions, if needed. 
		 * However, it cannot specify the actual function to call nor pass it an argument; 
		 * those have to be specified in the implementation of this method.
		 * 
		 * @param functionName
		 */
		void registerFunctionCall(String functionName);
		
		/**
		 * Creates a function <code>I_INFINTY(sign)</code> that returns an integer representing an infinite value, 
		 * and registers it with the Dataframe Factory for use in queries as an SQL user-defined function. 
		 * If the sign parameter is either empty or plus, the return value is the <code>Integer.MAX_VALUE</code>; 
		 * if it is "-", the return value is the <code>Integer.MIN_VALUE</code>.
		 */
		void registerIntegerInfinity();

		/**
		 * Creates a function <code>INFINITY(sign)</code> that returns a double representing an infinite value, 
		 * and registers it with the Dataframe Factory for use in queries as an SQL user-defined function. 
		 * If the sign parameter is either empty or plus, the return value is the <code>Double.POSITIVE_INFINITY</code>; 
		 * if it is "-", the return value is the <code>Double.NEGATIVE_INFINITY</code>.
		 */
		void registerDoubleInfinity();

	}//interface MsdxDataframe.Factory
	

}//interface MsdxDataframe
