/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.span;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.github.JeremyBloom.mosdex2.MsdxContainer;

/**
 * An Operator analog of Java BinaryOperator that keeps track of the Container Schema of
 * the result.
 * <p>
 * An Operator with two arguments is a transformation that acts two a data Containers 
 * and returns a Container result. Because the data in a Container 
 * requires a Schema to specify the types of its items, the Operator needs to specify 
 * the Schema of its result. 
 * <p>
 * Most applications of this class involve matching a key field 
 * in the two argument Containers. 
 * The apply method, which implements the transformation, 
 * takes an Optional for each of the arguments 
 * to deal with the case where there is no matching key in one of 
 * the Spans that supply the arguments. 
 * If the keys match, the apply method calls the onKeyMatch method to create the result Container.
 * When there is no matching key, the apply method calls the noKeyMatch method, 
 * which processes the items from the argument that is present and
 * fills the empty items from the absent argument 
 * with some appropriate, context-specific default values (often Java null). 
 * The missing keys are recorded, and 
 * can be retrieved with the getMissingKeys method.
 * <p>
 * Thus, defining a two argument Operator requires specifying
 * three abstract methods:
 * <ul style="list-style-type:bullet;">
 * <li> the withResultSchema method creates the Schema of the result Containers, usually by modifying the Schema 
 * of the argument Containers</li>
 * <li> the onKeyMatch method transforms the two argument Containers with matching keys into the result Container</li>
 * <li> the noKeyMatch method transforms the argument that is present 
 * and fills the items from the absent argument with default values</li>
 * </ul>
 * <p>
 * Examples of how to implement these methods are given in their documentation.
 * <p>
 * This Operator class also includes several auxiliary methods that enable 
 * retrieving the result Schema and viewing the Containers as they are transformed, 
 * among other capabilities.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public abstract class OperatorWithTwoArguments {

	/**Holds the Schema of the result Containers.*/
	protected MsdxContainer<Class<?>> resultSchema;
	
	/**Holds the Schema of the left argument.*/
	protected MsdxContainer<Class<?>> leftInputSchema;

	/**Holds the Schema of the right argument.*/
	protected MsdxContainer<Class<?>> rightInputSchema;

	/**Holds the name of the key field in the left argument.*/
	protected String leftKeyFieldName;

	/**Holds the name of the key field in the right argument.*/
	protected String rightKeyFieldName;

	/**True if the key fields in two arguments match.*/
	protected boolean keysMatch;
	
	/**Indicates whether the left, right, or neither argument is not present.*/
	protected String isNotPresent;
	
	/**
	 * 
	 * Constructs a new OperatorWithTwoArguments object.
	 * <p>
	 * Note: in usage, a concrete instance of this class is often created inline,
	 * with the following syntax:<br>
	 * <pre><code>
	 * new OperatorWithTwoArguments() {
	 * <br>
	 * 	{@literal @Override} public OperatorWithTwoArguments withResultSchema(
	 * 		MsdxContainer<Class<?>> leftInputSchema,
	 * 		leftKeyFieldName,
	 * 		MsdxContainer<Class<?>> rightInputSchema,
	 * 		String rightKeyFieldName)
	 * 	{
	 * 		...
	 * 		return this;
	 * 	}//withResultSchema
	 * <br>
	 * 	{@literal @Override} protected MsdxContainer<Object> onKeyMatch(
	 * 		MsdxContainer<Object> left,
	 * 		MsdxContainer<Object> right)
	 * 	{
	 * 		...
	 * 	}//onKeyMatch
	 * <br>
	 * 	{@literal @Override} protected MsdxContainer<Object> noKeyMatch(
	 * 		Optional<MsdxContainer<Object>> left,
	 * 		Optional<MsdxContainer<Object>> right)
	 * 	{
	 * 		...
	 * 	}//noKeyMatch
	 * <br>
	 * };//OperatorWithTwoArguments
	 * </code></pre>
	 */	
	public OperatorWithTwoArguments() {
		super();
		this.resultSchema = null;
		this.leftInputSchema = null;
		this.leftKeyFieldName = null;
		this.rightInputSchema = null;
		this.rightKeyFieldName = null;
		this.keysMatch = false;
		this.isNotPresent = "";
	}

	/**
	 * Specifies the Schema of the result Containers. This method is called
	 * invisibly and automatically by the Span transformations that use a two
	 * argument Operator, including the joins and reduce. You can also call it
	 * explicitly when creating an Operator instance. To avoid the automatic call
	 * overwriting the explicit call, include an early return statement as indicated
	 * in the comment below.
	 * <p>
	 * Here is an example of how to write a concrete implementation of this
	 * method:<br>
	 * <pre><code>
	 * {@literal @Override} 
	 * public OperatorWithTwoArguments withResultSchema(
	 * 	MsdxContainer<Class<?>> leftInputSchema, 
	 * 	String leftKeyFieldName,
	 * 	MsdxContainer<Class<?>> rightInputSchema,
	 * 	String rightKeyFieldName)
	 * {
	 * 	if(this.resultSchema!=null)
	 * 		return this;
	 * 	...
	 * 	this.resultSchema= MsdxContainer.<Class<?>>builder()
	 * 		.copyItem(leftInputSchema, "itemName")
	 * 		.addItem("Name", String.class)
	 * 		...													
	 * 		.build();
	 * 	return this;
	 * }//withResultSchema
 	 * </code></pre>
 	 * 
	 * @param leftInputSchema   used to copy fields from the input; this is an
	 *                              optional parameter and can be set as empty or
	 *                              null if the input fields are not used; fields
	 *                              that are not part of the input schema can also
	 *                              be specified.
	 * 
	 * @param leftKeyFieldName  in the leftInputSchema
	 * 
	 * @param rightInputSchema  used to copy fields from the input; this is an
	 *                              optional parameter and can be set as empty or
	 *                              null if the input fields are not used; fields
	 *                              that are not part of the input schema can also
	 *                              be specified.
	 * 
	 * @param rightKeyFieldName in the rightInputSchema
	 * 
	 * @return this Operator
	 * 
	 * @throws IllegalStateException if the result schema has already been specified
	 */
	public abstract OperatorWithTwoArguments withResultSchema(
		MsdxContainer<Class<?>> leftInputSchema, 
		String leftKeyFieldName, 
		MsdxContainer<Class<?>> rightInputSchema, 
		String rightKeyFieldName);
	// The schema definition goes here.
	// Optional early return
	//    if(this.resultSchema!=null)
	//        return this;
	// This method must include
	//	  this.leftInputSchema= leftInputSchema;
	//	  this.leftKeyFieldName= leftKeyFieldName;
	//	  this.rightInputSchema= rightInputSchema;
	//	  this.rightKeyFieldName= rightKeyFieldName;
	//    this.resultSchema= ...
	//	  return this;

	/**@return the Schema of the result Containers*/
	public MsdxContainer<Class<?>> getResultSchema() {
		if(this.resultSchema == null)
			throw new IllegalStateException("Result schema has not been defined");
		return this.resultSchema;
	}
	
	/**
	 * Transforms the two input Containers to the result Container.
	 * The apply method takes an Optional object for each argument 
	 * to deal with the case where there is no matching key in the other Span, 
	 * as can happen in a join operation.
	 * If the keys match, it calls the onKeyMatch method to create the result Container.
	 * When there is no matching key in the one of the Spans, the noKeyMatch method fills the empty 
	 * components with some appropriate, context-specific default values. 
	 * 
	 * @param left Span Container input 
	 * @param right Span Container input
	 * @return the result Container
	 * @throws IllegalArgumentException if the result container is inconsistent with the result schema 
	 * (i.e. missing or extra fields or type mismatch on any field)
	 */
	public MsdxContainer<Object> apply(Optional<MsdxContainer<Object>> left, Optional<MsdxContainer<Object>> right) {
		if(left.isPresent() && right.isPresent()) {
			this.keysMatch= true;
			this.isNotPresent= "neither";
			return onKeyMatch(left.get(), right.get());
		}
		this.keysMatch= false;
		this.isNotPresent= !right.isPresent() ? "right" : "left";
		return noKeyMatch(left, right);
	}//apply
	
	/**
	 * Creates the result Container from two input Containers, assuming that both are present. 
	 * <p>
	 * Here is an example of how to write a concrete implementation of this
	 * method:<br>
	 * <pre><code>
	 * {@literal @Override}																
	 * protected MsdxContainer<Object> onKeyMatch(
	 * 	MsdxContainer<Object> left,
	 * 	MsdxContainer<Object> right) 
	 * {
	 * 	...
	 * 	return MsdxRecord.builder(this.getResultSchema())
	 * 		.copyItem(left, "itemName")
	 * 		...
	 * 		.addItem("Name", "name")							
	 * 		.build();
	 * }//onKeyMatch
 	 * </code></pre>
 	 * 
	 * @param left Span Container input
	 * @param right Span Container input
	 * @return the result Container
	 */
	protected abstract MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> right);
	// The inputs to result transformation goes here.

	/**
	 * Creates a result Container with default values for some the items when there is no Container in one of the 
	 * Spans whose key matches a Container in the other Span.
	 * 
	 * The arguments of the noKeyMatch method are used to differentiate the default values 
	 * when an empty optional value of one of the Containers is returned;
	 * may which depend on other fields in the non-empty Container; 
	 * usually it would include the key field of the merged Container.
	 * Note: one and only one of the two parameters can be empty.
	 * <p>
	 * Here is an example of how to write a concrete implementation of this
	 * method:<br>
	 * <pre><code>
	 * {@literal @Override}																
	 * protected MsdxContainer<Object> noKeyMatch(
	 * 	Optional<MsdxContainer<Object>>	left, 
	 * 	Optional<MsdxContainer<Object>> right) 
	 * {
	 * 	if(!(left.isPresent() ^ right.isPresent()))
	 * 		throw new IllegalStateException("One and only one of the parameters can be present");
	 * 	MsdxContainer<Object> present= left.isPresent() ?
	 * 		left.get() :
	 * 		right.get();
	 * 	MsdxContainer<Class<?>> absentSchema= left.isPresent() ?
	 * 		this.rightInputSchema.delete(this.rightKeyFieldName) :
	 * 		this.leftInputSchema.delete(this.leftKeyFieldName);
	 * 	MsdxRecord.Builder builder= MsdxRecord.builder(this.getResultSchema())
	 * 		.copyItems(present);
	 * 	for(String fieldName: absentSchema.itemNames())
	 * 		builder.addItemIf(!present.containsField(fieldName), fieldName, null);
	 * 	return builder.build();
	 * }//noKeyMatch
 	 * </code></pre>
	 * 
	 * @param left Span Container input if present
	 * @param right  Span Container input if present
	 * @return the result Container
	 * @throws IllegalStateException if the arguments are both present or both empty.
	 */
	protected abstract MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> left, Optional<MsdxContainer<Object>> right);
	// The default result definition goes here.

	/**@return if there is a matching key in both Spans*/
	public boolean hasKeyMatch() {
		return keysMatch;
	}

	/**@return left, right, or neither depending on which argument is not present*/
	public String isNotPresent() {
		return isNotPresent;
	}

	/**
	 * Returns the result Containers generated by this Operator, 
	 * additionally performing the provided action on each Container 
	 * as they are consumed.
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
	
	/**
	 * Used after a join, this Operator finds the keys from each Span that have no match in the other Span.
	 * <p>
	 * Usage:<br>
	 * <pre><code>
	 * OperatorWithTwoArguments combiner= ...;
	 * 	Map<String, Object> result= new ...;
	 * 	terms.leftOuterJoin(variables, "Column", combiner).
	 * 		.map(combiner.unmatchedKeys(result));
	 * 	do something with result
	 * </code></pre>
	 * <p>
	 * Note: this directive is not executed until a terminal operation is performed on this span.
	 *  
	 * @param result is a map modified by this method to include the unmatched keys.
	 * @return set of unmatched keys in the left and right Spans
	 */
	public OperatorWithOneArgument unmatchedKeys(final Map<String, Set<Object>> result) {
		result.clear();
		result.put("left", new LinkedHashSet<Object>());
		result.put("right", new LinkedHashSet<Object>());
		return OperatorWithTwoArguments.this.peek(
			record -> 
				{if(!OperatorWithTwoArguments.this.hasKeyMatch()) {
					if(OperatorWithTwoArguments.this.isNotPresent.equals("right"))
						result.get("left").add(record.get(leftKeyFieldName));
					else //left is not present
						result.get("right").add(record.get(rightKeyFieldName));
					}
				}
		);//return		
	}//missingKeys
	
	/**
	 * Filters records that have matching keys, 
	 * and removes those that do not, to create an inner join.
	 * <p>
	 * Usage:<br>
	 * <pre><code>
	 * OperatorWithTwoArguments combiner= ...;
	 * terms.leftOuterJoin(variables, "Column", combiner)
	 * 	.filter(combiner.inner());
	 * </code></pre>
	 * <p>    
	 * Note: this directive is not executed until a terminal operation is performed on this span.
	 *  
	 * @return a Java Predicate
	 */
	public Predicate<MsdxContainer<Object>> inner() {
		return record -> OperatorWithTwoArguments.this.hasKeyMatch();
	}//inner
	
			
}//class OperatorWithTwoArguments