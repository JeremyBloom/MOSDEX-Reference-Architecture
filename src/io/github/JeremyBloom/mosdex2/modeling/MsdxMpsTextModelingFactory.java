/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe.Factory;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import io.github.JeremyBloom.mosdex2.span.OperatorWithOneArgument;
import io.github.JeremyBloom.mosdex2.span.OperatorWithTwoArguments;

/**
 * This class implements the methods used to create modeling objects 
 * to write an .mps text file representation of an optimization model.
 * Illustrates column-oriented model generation.
 *
 * This class includes static member classes to represent .mps format modeling objects.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxMpsTextModelingFactory implements MsdxSolverModelingFactory {

	/**The factory that creates dataframes.*/
	private MsdxDataframe.Factory dataframeFactory;
	
	/**
	 * The orientation, either "Row" or "Column".
	 * This is a constant in each implementation, set in the constructor and 
	 * immutable thereafter.
	 */
	final private String orientation;
	
	/**The model name.*/
	private String modelName;
	
	/**
	 * Not used in .mps files. 
	 * However, function calls may appear in some MOSDEX tables, 
	 * when a MOSDEX File is used with other solvers,
	 */
	protected Map<String, Function<MsdxContainer<Object>, ?>> functionTable;

	/**
	 * Creates a new mps factory instance.
	 * @param dataframeFactory creates the Dataframes
	 */
	public MsdxMpsTextModelingFactory(Factory dataframeFactory) {
		super();
		this.orientation= "Column";
		this.modelName= null;
		this.dataframeFactory= dataframeFactory;
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
		if(!this.orientation().equals("Column"))
			throw new UnsupportedOperationException();	//must override this method for row orientation
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
					.addItem("Variable", Variable.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable(parameter.get("Column"), tableType)
					.setBounds(
						parameter.get("LowerBound"), 
						parameter.get("UpperBound"));
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.addItem("Expression", new Expression())
					.build();		
			}//apply		
		}/*OperatorWithOneArgument*/;//return
	}//makeVariable

	@Override
	public OperatorWithOneArgument makeConstraint(String tableClass, String tableType) {
		if(!this.orientation().equals("Column"))
			throw new UnsupportedOperationException();	//must override this method for row orientation
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
					.addItem("Constraint", Constraint.class)
					.build();
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint(parameter.get("Row"))
				.setSense(parameter.get("Sense"))
				.setRHS(parameter.get("RHS"));
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.build();		
			}//apply		
		}/*OperatorWithOneArgument*/;//return		
	}//makeConstraint

	@Override
	public OperatorWithOneArgument makeObjective(String tableClass, String tableType) {
		if(!this.orientation().equals("Column"))
			throw new UnsupportedOperationException();	//must override this method for row orientation
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
					.addItem("Objective", Objective.class)
					.build();
				return this;
			}//withResultSchema

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Objective objective= new Objective(parameter.get("Row"))
					.setSense(parameter.get("Sense"))
					.setConstant(parameter.get("Constant"));
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Objective", objective)
					.build();		
			}//apply			
		}/*OperatorWithOneArgument*/;//return		
	}//makeObjective

	@Override
	public OperatorWithTwoArguments multiply(
		final String tableClass, 
		final String tableType) 
	{
		if(!this.orientation().equals("Column"))
			throw new UnsupportedOperationException();	//must override this method for row orientation
		if(!tableClass.equals("TERM"))
			throw new IllegalArgumentException("Illegal table class");
		if(!(tableType.equals("LINEAR")))
			throw new IllegalArgumentException("Illegal table type");
				
		return new OperatorWithTwoArguments() {
			@Override
			public OperatorWithTwoArguments withResultSchema(
					MsdxContainer<Class<?>> termParameterSchema,	//term parameters schema
					String termParameterKeyFieldName, 
					MsdxContainer<Class<?>> rowsSchema, 
					String rowsKeyFieldName) 
			{
			    if(this.resultSchema!=null)
			        return this;
				this.leftInputSchema= termParameterSchema;			//{Row, Column, Coefficient}
				this.leftKeyFieldName= termParameterKeyFieldName;	//should be "Row"
				this.rightInputSchema= rowsSchema;					//{Row, Name, Constraint, Objective}
				this.rightKeyFieldName= rowsKeyFieldName;			//should be "Row"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(termParameterSchema.merge(rowsSchema.delete(rowsKeyFieldName)))	//{Row, Column, Constraint, Objective, Coefficient}
					.removeItem("Coefficient")
					.removeItem("Name")
					.removeItem("Constraint")
					.removeItem("Objective")
					.addItem("Expression", Expression.class)
					.build();
					// termSchema= {Row, Column, Expression};
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> termParameter, MsdxContainer<Object> row) {
			Expression product;
				product= Expression.multiply(
					termParameter.get("Coefficient").toString(), 
					(Constraint) row.get("Constraint"),
					(Objective) row.get("Objective"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(termParameter, "Row")
					.copyItem(termParameter, "Column")
					.addItem("Expression", product)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(
				Optional<MsdxContainer<Object>> termParameter,
				Optional<MsdxContainer<Object>> right) 
			{
				if(!termParameter.isPresent())
					throw new IllegalArgumentException("Missing term parameter container");	//can't happen
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(termParameter.get(), "Row")
					.copyItem(termParameter.get(), "Column")
					.addItem("Expression", Expression.empty())
					.build();
			}//noKeyMatch			
		}/*OperatorWithTwoArguments*/;//return	
	}//multiply
	
	@Override
	public OperatorWithTwoArguments add(String tableClass, String tableType) {
		if(!this.orientation().equals("Column"))
			throw new UnsupportedOperationException();	//must override this method for row orientation
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
				this.leftKeyFieldName= accumulationKeyFieldName;	//should be "Column"
				this.rightInputSchema= valueSchema;
				this.rightKeyFieldName= valueKeyFieldName;			//should be "Column"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.addItem(accumulationKeyFieldName/*=="Column"*/, valueSchema.get(valueKeyFieldName))
					.copyItem(accumulationSchema, "Variable")		//if present
					.copyItem(valueSchema, "Expression")
					.build();
					//expressionSchema= {Column, Variable or nothing, Expression}
				return this;				
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(
					MsdxContainer<Object> accumulation, 
					MsdxContainer<Object> term) 
			{
				Expression accumulationPlusTerm= ((Expression) accumulation.get("Expression")).add((Expression) term.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.addItem(this.leftKeyFieldName/*=="Column"*/, accumulation.get(this.leftKeyFieldName/*=="Column"*/))
					.copyItem(accumulation, "Variable")		//if present	
					.addItem("Expression", accumulationPlusTerm)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> accumulation, Optional<MsdxContainer<Object>> right) {
				if(!accumulation.isPresent())
					throw new IllegalStateException("Empty accumulation");
				return accumulation.get();	//nothing added to current accumulation
			}//noKeyMatch			
		}/*OperatorWithOneArgument*/;//return		
	}//add

	@Override
	public OperatorWithTwoArguments attach() {
		if(!this.orientation().equals("Column"))
			throw new UnsupportedOperationException();	//must override this method for row orientation

		return new OperatorWithTwoArguments() {
			@Override
			public OperatorWithTwoArguments withResultSchema(
				MsdxContainer<Class<?>> expressionSchema,
				String expressionKeyFieldName,
				MsdxContainer<Class<?>> variableSchema, 
				String variableKeyFieldName) 
			{
			    if(this.resultSchema!=null)
			        return this;
				
				if(!(variableSchema.containsField("Variable")))
					throw new IllegalArgumentException("Modeling object must contain a variable");
				this.leftInputSchema= expressionSchema;
				this.leftKeyFieldName= expressionKeyFieldName;
				this.rightInputSchema= variableSchema;
				this.rightKeyFieldName= variableKeyFieldName;
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItem(variableSchema, "Name")
					.copyItem(variableSchema, "Column")
					.copyItem(variableSchema, "Variable")
					.copyItem(variableSchema, "Expression")
					.build();
					//expressionSchema= {Column Name, Column, Variable, Expression}
				return this;
			}//withResultSchema

			/**
			 * Same as super.apply except that only a missing row container counts as a key mismatch.
			 */
			@Override
			public MsdxContainer<Object> apply(Optional<MsdxContainer<Object>> expressionContainer, Optional<MsdxContainer<Object>> variableContainer) {
				if(expressionContainer.isPresent() && variableContainer.isPresent()) {
					this.keysMatch= true;
					return onKeyMatch(expressionContainer.get(), variableContainer.get());
				}
				//else
					if(!variableContainer.isPresent())
						this.keysMatch= false;
					return noKeyMatch(expressionContainer, variableContainer);
			}//apply
			
			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> expressionContainer, MsdxContainer<Object> variableContainer) {
				Expression sum= ((Expression) variableContainer.get("Expression")).add((Expression) expressionContainer.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(variableContainer, "Name")
					.copyItem(variableContainer, "Column")
					.copyItem(variableContainer, "Variable")
					.addItem("Expression", sum)
					.build();
			}//onKeyMatch

			@Override
			protected MsdxContainer<Object> noKeyMatch(Optional<MsdxContainer<Object>> expressionContainer, Optional<MsdxContainer<Object>> variableContainer) {
				if(!variableContainer.isPresent()) {	//expression container is present
					MsdxRecord.Builder builder= MsdxRecord.builder(this.getResultSchema())
						.copyItems(expressionContainer.get());	//adds Row and Expression
					for(String fieldName: (rightInputSchema/*allRows*/.delete(rightKeyFieldName, "Name")).itemNames())
						builder.addItemIf(!expressionContainer.get().containsField(fieldName), fieldName, null);
					builder.addItem("Name", "NO_MATCH");	//marker for unmatched expressions
					return builder.build();
				}
				//else variableContainer is present with no expression
				return variableContainer.get();	//nothing attached to current modeling object						
			}//noKeyMatch		
		}/*OperatorWithOneArgument*/;//return
	}//attach
	
	@Override
	public void generate(MsdxModel model, PrintStream out) {	
		if(out==null)
			throw new IllegalArgumentException("Missing output stream");
		
		out.println("NAME " + model.getModelName() + ".mps");
		out.println("OBJSENSE");
		
		model.getSolverObjects("OBJECTIVE")
			.flatMap(modelingObject -> modelingObject.getBridge().apply())
			.forEach(item -> out.println(((Objective)item.get("Objective")).getRowId()));
		
		out.println("ROWS");
		model.getSolverObjects("OBJECTIVE")
			.flatMap(modelingObject -> modelingObject.getBridge().apply())
			.forEach(item -> out.println(((Objective)item.get("Objective")).toString()));
		model.getSolverObjects("CONSTRAINT")
			.flatMap(modelingObject -> modelingObject.getBridge().apply())
			.forEach(item -> out.println(((Constraint)item.get("Constraint")).toString()));
		
		Consumer<MsdxContainer<Object>> formatColumn= new  Consumer<MsdxContainer<Object>>() {

			@Override
			public void accept(MsdxContainer<Object> modelingObject) {
				if(modelingObject.get("Variable")==null)
					throw new IllegalArgumentException("Modeling object does not contain a variable");
				String variableName= ((Variable)modelingObject.get("Variable")).getColumnId();

				if(modelingObject.get("Expression")==null)
					throw new IllegalArgumentException("Modeling object does not contain an expression");
				Expression expression= ((Expression)modelingObject.get("Expression"));
				
				StringBuilder result= new StringBuilder();
				Iterator<String> components= expression.twoAtATime();
				
				while(components.hasNext()) {
					result
						.append(variableName)
						.append(" ")
						.append(components.next());
					if(components.hasNext())
						result.append(System.lineSeparator());				
				}
				out.println(result.toString());
			}//accept			
		};//formatColumn
		
		out.println("COLUMNS");
		model.getSolverObjects("VARIABLE")
			.flatMap(variable -> variable.getBridge().apply())
			.forEach(modelingObject -> formatColumn.accept(modelingObject));
		
		out.println("BOUNDS");
		model.getSolverObjects("VARIABLE")
			.flatMap(modelingObject -> modelingObject.getBridge().apply())
			.flatMap(item -> ((Variable)item.get("Variable")).getBounds().stream())
			.forEach(bound -> out.println(bound));
		
		out.println("ENDATA");
	}//generate

	@Override
	public String solve(MsdxModel model, PrintStream out) {
		return "Not supported";
	}

	@Override
	public Map<String, Function<MsdxContainer<Object>, ?>> getFunctionTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Function<MsdxContainer<Object>, ?>> initializeFunctionTable(MsdxDataframe.Factory dataframeFactory) {
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
	}

	@Override
	public OperatorWithTwoArguments retrieveSolution(String tableClass) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the expression as a string without a leading plus sign, if any
	 */
	public static String trim(StringBuilder expression) {
		if(expression.length()==0)
			return expression.toString();
		return expression.charAt(0)=='+' ?
			expression.deleteCharAt(0).toString() :
			expression.toString();
	}
	
	/**
	 * This class is part of the API for the .mps "solver"
	 */
	public static class Variable {
		
		String columnId;
		String type;
		List<String> bounds;
	
		Variable(Object columnId, String type) {
			super();
			this.columnId = columnId.toString();
			this.type = type;
		}
		
		String getColumnId() {
			return columnId;
		}

		List<String> getBounds() {
			return bounds;
		}

		Variable setBounds(Object lower, Object upper) {
			if(lower instanceof Integer && upper instanceof Integer)
				return setBounds((Integer) lower, (Integer) upper);
			if(lower instanceof Double && upper instanceof Double)
				return setBounds((Double) lower, (Double) upper);
			if(lower instanceof IEEEDouble && upper instanceof IEEEDouble)
				return setBounds((IEEEDouble) lower, (IEEEDouble) upper);
			return setBounds(lower.toString(), upper.toString());		
		}
			
		Variable setBounds(String lower, String upper) {
			Map<String, StringBuilder> bounds= new LinkedHashMap<String, StringBuilder>();
			bounds.put("LO", new StringBuilder());
			bounds.put("UP", new StringBuilder());
			
			if(this.type.equals("BINARY")) {
				bounds.get("LO")
					.append("BV BOUND")
					.append(" ")
					.append(this.getColumnId());				
			}
			else if(this.type.equals("INTEGER")) {
				if(!lower.equals("-Infinity"))
					bounds.get("LO")
						.append("LI BOUND")
						.append(" ")
						.append(this.getColumnId())
						.append(" ")
						.append(lower);
					
				if(!upper.equals("Infinity"))
					bounds.get("UP")
						.append("UI BOUND")
						.append(" ")
						.append(this.getColumnId())
						.append(" ")
						.append(upper);
			}
			else if(this.type.equals("CONTINUOUS")) {
				if(lower.equals("-Infinity"))
					bounds.get("LO")
						.append("MI BOUND")
						.append(" ")
						.append(this.getColumnId());
				else
					bounds.get("LO")
						.append("LO BOUND")
						.append(" ")
						.append(this.getColumnId())
						.append(" ")
						.append(lower);
					
				if(upper.equals("Infinity"))
					bounds.get("UP")
						.append("PL BOUND")
						.append(" ")
						.append(this.getColumnId());
				else
					bounds.get("UP")
						.append("UP BOUND")
						.append(" ")
						.append(this.getColumnId())
						.append(" ")
					.append(upper);
			}
			
			else
				throw new IllegalArgumentException("Illegal variable type " + this.type);
			
			this.bounds= bounds.values().stream()
				.filter(value -> value.length() > 0)
				.map(value -> value.toString())
				.collect(Collectors.toList());
			return this;
		}//setBounds
		
		Variable setBounds(Double lb, Double ub) {
			if(lb > ub)
				throw new IllegalArgumentException("Lower bound greater than upper bound for " + this.getColumnId());		
			return setBounds(lb.toString(), ub.toString());
		}
		
		Variable setBounds(IEEEDouble lb, IEEEDouble ub) {
			if(lb.doubleValue() > ub.doubleValue())
				throw new IllegalArgumentException("Lower bound greater than upper bound for " + this.getColumnId());		
			return setBounds(lb.toString(), ub.toString());
		}
		
		Variable setBounds(Integer lb, Integer ub) {
			if(lb > ub)
				throw new IllegalArgumentException("Lower bound greater than upper bound for " + this.getColumnId());
			String lower= lb.equals(Integer.MIN_VALUE) ? "-Infinity" : lb.toString();
			String upper= ub.equals(Integer.MAX_VALUE) ? "Infinity" : ub.toString();
			return setBounds(lower, upper);
		}

		@Override
		public String toString() {
			return this.bounds.toString();
		}
		
	}//class Variable

	/**
	 * This class is part of the API for the .mps "solver"
	 */
	public static class Constraint {
		
		String rowId;
		String sense;
		String RHS;
	
		Constraint(Object rowId) {
			super();
			this.rowId = rowId.toString();
		}

		String getRowId() {
			return rowId;
		}

		String getSense() {
			return sense.toString();
		}

		Constraint setSense(Object sense) {
			String sns= sense.toString();
			if(sns.equals("<=") || sns.equals("=<") || sns.equals("LE"))
				this.sense = "L";
			else if(sns.equals(">=") || sns.equals("=>") || sns.equals("GE"))
				this.sense = "G";
			else if(sns.equals("==") || sns.equals("=") || sns.equals("EQ"))
				this.sense = "E";
			else
				this.sense = "N";
			return this;
		}

		String getRHS() {
			return this.RHS;
		}

		Constraint setRHS(Object rHS) {
			RHS = rHS.toString();
			return this;
		}

		@Override
		public String toString() {
			return 	new StringBuilder(this.sense)
				.append(" ")
				.append(this.rowId)
				.toString();
		}
		
	}//class Constraint

	/**
	 * This class is part of the API for the .mps "solver"
	 */
	public static class Objective {
		
		String rowId;
		String sense;
		String constant;
	
		Objective(Object rowId) {
			super();
			this.rowId = rowId.toString();
		}

		String getRowId() {
			return rowId;
		}

		String getSense() {
			return sense;
		}

		Objective setSense(Object sense) {
			String sns= sense.toString();
			if(sns.equals("MIN") || sns.equals("Min") || sns.equals("Minimize") || sns.equals("MINIMIZE"))
				this.sense = "MIN";
			else if(sns.equals("MAX") || sns.equals("MAX") || sns.equals("Maximize") || sns.equals("MAXIMIZE"))
				this.sense = "MAX";
			else
				throw new IllegalArgumentException("Unrecognized objective sense " + sense.toString() + " in " + this.rowId);
			return this;
		}

		String getConstant() {
			return constant;
		}

		Objective setConstant(Object constant) {
			if(constant instanceof Integer)
				return setConstant((Integer) constant);
			if(constant instanceof Double)
				return setConstant((Double) constant);
			if(constant instanceof IEEEDouble)
				return setConstant((IEEEDouble) constant);
			return setConstant(constant.toString());				
		}
		
		Objective setConstant(String constant) {
			this.constant = new StringBuilder(this.rowId)
					.append(" ") 
					.append(constant)
					.toString();
			return this;
		}
		
		Objective setConstant(Double constant) {
			return setConstant(Double.valueOf(-constant).toString());	//note: negative because MPS 
																		//treats the constant as the 
																		//RHS of an objective row 
		}

		Objective setConstant(IEEEDouble constant) {
			return setConstant(new IEEEDouble(-constant.doubleValue())	//note: negative because MPS
				.toString());										//treats the constant as the 
																		//RHS of an objective row 
		}

		Objective setConstant(Integer constant) {
			return setConstant(Integer.valueOf(-constant).toString());	//note: negative because MPS 
																		//treats the constant as the 
																		//RHS of an objective row 
		}

		@Override
		public String toString() {
			return 	new StringBuilder("N")
				.append(" ")
				.append(this.rowId)
				.toString();
		}
	
	}//class Objective
	
	/**
	 * This class is part of the API for the .mps "solver"
	 */
	public static class Term {
		
		String type;
		String rowId;
		String columnId;
		String columnId2;
		String coefficient;
		
		public Term(String type) {
			super();
			this.type = type;
			this.rowId="";
			this.columnId = "";
			this.columnId2= "";
		}

		String getType() {
			return type;
		}

		String getRowId() {
			return rowId;
		}

		Term setRow(Object row) {
			this.rowId = row.toString();
			return this;
		}

		String getColumnId() {
			return columnId;
		}

		Term setColumnId(Object column) {
			this.columnId = column.toString();
			return this;
		}

		String getColumnId2() {
			if(!type.equals("QUADRATIC"))
				throw new IllegalArgumentException("Not valid for " + type);
			return columnId2;
		}

		Term setColumnId2(Object column2) {
			if(!type.equals("QUADRATIC"))
				throw new IllegalArgumentException("Not valid for " + type);
			this.columnId2 = column2.toString();
			return this;
		}

		String getCoefficient() {
			return coefficient;
		}

		Term setCoefficient(Object coefficient) {
			return setCoefficient(coefficient.toString());				
			
		}
		
		@Override
		public String toString() {
			return new StringBuilder(rowId)
				.append(" ")
				.append(coefficient)
				.toString();
		}
		
	}//class Term

	/**
	 * This class is part of the API for the .mps "solver"
	 */
	public static class Expression {
		
		List<String> contents;
	
		public Expression() {
			this.contents= new ArrayList<String>();
		}
		
		public Expression(String contents) {
			this();
			this.contents.add(contents);
		}
		
		public Expression(Expression expression) {
			this();
			this.contents.addAll(expression.contents);
		}

		public static Expression empty() {
			return new Expression();
		}

		List<String> get() {
			return contents;
		}

		public Expression append(String str) {
			contents.add(str);
			return this;
		}
		
		public Expression append(Expression expr) {
			contents.addAll(expr.contents);
			return this;
		}
		
		public static Expression multiply(String coefficient, Constraint constraint, Objective objective) {
			if(!(constraint==null ^ objective==null))
				throw new IllegalArgumentException("Only one of constraint or objective can be non-null");	
			return new Expression(new StringBuilder()
				.append(constraint!= null ? constraint.getRowId() : objective.getRowId())
				.append(" ")
				.append(coefficient)
				.toString());		
		}

		public Expression add(Expression expression) {
			this.contents.addAll(expression.contents);
			return this;
		}

		public Stream<String> stream() {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(twoAtATime(), 0), false);
		}

		public Iterator<String> twoAtATime(){
			return new Iterator<String>() {
			
				private Iterator<String> strings;
				StringBuilder pairs;
				String nextPair;
	
				{
					strings= Expression.this.contents.iterator();
					pairs= new StringBuilder();
					nextPair= "";
				}
	
				@Override
				public boolean hasNext() {
					return strings.hasNext();
				}
	
				@Override
				public String next() {
					if(!hasNext())
						throw new NoSuchElementException();
					pairs.append(strings.next());
					if(strings.hasNext())
						pairs.append(" ").append(strings.next());
					nextPair= pairs.toString();	
					pairs.delete(0, pairs.length());
					return nextPair;
				}
				
			}/*Iterator*/;//return
		}//twoAtATime
		
		public int size() {
			return contents.size();
		}

		public String toString() {
			return !contents.isEmpty() ? contents.toString() : "empty";
		}
	
		/**
		 * Creates a string representation of a column expression in .mps format.
		 * 
		 * @param modelingObject an element of the the modeling object span representing a variable modeling object (not a Variable object in this class).
		 * @return a string representation of a column expression
		 */
		public static String format(MsdxContainer<Object> modelingObject) {
			if(modelingObject.get("Variable")==null)
				throw new IllegalArgumentException("Modeling object does not contain a variable");
			String variableName= ((Variable)modelingObject.get("Variable")).getColumnId();

			if(modelingObject.get("Expression")==null)
				throw new IllegalArgumentException("Modeling object does not contain an expression");
			Expression expression= ((Expression)modelingObject.get("Expression"));
			
			StringBuilder result= new StringBuilder();
			Iterator<String> components= expression.twoAtATime();
			
			while(components.hasNext()) {
				result
					.append(variableName)
					.append(" ")
					.append(components.next());
				if(components.hasNext())
					result.append(System.lineSeparator());				
			}
			return result.toString();
		}//format
		
	}//class Expression
	

}//MsdxMpsTextModelingFactory
