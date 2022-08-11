/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.span;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;

/**
 * A Span is an abstraction representing the data streams which connect the
 * MOSDEX object model with the solver specific modeling objects. A Span is
 * intended to work as a distributed, parallel pipeline that does not realize
 * its content except at its origin and destination. A Span transforms the data
 * that passes through it according to an Operator. Operators are the means by
 * which a solver Modeling Factory specifies the transformations necessary to
 * create and use the solver-specific API in MOSDEX. The sequence of Spans that
 * transforms MOSDEX data into solver-specific modeling objects is called a
 * bridge.
 * <p>
 * The MOSDEX Span interface is implemented with two concrete classes, based on
 * Java streams or on Apache Spark distributed data sets. The interface specifies the
 * key operations on Spans, which are a subset of the operations available for
 * Spark distributed datasets and Java streams.
 * <p>
 * The fundamental component of the Span interface is the Container class. A
 * Container holds heterogeneous data together with their class identities, which
 * constitutes the Container's schema. All Containers in a Span must have the
 * same schema.
 * <p>
 * The classes implementing MsdxSpan do not have public constructors. Instead,
 * each has a companion, static member class implementing the MsdxSpan.Factory
 * interface. The create methods of this companion class provide for
 * construction of Span objects from various input data structures.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public interface MsdxSpan extends Serializable{
	
	/**
	 * Saves a Span for reuse by converting this Span to a persistent Span.
	 * If this Span is already persistent, copies it to a new instance.
	 * A persistent Span realizes its data in memory.
	 * 
	 * @return a persistent Span
	 */
	MsdxSpan persist();

	/**@return true if this Span is persistent, false otherwise*/
	boolean isPersistent();
	
	/**
	 * Saves a Span for reuse by converting this Span to a keyed Span.
	 * If this Span is already keyed, copies it to a new instance.
	 * 
	 * @param keyFieldName
	 * @return a keyed Span
	 */
	MsdxSpan key(String keyFieldName);

	/**@return true if this Span is keyed, false otherwise*/
	boolean isKeyed();

	/**
	 * Returns the items in the Span as a stream of Records.
	 * This method is overridden in a persistent Span and a keyed Span.
	 * Use of the apply method enables using the inherited transformation methods 
	 * of this class on either a Record stream or a persistent or keyed Span.
	 * <p>
	 * Note: the inherited methods return a Record stream, 
	 * so you can perform a series of transformations on the stream without 
	 * realizing it as a collection. You may need to persist the transformed stream again 
	 * if you want to reuse it.
	 */
	Stream<MsdxContainer<Object>> apply();
	
	/**
	 * @return the Record stream as an unmodifiable view of the collection.
	 * @throws UnsupportedOperationException if this instance is not persistent or is not a Java Span
	 */
	Collection<MsdxContainer<Object>> getAsCollection();	
	
	/**
	 * @return the Record stream as an unmodifiable view of the keyed map.
	 * @throws UnsupportedOperationException if this instance is not keyed or is not a Java Span
	 */
	Map<Object, MsdxContainer<Object>> getAsMap();

	/**@return the Record stream as a Java List*/
	default List<MsdxContainer<Object>> toList() {
		return this.apply().collect(Collectors.toList());
	}//toList
	
	/**
	 * @return the Record stream as a new keyed Java Map
	 * @throws IllegalArgumentException if the key field name is not in the record schema 
	 * or if there are multiple Records in the stream with the same key
	 */
	default Map<Object, MsdxContainer<Object>> toMap(String keyFieldName) {
		if(keyFieldName.isEmpty() || !this.containsField(keyFieldName))
			throw new IllegalArgumentException("Missing key field " + keyFieldName);
		return this.apply()
			.collect(Collectors.toMap(
				record -> record.get(keyFieldName), 
				Function.identity(),
				(record1, record2) -> {
					if(record1.equals(record2))
						return record1;
					else
						throw new IllegalArgumentException("Duplicate records");
				},
				LinkedHashMap::new)
			);
	}//toMap

	/**@return the name of the key field in the records of this Span, if any, or 
	 * returns an empty string*/
	String getKeyFieldName();

	/**
	 * @return the number of Records in the Span; 
	 * note, counting is a terminal operation which exhausts the stream, 
	 * unless there is a collection or map underlying the Span.
	 */
	long count();

	/**
	 * @return false of there are records in the Span, true if none
	 * @throws UnsupportedOperationException if a stream underlies the Span. 
	 */
	boolean isEmpty();
	
	/**
	 * @return the Schema of the Records in this Span;
	 * note, all Records reference the same Schema Container object.
	 */
	MsdxContainer<Class<?>> getSchema();

	/**
	 * @return names of the fields in the Span's schema
	 */
	Set<String> fieldNames();

	/**
	 * @return true if the argument is among the names of the fields in the Span's Schema
	 */
	boolean containsField(String fieldName);

	/**
	 * @return the type of the argument in the Span's schema
	 */
	Class<?> getFieldType(String fieldName);

	/**@return an iterator over the Records of the Span*/
	Iterator<MsdxContainer<Object>> iterator();

	/**
	 * Returns a Span with records derived by selecting the given field names.
	 * Also selects the named fields from the Span's Schema. 
	 * Does not check whether the field names actually exist in the record.
	 * 
	 * @param fieldNames
	 * @return a new Span with the selected fields
	 */
	MsdxSpan select(Collection<String> fieldNames);
	
	default MsdxSpan select(String... fieldNames) {
		return this.select(Arrays.asList(fieldNames));
	}
	
	/**
	 * Returns a Span with Records derived by deleting the given field names.
	 * Also deletes the named fields from the Span's Schema. 
	 * Does not check whether the item names actually exist in the record.
	 * 
	 * @param itemNames
	 * @return a new Span
	 */
	MsdxSpan delete(Collection<String> fieldNames);
	
	default MsdxSpan delete(String... fieldNames) {
		return this.delete(Arrays.asList(fieldNames));
	}
	
	/**@return a new Span consisting of the Records for which the predicate is true*/
	MsdxSpan filter(Predicate<MsdxContainer<Object>> predicate);
	
	/**@return a new Span consisting of the results of applying the given mapper function to the Records of this Span*/
	MsdxSpan map(OperatorWithOneArgument mapper);
		
	/**
	 * Performs an action for each Record of this Span.
	 * 
	 * @param action
	 */
	void forEach(Consumer<MsdxContainer<Object>> action);
	
	/**
	 * Joins two Spans by combining their Records with a common key, using a left
	 * outer join. The result has one Record for each key in this Span. Where there
	 * is no matching key in the other Span, the Record fields from the other Span
	 * is treated as null.
	 * <p>
	 * Creates new Span with component Containers of the following form:<br>
	 * <code>[key field value, value1,..., valueM] merge [other key field value, valueM+1,..., valueN] -></code><br>
	 * <code>[key field value, value1,..., valueM, valueM+1,..., valueN]</code><br>
	 * and creates a merged schema of the form:<br>
	 * <code>[key field name and type, field1,..., fieldM] merge [other key field name and
	 * type, fieldM+1,..., fieldN] -> </code><br>
	 * <code>[key field name and type, field1,..., fieldM, fieldM+1,..., fieldN]</code><br>
	 * where all of the Records point to the same Schema. 
	 * <p>
	 * The key field names do not have to match between the two
	 * Records as long as they have compatible types. The joiner looks at the key fields in the Records of both Spans, 
	 * and, if the their values match, the two Records are joined. 
	 * For example, suppose this Span has a Schema <br>
	 * <code> {rowId: key columnId2: String, term: Expression}, </code><br>
	 * and the second has Schema  
	 * <code> {key columnId: String, variable: SolverVariable}. </code><br>
	 * If the value in <code>columnId2</code> matches the value in <code>columnId</code>, the two Records are joined, 
	 * and the result has Schema<br>
	 * <code> {rowId: key columnId2: String, term: Expression, variable: SolverVariable}. </code><br>
	 * However, by default, it is
	 * assumed that the other Record has the same key field name as this one.
	 * If the two Spans have different key field names, the result has the key field name of this Span. 
	 * <p>
	 * When there is no matching key in the other argument, the joiner maps the missing
	 * components to some appropriate, context-specific default values (often null).
	 * 
	 * @param other        Span to be joined; if other is keyed, it need not have
	 *                     the same key field name as the given key field name,
	 *                     provided that the data in the key fields of the two Spans
	 *                     can be matched. If the other Span is not keyed, a key
	 *                     Span will be constructed from it using the given key
	 *                     field name.
	 * 
	 * @param keyFieldName in this Span used to match the other Span
	 * 
	 * @param joiner       combines and possibly transforms the two records from the
	 *                     input Spans and provides default values when there in no
	 *                     matching key
	 * 
	 * @return a new Span
	 * 
	 * @throws IllegalArgumentException if the key field is missing either Span or
	 *                 if the types of the key field do not match
	 * 
	 * @throws IllegalArgumentException if both Spans have fields with the same
	 *                 name but their contents do not match in any pair of records
	 *                 with the same key
	 */
	MsdxSpan leftJoin(MsdxSpan other, String keyFieldName, OperatorWithTwoArguments joiner);
	
	/**
	 * Joins two Spans by combining the records with a common key, using a full
	 * outer join. The result has one record for each key in both Spans. Where there
	 * is no matching key between the two Spans, the joiner maps the components of
	 * the missing record to default values.
	 * <p>
	 * Creates new Span with component Containers of the following form:<br>
	 * <code>[key field value, value1,..., valueM] merge [other key field value, valueM+1,..., valueN] -></code><br>
	 * <code>[key field value, value1,..., valueM, valueM+1,..., valueN]</code><br>
	 * and creates a merged schema of the form:<br>
	 * <code>[key field name and type, field1,..., fieldM] merge [other key field name and type, fieldM+1,..., fieldN] -> </code><br>
	 * <code>[key field name and type, field1,..., fieldM, fieldM+1,..., fieldN]</code><br>
	 * where all of the Records point to the same Schema.
	 * <p>
	 * The key field names do not have to match between the two Records as long as
	 * they have compatible types. The joiner looks at the key fields in the Records
	 * of both Spans, and, if the their values match, the two Records are joined.
	 * For example, suppose this Span has a Schema <br>
	 * <code> {rowId: key columnId2: String, term: Expression}, </code><br>
	 * and the second has Schema
	 * <code> {key columnId: String, variable: SolverVariable}. </code><br>
	 * If the value in <code>columnId2</code> matches the value in
	 * <code>columnId</code>, the two Records are joined, and the result has
	 * Schema<br>
	 * <code> {rowId: key columnId2: String, term: Expression, variable: SolverVariable}. </code><br>
	 * However, by default, it is assumed that the other Record has the same key
	 * field name as this one. If the two Spans have different key field names, the
	 * result has the key field name of this Span.
	 * <p>
	 * When there is no matching key between the two Spans, the joiner maps the
	 * missing components to some appropriate, context-specific default values.
	 * 
	 * @param other        Span to be joined; if other is keyed, it need not have
	 *                     the same key field name as the given key field name,
	 *                     provided that the data in the key fields of the two Spans
	 *                     can be matched. If the other Span is not keyed, a key
	 *                     Span will be constructed from it using the given key
	 *                     field name.
	 * 
	 * @param keyFieldName in this Span used to match the other Span
	 * 
	 * @param joiner       combines and possibly transforms the two records from the
	 *                     input Spans and provides default values when there in no
	 *                     matching key
	 * 
	 * @return a new Span
	 * 
	 * @throws IllegalArgumentException if the key field is missing either Span or
	 *                 if the types of the key field do not match.
	 * 
	 * @throws IllegalArgumentException if both Spans have fields with the same
	 *                 name but their contents do not match in any pair of records
	 *                 with the same key.
	 */
	MsdxSpan outerJoin(MsdxSpan other, String keyFieldName, OperatorWithTwoArguments joiner);
	
	/**
	 * Joins two Spans by combining the records with a common key, using an inner
	 * join. The result has one record for each key in both Spans. Where there is no
	 * matching key between the two Spans, the joiner skips that key.
	 * <p>
	 * Creates new Span with component Containers of the following form:<br>
	 * <code>[key field value, value1,..., valueM] merge [other key field value, valueM+1,..., valueN] -></code><br>
	 * <code>[key field value, value1,..., valueM, valueM+1,..., valueN]</code><br>
	 * and creates a merged schema of the form:<br>
	 * <code>[key field name and type, field1,..., fieldM] merge [other key field name and type, fieldM+1,..., fieldN] -> </code><br>
	 * <code>[key field name and type, field1,..., fieldM, fieldM+1,..., fieldN]</code><br>
	 * where all of the Records point to the same Schema.
	 * <p>
	 * The key field names do not have to match between the two Records as long as
	 * they have compatible types. The joiner looks at the key fields in the Records
	 * of both Spans, and, if the their values match, the two Records are joined.
	 * For example, suppose this Span has a Schema <br>
	 * <code> {rowId: key columnId2: String, term: Expression}, </code><br>
	 * and the second has Schema
	 * <code> {key columnId: String, variable: SolverVariable}. </code><br>
	 * If the value in <code>columnId2</code> matches the value in
	 * <code>columnId</code>, the two Records are joined, and the result has
	 * Schema<br>
	 * <code> {rowId: key columnId2: String, term: Expression, variable: SolverVariable}. </code><br>
	 * However, by default, it is assumed that the other Record has the same key
	 * field name as this one. If the two Spans have different key field names, the
	 * result has the key field name of this Span.
	 * <p>
	 * The joiner filters any records that do not have a common key value between
	 * the two Spans.
	 * 
	 * @param other        Span to be joined; if other is keyed, it need not have
	 *                     the same key field name as the given key field name,
	 *                     provided that the data in the key fields of the two Spans
	 *                     can be matched. If the other Span is not keyed, a key
	 *                     Span will be constructed from it using the given key
	 *                     field name.
	 * 
	 * @param keyFieldName in this Span used to match the other Span
	 * 
	 * @param joiner       combines and possibly transforms the two records from the
	 *                     input Spans
	 * 
	 * @return a new Span
	 * 
	 * @throws IllegalArgumentException if the key field is missing either Span or
	 *                 if the types of the key field do not match.
	 * 
	 * @throws IllegalArgumentException if both Spans have fields with the same
	 *                 name but their contents do not match in any pair of records
	 *                 with the same key.
	 */
	MsdxSpan innerJoin(MsdxSpan other, String keyFieldName, OperatorWithTwoArguments joiner);
	
	/**
	 * Defines a default joiner for two Spans with the following
	 * characteristics: the result schema is the merger of the two input schemas
	 * with a single key field inherited from the left Span. the Result record when
	 * the keys match is the merger of the two input records. when the right Span
	 * has no key match in the left, the default result inherits all the items of
	 * the left record and all items corresponding the right fields are set to null.
	 * <p>
	 * Usage:<br>
	 * <code>MsdxSpan terms= parameters.join(allVariables, "Column", merge());</code>
	 * 
	 * @return an operator that merges the Records of two Spans
	 */
	static OperatorWithTwoArguments merge() {	
		return new OperatorWithTwoArguments() {

			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> leftInputSchema,
				String leftKeyFieldName, 
				MsdxContainer<Class<?>> rightInputSchema, 
				String rightKeyFieldName) 
			{
				if(this.resultSchema!=null)
					return this;
				this.leftInputSchema= leftInputSchema;
				this.leftKeyFieldName= leftKeyFieldName;
				this.rightInputSchema= rightInputSchema;
				this.rightKeyFieldName= rightKeyFieldName;
				this.resultSchema= leftInputSchema.merge(rightInputSchema.delete(this.rightKeyFieldName));
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> right) {
				return (MsdxContainer<Object>) left.merge(right.delete(this.rightKeyFieldName));
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> left, Optional<MsdxContainer<Object>> right) {
				if(!(left.isPresent() ^ right.isPresent()))
					throw new IllegalStateException("One and only one of the parameters can be present");
				MsdxContainer<Object> present= left.isPresent() ? 
					left.get() : 
					right.get();
				MsdxContainer<Class<?>> absentSchema= left.isPresent() ? 
					this.rightInputSchema.delete(this.rightKeyFieldName) : 
					this.leftInputSchema.delete(this.leftKeyFieldName);
				//Use the MsdxRecord.Builder to assure that the built container conforms to the schema.
				//Note: MsdxRecord is a subclass of MsdxContainer<Object>, so the returned object is a container.
				MsdxRecord.Builder builder= MsdxRecord.builder(this.getResultSchema())
					.copyItems(present);
				for(String fieldName: absentSchema.itemNames())
					builder.addItemIf(!present.containsField(fieldName), fieldName, null);
				return builder.build();
			}//noKeyMatch
			
		}/*OperatorWithTwoArguments*/;//return		
	}//merge
	
	/**
	 * For each value of the key field, this method returns a Span that accumulates
	 * the elements (i.e. Records) in the other Span corresponding to that key to
	 * produce a single result element for that key.
	 * <p>
	 * The accumulator operator defines the fields to be accumulated and how the
	 * accumulation is computed (typically, the accumulation is a "sum" of terms,
	 * where the sum operation is specific to the solver). The type of the value
	 * field in the input Records (in this Span) must match the type of the
	 * accumulation field in the result Records, and thus, the accumulator function
	 * should verify that condition
	 * 
	 * @param keyFieldName defines the field in the input elements to be grouped for
	 *                     accumulation
	 * 
	 * @param accumulator  defines the fields or fields to be accumulated and how
	 *                     the accumulation is computed
	 * 
	 * @return a new Span in which the Records result from applying the accumulator
	 *         to each group of input Records according to the key field value
	 */
	MsdxSpan reduceByKey(
		String keyFieldName, 
		OperatorWithTwoArguments accumulator);
	
	/**
	 * Creates a new Span that contains all of the Records in this Span and the other Span. 
	 * Both Spans must have the same Schema, unless one of them is empty, in which case 
	 * the union inherits the non-empty schema. 
	 * (Thus, a sequence of unions can be initialized with an empty Span.
	 * If this schema is persistent or keyed, so is the union.)
	 * 
	 * @param other Span
	 * @return a new Span
	 * @throws IllegalArgumentException if the Schemas are both non-empty and are not equal
	 */
	MsdxSpan union(MsdxSpan other);

	/**
	 * Writes a Span as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * Primarily used for displaying the contents of the Span.
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
					objectFactory.writeSchema(MsdxSpan.this.getSchema(), generator);
					return true;
				}
				else if(keyword.equals("INSTANCE")) {
					objectFactory.writeRecords(
						MsdxSpan.this.apply() 
							.limit(limit!=null ? limit : Long.MAX_VALUE),
						MsdxSpan.this.getSchema(), 
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
	 * Writes a Span as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * Primarily used for displaying the contents of the Span.
	 * Note: the various write operations are adapted from the corresponding operations 
	 * in MsdxObject.Factory.
	 * 
	 * @param name used in the display
	 * @param dst destination
	 */
	default void show(String name, MsdxOutputDestination dst) {
		show(name, dst, null);
	}//show

	/**
	 * Writes a Span as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * Primarily used for displaying the contents of the Span.
	 * Note: the various write operations are adapted from the corresponding operations 
	 * in MsdxObject.Factory.
	 * 
	 * @param name used in the display
	 * @param out destination
	 * @param limit maximum number of records written (null for no limit)
	 */
	default void show(String name, PrintStream out, Long limit) {
		show(name, MsdxOutputDestination.toStream(out), limit);
	}//show	

	/**
	 * Writes a Span as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * Primarily used for displaying the contents of the Span.
	 * Note: the various write operations are adapted from the corresponding operations 
	 * in MsdxObject.Factory.
	 * 
	 * @param name used in the display
	 * @param out destination
	 */
	default void show(String name, PrintStream out) {
		show(name, MsdxOutputDestination.toStream(out), null);
	}//show	

	/**
	 * The Span Factory interface provides a number of utility methods that create and manipulate Spans. 
	 * The wrap method puts a Span facade around a Record stream or collection.
	 * The create method copies a Record stream or collection into a Span.
	 * The union method flatmaps several Spans into a single Span. 
	 * <p>
	 * Classes implementing the MsdxSpan interface must also have a static member class 
	 * that implements the Factory interface. A class implementing the Factory must have a 
	 * public constructor or static factory to create a Factory instance.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public interface Factory {
		
		/**
		 * This method calls the constructor of the class implementing MsdxSpan to create a new Span 
		 * using a copy of the underlying source of records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with a copy of the underlying source
		 */
		MsdxSpan create(MsdxSpan records);
		
		/**
		 * This method calls the constructor of the class implementing MsdxSpan to create a new Span 
		 * using a copy of the underlying source of records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with a copy of the underlying source
		 */
		MsdxSpan create(Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This method calls the constructor of the class implementing MsdxSpan to create a new Span 
		 * using a copy of the underlying source of records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with a copy of the underlying source
		 */
		MsdxSpan create(Iterator<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This method calls the constructor of the class implementing MsdxSpan to create a new Span 
		 * using a copy of the underlying source of records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with a copy of the underlying source
		 */
		MsdxSpan create(Collection<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This method calls the constructor of the class implementing MsdxSpan to create a new Span 
		 * using a copy of the underlying source of records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with a copy of the underlying source
		 */
		MsdxSpan create(Map<Object, MsdxContainer<Object>> records, String keyFieldName, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This method calls the constructor of the class implementing MsdxSpan to create a new Span 
		 * using a copy of the underlying source of records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with a copy of the underlying source
		 */
		MsdxSpan create(MsdxDataframe records);
		
		/**
		 * This methods wraps a source of Records in a new Span 
		 * using the same underlying source of Records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with the same underlying source
		 * @throws UnsupportedOperationException if attempting to wrap a Java Span as an Apache Spark Span or vice versa
		 */
		MsdxSpan wrap(Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This methods wraps a source of Records in a new Span 
		 * using the same underlying source of Records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with the same underlying source
		 * @throws UnsupportedOperationException if attempting to wrap a Java Span as an Apache Spark Span or vice versa
		 */
		MsdxSpan wrap(Iterator<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This methods wraps a source of Records in a new Span 
		 * using the same underlying source of Records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with the same underlying source
		 * @throws UnsupportedOperationException if attempting to wrap a Java Span as an Apache Spark Span or vice versa
		 */
		MsdxSpan wrap(Collection<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema);
		
		/**
		 * This methods wraps a source of Records in a new Span 
		 * using the same underlying source of Records. 
		 * This method is typically used after a series of transformations on a parent Span.
		 * 
		 * @param records a source such as another Span, stream, or Dataframe
		 * @param recordSchema
		 * @return a new Span with the same underlying source
		 * @throws UnsupportedOperationException if attempting to wrap a Java Span as an Apache Spark Span or vice versa
		 */
		MsdxSpan wrap(Map<Object, MsdxContainer<Object>> records, String keyFieldName, MsdxContainer<Class<?>> recordSchema);
		
		/**@return an empty Span (which has no elements)*/
		MsdxSpan empty();

		/**
		 * Creates a new Span that wraps of all the records in the argument Spans.
		 * All the Spans must have the same Schema, except if any are empty.
		 * 
		 * @param spans
		 * @return a new Span
		 * @throws IllegalArgumentException if any schema or key field name does not match
		 */
		MsdxSpan union(Collection<MsdxSpan> spans);

		/**
		 * Creates a new Span that wraps of all the records in the argument Spans.
		 * All the Spans must have the same Schema, except if any are empty.
		 * 
		 * @param spans
		 * @return a new Span
		 * @throws IllegalArgumentException if any schema or key field name does not match
		 */
		MsdxSpan union(MsdxSpan... spans);
		
		/**
		 * Creates a new Span that wraps of all the records in the argument Spans.
		 * All the Spans must have the same Schema, except if any are empty.
		 * 
		 * @param spans
		 * @return a new Span
		 * @throws IllegalArgumentException if any schema or key field name does not match
		 */
		MsdxSpan union(Map<String, MsdxSpan> spans);

		/**
		 * Creates a new Span that wraps of all the records in the argument Spans.
		 * All the Spans must have the same Schema, except if any are empty.
		 * 
		 * @param spans
		 * @return a new Span
		 * @throws IllegalArgumentException if any schema or key field name does not match
		 */
		MsdxSpan union(Stream<MsdxSpan> spans);

	}//interface MsdxSpan.Factory
	

}//interface MsdxSpan
