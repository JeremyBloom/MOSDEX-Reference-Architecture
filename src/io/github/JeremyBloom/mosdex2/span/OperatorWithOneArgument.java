/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.span;

import java.util.function.Consumer;

import io.github.JeremyBloom.mosdex2.MsdxContainer;

/**
 * An Operator analog of Java Function that keeps track of the Container Schema of
 * the result.
 * <p>
 * An Operator with one argument is a transformation that acts on a data Container 
 * and returns a Container result. Because the data in a Container 
 * requires a Schema to specify the types of its items, the Operator needs to specify 
 * the Schema of its result. Thus, defining an Operator requires specifying
 * two abstract methods:
 * <ul style="list-style-type:bullet;">
 * <li> the withResultSchema method creates the Schema of the result Containers, usually by modifying the Schema 
 * of the argument Containers</li>
 * <li> the apply method transforms the argument Container into the result Container</li>
 * </ul>
 * <p>
 * Examples of how to implement these methods are given in their documentation.
 * <p>
 * This Operator class also includes several auxiliary methods that enable 
 * retrieving the result Schema and viewing the Containers as they are transformed.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public abstract class OperatorWithOneArgument {
	
	/**Holds the Schema of the result Containers*/
	protected MsdxContainer<Class<?>> resultSchema;

	/**
	 * Constructs a new OperatorWithOneArgument object.
	 * <p>
	 * Note: in usage, a concrete instance of this class is often created inline,
	 * with the following syntax:<br>
	 * 
	 * <pre><code>
	 * new OperatorWithOneArgument() {
	 * 
	 * 	{@literal @Override} public OperatorWithOneArgument withResultSchema(
	 * 		MsdxContainer<Class<?>> inputSchema)
	 * 	{
	 * 		...
	 * 		return this;
	 * 	}//withResultSchema
	 * 
	 * 	{@literal @Override} protected MsdxContainer<Object> apply(
	 * 		MsdxContainer<Object> input)
	 * 	{
	 * 		...
	 * 	}//apply
	 * 
	 * };//OperatorWithOneArgument
	 * 
	 * </code></pre>
	 */	
	public OperatorWithOneArgument() {
		super();
		this.resultSchema = null;
	}
	
	/**
	 * Specifies the Schema of the result Containers. This method is called
	 * invisibly and automatically by the Span transformations that use a one
	 * argument Operator, including map. You can also call it explicitly when
	 * creating an Operator instance. To avoid the automatic call overwriting the
	 * explicit call, include an early return statement as indicated in the code
	 * below.
	 * <p>
	 * Here is an example of how to write a concrete implementation of this
	 * method:<br>
	 * <pre><code>
	 * {@literal @Override} public OperatorWithOneArgument withResultSchema(MsdxContainer<Class>> parameterSchema) {	
	 * 	if(this.resultSchema!=null)//early return												
	 * 		return this;																	
	 * 	this.resultSchema= MsdxContainer.<Class>>builder()										
	 * 		.copyItem(parameterSchema, "...")												
	 * 		.addItem("Name", String.class)													
	 * 		...																				
	 * 		.build();																		
	 *   return this;																			
	 * }//withResultSchema	
	 * </code></pre>
	 * <p>
	 * @param inputSchema used to copy fields from the input Schema; this is an
	 *                    optional parameter and can be set as empty or null if the
	 *                    input fields are not used; fields that are not part of the
	 *                    input Schema can also be specified.
	 * 
	 * @return a Schema Container
	 * 
	 * @throws IllegalStateException if the result schema has already been specified
	 */
	public abstract OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema);
		// The schema definition goes here.
		// Optional early return
		//    if(this.resultSchema!=null)
		//        return this;
		// This method must include
		//	  this.inputSchema= inputSchema;

	/**@return the Schema of the result Containers*/
	public MsdxContainer<Class<?>> getResultSchema() {
		if(this.resultSchema == null)
			throw new IllegalStateException("Result schema has not been defined");
		return this.resultSchema;
	}
	
	/**
	 * Transforms an input Container to a result Container.
	 * <p>
	 * Here is an example of how to write a concrete implementation of this
	 * method:<br>
	 * <pre><code>
	 * {@literal @Override}																
	 * public MsdxContainer<Object> apply(MsdxContainer<Object> input) { 	
	 * 	...																	 	
	 * 	return MsdxRecord.builder(this.getResultSchema())					
	 * 		.copyItem(input, "itemName")							
	 * 		.addItem("Name", "name")							
	 * 		...													
	 * 		.build();													
	 * }//apply																			
	 * </code></pre>
	 * @param input Container from the applicable Span 
	 * 
	 * @return a Record Container
	 * 
	 * @throws IllegalArgumentException if the result Container is inconsistent with
	 *                                  the result Schema (i.e. missing or extra
	 *                                  fields or type mismatch on any field)
	 */
	public abstract MsdxContainer<Object> apply(MsdxContainer<Object> input); 
		// The input to result transformation goes here.

	/**
	 * Returns the result Containers generated by this Operator, 
	 * additionally performing the provided action on each Container 
	 * as those Containers are consumed.
	 * <p>
	 * Usage:<br>
	 * <pre><code>
	 * OperatorWithTwoArguments combiner= ...;												
	 * terms.leftOuterJoin(variables, "Column", combiner)										
	 * 	.map(combiner.peek(record -> System.out(record.get(keyFieldName).toString));	
	 * </code></pre>
	 * <p>
	 * Note: this directive is not executed until a terminal operation is performed on the result Span.
	 *  
	 * @param consumer specifies the action to be performed as the Containers are returned
	 * @return a one argument Operator
	 */
	public OperatorWithOneArgument peek(Consumer<MsdxContainer<Object>> consumer) {
		
		OperatorWithOneArgument peek= new OperatorWithOneArgument() {

			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> inputSchema) {
				this.resultSchema= inputSchema;
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> record) {
				consumer.accept(record);
				return record;
			}//apply
			
		};//OperatorWithOneArgument
		 
		return peek.withResultSchema(this.getResultSchema());
	}//peek

	
}//class OperatorWithOneArgument