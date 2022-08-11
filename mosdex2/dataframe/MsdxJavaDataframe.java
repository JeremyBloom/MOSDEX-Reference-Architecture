/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.dataframe;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery;

/**
 * A dummy Dataframe for testing purposes. Does not support SQL queries.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxJavaDataframe implements MsdxDataframe {
	
	private List<MsdxContainer<Object>> dataframe;
	
	private MsdxContainer<Class<?>> schema;
	
	private MsdxJavaDataframe.Factory factory;

	/**
	 * Constructs a new Dataframe instance. 
	 * Use of this constructor is discouraged; use a create method of the Factory.
	 * 
	 * @param dataframe
	 * @param schema
	 * @param factory
	 */
	protected MsdxJavaDataframe(List<MsdxContainer<Object>> dataframe, MsdxContainer<Class<?>> schema, Factory factory) {
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
	protected MsdxJavaDataframe(MsdxContainer<Class<?>> schema, MsdxJavaDataframe.Factory factory) {
		this(new LinkedList<MsdxContainer<Object>>(), schema, factory);
	}

	@Override
	public MsdxContainer<Class<?>> getSchema() {
		return this.schema;
	}

	@Override
	public Stream<MsdxContainer<Object>> toStream() {
		return this.dataframe.stream();
	}

	@Override
	public long size() {
		return dataframe.size();
	}

	@Override
	public MsdxContainer<Object> first() {
		return dataframe.get(0);
	}

	@Override
	public void forEach(Consumer<MsdxContainer<Object>> action) {
		this.dataframe.forEach(action);
	}

	@Override
	public MsdxDataframe.Factory getFactory() {
		return this.factory;
	}

	/**
	 * The Dataframe Factory provides public methods for creating Dataframes and executing queries.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 */
	public static class Factory implements MsdxDataframe.Factory {
	
		public Factory() {
			super();
		}
	
		@Override
		public MsdxDataframe create(String tableName, Stream<MsdxContainer<Object>> records, MsdxContainer<Class<?>> schema) {
			return new MsdxJavaDataframe(records.collect(Collectors.toList()), schema, this);
		}
	
		/**
		 * SQL queries are not supported for Java Dataframes.
		 */
		@Override
		public MsdxDataframe create(String tableName, MsdxQuery query, MsdxContainer<Class<?>> schema) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Not supported.
		 */
		@Override
		public void registerStringIDFunction(int numberOfKeyFields) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Not supported.
		 */
		@Override
		public void registerIntegerIDFunction(int numberOfKeyFields) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Not supported.
		 */
		@Override
		public void registerFunctionCall(String functionName) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Not supported.
		 */
		@Override
		public void registerIntegerInfinity() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Not supported.
		 */
		@Override
		public void registerDoubleInfinity() {
			throw new UnsupportedOperationException();
		}
	
	}//class MsdxJavaDataframe.Factory
	

}//class MsdxJavaDataframe
