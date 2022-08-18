/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import io.github.JeremyBloom.mosdex2.span.OperatorWithOneArgument;
import io.github.JeremyBloom.mosdex2.span.OperatorWithTwoArguments;

/**
 * This interface provides the template for creating and using all solver-specific modeling objects,
 * Variables, Constraints, Objectives, and Terms. 
 * It encapsulates all of the solver-specific classes needed to create, solve, 
 * and use an optimization model specified in MOSDEX. 
 * Other than in the implementations of this factory for different solvers, 
 * the solver-specific APIs are not exposed to MOSDEX.
 * <p>
 * This interface defines (and in some cases, provides default implementations) 
 * of the transformations used to create the bridge from a MOSDEX modeling object Table
 * to its presentation as a solver-specific object. These transformations take 
 * the form of operators with one or two Container arguments. The operators provide the means 
 * to transform the Container and their schemas simultaneously, to prevent inconsistencies between them.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public interface MsdxSolverModelingFactory {
	
	/**
	 * @return the orientation, either "Row" or "Column".
	 * The orientation should be set in the constructor and 
	 * should be the same for all modeling objects created by the factory.
	 */
	String orientation();
	
	/**
	 * @return the opposite of the orientation, either "Column" for row orientation 
	 * or "Row" for column orientation.
	 * The orientation should be set in the constructor and 
	 * should be the same for all modeling objects created by the factory.
	 */
	default String contraOrientation(){
		if(orientation().equalsIgnoreCase("Column"))
			return "Row";
		if(orientation().equalsIgnoreCase("Row"))
			return "Column";
		throw new IllegalArgumentException(orientation() + " is not an allowed orientation");
	}
	
	/**
	 * Returns the order in which the modeling objects are created.
	 * In general, MOSDEX Terms cannot be read to create solver expressions until both the 
	 * solver variables, solver constraints, and solver objectives involved 
	 * in the expression have been created. However, the order differs for row- or column-orientation: 
	 * in row orientation variables are created before constraints and objectives, 
	 * while the reverse is true for column-orientation.
	 * <p>
	 * Note, that aside from this general consideration, the query resolver will assure that 
	 * no table is used before its dependent tables have been resolved.
	 * 
	 * @return the creation order
	 */
	default Set<String> creationOrder() {
		if(orientation().contentEquals("Row"))
			return new LinkedHashSet<String>(Arrays.asList("DATA", "VARIABLE", "CONSTRAINT", "OBJECTIVE", "TERM"));
		//else orientation().contentEquals("Column")
			return new LinkedHashSet<String>(Arrays.asList("DATA", "CONSTRAINT", "OBJECTIVE", "VARIABLE", "TERM"));
		
	}//creationOrder
	
	/**@return the name of the model created by this factory; 
	 * you should create an instance field to hold the name*/
	String getName();
	
	/**
	 * Sets the name of the model created by this factory.
	 * You should create a instance field to hold the name.
	 * @param modelName TODO
	 * @return this factory
	 */
	MsdxSolverModelingFactory withName(String modelName); 
	
	/**
	 * Extracts the parameters from each Record in a Table. Sets default values for missing parameters.
	 * Performs the following transformation:
	 * <pre><code>
	 * MOSDEX table record -&gt (extractParameters) -&gt [parameters...]
	 * </code></pre>
	 * Coding the extractParameters operator requires defining two components: the
	 * makeResultSchema method, which specifies the names and types of the parameters, 
	 * and the apply method, which filters the input Container to find the parameters and set the defaults.
	 * <p>
	 * This method is called by the modeling object creators of this class and 
	 * serves as the first Span in the sequence of transformations forming the bridge to the solver.
	 * 
	 * @param tableName
	 * @param tableClass 
	 * @param tableType 
	 * @param tableSchema
	 * @return an operator that transforms a data Record to a Container of the form <code>[parameters...]</code>
	 * @throws IllegalArgumentException if any missing parameter has no default value
	 */
	default OperatorWithOneArgument extractParameters(
		String tableName,
		String tableClass, 
		String tableType, 
		MsdxContainer<Class<?>> tableSchema) 
	{
		return new OperatorWithOneArgument() {
			Set<String> useDefault;
			{//Initialization
				useDefault= Collections.emptySet();
			}
			
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> tableSchema) {
				//Finds missing parameter fields in the table schema and inserts default field if available
				this.useDefault= parameterDefaults(tableClass, tableType).itemNames().stream()
					.filter(name -> !tableSchema.containsField(name))
					.collect(Collectors.toSet());
				Set<String> missing= parameterFields(tableClass, tableType).stream()
					.filter(name -> !(tableSchema.containsField(name) || useDefault.contains(name)))
					.collect(Collectors.toSet());
				if(!missing.isEmpty())
					throw new IllegalArgumentException(tableName + " missing field(s) " + missing.toString());
	
				MsdxContainer<Class<?>> defaultSchema= new MsdxContainer<Class<?>>( 
					parameterDefaults(tableClass, tableType).toStream()
						.collect(Collectors.toMap(
							entry -> entry.getKey(),
							entry -> entry.getValue().getClass())));
				
				this.resultSchema= new MsdxContainer<Class<?>>(
					Stream.concat(
						tableSchema.select(parameterFields(tableClass, tableType)).toStream(),
						defaultSchema.toStream())
					.collect(Collectors.toMap(
						entry -> entry.getKey(), 
						entry -> entry.getValue(), 
						(parameterValue, defaultValue) -> parameterValue, 
						LinkedHashMap<String, Class<?>>::new)));
				return this;
			}//withResultSchema
	
			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> record) {
				MsdxContainer.Builder<Object> parametersBuilder= MsdxRecord.builder(this.getResultSchema())
					.copyItems(record, parameterFields(tableClass, tableType));
				this.useDefault.forEach(name -> {
					if(!record.containsField(name))
						parametersBuilder.addItem(name, parameterDefaults(tableClass, tableType).get(name));
				});
				return parametersBuilder.build();
			}//apply
			
		}/*OperatorWithOneArgument*/;//return	
	}//extractParameters

	/**
	 * Builds a solver-specific variable object.
	 * The underlying operator encapsulates all of the components required to build a variable object.
	 * <p>
	 * Performs the following transformation:
	 * <pre><code>
	 * [VARIABLE table parameters...] -&gt (makeVariable)  -&gt [solver variables...]
	 * </code></pre>
	 * Note that in the diagram above, the end point of each row represents a Span and 
	 * the transformation is denoted by the operator in parentheses.
	 * <p>
	 * Coding the makeVariable operator requires defining two components: the
	 * makeResultSchema method, which specifies the solver-specific variable class, 
	 * and the apply method, which creates the variable from the parameters.
	 * <p>
	 * This method is called by the MsdxModel.createVariable method as part of a two Span bridge.
	 * 
	 * @param tableClass
	 * @param tableType
	 * @return an operator that is used by the Span to transform the parameters to a Container holding a solver-specific variable
	 */
	OperatorWithOneArgument makeVariable(String tableClass, String tableType);

	/**
	 * Builds a solver-specific constraint object.
	 * The underlying operator encapsulates all of the components required to build a constraint object.
	 * <p>
	 * Performs the following transformation:
	 * <pre><code>
	 * [CONSTRAINT parameters...] -&gt (makeConstraint)  -&gt [solver constraints...]
	 * </code></pre>	
	 * Note that in the diagram above, the end point of each row represents a span and 
	 * the transformation is denoted by the operator in parentheses.
	 * <p>
	 * Coding the makeConstraint operator requires defining two components: the
	 * makeResultSchema method, which specifies the solver-specific constraint class, 
	 * and the apply method, which creates the constraint from the parameters.
	 * <p>
	 * This method is called by the MsdxModel.createConstraint method as part of a two Span bridge.
	 * 
	 * @param tableClass
	 * @param tableType
	 * @return an operator that is used by the Span to transform the parameters to a Container holding a solver-specific constraint
	 */
	OperatorWithOneArgument makeConstraint(String tableClass, String tableType);

	/**
	 * Builds a solver-specific objective object.
	 * The underlying operator encapsulates all of the components required to build an objective object.
	 * <p>
	 * Performs the following transformation:
	 * <pre><code>
	 * [OBJECTIVE parameters...] -&gt (makeObjective)  -&gt [solver objectives...]
	 * </code></pre>	
	 * Note that in the diagram above, the end point of each row represents a span and 
	 * the transformation is denoted by the operator in parentheses.
	 * <p>
	 * Coding the makeObjective operator requires defining two components: the
	 * makeResultSchema method, which specifies the solver-specific objective class, 
	 * and the apply method, which creates the objective from the parameters.
	 * <p>
	 * This method is called by the MsdxModel.createObjective method as part of a two Span bridge.
	 * 
	 * @param tableClass
	 * @param tableType
	 * @return an operator that is used by the Span to transform the parameters to a Container holding a solver-specific objective
	 */
	OperatorWithOneArgument makeObjective(String tableClass, String tableType);

	/**
	 * Builds a solver-specific expression object by computing a solver-specific
	 * product of a coefficient and one or two modeling objects. The underlying
	 * operator encapsulates all of the components required to build a Term object.
	 * Called by a join operation which combines the Term parameters with the
	 * Variables in a Row orientation, where it creates either linear or quadratic
	 * terms, or with the Constraints and Objectives in a Column orientation, where
	 * only linear terms are allowed.
	 * <p>
	 * If the Term is of type LINEAR, the multiply operator is applied once for the
	 * product of a coefficient with a Variable; if the Term is of type QUADRATIC,
	 * it is applied following a standard merge join with the first Variable for the
	 * product of a coefficient with two Variables.
	 * <p>
	 * Performs the following transformation:<br>
	 * In Row Orientation - 
	 * <pre><code>
	 * [rowId, columnId, coefficient] {if LINEAR} (join and multiply) [columnId, solver variable ] -&gt 
	 * [rowId, columnId, solver expression(coefficient * variable object)]
	 * or 
	 * [rowId, columnId, coefficient] {if QUADRATIC} -&gt  (join) [columnId, solver variable ] -&gt 
	 * (join and multiply) [columnId2, solver variable2] -&gt [rowId, solver expression(coefficient * solver variable * solver variable2)] 
	 * </code></pre>
	 * In Column Orientation - 
	 * <pre><code>
	 * [columnId, rowId, coefficient] -&gt (join and multiply) [rowId, constraint or objective object] -&gt 
	 * [columnId, rowId, expression(coefficient)] 
	 * </code></pre>
	 * The multiply operator takes as its input arguments two Containers holding
	 * respectively the coefficient and modeling object, each also having the key (i.e. the column id or,
	 * less commonly the row id). It produces a result container holding the key
	 * and the expression object representing the product. (Note that in an optimization model, a product is
	 * not a number, but it is a solver-specific object that is usually an instance
	 * of an expression class).
	 * <p>
	 * Coding the multiply operator requires defining two components: the
	 * makeResultSchema method, which specifies the class of the product 
	 * expression, and the apply methods (onKeyMatch and noKeyMatch), which call
	 * the solver-specific method that computes the product.
	 * <p>
	 * This method is called by the MsdxModel.createTermsByRows and 
	 * MsdxModel.createTermsByColumns methods as part of a multi-Span bridge.
	 * 
	 * @param tableClass
	 * 
	 * @param tableType
	 * 
	 * @return an operator that is used by the Span to transform the parameters and
	 *         solver modeling object to a Container holding a solver-specific product expression
	 */
	OperatorWithTwoArguments multiply(String tableClass, String tableType);

	/**
	 * Adds (reduces) Term expressions across the columns (in row orientation) or
	 * rows (in column orientation). Called by a reduce method, it provides an operator 
	 * which invokes the solver-specific sum of terms. 
	 * It should throw UnsupportedOperationException for inapplicable kinds of modeling objects.
	 * <p>
	 * The add operator performs the following transformation: <br>
	 * For row orientation --
	 * <pre><code>
	 * [rowId, columnId, solver term] -&gt  (add across columnIds) [rowId, solver expression(sum)]
	 * </code></pre>
	 * For column orientation --
	 * <pre><code>
	 * [rowId, columnId, solver term] -&gt  (add across rowIds) [columnId, solver expression(sum)]
	 * </code></pre>
	 * The add operator takes as its input arguments two containers holding
	 * respectively the value field or fields representing the terms to be
	 * adjoined to the accumulation and the accumulation field (or fields) representing the sum
	 * expression, each also having the key (i.e. the row id or,
	 * less commonly the column id). The add operator is used to add the individual
	 * terms to the sum expression. It produces a result container holding the key
	 * and the accumulation objects. (Note that in an optimization model, a sum is
	 * not a number, but it is a solver-specific object that is usually an instance
	 * of an expression class representing a sum of terms).
	 * <p>
	 * Coding the add operator requires defining two components: the
	 * makeResultSchema method, which specifies the class(es) of the accumulation
	 * expression(s), and the apply methods (onKeyMatch and noKeyMatch), which call
	 * the solver-specific methods that computes the sum. The result schema merges 
	 * the value schema with the key and accumulation fields of the accumulator schema. 
	 * The accumulator schema should be empty and the value schema should be
	 * the term schema. The apply methods take the value and accumulator Containers
	 * as input arguments and returns the result container. The result schema is
	 * <pre><code>
	 * {key field name, accumulation field(s)}.
	 * </code></pre>
	 * This method is called by the MsdxModel.createTermsByRows and 
	 * MsdxModel.createTermsByColumns methods as part of a multi-Span bridge.
	 * 
	 * @param tableClass
	 * @param tableType
	 * @return an operator that is used by the Span to combine the accumulation
	 *   container with the item Container to produce the sum expression result Container
	 */
	OperatorWithTwoArguments add(String tableClass, String tableType);
	
	/**
	 * Attaches sum expressions to constraints and objectives in row orientation or
	 * to variables in column orientation. Called by a join method, it provides
	 * an Operator which invokes the solver-specific attachment of expressions to
	 * modeling objects. It should throw UnsupportedOperationException for
	 * inapplicable kinds of modeling objects.
	 * <p>
	 * The attach Operator performs the following transformation: <br>
	 * for row orientation --
	 * <pre><code>
	 * [rowId, solver expression(sum)] -&gt  
	 * (join and attach constraints or objectives) [rowId, solver constraint or objective] -&gt  
	 * [rowId, solver constraint or objective, solver expression] 
	 * </code></pre>
	 * for column orientation --
	 * <pre><code>
	 * [columnId, solver expression(sum)] -&gt 
	 * (join and attach variables) [columnId, solver variable] -&gt 
	 * [columnId, solver variable, solver expression] 
	 * </code></pre>
	 * The attach Operator takes as its input arguments two Containers holding
	 * respectively the accumulation Container holding the sum expression field (or
	 * fields) to be adjoined and the Container holding a modeling object (constraint or objective in row orientation or
	 * variable in column orientation). Each pair of Containers is linked by a key field (i.e. the row id or,
	 * less commonly the column id). 
	 * It produces a result Container holding the key and the accumulation
	 * expression and the constraint/objective (or variable) to which the
	 * accumulation expression is to be attached. (Note that in an optimization
	 * model, a sum is not a number, but it is a solver-specific object that is
	 * usually an instance of an expression class representing a sum of terms).
	 * <p>
	 * Coding the attach Operator requires defining two components: the
	 * makeResultSchema method, which specifies the class(es) of the accumulation
	 * expression(s), and the apply method, which calls the solver-specific methods
	 * that attaches the accumulation. The result Schema specifies the row or column key
	 * field, the constraint/objective (or variable) and the expression Schema that
	 * results from the reduction of the terms using the add operator. The
	 * apply method takes the modeling object and reduced terms Containers as its
	 * input arguments and returns the result Container. Note: the result Schema is
	 * <pre><code>
	 * {key field name, constraint/objective or variable expressions}.
	 * </code></pre>
	 * <p>
	 * This method is called by the MsdxModel.createTermsByRows and 
	 * MsdxModel.createTermsByColumns methods as part of a multi-Span bridge.
	 * 
	 * @return an Operator that is used by the Span to attach the accumulation
	 *   Container to the modeling object Container
	 */
	OperatorWithTwoArguments attach();
	
	/**
	 * Write a text representation of the model. 
	 * Usually, the text will use a standard (non-MOSDEX) format, such as .mps or .lp;
	 * Used, in particular, when the model is not to be solved.
	 * 
	 * @param model
	 * @param out destination for the text (no output if null)
	 */
	public void generate(MsdxModel model, PrintStream out);
	
	/**
	 * Write a text representation of the model. 
	 * Usually, the text will use a standard (non-MOSDEX) format, such as .mps or .lp;
	 * Used, in particular, when the model is not to be solved.
	 * 
	 * @param model
	 * @param out destination for the text (no output if null)
	 */
	default void generate(MsdxModel model, MsdxOutputDestination out) {
		generate(model, new PrintStream(MsdxOutputDestination.toStream(out)));
	}

	/**
	 * Calls the solver, returns its status, and creates the solver results.
	 * The solver results are the native output of the solver, 
	 * not in the the form of a MOSDEX File;
	 * the solution is also returned in the Collector of Tables in the Model, 
	 * from which a MOSDEX File or Files are created by the Application.
	 * If the "solver" does not actually produce a solution 
	 * (but instead generates, for example, a .lp or .mps representation),
	 * the status returned should be "Not supported" (case insensitive).
	 * 
	 * @param model
	 * @param out destination for native solver results (null if not needed)
	 * @return a string containing the status of the solve (e.g. optimal, infeasible, unbounded, etc.)
	 */
	public String solve(MsdxModel model, PrintStream out);

	/**
	 * Calls the solver, returns its status, and creates the solver results.
	 * The solver results are the native output of the solver, 
	 * not in the the form of a MOSDEX File;
	 * the solution is also returned in the Collector of Tables in the Model, 
	 * from which a MOSDEX File or Files are created by the Application.
	 * If the "solver" does not actually produce a solution 
	 * (but instead generates, for example, a .lp or .mps representation),
	 * the status returned should be "Not supported" (case insensitive).
	 * 
	 * @param model
	 * @param out destination for native solver results (null if not needed)
	 * @return a string containing the status of the solve (e.g. optimal, infeasible, unbounded, etc.)
	 */
	default String solve(MsdxModel model, MsdxOutputDestination out) {
		return solve(model, new PrintStream(MsdxOutputDestination.toStream(out)));
	}
	
	/**
	 * The function table is a mapping between the function names, established in the 
	 * FunctionCall object, and the function implementations as Java Functions.
	 * The is method presupposes that a function table has been created in the implementing classes 
	 * of this factory interface.
	 * 
	 * @return the function table as a Map
	 */
	Map<String, Function<MsdxContainer<Object>, ?>> getFunctionTable();
	
	/**
	 * This method creates an initial set of function entries for the most common 
	 * solution information produced by a solver. You can add others if you need them, 
	 * using the steps outlined below.
	 * <p>
	 * Optimization solvers can produce a plethora of information about the solution. As this time, MOSDEX supports 
	 * only the basics: optimal primal value, reduced cost, and basis status of a variable, optimal dual value and slack of a constraint, and 
	 * the optimal value of the objective function. However, a user, with a little bit of Java programming, can 
	 * add to this list by taking the following steps.
	 * <ul>
	 * <li>Define a functionTable field in the solver factory class that implements this interface and 
	 *   initialize it in the constructor by calling the initializeFunctionTable method.</li>
	 * <li>Specify the name and argument of the solver call in the MOSDEX Table Instance. This is simply a string entered as an item in a Record, 
	 *   of the form "functionName(argument)" (see the Msdx2FunctionCall documentation). 
	 *   The Schema field for the item must have a field type of "xx_FUNCTION", where xx is the return type (e.g. DOUBLE).
	 *   The MOSDEX parser will create a Msdx2FunctionCall instance from this text.</li>
	 * <li>In your application program, retrieve the function table from this modeling factory.</li>
	 * <li>Create a new entry in this function table by writing a short Java Function 
	 *   and include the solver call in its apply method. 
	 *   See the intializeFunctionTable implementation in the MsdxCplexModelingFactory for details.</li>
	 * <li>Make sure you register the function for use in SQL by calling 
	 *   MsdxDataframe.Factory.registerFunctionCall(functionName) when you create the new entry.</li>
	 * <li>That's it. MOSDEX will handle the rest.</li>
	 *</ul> 
	 * The default method simply defines the function calls as SQL user-defined functions that produce 
	 * the appropriate call strings. Override the default to add calls to the solver-specific methods that  
	 * retrieve values from the solution.
	 * 
	 * @param dataframeFactory
	 * @return the function table as a Map
	 */
	default public Map<String, Function<MsdxContainer<Object>, ?>> initializeFunctionTable(MsdxDataframe.Factory dataframeFactory) {
		if(dataframeFactory==null)
			throw new IllegalStateException("Dataframe factory is not defined");
		//These are not used in generating the model, but may appear in some of the modeling objects
		dataframeFactory.registerFunctionCall("PrimalValue");
		dataframeFactory.registerFunctionCall("ReducedCost");
		dataframeFactory.registerFunctionCall("BasisStatus");
		dataframeFactory.registerFunctionCall("DualValue");
		dataframeFactory.registerFunctionCall("Slack");
		dataframeFactory.registerFunctionCall("ObjectiveValue");

		return new LinkedHashMap<String, Function<MsdxContainer<Object>, ?>>();
	}//initializeFunctionTable
	
	/**
	 * This Operator is used in MsdxModel to retrieve the solution items from the
	 * solver. The solution items are denoted in a Table Instance Record as those
	 * with type Msdx2FunctionCall. Their names are not pre-specified in the Record
	 * Schema (in contrast to the solver modeling objects, which are prescribed by
	 * MOSDEX) to leave the opportunity for the MOSDEX user to give them her own
	 * names or to retrieve items of the solution beyond those currently specified,
	 * as discussed in the initializeFunctionTable method. The set of those function
	 * field names is created as the schema is processed in withResultSchema method
	 * of this operator.
	 * <p>
	 * Performs the following transformation:
	 * <pre><code>
	 * [key field, solver modeling object, other element fields] 
	 * (join and retrieve) [table name, key field, solver function calls, other record fields] -&gt 
	 * [table name, key field, solution values, other record fields] 
	 * </code></pre>
	 * This method processes each Table Record by joining it to the corresponding
	 * element of the modeling object bridge, in order to have access to the
	 * solver-specific components of the element (i.e. variable, constraint, or
	 * objective). It then gets the function that retrieves the solution item from
	 * the function table using the function name that is part of the
	 * Msdx2FunctionCall object of the function field in the table record. It
	 * applies that function and returns the value of the solution item, which is
	 * stored in the function field's Msdx2FunctionCall object. It then returns the
	 * updated table record.
	 * <p>
	 * Optimization solvers can produce a plethora of information about the
	 * solution. As this time, MOSDEX supports only the basics: optimal primal
	 * value, reduced cost, and basis status of a variable, optimal dual value and
	 * slack of a constraint, and the optimal value of the objective function. To
	 * add to this list, see the instructions in the initializeFunctionTable method.
	 * <p>
	 * This method is called by the MsdxModel.createSolutionObjects method as part
	 * of a single Span bridge.
	 * 
	 * @param tableClass to which this retrieval applies
	 */
	OperatorWithTwoArguments retrieveSolution(String tableClass);

	/**
	 * Specifies the names of the parameter fields for each type of MOSDEX modeling object. 
	 * While the table will probably include other fields, these are essential, 
	 * although some may have default values. If a required parameter field is missing, 
	 * and does not have a default value, an exception will be thrown by the extractParameters call.
	 * 
	 * @param tableClass
	 * @param tableType
	 * @return names of the parameter fields 
	 */
	default Set<String> parameterFields(String tableClass, String tableType) {
		HashSet<String> parameterFields= new HashSet<String>();		
		if(tableClass.equals("VARIABLE"))
			Collections.addAll(parameterFields, "Name", "Column", "LowerBound", "UpperBound");
		else if(tableClass.equals("CONSTRAINT"))
			Collections.addAll(parameterFields, "Name", "Row", "Sense", "RHS");
		else if(tableClass.equals("OBJECTIVE"))
			Collections.addAll(parameterFields, "Name", "Row", "Sense", "Constant");
		else if(tableClass.equals("TERM") && tableType.equals("LINEAR")) 
			Collections.addAll(parameterFields, "Row", "Column", "Coefficient");	
		else if(tableClass.equals("TERM") && tableType.equals("QUADRATIC"))
				Collections.addAll(parameterFields, "Row", "Column", "Column2", "Coefficient");
		else
			throw new IllegalArgumentException("Unsupported modeling object " + tableClass + ": " + tableType);
		return Collections.unmodifiableSet(parameterFields);
	}//parameterFields
	
	/**
	 * Sets default values for certain missing parameter fields.
	 * 
	 * @param tableClass
	 * @param tableType
	 * @return default values
	 */
	default MsdxContainer<Object> parameterDefaults(String tableClass, String tableType) {
		MsdxContainer.Builder<Object> defaults= MsdxContainer.<Object>builder();
		if(tableClass.equals("VARIABLE")) {
			if(tableType.equals("CONTINUOUS")) {
				defaults
					.addItem("LowerBound", 0.0)
					.addItem("UpperBound", Double.POSITIVE_INFINITY);
			}
			else if(tableType.equals("INTEGER")) {
				defaults
					.addItem("LowerBound", 0)
					.addItem("UpperBound", Integer.MAX_VALUE);
			}
			else if(tableType.equals("BINARY")) {
				defaults
					.addItem("LowerBound", 0)
					.addItem("UpperBound", 1);
			}
		}
		else if(tableClass.equals("CONSTRAINT")) {
			//empty
		}
		else if(tableClass.equals("OBJECTIVE")) {
			defaults
				.addItem("Constant", 0.0);
		}
		else if(tableClass.equals("TERM")) {
			//empty
		}
		else
			throw new IllegalArgumentException("Unsupported modeling object " + tableClass + ": " + tableType);
		return defaults.build();	
	}//parameterDefaults

	
}//interface MsdxSolverModelingFactory
