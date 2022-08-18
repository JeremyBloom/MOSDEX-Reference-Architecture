/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxFunctionCall;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import io.github.JeremyBloom.mosdex2.span.OperatorWithOneArgument;
import io.github.JeremyBloom.mosdex2.span.OperatorWithTwoArguments;

/**
 * This class implements the methods used to create solver-specific modeling objects 
 * for IBM's CPLEX solver.<br>
 * You need to have a licensed copy of IBM CPLEX in order to use this code.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxCplexModelingFactory implements MsdxSolverModelingFactory {

	/**The factory that creates dataframes.*/
	private MsdxDataframe.Factory dataframeFactory;
	
	/**
	 * The orientation, either "Row" or "Column".
	 * This is a constant in each implementation, set in the constructor and 
	 * immutable thereafter.
	 */
	final private String orientation;
	
	/**Creates CPLEX-specific modeling objects.*/
	protected IloCplex modeler;
	
	/**Calls the CPLEX solver (for CPLEX, the modeler and solver are the same object.*/
	protected IloCplex solver;
	
	/**The model name.*/
	private String modelName;
	
	/**
	 * The function table is a mapping between the function names, established in the 
	 * FunctionCall object, and the function implementations as Java Functions.
	 * The function table is created by the initializeFunctionTable method in this  
	 * this factory class.
	 */
	protected Map<String, Function<MsdxContainer<Object>, ?>> functionTable;

	/**
	 * Creates a new CPLEX factory instance.
	 * @param modeler a CPLEX instance
	 * @param dataframeFactory creates the Dataframes
	 */
	public MsdxCplexModelingFactory(IloCplex modeler, MsdxDataframe.Factory dataframeFactory) {
		super();
		this.orientation= "Row";
		this.modeler = modeler;
		this.solver= modeler;
		this.dataframeFactory= dataframeFactory;
		this.modelName= null;
		this.functionTable= initializeFunctionTable(this.dataframeFactory);
	}

	@Override
	public String getName() {
		if(this.modelName == null)
			throw new IllegalArgumentException("Model name has not been set");
		return this.modelName;
	}

	@Override
	public MsdxSolverModelingFactory withName(String modelName) {
		if(this.modelName != null)
			throw new IllegalArgumentException("Model name has already been set");
		this.modelName= modelName;
		return this;
	}

	@Override
	public String orientation() {
		return this.orientation;
	}

	@Override
	public OperatorWithOneArgument makeVariable(String tableClass, String tableType) {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		if(!tableClass.equals("VARIABLE"))
			throw new IllegalArgumentException("Illegal table class");
		
		return new OperatorWithOneArgument() {
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> parameterSchema) {
			    if(this.resultSchema!=null)
			        return this;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(parameterSchema, "Name")
					.copyItem(parameterSchema, "Column")
					.addItem("Variable", IloNumVar.class)
					.build();
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				String columnId= parameter.get("Column").toString();	
				IloNumVar variable= null;
				try {
					if(tableType.equals("CONTINUOUS")) {
						variable= MsdxCplexModelingFactory.this.modeler.numVar(
							((Number)parameter.get("LowerBound")).doubleValue(), 
							((Number)parameter.get("UpperBound")).doubleValue(), 
							IloNumVarType.Float,
							columnId);
					}
					else if (tableType.equals("INTEGER")) {
						variable= MsdxCplexModelingFactory.this.modeler.intVar(
							((Number)parameter.get("LowerBound")).intValue(), 
							((Number)parameter.get("UpperBound")).intValue(), 
							columnId);
					}
					else if (tableType.equals("BINARY")) {
						variable= MsdxCplexModelingFactory.this.modeler.boolVar(columnId);
					}
					else
						throw new IllegalArgumentException("Illegal type " + tableType.toString());
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace(System.err);
				}
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply			
		}/*OperatorWithOneArgument*/;//return	
	}//makeVariable

	@Override
	public OperatorWithOneArgument makeConstraint(String tableClass, String tableType) {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		if(!tableClass.equals("CONSTRAINT"))
			throw new IllegalArgumentException("Illegal table class");
		
		return new OperatorWithOneArgument() {
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> parameterSchema) {
			    if(this.resultSchema!=null)
			        return this;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(parameterSchema, "Name")
					.copyItem(parameterSchema, "Row")
					.addItem("Constraint", IloRange.class)
					.addItem("Expression", IloNumExpr.class)
					.build();
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				String rowId= parameter.get("Row").toString();
				IloRange constraint= null;	
				IloNumExpr expression= null;
				String sense= (String) parameter.get("Sense");
				try {
					expression= MsdxCplexModelingFactory.this.modeler.numExpr();
					
					if(new HashSet<String>(Arrays.asList("LE", "<=", "=<")).contains(sense))
						constraint= MsdxCplexModelingFactory.this.modeler.addLe(expression, ((Number)parameter.get("RHS")).doubleValue(), rowId);
					else if(new HashSet<String>(Arrays.asList("EQ", "==")).contains(sense))
						constraint= MsdxCplexModelingFactory.this.modeler.addEq(expression, ((Number)parameter.get("RHS")).doubleValue(), rowId);
					else if(new HashSet<String>(Arrays.asList("GE", ">=", "=>")).contains(sense))
						constraint= MsdxCplexModelingFactory.this.modeler.addGe(expression, ((Number)parameter.get("RHS")).doubleValue(), rowId);
					else 
						throw new IllegalArgumentException(sense + " is not an allowed sense for " + tableClass);
	
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace(System.err);
				}
				return MsdxRecord.builder(this.getResultSchema())
						.copyItem(parameter, "Name")
						.copyItem(parameter, "Row")
						.addItem("Constraint", constraint)
						.addItem("Expression", expression)
						.build();				
			}//apply		
		}/*OperatorWithOneArgument*/;//return
	}//makeConstraint

	@Override
	public OperatorWithOneArgument makeObjective(String tableClass, String tableType) {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		if(!tableClass.equals("OBJECTIVE"))
			throw new IllegalArgumentException("Illegal table class");
		
		return new OperatorWithOneArgument() {
			@Override
			public OperatorWithOneArgument withResultSchema(MsdxContainer<Class<?>> parameterSchema) {
			    if(this.resultSchema!=null)
			        return this;
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(parameterSchema, "Name")
					.copyItem(parameterSchema, "Row")
					.addItem("Objective", IloObjective.class)
					.addItem("Expression", IloNumExpr.class)
					.build();
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				String rowId= parameter.get("Row").toString();
				IloObjective objective= null;	
				IloNumExpr expression= null;
				String sense= (String) parameter.get("Sense");
				try {
					expression= MsdxCplexModelingFactory.this.modeler.numExpr();
					
					if(new HashSet<String>(Arrays.asList("Minimize", "MINIMIZE", "Min", "MIN")).contains(sense))
						objective= MsdxCplexModelingFactory.this.modeler.addObjective(IloObjectiveSense.Minimize, expression, rowId);
					else if(new HashSet<String>(Arrays.asList("Maximize", "MAXIMIZE", "Max", "MAX")).contains(sense))
						objective= MsdxCplexModelingFactory.this.modeler.addObjective(IloObjectiveSense.Maximize, expression, rowId);
					else 
						throw new IllegalArgumentException(sense + " is not an allowed sense for " + tableClass);
					objective.setConstant(((Number) parameter.get("Constant")).doubleValue());
	
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace(System.err);
				}
				return MsdxRecord.builder(this.getResultSchema())
						.copyItem(parameter, "Name")
						.copyItem(parameter, "Row")
						.addItem("Objective", objective)
						.addItem("Expression", expression)
						.build();				
			}//apply	
		}/*OperatorWithOneArgument*/;//return	
	}//makeObjective

	@Override
	public OperatorWithTwoArguments multiply(String tableClass, String tableType) {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		if(!tableClass.equals("TERM"))
			throw new IllegalArgumentException("Illegal table class");
		if(!(tableType.equals("LINEAR") || tableType.equals("QUADRATIC")))
			throw new IllegalArgumentException("Illegal table type");
		
		return new OperatorWithTwoArguments() {
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> leftInputSchema,	//either a term parameters schema or a joined schema
				String leftKeyFieldName, 
				MsdxContainer<Class<?>> elementSchema,	//a bridge element that contains a solver variable
				String elementKeyFieldName) 
		{
		    if(this.resultSchema!=null)
		        return this;
			this.leftInputSchema= leftInputSchema;			//{Row, Column, nothing or (Variable and Column2), Coefficient}
			this.leftKeyFieldName= leftKeyFieldName;		//should be "Column"
			this.rightInputSchema= elementSchema;			//{Column, Name, Variable}
			this.rightKeyFieldName= elementKeyFieldName;	//should be "Column"
			
			this.resultSchema= MsdxContainer.<Class<?>>builder()
				.copyItems(leftInputSchema.merge(elementSchema.delete(elementKeyFieldName)))
				.removeItem("Name")
				.removeItem("Variable")
				.removeItem("Coefficient")
				.addItem("Expression", IloNumExpr.class)
				.build();
				// termSchema= {Row, Column, Column2 or nothing, Expression};
			return this;
			}

			@Override
			protected MsdxContainer<Object> onKeyMatch(
				MsdxContainer<Object> left, 
				MsdxContainer<Object> variable) 
			{
				Double coefficient= ((Number)left.get("Coefficient")).doubleValue();					
				IloNumExpr product= null;
				
				try {
					product= tableType.equals("LINEAR") ?
						MsdxCplexModelingFactory.this.modeler.prod(coefficient, (IloNumVar) variable.get("Variable")) :					
						MsdxCplexModelingFactory.this.modeler.prod(coefficient, (IloNumVar) variable.get("Variable"), (IloNumVar) variable.get("Variable2"));					
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left, "Row")
					.copyItem(left, "Column")
					.copyItem(left, "Column2")	//if present
					.addItem("Expression", product)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> left,
				Optional<MsdxContainer<Object>> right) 
			{
				if(!left.isPresent())
					throw new IllegalArgumentException("Missing left container");	//can't happen
				
				IloNumExpr empty= null;
				try {
					empty= MsdxCplexModelingFactory.this.modeler.numExpr();
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}			
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left.get(), "Row")
					.copyItem(left.get(), "Column")
					.copyItem(left.get(), "Column2")	//if present
					.addItem("Expression", empty)
					.build();
			}//noKeyMatch
		}/*OperatorWithTwoArguments*/;//return	
	}//multiply

	@Override
	public OperatorWithTwoArguments add(String tableClass, String tableType) {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		if(!tableClass.equals("TERM"))
			throw new IllegalArgumentException("Illegal table class");
		if(!(tableType.equals("LINEAR")))
			throw new IllegalArgumentException("Illegal table type");
		
		return new OperatorWithTwoArguments() {
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> accumulationSchema,
				String accumulationKeyFieldName, 
				MsdxContainer<Class<?>> valueSchema, 
				String valueKeyFieldName) 
			{
			    if(this.resultSchema!=null)
			        return this;
				this.leftInputSchema= accumulationSchema;			//may be empty if using with reduceByKey
				this.leftKeyFieldName= accumulationKeyFieldName;	//should be "Row"
				this.rightInputSchema= valueSchema;
				this.rightKeyFieldName= valueKeyFieldName;			//should be "Row"
			    
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.addItem(accumulationKeyFieldName/*=="Row"*/, valueSchema.get(valueKeyFieldName))
					.copyItem(accumulationSchema, "Constraint")		
					.copyItem(accumulationSchema, "Objective")		
					.copyItem(valueSchema, "Expression")
					.build();
					//expressionSchema= {Row, Constraint or Objective or nothing, Expression}
					return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(
				MsdxContainer<Object> accumulation, 
				MsdxContainer<Object> term) 
			{
				IloNumExpr sum= null;
				try {
					sum = MsdxCplexModelingFactory.this.modeler.sum(
						((IloNumExpr) accumulation.get("Expression")), 
						((IloNumExpr) term.get("Expression")));
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}			
				return MsdxRecord.builder(this.getResultSchema())
					.addItem(this.leftKeyFieldName/*=="Row"*/, accumulation.get(this.leftKeyFieldName/*=="Row"*/))
					.copyItem(accumulation, "Constraint")	//at most one of these will be present	
					.copyItem(accumulation, "Objective")	//at most one of these will be present
					.addItem("Expression", sum)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> accumulation, 
				Optional<MsdxContainer<Object>> term) 
			{
				if(!accumulation.isPresent())
					throw new IllegalStateException("Empty accumulation");
				return accumulation.get();	//nothing added to current accumulation
			}//noKeyMatch		
		}/*OperatorWithTwoArguments*/;//return		
	}//add

	@Override
	public OperatorWithTwoArguments attach() {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation

		return new OperatorWithTwoArguments() {
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> expressionsSchema,
				String expressionsKeyFieldName, 
				MsdxContainer<Class<?>> allRowsSchema, 
				String allRowsKeyFieldName) 
			{
			    if(this.resultSchema!=null)
			        return this;
			    
				this.leftInputSchema= expressionsSchema;
				this.leftKeyFieldName= expressionsKeyFieldName;
				this.rightInputSchema= allRowsSchema;
				this.rightKeyFieldName= allRowsKeyFieldName;
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					//expression schema=	{Row, Expression}
					//all rows schema=		{Name, Row, Constraint, Objective, Expression}
					.copyItem(allRowsSchema, "Row")
					.copyItem(allRowsSchema, "Name")
					.copyItem(allRowsSchema, "Constraint")
					.copyItem(allRowsSchema, "Objective")
					.copyItem(allRowsSchema, "Expression")
					.build();
					//attached result schema= {Name, Row, Constraint, Objective, Expression}
				return this;
			}//withResultSchema

			/**
			 * Same as super.apply except that only a missing row container counts as a key mismatch.
			 */
			@Override
			public MsdxContainer<Object> apply(Optional<MsdxContainer<Object>> expressionContainer, Optional<MsdxContainer<Object>> rowContainer) {
				if(expressionContainer.isPresent() && rowContainer.isPresent()) {
					this.keysMatch= true;
					return onKeyMatch(expressionContainer.get(), rowContainer.get());
				}
				//else
					if(!rowContainer.isPresent())
						this.keysMatch= false;
					return noKeyMatch(expressionContainer, rowContainer);
			}//apply
			
			@Override
			protected MsdxContainer<Object> onKeyMatch(
				MsdxContainer<Object> expressionContainer, 
				MsdxContainer<Object> rowContainer) 
			{
				IloNumExpr sum= null;
				try {
					sum = MsdxCplexModelingFactory.this.modeler.sum(
						((IloNumExpr) rowContainer.get("Expression")), 
						((IloNumExpr) expressionContainer.get("Expression")));
					
					if(rowContainer.get("Constraint")!=null)
						((IloRange)rowContainer.get("Constraint")).setExpr(sum);
					else //rowContainer.get("Objective")!=null
						((IloObjective)rowContainer.get("Objective")).setExpr(sum);
					
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(rowContainer, "Row")
					.copyItem(rowContainer, "Name")
					.copyItem(rowContainer, "Constraint")	//only one of these will be non-null
					.copyItem(rowContainer, "Objective")	//only one of these will be non-null
					.addItem("Expression", sum)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> expressionContainer, 
				Optional<MsdxContainer<Object>> rowContainer) 
			{
				if(!rowContainer.isPresent()) {	//expression container is present
					MsdxRecord.Builder builder= MsdxRecord.builder(this.getResultSchema())
						.copyItems(expressionContainer.get());	//adds Row and Expression
					for(String fieldName: (rightInputSchema/*allRows*/.delete(rightKeyFieldName, "Name")).itemNames())
						builder.addItemIf(!expressionContainer.get().containsField(fieldName), fieldName, null);
					builder.addItem("Name", "NO_MATCH");	//marker for unmatched expressions
					return builder.build();
				}
				//else rowContainer is present with no expression
				return rowContainer.get();	//nothing attached to current modeling object			
			}//noKeyMatch		
		}/*OperatorWithOneArgument*/;//return
	}//attach

	/**
	 * Write a text representation of the model. 
	 * Usually, the text will use a standard (non-MOSDEX) format, such as .mps or .lp;
	 * Used, in particular, when the model is not to be solved.
	 * In this version, the .lp format is used, 
	 * but you can change the file type of tempFile 
	 * to .mps, if that is the preferred format.
	 * 
	 * @param model not used in CPLEX
	 * @param out destination for the text
	 */
	@Override
	public void generate(MsdxModel model, PrintStream out) {
		if(out==null) 
			return;
		Path tempFile= null;
		try {
			tempFile= Files.createTempFile(this.modelName, ".lp");
		MsdxCplexModelingFactory.this.modeler.exportModel(tempFile.toString());			
			Files.copy(tempFile, out);
			out.println();
		} catch (IOException | IloException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}//generate

	@Override
	public String solve(MsdxModel model, PrintStream out) {
		boolean success= false;
		String status= null;
		if(out!=null)
			solver.setOut(out);
		else
			solver.setOut(System.out);
		Path tempFile= null;

		try {
			success= this.solver.solve();
			status= this.solver.getCplexStatus().toString();
			if(out!=null) {
				out.println("CPLEX solve status= " + (success ? status : "Failure"));
				out.println();
				tempFile= Files.createTempFile(this.modelName, ".sol");		
				this.solver.writeSolution(tempFile.toString());
				Files.copy(tempFile, out);
				out.println();
			}		
		} catch (IloException | IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return success ? status : "Failure";
	}//solve
	
	/**
	 * This method creates an initial set of function entries for the most common 
	 * solution information produced by a solver. You can add others if you need them, 
	 * using the steps outlined below.
	 * 
	 * Optimization solvers can produce a plethora of information about the solution. As this time, MOSDEX supports 
	 * only the basics: optimal primal value, reduced cost, and basis status of a variable, optimal dual value and slack of a constraint, and 
	 * the optimal value of the objective function. However, a user, with a little bit of Java programming, can 
	 * add to this list by taking the following steps.
	 * - specify the name and argument of the solver call in the MOSDEX Table instance. This is simply a string entered as an item in the record, of the form
	 *   "functionName(argument)" (see the Msdx2FunctionCall documentation). The MOSDEX parser will create a Msdx2FunctionCall instance from this text.
	 * - In your application program, retrieve the function table from this modeling factory.
	 * - Create a new entry in this function table by writing a short Java Function and include the solver call in its apply method. 
	 *   See the intializeTable method for details.
	 * - Make sure you register the function for use in SQL by calling 
	 *   MsdxDataframe.Factory.registerFunctionCall(functionName) when you create the new entry.
	 * - That's it. MOSDEX will handle the rest.
	 * 
	 * @param dataframeFactory
	 * @return the function table
	 */
	@Override
	public Map<String, Function<MsdxContainer<Object>, ?>> initializeFunctionTable(MsdxDataframe.Factory dataframeFactory) {
		if(dataframeFactory==null)
			throw new IllegalStateException("Dataframe factory is not defined");
		Map<String, Function<MsdxContainer<Object>, ?>> functions= new LinkedHashMap<String, Function<MsdxContainer<Object>, ?>>();
		String functionName;
		Function<MsdxContainer<Object>, ?> function;	

		functionName= "PrimalValue";
		dataframeFactory.registerFunctionCall(functionName);
		function= new Function<MsdxContainer<Object>, Double>() {
			@Override
			public Double apply(MsdxContainer<Object> var) {
				//make sure to include the analogous test in each function call to assure the applicability of the function to the modeling object
				if(!var.containsField("Variable"))
					throw new IllegalArgumentException("Function PrimalValue is applicable only for a Variable");				
				try {
					return solver.getValue((IloNumVar) var.get("Variable"));
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}//apply	
		};//Function
		
		if(functions.containsKey(functionName))
			throw new IllegalArgumentException("Function " + functionName + " has already been defined");
		functions.put(functionName, function);
		
		functionName= "ReducedCost";
		dataframeFactory.registerFunctionCall(functionName);
		function= new Function<MsdxContainer<Object>, Double>() {
			@Override
			public Double apply(MsdxContainer<Object> var) {
				if(!var.containsField("Variable"))
					throw new IllegalArgumentException("Function ReducedCost is applicable only for a Variable");				
				try {
					return solver.getReducedCost((IloNumVar) var.get("Variable"));
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}//apply		
		};//Function
		if(functions.containsKey(functionName))
			throw new IllegalArgumentException("Function " + functionName + " has already been defined");
		functions.put(functionName, function);
		
		functionName= "BasisStatus";
		dataframeFactory.registerFunctionCall(functionName);
		function= new Function<MsdxContainer<Object>, String>() {
			@Override
			public String apply(MsdxContainer<Object> var) {
				if(!var.containsField("Variable"))
					throw new IllegalArgumentException("Function BasisStatus is applicable only for a Variable");				
				try {
					return solver.getBasisStatus((IloNumVar) var.get("Variable")).toString();
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}//apply		
		};//Function
		if(functions.containsKey(functionName))
			throw new IllegalArgumentException("Function " + functionName + " has already been defined");
		functions.put(functionName, function);
		
		functionName= "DualValue";
		dataframeFactory.registerFunctionCall(functionName);
		function= new Function<MsdxContainer<Object>, Double>() {
			@Override
			public Double apply(MsdxContainer<Object> con) {
				if(!con.containsField("Constraint"))
					throw new IllegalArgumentException("Function DualValue is applicable only for a Constraint");				
				try {
					return solver.getDual((IloRange) con.get("Constraint"));
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}//apply	
		};//Function
		if(functions.containsKey(functionName))
			throw new IllegalArgumentException("Function " + functionName + " has already been defined");
		functions.put(functionName, function);
		
		functionName= "Slack";
		dataframeFactory.registerFunctionCall(functionName);
		function= new Function<MsdxContainer<Object>, Double>() {
			@Override
			public Double apply(MsdxContainer<Object> con) {
				if(!con.containsField("Constraint"))
					throw new IllegalArgumentException("Function Slack is applicable only for a Constraint");				
				try {
					return solver.getSlack((IloRange) con.get("Constraint"));
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}//apply		
		};//Function
		if(functions.containsKey(functionName))
			throw new IllegalArgumentException("Function " + functionName + " has already been defined");
		functions.put(functionName, function);
		
		functionName= "ObjectiveValue";
		dataframeFactory.registerFunctionCall(functionName);
		function= new Function<MsdxContainer<Object>, Double>() {
			@Override
			public Double apply(MsdxContainer<Object> obj) {
				if(!obj.containsField("Objective"))
					throw new IllegalArgumentException("Function ObjectiveValue is applicable only for an Objective");				
				try {
					return solver.getObjValue();
				} catch (IloException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}//apply	
		};//Function
		if(functions.containsKey(functionName))
			throw new IllegalArgumentException("Function " + functionName + " has already been defined");
		functions.put(functionName, function);
		
		return functions;
	}//initializeFunctionTable

	@Override
	public Map<String, Function<MsdxContainer<Object>, ?>> getFunctionTable() {
		return functionTable;
	}

	@Override
	public OperatorWithTwoArguments retrieveSolution(String tableClass) {
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		
		return new OperatorWithTwoArguments() {
			
			Set<String> functionFields;
			Set<String> otherFields;

			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> solutionElementSchema,			//for an element of the solution bridge
				String solutionElementKeyFieldName, 
				MsdxContainer<Class<?>> recordSchema,					//for a table record
				String recordKeyFieldName) 
			{
				this.leftInputSchema= solutionElementSchema;			//{Column, Name, Variable, ...}
				this.leftKeyFieldName= solutionElementKeyFieldName;		//should be "Column"
				this.rightInputSchema= recordSchema;					//{Column, Name, Value, ...}
				this.rightKeyFieldName= recordKeyFieldName;				//should be "Column"		
				
			    if(this.resultSchema!=null)
			        return this;
				this.functionFields= MsdxFunctionCall.findFunctionFieldsIn(recordSchema);
				this.otherFields= recordSchema.toStream()
					.filter(entry -> !this.functionFields.contains(entry.getKey()))
					.collect(Collectors.toMap(
						entry -> entry.getKey(), 
						entry -> entry.getValue(), 
						(leftValue, rightValue) -> leftValue,	//can't happen 
						LinkedHashMap::new))					//used to preserve the order of the fields
					.keySet();
				MsdxContainer.Builder<Class<?>> schemaBuilder = MsdxContainer.<Class<?>>builder();
				//element schema=	{Column/Row, Variable/Constraint/Objective, other element fields}
				//record schema=	{Name, Column/Row, function fields, other record fields}
				schemaBuilder.copyItems(recordSchema, this.otherFields);
				for(String fieldName: this.functionFields) {
					schemaBuilder.addItem(fieldName, MsdxFunctionCall.getResultTypeFor(recordSchema.get(fieldName)));					
				}//for functionName
				//result schema=	{Name, Column/Row, function values, other record fields}
				this.resultSchema= 	schemaBuilder.build();
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(
				MsdxContainer<Object> element,//an element of the modeling object bridge
				MsdxContainer<Object> record) //a table record 
			{
				MsdxContainer.Builder<Object> recordBuilder= MsdxRecord.builder(this.resultSchema);
				Object solutionItem= null;	//an item of a modeling object bridge element
				MsdxFunctionCall functionCall;
				recordBuilder.copyItems(record, this.otherFields);						
				for(String fieldName: this.functionFields) {
					functionCall= (MsdxFunctionCall) record.get(fieldName);
					functionCall.validate(
						functionTable.keySet(), 
						leftInputSchema /*solutionElementSchema*/);
					solutionItem= functionTable.get(functionCall.getFunctionName()).apply(element);
					functionCall.setValue(solutionItem);
					recordBuilder.addItem(fieldName, solutionItem);					
				}//for functionName
				return recordBuilder.build();
			}//onKeyMatch

			/**
			 * Returns a record with null values for the function call values.
			 */
			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> element,
				Optional<MsdxContainer<Object>> record) 
			{
				if(!record.isPresent())
					throw new IllegalStateException("Table record is not present");
				MsdxContainer.Builder<Object> recordBuilder= MsdxRecord.builder(this.resultSchema);
				MsdxFunctionCall functionCall;
				recordBuilder.copyItems(record.get(), this.otherFields);
				for(String fieldName: this.functionFields) {
					functionCall= (MsdxFunctionCall) record.get().get(fieldName);
					functionCall.validate(
						functionTable.keySet(), 
						leftInputSchema /*solutionElementSchema*/);
					recordBuilder.addItem(fieldName, null);
					//Note FunctionCall value field is null when created.
				}//for functionName
				return recordBuilder.build();
			}//noKeyMatch	
		}/*OperatorWithTwoArguments*/;//return
	}//retrieveVariableSolution
	

}//MsdxCplexModelingFactory
