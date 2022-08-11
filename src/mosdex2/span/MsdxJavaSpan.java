/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.span;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;

/**
 * An implementation of Span using Java Stream and Map.
 * Uses the Operator interfaces to create the result Records and Schemas.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxJavaSpan implements MsdxSpan {
	
	private static final long serialVersionUID = -7569304091634678526L;

	/**The content of the Span is a Java stream of Records*/
	protected Stream<MsdxContainer<Object>> recordStream;
	
	/**The Schema of the Records*/
	private MsdxContainer<Class<?>> recordSchema;
	
	/**
	 * Constructs a new Span instance from a stream of Records. Wraps the stream as
	 * a Span; does not copy the stream nor the Record Schema, and thus, the new
	 * Span is not independent of the original stream. Verifies that the content of
	 * each Record conforms to the specified Schema and throws an exception if it does
	 * not.
	 * 
	 * @param records
	 * 
	 * @param recordSchema
	 * 
	 * @throws IllegalStateException if any of the following violations occurs: the
	 *                               record is missing a field of the schema; or the
	 *                               record has an extra item not in the schema; or
	 *                               the type of an item is not consistent with or
	 *                               not assignment compatible with the type
	 *                               specified in the schema.
	 */
	protected MsdxJavaSpan(Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
		super();
		if(recordSchema.isEmpty())
			throw new IllegalArgumentException("Empty record schema");
		this.recordSchema = recordSchema;
		this.recordStream = records.map(record -> record.verify(recordSchema));
	}

	/**
	 * Creates a Span with an empty stream and Schema.
	 */
	public MsdxJavaSpan() {
		super();
		this.recordSchema= MsdxContainer.<Class<?>>empty();
		this.recordStream= Stream.empty();
	}
	
	/**
	 * Constructs a new Span instance from an existing Span. Wraps the stream as a
	 * Span; does not copy the stream and thus, the new Span is not independent of
	 * the original stream. Verifies that the content of each Record conforms to the
	 * specified Schema, and throws an exception if it does not.
	 * 
	 * @param span
	 * 
	 * @throws IllegalStateException if any of the following violations occurs: the
	 *                               record is missing a field of the schema; or the
	 *                               record has an extra item not in the schema; or
	 *                               the type of an item is not consistent with or
	 *                               not assignment compatible with the type
	 *                               specified in the schema.
	 */
	public MsdxJavaSpan(MsdxSpan span) {
		this(span.apply(), new MsdxContainer<Class<?>>(span.getSchema()));
	}

	@Override
	public Stream<MsdxContainer<Object>> apply() {
		return this.recordStream;
	}

	@Override
	public MsdxSpan persist() {
		return new MsdxJavaSpan.Persistent(this.apply(), this.getSchema());
	}
	
	@Override
	public boolean isPersistent() {
		return this instanceof MsdxJavaSpan.Persistent;
	}
	
	@Override
	public MsdxSpan key(String keyFieldName) {
		return new MsdxJavaSpan.Keyed(this.apply(), keyFieldName, this.getSchema());
	}

	@Override
	public boolean isKeyed() {
		return this instanceof MsdxJavaSpan.Keyed;
	}

	@Override
	public Collection<MsdxContainer<Object>> getAsCollection() {
		throw new UnsupportedOperationException("Not valid for a stream span");
	}

	@Override
	public Map<Object, MsdxContainer<Object>> getAsMap() {
		throw new UnsupportedOperationException("Not valid for a stream span");
	}

	@Override
	public String getKeyFieldName() {
		return "";
	}

	@Override
	public long count() {
		throw new UnsupportedOperationException("Not valid for a stream span");
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException("Not valid for a stream span");
	}

	@Override
	public MsdxContainer<Class<?>> getSchema() {
		return this.recordSchema;
	}

	@Override
	public Set<String> fieldNames() {
		return this.recordSchema.itemNames();
	}

	@Override
	public boolean containsField(String fieldName) {
		return this.fieldNames().contains(fieldName);
	}

	@Override
	public Class<?> getFieldType(String fieldName) {
		return this.recordSchema.get(fieldName);
	}

	@Override
	public Iterator<MsdxContainer<Object>> iterator() {
		return this.apply().iterator();
	}

	@Override
	public MsdxSpan select(Collection<String> fieldNames) {
		return new MsdxJavaSpan(
			this.apply()
				.map(record -> record.select(fieldNames)),
			this.recordSchema.select(fieldNames)
		);
	}

	@Override
	public MsdxSpan delete(Collection<String> fieldNames) {
		return new MsdxJavaSpan(
				this.apply()
					.map(record -> record.delete(fieldNames)),
				this.recordSchema.delete(fieldNames)
			);
	}

	@Override
	public MsdxSpan filter(Predicate<MsdxContainer<Object>> predicate) {
		return new MsdxJavaSpan(this.apply().filter(predicate), this.getSchema());
	}

	@Override
	public MsdxSpan map(OperatorWithOneArgument mapper) {
		mapper.withResultSchema(this.getSchema());
		MsdxContainer<Class<?>> recordSchema= mapper.getResultSchema();
		return new MsdxJavaSpan(
			this.apply().map(wrapOperator(mapper)).map(record -> record.verify(recordSchema)),
			mapper.getResultSchema());
	}//map
	
	@Override
	public void forEach(Consumer<MsdxContainer<Object>> action) {
		this.apply().forEach(action);
	}

	@Override
	public MsdxSpan leftJoin(MsdxSpan other, final String keyFieldName, OperatorWithTwoArguments joiner) {
		
		if(!this.fieldNames().contains(keyFieldName))
			throw new IllegalArgumentException("Missing key field");
		if(other.isKeyed() && !this.getFieldType(keyFieldName).isAssignableFrom(other.getFieldType(other.getKeyFieldName())))
			throw new IllegalArgumentException("Incompatible key field type" + other.getKeyFieldName());
		
		final MsdxJavaSpan.Keyed otherByKey;
		if(other.isKeyed()) 
			otherByKey= (Keyed) other;	//note: the key field name in this case does not have to be the same as 
										//as the given key field name, as long as the data in the key fields are compatible;
										//e.g. the given key field name might be "Column1" but the key field in other might be "Column"
		else
			otherByKey= (Keyed) other.key(keyFieldName);
		
		joiner.withResultSchema(this.getSchema(), keyFieldName, otherByKey.getSchema(), otherByKey.getKeyFieldName());
		Stream<MsdxContainer<Object>> joined=
			this.apply().map(left -> 
				joiner.apply(Optional.ofNullable(left), Optional.ofNullable(otherByKey.recordMap.get(left.get(keyFieldName)))));
			
		return new MsdxJavaSpan(joined, joiner.getResultSchema());
	}//join

	@Override
	public MsdxSpan innerJoin(MsdxSpan other, String keyFieldName, OperatorWithTwoArguments joiner) {
		return this.leftJoin(other, keyFieldName, joiner)
			.filter(joiner.inner());
	}

	@Override
	public MsdxSpan outerJoin(MsdxSpan other, String keyFieldName, OperatorWithTwoArguments joiner) {
		if(!this.fieldNames().contains(keyFieldName))
			throw new IllegalArgumentException("Missing key field");
		
		String otherKeyFieldName= other.isKeyed() ? other.getKeyFieldName() : keyFieldName;
		if(!other.fieldNames().contains(otherKeyFieldName))
			throw new IllegalArgumentException("Missing key field");
		if(!this.getFieldType(keyFieldName).isAssignableFrom(other.getFieldType(otherKeyFieldName)))
			throw new IllegalArgumentException("Incompatible key field type" + other.getKeyFieldName());
		
		joiner.withResultSchema(this.getSchema(), keyFieldName, other.getSchema(), otherKeyFieldName);

		Stream<Map.Entry<Object, MsdxContainer<Object>>> widened= this.apply()
		//include the fields of the other span that are not in this span
			.map(record -> new AbstractMap.SimpleEntry<Object, MsdxContainer<Object>>(
				record.get(keyFieldName), 										//key 
				joiner.apply(Optional.ofNullable(record), Optional.empty()))	//record value
			);
		
		Stream<Map.Entry<Object, MsdxContainer<Object>>> otherWidened= other.apply()
		//include the fields of this span that are not in the other span
			.map(record -> new AbstractMap.SimpleEntry<Object, MsdxContainer<Object>>(
				record.get(otherKeyFieldName), 									//key
				joiner.apply(Optional.empty(), Optional.ofNullable(record)))	//record value
				);
			
		Map<Object, MsdxContainer<Object>> result= Stream.concat(widened, otherWidened)
			.collect(Collectors.toMap(
				entry -> entry.getKey(), 
				entry -> entry.getValue(), 
				(left, right) ->joiner.apply(Optional.ofNullable(left), Optional.ofNullable(right)),
				LinkedHashMap::new));
		
		return new MsdxJavaSpan.Keyed(result, keyFieldName,joiner.getResultSchema());
	}//outerJoin

	@Override
	public MsdxSpan reduceByKey(
		String keyFieldName, 
		OperatorWithTwoArguments accumulator) 
	{
		if(!this.fieldNames().contains(keyFieldName))
			throw new IllegalArgumentException("Missing key field");
		
		Function<MsdxContainer<Object>, Object> keySelector= (MsdxContainer<Object> record) -> record.get(keyFieldName);
		
		accumulator.withResultSchema(MsdxContainer.<Class<?>>empty(), keyFieldName, this.getSchema(), keyFieldName);
		Map<Object, MsdxContainer<Object>> reduced= this.apply()
			.collect(Collectors.toMap(
				record -> keySelector.apply(record), 
				record -> record.select(accumulator.getResultSchema().itemNames()),
				(accumulation, value) -> accumulator.apply(Optional.ofNullable(accumulation), Optional.ofNullable(value)), 
				LinkedHashMap<Object, MsdxContainer<Object>>::new));
		
		return new MsdxJavaSpan.Keyed(reduced, keyFieldName, accumulator.getResultSchema());
	}//reduceByKey
	
	@Override
	public MsdxSpan union(MsdxSpan other) {
		if( !this.getSchema().isEmpty() && !other.getSchema().isEmpty() &&
			!this.getSchema().equals(other.getSchema()))
			throw new IllegalArgumentException("Schemas do not match");
		MsdxContainer<Class<?>> resultSchema= !this.getSchema().isEmpty() ? 
			this.getSchema() : 
			other.getSchema();
		return new MsdxJavaSpan(Stream.concat(this.apply(), other.apply()), resultSchema);
	}

	/**
	 * Wraps a MOSDEX operator as its Java equivalent.
	 * 
	 * @param operator
	 * @return a Java functional interface object
	 */
	public static UnaryOperator<MsdxContainer<Object>> wrapOperator(OperatorWithOneArgument operator) {
		return new UnaryOperator<MsdxContainer<Object>>() {

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> input) {
				return operator.apply(input);
			}
			
		}/*UnaryOperator*/;/*return*/	
	}//wrapOperator

	/**
	 * Wraps a MOSDEX operator as its Java equivalent.
	 * 
	 * @param operator
	 * @return a Java functional interface object
	 */
	public static BinaryOperator<MsdxContainer<Object>> wrapOperator(OperatorWithTwoArguments operator) {
		return new BinaryOperator<MsdxContainer<Object>>() {

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> left, MsdxContainer<Object> right) {
				return operator.apply(Optional.ofNullable(left), Optional.ofNullable(right));
			}
			
		}/*BinaryOperator*/;/*return*/
	}//wrapOperator

	/**
	 * This class realizes a Span as a Java collection so that it can be reused.
	 * Methods inherited from Span generally produce Record streams, 
	 * so you can perform a series of transformations on the stream without 
	 * realizing it as a collection. You may need to persist the transformed stream again 
	 * if you want to reuse it.
	 * <p>
	 * This class also includes a number of supplemental methods that cannot be 
	 * executed on a stream.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Persistent extends MsdxJavaSpan {
	
		private static final long serialVersionUID = 3977590996669739363L;
	
		/**The content of the Span is a Java collection of Records*/
		private Collection<MsdxContainer<Object>> recordCollection;
		
		/**
		 * Creates a new Span instance from a collection of Records. Wraps the
		 * collection as a Span; does not copy the collection nor the record Schema, and
		 * thus, the new Span is not independent of the original collection. Verifies
		 * that the content of each Record conforms to the specified Schema, and throws an
		 * exception if it does not.
		 * 
		 * @param records
		 * 
		 * @param recordSchema
		 * 
		 * @throws IllegalStateException if any of the following violations occurs: the
		 *                               record is missing a field of the schema; or the
		 *                               record has an extra item not in the schema; or
		 *                               the type of an item is not consistent with or
		 *                               not assignment compatible with the type
		 *                               specified in the schema.
		 */
		public Persistent(Collection<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			super(records.stream(), recordSchema);
			this.recordCollection= records;
			this.recordStream= Stream.empty();
		}
	
		/**
		 * Creates a Span with an empty stream and Schema.
		 */
		public Persistent() {
			super();
			this.recordCollection= new LinkedList<MsdxContainer<Object>>();
		}
		
		/**
		 * Creates a new persistent Span from a stream of Records. Creates a new
		 * collection from the stream and copies the record Schema; thus, the new Span
		 * is independent of the original stream. Verifies that the content of each
		 * Record conforms to the specified Schema, and throws an exception if it does not.
		 * 
		 * @param records
		 * 
		 * @param recordSchema
		 * 
		 * @throws IllegalStateException if any of the following violations occurs: the
		 *                               record is missing a field of the schema; or the
		 *                               record has an extra item not in the schema; or
		 *                               the type of an item is not consistent with or
		 *                               not assignment compatible with the type
		 *                               specified in the schema.
		 */
		public Persistent(Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			this(records.collect(Collectors.toList()), new MsdxContainer<Class<?>>(recordSchema));		
		}

		/**
		 * Creates a new persistent Span from an existing Span. Creates a new collection
		 * from the Span and copies the record Schema; thus, the new Span is independent
		 * of the original Span. It also verifies that the content of each Record
		 * conforms to the specified Schema, and throws an exception if it does not.
		 * 
		 * @param span
		 * 
		 * @throws IllegalStateException if any of the following violations occurs: the
		 *                               record is missing a field of the schema; or the
		 *                               record has an extra item not in the schema; or
		 *                               the type of an item is not consistent with or
		 *                               not assignment compatible with the type
		 *                               specified in the schema.
		 */
		public Persistent(MsdxSpan span) {
			this(span.apply(), new MsdxContainer<Class<?>>(span.getSchema()));
		}

		@Override 
		public Stream<MsdxContainer<Object>> apply() {
			return this.recordCollection.stream();
		}

		@Override
		public Collection<MsdxContainer<Object>> getAsCollection() {
			return Collections.unmodifiableCollection(this.recordCollection);
		}
		
		@Override
		public long count() {
			return this.recordCollection.size();
		}

		@Override
		public boolean isEmpty() {
			return this.recordCollection.isEmpty();
		}
		
		@Override
		public Iterator<MsdxContainer<Object>> iterator() {
			return this.recordCollection.iterator();
		}

		@Override
		public MsdxSpan union(MsdxSpan other) {
			return new MsdxJavaSpan.Persistent(super.union(other).apply(), this.getSchema());
		}

	}//class MsdxJavaSpan.Persistent

	/**
	 * This class realizes a Span as a Java map so that it can be accessed by key. 
	 * The key field name identifies the field in each Record to be used as the key in the map; 
	 * designating a key field name does not alter the Records, so the key field name can be reassigned in 
	 * creating a new Keyed Span. 
	 * Methods inherited from Span generally produce Record streams, 
	 * so you can perform a series of transformations on the stream without 
	 * realizing it as a map. You may need to persist the transformed stream again 
	 * if you want to reuse it.
	 * <p>
	 * This class also includes a number of supplemental methods that cannot be 
	 * executed on a stream.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Keyed extends MsdxJavaSpan.Persistent {
	
		private static final long serialVersionUID = -6925586490725030959L;
	
		/**The content of the Span is a Java map of Records.*/
		private Map<Object, MsdxContainer<Object>> recordMap;
		
		/**The name of the key field in each Record.*/
		String keyFieldName;
		
		/**
		 * Constructs a new Span instance from a map of Records. Wraps the map as a
		 * Span; does not copy the stream nor the record Schema, and thus, the new Span
		 * is not independent of the original stream. Verifies that the content of each
		 * Record conforms to the specified Schema, and throws an exception if it does not.
		 * 
		 * @param records
		 * 
		 * @param keyFieldName
		 * 
		 * @param recordSchema
		 * 
		 * @throws IllegalStateException if any of the following violations occurs: the
		 *                               record is missing a field of the schema; or the
		 *                               record has an extra item not in the schema; or
		 *                               the type of an item is not consistent with or
		 *                               not assignment compatible with the type
		 *                               specified in the schema; or the key field name
		 *                               is empty, null, or not found in the schema.
		 */
		public Keyed(Map<Object, MsdxContainer<Object>> records, String keyFieldName, MsdxContainer<Class<?>> recordSchema) {
			super(records.values().stream(), recordSchema);
			if(keyFieldName==null || keyFieldName.isEmpty() || !recordSchema.containsField(keyFieldName))
				throw new IllegalArgumentException("Invalid key field");
			this.recordMap= records;
			this.keyFieldName= keyFieldName;
			this.recordStream= Stream.empty();
		}
		
		/**
		 * Creates a Span with an empty stream and Schema.
		 */
		public Keyed() {
			super();
			this.recordMap= new LinkedHashMap<Object, MsdxContainer<Object>>();
			this.keyFieldName= "";
		}
	
		/**
		 * Creates a new keyed Span from a stream of Records. Creates a new map from the
		 * stream and copies the record Schema; thus, the new Span is independent of the
		 * original stream. Verifies that the content of each Record conforms to the
		 * specified Schema, and throws an exception if it does not.
		 * 
		 * @param records
		 * 
		 * @param keyFieldName
		 * 
		 * @param recordSchema
		 * 
		 * @throws IllegalStateException if any of the following violations occurs: the
		 *                               record is missing a field of the schema; or the
		 *                               record has an extra item not in the schema; or
		 *                               the type of an item is not consistent with or
		 *                               not assignment compatible with the type
		 *                               specified in the schema; or the key field name
		 *                               is empty, null, or not found in the schema.
		 */
		public Keyed(Stream<MsdxContainer<Object>> records, String keyFieldName, MsdxContainer<Class<?>> recordSchema) {
			this(
				records.collect(Collectors.toMap(
					record -> record.get(keyFieldName), 
					Function.identity(),
					(record1, record2) -> {
						if(record1.equals(record2))
							return record1;
						else
							throw new IllegalArgumentException("Duplicate records");
					},
					LinkedHashMap::new)),
				keyFieldName, 
				new MsdxContainer<Class<?>>(recordSchema));
		}

		/**
		 * Creates a new keyed Span from an existing Span and copies the Record schema;
		 * thus, the new Span is independent of the existing Span. It also verifies that
		 * the content of each Record conforms to the specified Schema, and throws an
		 * exception if it does not.
		 * 
		 * @param span
		 * 
		 * @param keyFieldName
		 * 
		 * @throws IllegalStateException if any of the following violations occurs: the
		 *                               record is missing a field of the schema; or the
		 *                               record has an extra item not in the schema; or
		 *                               the type of an item is not consistent with or
		 *                               not assignment compatible with the type
		 *                               specified in the schema; or the key field name
		 *                               is empty, null, or not found in the schema.
		 */
		public Keyed(MsdxSpan span, String keyFieldName) {
			this(span.apply(), keyFieldName, span.getSchema());
		}
	
		@Override
		public Stream<MsdxContainer<Object>> apply() {
			return this.recordMap.values().stream();
		}

		@Override
		public Collection<MsdxContainer<Object>> getAsCollection() {
			return Collections.unmodifiableCollection(this.recordMap.values());
		}

		@Override
		public Map<Object, MsdxContainer<Object>> getAsMap() {
			return Collections.unmodifiableMap(this.recordMap);
		}

		@Override
		public String getKeyFieldName() {
			return this.keyFieldName;
		}

		@Override
		public long count() {
			return this.recordMap.size();
		}

		@Override
		public boolean isEmpty() {
			return this.recordMap.isEmpty();
		}
				
		@Override
		public Iterator<MsdxContainer<Object>> iterator() {
			return this.recordMap.values().iterator();
		}

		@Override
		public MsdxSpan union(MsdxSpan other) {
			return new MsdxJavaSpan.Keyed(super.union(other).apply(), this.getKeyFieldName(), this.getSchema());
		}
	
	}//class MsdxJavaSpan.Keyed

	/**
	 * The Span Factory class provides a number of utility methods that create and manipulate Spans. 
	 * The wrap method puts a Span facade around a Record stream or collection.
	 * The create method copies a Record stream or collection into a Span.
	 * The union method flatmaps several Spans into a single Span. 
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Factory implements MsdxSpan.Factory {
	
		/**
		 * Creates a new Factory instance.
		 */
		public Factory() {
			super();
		}

		@Override
		public MsdxSpan create(
			Map<Object, MsdxContainer<Object>> records, 
			String keyFieldName,
			MsdxContainer<Class<?>> recordSchema) 
		{
			return new MsdxJavaSpan.Keyed(new LinkedHashMap<Object, MsdxContainer<Object>>(records), keyFieldName, recordSchema);
		}

		@Override
		public MsdxSpan create(Collection<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			return new MsdxJavaSpan.Persistent(new LinkedList<MsdxContainer<Object>>(records), recordSchema);
		}

		@Override
		public MsdxSpan create(Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			return this.create(records.collect(Collectors.toList()), recordSchema);
		}

		@Override
		public MsdxSpan create(Iterator<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			Iterable<MsdxContainer<Object>> wrapper= new Iterable<MsdxContainer<Object>>() { 
				@Override public Iterator<MsdxContainer<Object>> iterator() { return records; }
			}/*Iterable*/;
			return this.create(StreamSupport.stream(wrapper.spliterator(), false), recordSchema);
		}

		@Override
		public MsdxSpan create(MsdxDataframe dataframe) {
			return this.create(dataframe.toStream(), dataframe.getSchema());
		}

		@Override
		public MsdxSpan create(MsdxSpan records) {
			if(records.isKeyed())
				return this.create(
					records.getAsMap(), 
					records.getKeyFieldName(), 
					records.getSchema());
			if(records.isPersistent())
				return this.create(records.getAsCollection(), records.getSchema());
			throw new UnsupportedOperationException("Cannot create a new stream of records from an existing stream");
		}

		@Override
		public MsdxSpan empty() {
			return new MsdxJavaSpan();
		}

		@Override
		public MsdxSpan wrap(Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			return new MsdxJavaSpan(records, recordSchema);
		}

		@Override
		public MsdxSpan wrap(Iterator<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			Iterable<MsdxContainer<Object>> wrapper= new Iterable<MsdxContainer<Object>>() { 
				@Override public Iterator<MsdxContainer<Object>> iterator() { return records; }
			}/*Iterable*/;
			return this.wrap(StreamSupport.stream(wrapper.spliterator(), false), recordSchema);
		}

		@Override
		public MsdxSpan wrap(Map<Object, MsdxContainer<Object>> records, String keyFieldName,
				MsdxContainer<Class<?>> recordSchema) {
			return new MsdxJavaSpan.Keyed(records, keyFieldName, recordSchema);
		}

		@Override
		public MsdxSpan wrap(Collection<MsdxContainer<Object>> records, MsdxContainer<Class<?>> recordSchema) {
			return new MsdxJavaSpan.Persistent(records, recordSchema);
		}

		@Override
		public MsdxSpan union(Collection<MsdxSpan> spans) {
			MsdxSpan first= spans.iterator().next();
			AtomicReference<MsdxContainer<Class<?>>> recordSchema= new AtomicReference<MsdxContainer<Class<?>>>(first.getSchema());
			AtomicReference<String> keyFieldName= new AtomicReference<String>(first.getKeyFieldName());
			if(!spans.stream().allMatch(span -> 
					span.getSchema().equals(recordSchema.get()) && 
					span.getKeyFieldName().equals(keyFieldName.get())))
				throw new IllegalArgumentException("Schema or key field names do not match");
				
			if(spans.stream().allMatch(span -> span.isKeyed())) {
				Map<Object, MsdxContainer<Object>> records= new LinkedHashMap<Object, MsdxContainer<Object>>();
				spans.iterator().forEachRemaining(span -> records.putAll(span.getAsMap()));
				return this.wrap(records, keyFieldName.get(), recordSchema.get());
			}
			
			else if(spans.stream().allMatch(span -> span.isPersistent())) {
				Collection<MsdxContainer<Object>> records= new LinkedList<MsdxContainer<Object>>();
				spans.iterator().forEachRemaining(span -> records.addAll(span.getAsCollection()));
				return this.wrap(records, recordSchema.get());
			}
			
			else
				return this.wrap(spans.stream()
					.map(MsdxSpan::apply)
					.flatMap(Function.identity()),
					recordSchema.get());
		}//union

		@Override
		public MsdxSpan union(MsdxSpan... spans) {
			return this.union(Arrays.asList(spans));
		}

		@Override
		public MsdxSpan union(Map<String, MsdxSpan> spans) {
			return this.union(spans.values());
		}

		@Override
		public MsdxSpan union(Stream<MsdxSpan> spans) {
			return this.union(spans.collect(Collectors.toList()));
		}
	
	}//class MsdxJavaSpan.Factory
	

}//class MsdxJavaSpan
