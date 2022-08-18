/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This family of classes represent calls to a solver function for use in a
 * MOSDEX Table schema. A Function Call is a string of the form 
 * <pre><code>
 * functionName(argumentField1, ...argumentFieldN).
 * </code></pre>
 * The argumentFields are the names of the fields in the current record
 * where the arguments are found. A Function Call has a result type, which may
 * be one of Double, IEEEDouble, Integer, or String, which is specified by the
 * constructor; member classes of this class implement these result types. In
 * the current implementation of MOSDEX, the function argumentFields are
 * ignored, since the solver relies on the row or column identifiers of each
 * record to match the solver results with the Function Calls in that record.
 * Note that a Function Call is not a user-defined function executed in the SQL;
 * rather, a Function Call is executed in the layer below the the Dataframe in
 * the Model.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxFunctionCall implements Serializable {
	
	private static final long serialVersionUID = 674809908386084840L;
	
	protected String functionName;
	
	/**the names of the fields in the current record where the arguments are found.*/
	protected List<String> argumentFields;
	
	/**The value returned by the function. Null before the call has been executed.*/
	protected Object value;
	
	/**The type of the result value.*/
	protected Class<?> resultType;
	
	/**
	 * Used to find identifiers in the parse method.
	 * Matches an identifier: lower or upper case letter or dollar sign 
	 * followed by zero or more combinations of lower or upper case letters or numerals or dollar signs or hyphens
	 */	
	private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_$][a-zA-Z\\d_$-]*");
	
	/**
	 * Matches strings of the form "identifier(identifier, identifier, ...)" where parentheses are optional.
	 * Parentheses are (...) or &lt...&gt or space, where the latter two are used in SQL to avoid conflicts with ( and ).
	 * Generally ignores white space between identifiers and the separators commas and parentheses.
	 */
	private static final Pattern FUNCTION_CALL= Pattern.compile("\\s*" +  IDENTIFIER.toString() + "\\s*[<\\(]?" + IDENTIFIER.toString() + "(\\s*,\\s*" + IDENTIFIER.toString() + "\\s*)*" + "[>\\)]?");
	
	/**Used to find integer values.*/
	private static final Pattern INTEGER = Pattern.compile("[1-9][0-9]*");

	/**
	 * Creates a new instance from a call string. The preferred way to create an
	 * instance is to use the static factory method create.. The format of the call
	 * string is 
	 * <br>
	 * <code>functionName(argumentField1, ... argumentFieldN)</code> 
	 * <br>where
	 * the function name is any legal Java identifier and the argument fields are
	 * legal Java identifiers. The opening parentheses separates the two; spaces or
	 * &lt and &gt are acceptable alternatives to ( and ).
	 * 
	 * @param callString
	 * 
	 * @param resultType
	 */
	public MsdxFunctionCall(String callString, Class<?> resultType) {
		super();
		this.functionName = null;
		this.argumentFields= new ArrayList<String>();
		this.resultType = resultType;
		this.value = null;	
		parse(callString);		
	}//Msdx2FunctionCall
	
	/**
	 * Creates a FunctionCall instance from a call string.
	 * 
	 * @param callString
	 * @param resultType (must be Double, IEEEDouble, Integer, or String)
	 * @return a new FunctionCall instance
	 */
	public static MsdxFunctionCall create(String callString, Class<?> resultType) {
		if (resultType.equals(Double.class))
			return new MsdxFunctionCall.DOUBLE(callString);
		if (resultType.equals(IEEEDouble.class))
			return new MsdxFunctionCall.IEEEDOUBLE(callString);
		if (resultType.equals(Integer.class))
			return new MsdxFunctionCall.INTEGER(callString);
		if (resultType.equals(String.class))
			return new MsdxFunctionCall.STRING(callString);
		throw new IllegalArgumentException("Unsupported function type " + resultType.getName());
	}//create
	
	/**
	 * Parses the call string to find the function name and the argument field
	 * names. Normally, parsing occurs automatically when the Function Call object
	 * is created, and the parse method should not be called directly.
	 * 
	 * @param callString has the format
	 *  <br><code>functionName(argumentField1, ... argumentFieldN)</code>
	 *  <br>where parentheses are ( and ), spaces, or &lt and &gt.
	 */
	protected void parse(String callString) {
		if(this.functionName!=null || /*this.argument!=null*/ !this.argumentFields.isEmpty())
			throw new IllegalArgumentException("Function has already been defined");
		
		Matcher function= FUNCTION_CALL.matcher(callString);
		if(!function.matches()) {
			Matcher region;
			String matched;
	        for (int i = callString.length(); i > 0; --i) {
	            region = function.region(0, i);
	            if (region.matches() || region.hitEnd()) {
	                matched= callString.substring(0, i)+ '^' + callString.substring(i, callString.length());
				throw new IllegalArgumentException("Illegal call syntax: " + matched + "^");
	            }	
	        }
		}
	        
		Matcher tokens= IDENTIFIER.matcher(callString);
		if(!tokens.find())
			throw new IllegalArgumentException("Function name not found: " + callString);
		this.functionName= tokens.group();
		while(tokens.find()) { //collect arguments
	        argumentFields.add(tokens.group());
		}
		
		return;
	}//parse

	/**@return the function name*/
	public String getFunctionName() {
		return this.functionName;
	}
	
	/**
	 * @param index
	 * @return the argument field at the given index
	 */
	public Object getArgumentField(int index) {
		return this.argumentFields.get(index);
	}
	
	/**@return the number of argument fields*/
	public int getNumberOfArguments() {
		return this.argumentFields.size();
	}
	
	/**@return an iterator over the argument fields*/
	public Iterator<String> getArgumentIterator() {
		return this.argumentFields.iterator();
	}
	
	/**@return the result type*/
	public Class<?> getResultType() {
		return this.resultType;
	}
	
	/**@return the field type used in a MOSDEX Schema (e.g. DOUBLE_FUNCTION)*/
	public String getSchemaType() {
		return this.getResultType().getSimpleName().toUpperCase()+"_FUNCTION";
	}

	/**
	 * @param fieldType
	 * @return true if the fieldType class is a subclass of FunctionCall, false otherwise  
	 */
	public static boolean isFunctionCall(Class<?> fieldType) {
		return MsdxFunctionCall.class.isAssignableFrom(fieldType);
	}
	
	/**
	 * @param functionType
	 * @return the result type for this function type
	 */
	public static Class<?> getResultTypeFor(Class<?> functionType) {
		if(functionType.equals(MsdxFunctionCall.DOUBLE.class))		return Double.class;					
		if(functionType.equals(MsdxFunctionCall.IEEEDOUBLE.class))	return IEEEDouble.class;					
		if(functionType.equals(MsdxFunctionCall.INTEGER.class))	return Integer.class;					
		if(functionType.equals(MsdxFunctionCall.STRING.class))		return String.class;					
		throw new IllegalArgumentException("Not a function call");
	}//getResultTypeFor

	/**
	 * @return the value of this Function Call (usually set by the solver)
	 */
	public Object getValue() {
		return this.value;
	}
	
	/**@return true if the solver has set a value for this Function Call, false otherwise*/
	public boolean hasValue() {
		return value!=null;
	}

	/**
	 * @param value to set
	 * @return this Function Call
	 * @throws IllegalArgumentException if the value has already been set
	 */
	public MsdxFunctionCall setValue(Object value) {
		if(this.value!=null)
			throw new IllegalArgumentException("Value has already been set");
		this.value = value;
		return this;
	}
	
	/**
	 * Finds the fields in the given schema corresponding to Function Calls.
	 * @param recordSchema
	 * @return the set of field names of the Function Calls
	 */
	public static Set<String> findFunctionFieldsIn(MsdxContainer<Class<?>> recordSchema) {
		return recordSchema.toStream()
			.filter(entry -> MsdxFunctionCall.isFunctionCall(entry.getValue()))
			.collect(Collectors.toMap(
				entry -> entry.getKey(), 
				entry -> entry.getValue(), 
				(leftValue, rightValue) -> leftValue,	//can't happen 
				LinkedHashMap::new))					//used to preserve the order of the fields
			.keySet();
	}

	/**@return the call string of this Function Call*/
	public String getCallString() {
		StringBuilder result= new StringBuilder();
		result
			.append(functionName)
			.append("(");
		this.argumentFields.forEach(argument -> result.append(String.valueOf(argument) + ", "));
		result.deleteCharAt(result.length()-1);	//final space
		result.setCharAt(result.length()-1, ')');	//final comma
		return result.toString();
	}
	
	/**
	 * Tests whether the function is a valid call.
	 * 
	 * @param definedFunctions names of functions (usually this comes from a function table defined in the Solver Modeling Factory)
	 * @param schema of the solution element for which the Function Calls apply
	 * @throws IllegalArgumentException if <ul> 
	 * <li>the function name does not appear in a set of function definitions,</li> 
	 * <li>the function is not applicable for the given table class, or</li> 
	 * <li>any argument is not among the fields of the table schema</li>
	 * </ul>
	 */
	public void validate(Set<String> definedFunctions, MsdxContainer<Class<?>> schema) {
		if(!definedFunctions.contains(this.getFunctionName()))
			throw new IllegalArgumentException("Undefined function " + this.functionName);
		Set<String> missing= this.argumentFields.stream()
			.filter(fieldName -> !schema.containsField(fieldName))
			.collect(Collectors.toSet());
		if(!missing.isEmpty())
			throw new IllegalArgumentException("Missing arguments(s) " + missing.toString());								
	}
	
	/**@return the call string of this Function Call*/
	public String toString() {
		return this.getCallString();
	}

	/**
	 * This class represents a Function Call that returns a Double value.
	 */
	public static class DOUBLE extends MsdxFunctionCall {
		
		private static final long serialVersionUID = -2049396571035874694L;
		
		/**
		 * Create a new function call with a Double result.
		 * 
		 * @param callString
		 */
		public DOUBLE(String callString) {
			super(callString, Double.class);
//			Msdx.GLOBAL.out.println("In Msdx2FunctionCall.DOUBLE constructor: " + String.valueOf(this));
		}

		@Override
		public String getSchemaType() {
			return "DOUBLE_FUNCTION";
		}

	}//class Msdx2FunctionCall.DOUBLE

	/**
	 * This class represents a Function Call that returns an IEEE Double value.
	 */
	public static class IEEEDOUBLE extends MsdxFunctionCall{
	
		private static final long serialVersionUID = 1337888127811670568L;

		/**
		 * Create a new function call with an IEEE Double result.
		 * 
		 * @param callString
		 */
		public IEEEDOUBLE(String callString) {
			super(callString, IEEEDouble.class);
		}

		@Override
		public String getSchemaType() {
			return "IEEEDOUBLE_FUNCTION";
		}

	}//class Msdx2FunctionCall.IEEEDOUBLE

	/**
	 * This class represents a Function Call that returns an Integer value.
	 */
	public static class INTEGER extends MsdxFunctionCall {
	
		private static final long serialVersionUID = 1115390954026032277L;
		
		/**
		 * Create a new function call with an Integer result.
		 * 
		 * @param callString
		 */
		public INTEGER(String callString) {
			super(callString, Integer.class);
		}

		@Override
		public String getSchemaType() {
			return "INTEGER_FUNCTION";
		}

	}//class Msdx2FunctionCall.INTEGER

	/**
	 * This class represents a Function Call that returns a String value.
	 */
	public static class STRING extends MsdxFunctionCall {
	
		private static final long serialVersionUID = -6607056512926648194L;
		
		/**
		 * Create a new function call with a String result.
		 * 
		 * @param callString
		 */
		public STRING(String callString) {
			super(callString, String.class);
		}
	
		@Override
		public String getSchemaType() {
			return "STRING_FUNCTION";
		}

	}//class Msdx2FunctionCall.STRING
	

}//class Msdx2FunctionCall
