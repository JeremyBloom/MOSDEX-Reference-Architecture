/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxFunctionCall;

import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxQuery;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxTable;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;
import io.github.JeremyBloom.mosdex2.span.OperatorWithTwoArguments;

/**
 * This class provides a basic implementation of an optimization problem using a
 * solver. Its methods build the solver modeling objects, variables,
 * constraints, objectives and terms, and make them visible to applications. It
 * hides the solver-specific data structures and methods that are inaccessible
 * in development.
 * <p>
 * The heart of the Model class is a set of CreateXx methods (where Xx stands
 * for Variable, Constraint, etc.). These methods all operate in a similar
 * fashion: each creates a bridge (a stream of data) that connects a MOSDEX
 * Table to a set of solver-specific data structures. The bridge consists of a
 * series of Spans that transform the data step-by-step. The first Span is
 * usually extractParmeters, followed by other Spans that perform other
 * transformations; the most complex transformations are used to create the
 * terms and the solver results, which involve joins between two or more Spans.
 * <p>
 * Each transformation of a Span is implemented by an operator function, usually
 * OperatorWithOneArgument or OperatorWithTwoArguments, which are defined in the
 * modeling factory. Each operator performs two functions: it derives the schema
 * of the result from the schema of the argument(s) and it derives the output
 * container from the argument container(s).
 * <p>
 * An optimization problem's Tables may be spread over multiple MOSDEX Modules
 * in multiple Files. It is strongly recommended that all the MOSDEX modeling
 * Tables (VARIABLE, CONSTRAINT, OBJECTIVE, and TERM) should be contained in
 * a single Module, with other Modules used for input and output data. A future
 * release may require each modeling Module to have its own solver instance, in
 * order to support decomposition algorithms.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxModel implements Serializable {
	
	private static final long serialVersionUID = -5428251322669828628L;
	
	/**The name of the model.*/
	private String modelName;
	
	/**The factory that creates spans.*/
	private MsdxSpan.Factory spanFactory;
	
	/**The factory which creates solver-specific modeling objects.*/
	private MsdxSolverModelingFactory modelingFactory;

	/**The factory that creates dataframes.*/
	private MsdxDataframe.Factory dataframeFactory;
	
	/**
	 * The collection of the solver-specific modeling objects associated with the problem.
	 * Note that the actual data structures embodied by these objects are resident 
	 * within the native data structures of solver.
	 */
	private Map<String, MsdxSolverModelingObject> solverObjects;
	
	/**Resolves dependencies among the tables in this model.*/
	private MsdxQuery.Resolver resolver;

	/**
	 * The sole constructor for a Model.
	 * 
	 * @param modelName
	 * @param modelingFactory creates solver-specific modeling objects
	 * @param spanFactory creates the spans
	 * @param dataframeFactory creates the dataframes
	 */
	public MsdxModel(
		String modelName, 
		MsdxSolverModelingFactory modelingFactory, 
		MsdxSpan.Factory spanFactory, 
		MsdxDataframe.Factory dataframeFactory) 
	{
		super();
		this.modelName = modelName;
		this.modelingFactory = modelingFactory;
		this.spanFactory = spanFactory;
		this.dataframeFactory = dataframeFactory;
		this.solverObjects= new LinkedHashMap<String, MsdxSolverModelingObject>(); 
	}//MsdxModel
	
	/**
	 * The main method for reading the MOSDEX Tables constituting an optimization problem 
	 * and generating the solver-specific modeling objects. 
	 * Calls specific CreateXx methods to create the different types of objects.
	 * Uses a Resolver to manage query tables and their dependencies. 
	 * 
	 * @param collector the MOSDEX data for the problem in Dataframes derived from the problem's Tables 
	 * (Assumes all Tables have unique names among all the Files and their Modules)
	 */
	public void createModelingObjects(Map<String, MsdxTable> collector) {		
		this.resolver= new MsdxQuery.Resolver(collector);
		this.resolver.findDependencies();	
		this.resolver.resolveDependencies();

		MsdxTable table;
		for(String tableName: this.resolver.getResolutionOrder()) {
			table= collector.get(tableName);
			if(table.getTableClass().equals("DATA") && table.getTableType().equals("OUTPUT"))
				continue;	//skip output data tables for now, 
							//since they depend on solution values yet to be computed
			if(table.hasField("QUERY") || table.hasField("INITIALIZE")) {
				table.createInstance();
			}
		}//for tableName
		//at this point, all input data and modeling object tables have been resolved

		//Create the solver objects in the order specified by the factory
		for(String tableClass: this.modelingFactory.creationOrder()) {
			for(String tableName: collector.keySet()) {
				table= collector.get(tableName);
				if(!table.getTableClass().equals(tableClass))
					continue;	//skip for now
				if(table.getTableClass().equals("VARIABLE"))
					this.solverObjects.put(tableName, this.createVariable(table));
				else if(table.getTableClass().equals("CONSTRAINT"))
					this.solverObjects.put(tableName, this.createConstraint(table));
				else if(table.getTableClass().equals("OBJECTIVE"))
					this.solverObjects.put(tableName, this.createObjective(table));
				else if(table.getTableClass().equals("TERM"))
					this.createTerm(table);
			}//for tableName
		}//for tableClass	
	}//createModelingObjects
	
	/**
	 * Creates solver-specific variable instances from a MOSDEX VARIABLE Table. 
	 * The result has one variable for each record in the table. 
	 * Uses the modeling factory makeVariable operator to perform the following transformations:<br>
	 * <pre><code>
	 * VARIABLE table record -&gt (extractParameters) -&gt 
	 * 	[parameters...] -&gt (makeVariable)  -&gt [solver variables...]<br>
	 * </code></pre>	
	 * Note that in the diagram above, the end point of each row represents a span and 
	 * the transformation is denoted by the operator in parentheses.
	 * <p>
	 * This method is called by the createModelingObjects method as a two span bridge.
	 * 
	 * @param table a MOSDEX VARIABLE
	 * @return a new Span 
	 */
	public MsdxSolverModelingObject createVariable(MsdxTable table) {
		if(!table.getTableClass().equals("VARIABLE"))
			throw new IllegalArgumentException(table.getName() + " is not a variable");

		MsdxSpan parameters= spanFactory.wrap(table.getInstance().asContainers(), table.getSchema().asContainer())
			.map(modelingFactory.extractParameters(table.getName(), table.getTableClass(), table.getTableType(), table.getSchema().asContainer()));
		MsdxSpan variable= parameters
			.map(modelingFactory.makeVariable(table.getTableClass(), table.getTableType()));

		return new MsdxSolverModelingObject(table.getName(), table.getTableClass(), table.getTableType(), variable.key("Column"));
	}//createVariable

	/**
	 * Creates solver-specific constraint instances from a MOSDEX CONSTRAINT Table. 
	 * The result has one constraint for each record in the table. 
	 * Uses the modeling factory makeConstraint operator to perform the following transformations:<br>
	 * <pre><code>
	 * CONSTRAINT table record -&gt (extractParameters) -&gt 
	 * 	[parameters...] -&gt (makeConstraint)  -&gt [solver constraints...]
	 * </code></pre>	
	 * Note that in the diagram above, the end point of each row represents a span and 
	 * the transformation is denoted by the operator in parentheses.
	 * <p>
	 * This method is called by the createModelingObjects method as a two span bridge.
	 * 
	 * @param table a MOSDEX CONSTRAINT
	 * @return a new Span 
	 */
	public MsdxSolverModelingObject createConstraint(MsdxTable table) {
		if(!table.getTableClass().equals("CONSTRAINT"))
			throw new IllegalArgumentException(table.getName() + " is not a constraint");

		MsdxSpan parameters= spanFactory.wrap(table.getInstance().asContainers(), table.getSchema().asContainer())
			.map(modelingFactory.extractParameters(table.getName(), table.getTableClass(), table.getTableType(), table.getSchema().asContainer()));
		MsdxSpan constraint= parameters
			.map(modelingFactory.makeConstraint(table.getTableClass(), table.getTableType()));

		return new MsdxSolverModelingObject(table.getName(), table.getTableClass(), table.getTableType(), constraint.key("Row"));
	}//createConstraint

	/**
	 * Creates solver-specific objective function instances from a MOSDEX OBJECTIVE Table. 
	 * The result has one objective for each record in the table (usually there will only be one). 
	 * Uses the modeling factory makeObjective operator to perform the following transformations:<br>
	 * <pre><code>
	 * OBJECTIVE table record -&gt (extractParameters) -&gt 
	 * 	[parameters...] -&gt (makeObjective)  -&gt [solver objectives...]
	 * </code></pre>	
	 * Note that in the diagram above, the end point of each row represents a span and 
	 * the transformation is denoted by the operator in parentheses.
	 * <p>
	 * This method is called by the createModelingObjects method as a two span bridge.
	 * 
	 * @param table a MOSDEX OBJECTIVE
	 * @return a new Span 
	 */
	public MsdxSolverModelingObject createObjective(MsdxTable table) {
		if(!table.getTableClass().equals("OBJECTIVE"))
			throw new IllegalArgumentException(table.getName() + " is not an objective");

		MsdxSpan parameters= spanFactory.wrap(table.getInstance().asContainers(), table.getSchema().asContainer())
			.map(modelingFactory.extractParameters(table.getName(), table.getTableClass(), table.getTableType(), table.getSchema().asContainer()));
		MsdxSpan objective= parameters
			.map(modelingFactory.makeObjective(table.getTableClass(), table.getTableType()));

		return new MsdxSolverModelingObject(table.getName(), table.getTableClass(), table.getTableType(), objective.key("Row"));
	}//createObjective

	/**
	 * Attaches solver-specific term expressions from a MOSDEX TERM Table 
	 * to the constraints and objectives or the variables 
	 * (depending on the orientation of the modeling factory). 
	 * 
	 * @param table a MOSDEX TERM
	 * @return void as this method does not produce a separate bridge for the terms, 
	 * since the terms are attached to other solver objects as expressions.

	 */
	public void createTerm(MsdxTable table) {
		if(modelingFactory.orientation().equals("Row"))
			createTermByRows(table);
		else if(modelingFactory.orientation().equals("Column"))
			createTermByColumns(table);
		else
			throw new IllegalArgumentException("Illegal orientation " + modelingFactory.orientation());
	}//createTerm
	
	/**
	 * Attaches solver-specific term expressions from a MOSDEX TERM Table to the
	 * constraints and objectives in a row oriented modeling factory. It operates as
	 * a sequence of Spans, each of which performs a specific transformation, as
	 * follows:
	 * <pre><code>
	 * TERM table record -&gt (extractParameters) -&gt [rowId, columnId, coefficient] -&gt 
	 * {if linear} (join and multiply) [columnId, solver variable ] -&gt [rowId, columnId, solver expression(coefficient * variable object)] -&gt 
	 * (add across columnIds) [rowId, solver expression(sum)] -&gt  
	 * (join and attach constraints or objectives) [rowId, solver constraint or objective] -&gt  
	 * [rowId, solver constraint or objective, solver expression] 
	 * </code></pre>
	 * Note, this sequence represents a linear expression; in a quadratic
	 * expression, there is an additional join transformation for a second variable
	 * <pre><code>
	 * {if quadratic} ... -&gt  (join) [columnId, solver variable ] -&gt 
	 * (join and multiply) [columnId2, solver variable2] -&gt [rowId, solver expression(coefficient * solver variable * solver variable2)] 
	 * (add across rowIds) -&gt ... 
	 * </code></pre>
	 * Note that in the diagrams above, the end point of each row represents a span
	 * and the transformation is denoted by the operator in parentheses.
	 * <p>
	 * This method is called by the createModelingObjects method as a multi-span
	 * bridge.
	 * 
	 * @param table a MOSDEX TERM
	 * @return void as this method does not produce a separate bridge for the terms,
	 *         since the terms are attached to other solver objects as expressions.
	 */
	public void createTermByRows(MsdxTable table) {
		if(!modelingFactory.orientation().equals("Row"))
			throw new IllegalArgumentException("Illegal orientation " + modelingFactory.orientation());
		if(!table.getTableClass().equals("TERM"))
			throw new IllegalArgumentException(table.getName() + " is not a term");	
		if(!(table.getTableType().equals("LINEAR") || table.getTableType().equals("QUADRATIC")))
			throw new IllegalArgumentException(table.getName() + " is not a linear or quadratic term");

		MsdxSpan allVariables= this.all("VARIABLE");

		MsdxSpan termParameters= spanFactory.wrap(table.getInstance().asContainers(), table.getSchema().asContainer())
			.map(modelingFactory.extractParameters(table.getName(), table.getTableClass(), table.getTableType(), table.getSchema().asContainer()));
		
		MsdxSpan terms;
		OperatorWithTwoArguments multiply= modelingFactory.multiply(table.getTableClass(), table.getTableType());
		Map<String, Set<Object>> missingColumns= new LinkedHashMap<String, Set<Object>>();
			
		if(table.getTableType().equals("LINEAR")) {
			terms= termParameters.leftJoin(allVariables, "Column", multiply)
				.map(multiply.unmatchedKeys(missingColumns));								
		}
		else {	//table type == QUADRATIC
			OperatorWithTwoArguments merge= MsdxSpan.merge();
			MsdxSpan joined= termParameters.leftJoin(allVariables, "Column", merge)
				.map(merge.unmatchedKeys(missingColumns));
			multiply.withResultSchema(
				joined.getSchema(), 
				"Column2", 
				allVariables.getSchema()
					.renameField("Column", "Column2")
					.renameField("Variable", "Variable2"), 
				"Column2");			
			terms= joined.leftJoin(allVariables, "Column2", multiply)
				.map(multiply.unmatchedKeys(missingColumns));		
		}

		MsdxSpan expressions= terms.reduceByKey("Row", modelingFactory.add(table.getTableClass(), table.getTableType()));
		if(!missingColumns.get("left").isEmpty())
			Msdx.GLOBAL.out.println("In MsdxModel.createTermByRows: missing columns  " + missingColumns.get("left").toString());

		MsdxSpan allRows= this.all("CONSTRAINT").outerJoin(this.all("OBJECTIVE"), "Row", MsdxSpan.merge());

		OperatorWithTwoArguments attacher= modelingFactory.attach();
		Map<String, Set<Object>> missingRows= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan attached= expressions.outerJoin(allRows, "Row", attacher)
			.map(attacher.unmatchedKeys(missingRows));

		attached= attached.filter(container -> !container.get("Name").equals("NO_MATCH"));
				
		Map<String, MsdxSolverModelingObject> replacement= attached.apply()
			.collect(Collectors.groupingBy(container -> (String)container.get("Name")))				//Map<String, List<MsdxContainer<Object>>
			.entrySet().stream()																	//Entry<String, List<MsdxContainer<Object>>
				.map(entry -> createReplacement(entry.getKey()).apply(entry.getValue().stream()))	//MsdxSolverModelingObject
				.collect(Collectors.toMap(
					replacementObject -> replacementObject.getTableName(), 
					replacementObject -> replacementObject, 
					(left, right) -> left,	//can't happen 
					LinkedHashMap::new));
		
		if(!missingRows.get("left").isEmpty())
			Msdx.GLOBAL.out.println("In MsdxModel.createTermByRows: missing rows  " + missingRows.get("left").toString());
		replacement.forEach(
			(tableName, modelingObj) -> 
				solverObjects.merge(
					tableName, 
					modelingObj, 
					(oldObject, newObject) -> newObject));	
		
	}//createTermByRows
	
	/**
	 * Attaches solver-specific term expressions from a MOSDEX TERM Table 
	 * to the variables in a column oriented modeling factory. 
	 * It operates as a sequence of spans, each of which performs a specific transformation, as follows:
	 * <pre><code>
	 * TERM table record -&gt (extractParameters) -&gt [columnId, rowId, coefficient] -&gt 
	 * (join and multiply) [rowId, constraint or objective object] -&gt [columnId, rowId, expression(coefficient)] -&gt 
	 * (add by columnId) [columnId, expression(sum)] -&gt 
	 * (join and attach Variables) [columnId, solver variable ] -&gt 
	 * [columnId, variable object, expression object] 
	 * </code></pre>
	 * Note, this sequence represents a linear expression only; there is no reasonable column-oriented 
	 * representation for quadratic expressions.
	 * <p>
	 * This method is called by the createModelingObjects method as a multi-span bridge.
	 * 
	 * @param table a MOSDEX TERM
	 * @return void as this method does not produce a separate bridge for the terms, 
	 * since the terms are attached to other solver objects as expressions.
	 */
	public void createTermByColumns(MsdxTable table) {
		if(!modelingFactory.orientation().equals("Column"))
			throw new IllegalArgumentException("Illegal orientation " + modelingFactory.orientation());
		if(!table.getTableClass().equals("TERM"))
			throw new IllegalArgumentException(table.getName() + " is not a term");
		if(!table.getTableType().equals("LINEAR"))
			throw new IllegalArgumentException(table.getName() + " is not a linear term");

		MsdxSpan termParameters= spanFactory.wrap(table.getInstance().asContainers(), table.getSchema().asContainer())
			.map(modelingFactory.extractParameters(table.getName(), table.getTableClass(), table.getTableType(), table.getSchema().asContainer()));

		MsdxSpan allRows= this.all("CONSTRAINT").outerJoin(this.all("OBJECTIVE"), "Row", MsdxSpan.merge());

		OperatorWithTwoArguments multiply= modelingFactory.multiply(table.getTableClass(), table.getTableType());
		Map<String, Set<Object>> missingRows= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan terms= termParameters.leftJoin(allRows, "Row", multiply)
			.map(multiply.unmatchedKeys(missingRows));								
			
		MsdxSpan expressions= terms.reduceByKey("Column", modelingFactory.add(table.getTableClass(), table.getTableType()));
		if(!missingRows.get("left").isEmpty())
			Msdx.GLOBAL.out.println("In MsdxModel.createTermByColumns: missing rows  " + missingRows.get("left").toString());

		MsdxSpan allVariables= this.all("VARIABLE");
		
		OperatorWithTwoArguments attacher= modelingFactory.attach();
		Map<String, Set<Object>> missingColumns= new LinkedHashMap<String, Set<Object>>();
		MsdxSpan attached= expressions.outerJoin(allVariables, "Column", attacher)
			.map(attacher.unmatchedKeys(missingColumns));
		
		attached= attached.filter(container -> !container.get("Name").equals("NO_MATCH"));
		
		Map<String, MsdxSolverModelingObject> replacement= attached.apply()
			.collect(Collectors.groupingBy(container -> (String)container.get("Name")))				//Map<String, List<MsdxContainer<Object>>
			.entrySet().stream()																	//Entry<String, List<MsdxContainer<Object>>
				.map(entry -> createReplacement(entry.getKey()).apply(entry.getValue().stream()))	//MsdxSolverModelingObject
				.collect(Collectors.toMap(
					replacementObject -> replacementObject.getTableName(), 
					replacementObject -> replacementObject, 
					(left, right) -> left,	//can't happen 
					LinkedHashMap::new));
		
		if(!missingColumns.get("left").isEmpty())
			Msdx.GLOBAL.out.println("In MsdxModel.createTermByColumns: missing columns  " + missingColumns.get("left").toString());
		
		replacement.forEach(
			(tableName, modelingObj) -> 
				solverObjects.merge(
					tableName, 
					modelingObj, 
					(oldObject, newObject) -> newObject));	
			
	}//createTermByColumns
	
	/**
	 * Creates a modeling object that can replace an existing object in the solverObjects map of this instance.
	 * Copies the metadata (table class, type, and schema) of the existing object.
	 * 
	 * @return maps the input container stream to a solver modeling object
	 */
	protected Function<Stream<MsdxContainer<Object>>, MsdxSolverModelingObject> createReplacement(String tableName) 
	{
		return new Function<Stream<MsdxContainer<Object>>, MsdxSolverModelingObject>() {

			@Override
			public MsdxSolverModelingObject apply(Stream<MsdxContainer<Object>> containers) 
			{
				Stream<MsdxContainer<Object>> narrowContainers= containers
					.map(container -> //input container schema= {Name, Row, Constraint, Objective, Expression}
						{
							final MsdxContainer<Class<?>> resultSchema= MsdxModel.this.solverObjects.get(tableName).getSchema();
							//result schema= {Name, Row, Constraint or Objective, Expression}
							final MsdxSolverModelingObject current= MsdxModel.this.solverObjects.get(tableName);
							
							//removes the unused Constraint or Objective field from the input containers
							MsdxContainer<Object> narrowedContainer=
							MsdxRecord.builder(resultSchema)
								.copyItems(container)
								.removeItemIf(!current.getTableClass().equals("CONSTRAINT"), "Constraint")
								.removeItemIf(!current.getTableClass().equals("OBJECTIVE"), "Objective")
							.build();
							return narrowedContainer;
						}//Function
					);//map
				
				//creates the replacement modeling object
				MsdxSolverModelingObject current= MsdxModel.this.solverObjects.get(tableName);
				MsdxSolverModelingObject replacement= new MsdxSolverModelingObject(
					tableName,
					current.getTableClass(),
					current.getTableType(),
					spanFactory.create(narrowContainers, current.getSchema()));
				return replacement; 
			}//apply
			
		}/*Function*/;//return
	}//createReplacement

	/**
	 * Creates the output Tables with the values of solution fields substituted for the function calls in the MOSDEX modeling objects.
	 * <p>
	 * Uses the modeling factory retrieveSolution operator to perform the following transformations:
	 * <pre><code>
	 * [key field, solver modeling object, other element fields] 
	 * (join and retrieve) [table name, key field, solver function calls, other record fields] -&gt 
	 * [table name, key field, solution values, other record fields] 
	 * </code></pre>
	 * For each modeling object, calls the solver-specific methods to extract the solution values and then
	 * joins those solver objects with the corresponding MOSDEX modeling object tables, 
	 * substituting the function call fields in the Tables with their corresponding solution values.
	 * Then resolves the output Table Queries from the modeling object Tables.
	 * 
	 * @param collector the data for the problem in Dataframes derived from the problem's Tables 
	 * (Assumes all Tables have unique names among all the Files and their Modules)
	 */
	public void createSolutionObjects(Map<String, MsdxTable> collector) {
		
		MsdxTable table;
		Set<String> solutionFieldNames;
		
		//Replace the solution function calls with their computed values
		for(String tableName: collector.keySet()) {
			table=collector.get(tableName);
			if(table.getTableClass().equals("DATA") && table.getTableType().equals("OUTPUT"))
				continue;	//skip the output data tables for now
			
			solutionFieldNames= MsdxFunctionCall.findFunctionFieldsIn(table.getSchema().asContainer());
			if(solutionFieldNames.isEmpty())
				continue;	
			
			MsdxSpan records= spanFactory.create(table.getInstance().asContainers(), table.getSchema().asContainer());

			OperatorWithTwoArguments retriever= modelingFactory.retrieveSolution(table.getTableClass());
			String keyFieldName= (table.getTableClass().equals("VARIABLE")) ?
				"Column":
				"Row";
			MsdxSpan newRecords= this.solverObjects.get(table.getName()).getBridge()
				.innerJoin(records, keyFieldName, retriever)
				.key(keyFieldName);
			
			table.replaceInstance(this.dataframeFactory.create(
				table.getName(), newRecords));		
		}//for tableName
		
		if(collector.values().stream().allMatch(tbl -> !(tbl.getTableClass().equals("DATA") && tbl.getTableType().equals("OUTPUT"))))
			return;
		
		//Resolve the output data tables
		for(String tableName: this.resolver.getResolutionOrder()) {
			table=collector.get(tableName);			
			if(!(table.getTableClass().equals("DATA") && table.getTableType().equals("OUTPUT")))
				continue;	//skip all but output data tables
			if(table.hasField("QUERY") || table.hasField("INITIALIZE")) {
				table.createInstance();
			}
		}//for tableName
		//at this point, all tables for  input data, solver objects, and output data have been resolved
	}//createSolutionObjects	

	/**@return the model name*/
	public String getModelName() {
		return modelName;
	}
	
	/**@return the solver objects*/
	public Stream<MsdxSolverModelingObject> getSolverObjects() {
		return this.solverObjects.values().stream();
	}
	
	/**@return the solver objects of the given class*/
	public Stream<MsdxSolverModelingObject> getSolverObjects(String tableClass) {
		return this.getSolverObjects()
			.filter(solverObject -> solverObject.getTableClass().equals(tableClass));
	}
	
	/**@return the solver objects of the given class and type*/
	public Stream<MsdxSolverModelingObject> getSolverObjects(String tableClass, String tableType) {
		return this.getSolverObjects(tableClass)
			.filter(solverObject -> solverObject.getTableType().equals(tableType));
	}
	
	/**@return the table names of solver objects of the given class*/
	public Set<String> getNames(String tableClass)  {
		return this.getSolverObjects(tableClass)
			.map(solverObject -> solverObject.getTableName())
			.collect(Collectors.toSet());
	}

	/**@return the table names of solver objects of the given class and type*/
	public Set<String> getNames(String tableClass, String tableType)  {
		return this.getSolverObjects(tableClass, tableType)
			.map(solverObject -> solverObject.getTableName())
			.collect(Collectors.toSet());
	}

	/**
	 * Returns a stream of all the solver objects of a particular class 
	 * (i.e. Variables, Constraints, or Objectives) defined so far.
	 * @param tableClass
	 * @return a stream of solver objects
	 */
	public MsdxSpan all(String tableClass) {
		return this.spanFactory.union(
			this.solverObjects.values().stream()
				.filter(solverObject -> solverObject.getTableClass().equals(tableClass))
				.map(solverObject -> solverObject.getBridge()));	
	}//all


}//MsdxModel
