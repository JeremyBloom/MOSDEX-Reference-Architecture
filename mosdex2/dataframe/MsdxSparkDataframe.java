/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.dataframe;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.api.java.UDF3;
import org.apache.spark.sql.api.java.UDF4;
import org.apache.spark.sql.api.java.UDF5;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxFunctionCall;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import scala.collection.JavaConverters;

/**
 * Implements the Dataframe interface using a Apache Spark Dataset of Rows. 
 * Wraps Spark Row as a Record and Spark StructType as a Schema.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxSparkDataframe implements MsdxDataframe {
	
	private Dataset<Row> dataframe;
	
	/**
	 * Represents the schema of the Dataframe as a MOSDEX Container. 
	 * Note, Spark datasets have an internal schema called StructType; 
	 * However, Spark does not support all MOSDEX data types, so it is necessary 
	 * to carry the MOSDEX schema along. Factory methods convert between the two schemas.
	 */
	private MsdxContainer<Class<?>> schema;
	
	private MsdxSparkDataframe.Factory factory;

	/**
	 * Constructs a new Dataframe instance. 
	 * Use of this constructor is discouraged; use a create method of the Factory.
	 * 
	 * @param dataframe
	 * @param schema
	 * @param factory
	 */
	protected MsdxSparkDataframe(Dataset<Row> dataframe, MsdxContainer<Class<?>> schema, MsdxSparkDataframe.Factory factory) {
		super();
		this.dataframe = dataframe;
		this.schema = schema;
		this.factory = factory;
	}

	/**
	 * Constructs an empty Dataframe instance. 
	 * Use of this constructor is discouraged; use a create method of the Factory.
	 * 
	 * @param schema
	 * @param factory
	 */
	protected MsdxSparkDataframe(MsdxContainer<Class<?>> schema, MsdxSparkDataframe.Factory factory) {
		this(factory.session.emptyDataFrame(), schema, factory);
	}

	@Override
	public Factory getFactory() {
		return factory;
	}

	@Override
	public MsdxContainer<Class<?>> getSchema() {
		return this.schema;
	}

	/**
	 * Creates a MOSDEX schema from a Spark StructType. 
	 * Maps Spark data types to their equivalent Java classes.
	 * Note only Spark data types String, Integer, and Double are supported.
	 * 
	 * @param sparkSchema
	 * @return a MOSDEX schema container
	 */
	protected static MsdxContainer<Class<?>> schemaFromSpark(StructType sparkSchema) {
		MsdxContainer.Builder<Class<?>> builder= MsdxContainer.<Class<?>>builder();
		Iterator<StructField> fields= JavaConverters.asJavaIteratorConverter(sparkSchema.iterator()).asJava();
		StructField field;
		while(fields.hasNext()) {
			field= fields.next();
			if(field.dataType().equals(DataTypes.DoubleType))
				builder.addItem(field.name(), Double.class);
			else if(field.dataType().equals(DataTypes.IntegerType))
				builder.addItem(field.name(), Integer.class);
			else if(field.dataType().equals(DataTypes.StringType))
				builder.addItem(field.name(), String.class);
			else
				throw new IllegalArgumentException("Unsupported type " + field.dataType().simpleString());			
		}
		return builder.build();
	}//schemaFromSpark

	/**
	 * Creates a MOSDEX Record from a Spark Row.
	 * Converts Double to IEEEDouble and call strings to function call objects when called for by the Schema.
	 * Assures that the resulting Record conforms with the Schema of this Dataframe.
	 * 
	 * @param row
	 * @return a MOSDEX record Container
	 */
	protected MsdxRecord recordFromSpark(Row row) {
		Iterator<String> fieldNames= this.schema.itemNames().iterator();
		String fieldName;
		Class<?> fieldType;
		String callString;
		Object item;
		MsdxRecord.Builder builder= MsdxRecord.builder(schema);
		for(int itemIndex= 0; itemIndex< row.size(); itemIndex++) {
			if(!fieldNames.hasNext())
				throw new IllegalArgumentException("Row has more items than table's schema has fields");
			fieldName= fieldNames.next();
			fieldType= this.schema.get(fieldName);
			if(IEEEDouble.class.isAssignableFrom(fieldType))
				item= IEEEDouble.valueOf((Double)row.get(itemIndex));
			else if (MsdxFunctionCall.class.isAssignableFrom(fieldType)) {
				callString= (String)row.get(itemIndex);
				item= MsdxFunctionCall.create(callString, MsdxFunctionCall.getResultTypeFor(fieldType)); 				
			}
			else
				item= row.get(itemIndex);
			builder.addItem(fieldName, item);
		}//for itemIndex
		if(fieldNames.hasNext())
			throw new IllegalArgumentException("Table schema has more fields than row has items");
		return builder.build();
	}//recordFromSpark
	
	/**.
	 * Creates a stream of Rows from a Spark dataset.
	 * Note, this method relies on using an iterator over the dataset's rows; 
	 * it would be preferred to generate a stream directly from the dataset, 
	 * but Spark does not currently support that operation.
	 * 
	 * @return a Java stream of Rows
	 */
	protected Stream<Row> streamOfRows() {
		return 	StreamSupport.
			stream(Spliterators
				.spliteratorUnknownSize(dataframe.toLocalIterator(), 0),	//should try to write from Spark to stream without an iterator 
			false);
	}

	@Override
	public Stream<MsdxContainer<Object>> toStream() {
		return streamOfRows().map(row -> recordFromSpark(row));
	}

	@Override
	public long size() {
		return dataframe.count();
	}

	@Override
	public MsdxContainer<Object> first() {
		return this.recordFromSpark(dataframe.first());
	}

	@Override
	public void forEach(Consumer<MsdxContainer<Object>> action) {
		this.toStream().forEach(action);
	}

	/**
	 * The Dataframe Factory provides public methods for creating dataframes and executing queries.
	 * The Factory also enables registering SQL user-defined functions with the database engine at implements the Dataframe.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Factory implements MsdxDataframe.Factory {
		
		/**
		 * Configuration for Apache Spark applications using MOSDEX.
		 */
		public final SparkConf configuration;
		
		/**
		 * Represents the connection to a Spark cluster.
		 */
		public final JavaSparkContext context;
		
		/**
		 * The entry point to programming Spark with the Dataset API. 
		 */
		public final SparkSession session;
	
		/**
		 * Creates a new Factory instance.
		 * 
		 * @param configuration
		 */
		public Factory(SparkConf configuration) {
			super();
			this.configuration = configuration;
			context= new JavaSparkContext(configuration);
			context.setLogLevel("ERROR");
			session= SparkSession.builder()
				.config(configuration)
				.getOrCreate();
			session.sparkContext().setLogLevel("ERROR");
			for(int numberOfKeyFields= 1; numberOfKeyFields <= 5; numberOfKeyFields++)
				this.registerStringIDFunction(numberOfKeyFields);
			this.registerIntegerInfinity();
			this.registerDoubleInfinity();
		}//Factory
		
		/**
		 * Creates a Spark StructType from a MOSDEX schema. 
		 * Maps Java classes to their equivalent Spark data types.
		 * Note that only Spark data types String, Integer, and Double are supported.
		 * 
		 * @param schema
		 * @return an Apache Spark representation of the schema
		 * @throws IllegalArgumentException if the Java class is not supported in Spark
		 */
		protected StructType schemaToSpark(MsdxContainer<Class<?>> schema) {
			StructType sparkSchema= new StructType();
			Class<?> fieldType;
			for(String fieldName: schema.itemNames()) {
				fieldType= schema.get(fieldName);
				if(fieldType.equals(String.class))		
					sparkSchema= sparkSchema.add(fieldName, 		DataTypes.StringType, true);		
				else if(fieldType.equals(Integer.class))			
					sparkSchema= sparkSchema.add(fieldName, 		DataTypes.IntegerType, true);		
				else if(fieldType.equals(Double.class))		
					sparkSchema= sparkSchema.add(fieldName, 		DataTypes.DoubleType, true);		
				else if(fieldType.equals(IEEEDouble.class))	
					sparkSchema= sparkSchema.add(fieldName, 		DataTypes.DoubleType, true);
				else if(MsdxFunctionCall.class.isAssignableFrom(fieldType)) 
					sparkSchema= sparkSchema.add(fieldName, 		DataTypes.StringType, true);
				else 
					throw new IllegalArgumentException(fieldType.getSimpleName() + " is not a supported type");			
			}//for fieldName
			return sparkSchema;	
		}//schemaToSpark
		
		/**
		 * Converts a MOSDEX record Container to a Spark Row.
		 * 
		 * @param record
		 * @param schema
		 * @return a Spark Row
		 */
		protected Row recordToSpark(MsdxContainer<Object> record, MsdxContainer<Class<?>> schema) {
			Object[] contents= record.toStream()
				.map(entry -> {
					String fieldName= entry.getKey();
					Object fieldValue= entry.getValue();
					if(IEEEDouble.class.isAssignableFrom(schema.get(fieldName)))
						return ((IEEEDouble)fieldValue).exposeDoubleValue();
					if (MsdxFunctionCall.class.isAssignableFrom(schema.get(fieldName)))
						return ((MsdxFunctionCall)fieldValue).getCallString();
					/* else */ 
						return fieldValue;
					}
				)
				.toArray(Object[]::new);
			return RowFactory.create(contents);			
		}//recordToSpark

		/**
		 * Creates a new MOSDEX Dataframe by wrapping a Spark Dataset. 
		 * Registers the corresponding Table for use with Spark SQL.
		 * 
		 * @param tableName
		 * @param spark Dataset
		 * @param schema MOSDEX Container
		 * @return a new Dataframe
		 */
		protected MsdxDataframe create(String tableName, Dataset<Row> spark, MsdxContainer<Class<?>> schema) {
			spark.createOrReplaceTempView(tableName);
			return new MsdxSparkDataframe(spark, schema, this);
		}

		/**
		 * Creates a new Dataframe using Spark from a Java stream. 
		 * Note, this method requires creating an intermediate Java list in memory;
		 * it would be preferable to create the Dataframe directly from the stream, 
		 * but Spark does not currently support that operation.
		 */
		@Override
		public MsdxDataframe create(String tableName, Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> schema) {
			List<Row> rows= records
				.map(record -> recordToSpark(record, schema))
				.collect(Collectors.toList());
			Dataset<Row> spark= session.createDataFrame(rows, schemaToSpark(schema));
			return create(tableName, spark, schema);
		}

		@Override
		public MsdxDataframe create(String tableName, MsdxQuery query, MsdxContainer<Class<?>> schema) {
			return create(tableName, session.sql(query.toSQL()), schema);
		}
	
		@Override
		public void registerStringIDFunction(int numberOfKeyFields) {
			if(numberOfKeyFields>5)
				throw new IllegalArgumentException("Too many key fields");
			if(numberOfKeyFields<1)
				throw new IllegalArgumentException("Too few key fields");
			
			if(numberOfKeyFields==1) {
				this.session.udf().register(
					"ID1", 
					new UDF1<String, String>() { 
						private static final long serialVersionUID = 1L;
						@Override public String call(String key1) throws Exception {
							return key1;
						}//call
					}/*UDF1*/, 
					DataTypes.StringType);
				return;
			}
			if(numberOfKeyFields==2) {
				this.session.udf().register(
					"ID2", 
					new UDF2<String, String, String>() { 
						private static final long serialVersionUID = 1L;
						@Override public String call(String key1, String key2) throws Exception {
							return String.join("_", key1, key2);
						}//call
					}/*UDF2*/, 
					DataTypes.StringType);
				return;
			}
			if(numberOfKeyFields==3) {
				this.session.udf().register(
					"ID3", 
					new UDF3<String, String, String, String>() { 
						private static final long serialVersionUID = 1L;
						@Override public String call(String key1, String key2, String key3) throws Exception {
							return String.join("_", key1, key2, key3);
						}//call
					}/*UDF3*/, 
					DataTypes.StringType);
				return;
			}
				if(numberOfKeyFields==4) {
					this.session.udf().register(
						"ID4", 
						new UDF4<String, String, String, String, String>() { 
							private static final long serialVersionUID = 1L;
							@Override public String call(String key1, String key2, String key3, String key4) throws Exception {
								return String.join("_", key1, key2, key3, key4);
							}//call
						}/*UDF4*/, 
						DataTypes.StringType);
					return;
			}
				if(numberOfKeyFields==5) {
					this.session.udf().register(
						"ID5", 
						new UDF5<String, String, String, String, String, String>() { 
							private static final long serialVersionUID = 1L;
							@Override public String call(String key1, String key2, String key3, String key4, String key5) throws Exception {
								return String.join("_", key1, key2, key3, key4, key5);
							}//call
						}/*UDF5*/, 
						DataTypes.StringType);
					return;
			}
		}//registerStringIDFunction
		
		/**Currently unsupported.*/
		@Override
		public void registerIntegerIDFunction(int numberOfKeyFields) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void registerFunctionCall(String functionName) {
			this.session.udf().register(
				functionName, 
				new UDF1<String, String>() { 
					private static final long serialVersionUID = 1L;
					@Override public String call(String arguments) throws Exception {			
						return functionName + "(" + arguments + ")";
					}//call
				}/*UDF1*/, 
				DataTypes.StringType);
		}//registerFunctionCall

		@Override
		public void registerIntegerInfinity() {
			this.session.udf().register(
				"I_INFINTY", 
				new UDF1<String, Integer>() { 
					private static final long serialVersionUID = 1L;
					@Override public Integer call(String sign) throws Exception {
						return (sign.equals("-")) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
					}//call
				}/*UDF1*/, 
				DataTypes.IntegerType);
			return;
		}//registerIntegerInfinity

		@Override
		public void registerDoubleInfinity() {
			this.session.udf().register(
				"INFINITY", 
				new UDF1<String, Double>() { 
					private static final long serialVersionUID = 1L;
					@Override public Double call(String sign) throws Exception {
						return (sign.equals("-")) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
					}//call
				}/*UDF1*/, 
				DataTypes.DoubleType);
			return;
		}//registerDoubleInfinity
		
	}//class MsdxSparkDataframe.Factory 
	

}//class MsdxSparkDataframe
