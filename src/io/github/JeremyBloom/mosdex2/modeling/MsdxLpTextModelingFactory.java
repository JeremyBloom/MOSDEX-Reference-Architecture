/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.github.JeremyBloom.mosdex2.IEEEDouble;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe.Factory;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxRecord;
import io.github.JeremyBloom.mosdex2.span.OperatorWithOneArgument;
import io.github.JeremyBloom.mosdex2.span.OperatorWithTwoArguments;

/**
 * This class implements the methods used to create modeling objects 
 * to write an .lp text file representation of an optimization model.
 * Illustrates row-oriented model generation.
 * <p>
 * This class includes static member classes to represent .lp format modeling objects.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxLpTextModelingFactory implements MsdxSolverModelingFactory{
	
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
	 * Not used in .lp files. 
	 * However, function calls may appear in some MOSDEX tables, 
	 * when a MOSDEX File is used with other solvers,
	 */
	protected Map<String, Function<MsdxContainer<Object>, ?>> functionTable;

	/**
	 * Creates a new lp factory instance.
	 * @param dataframeFactory creates the Dataframes
	 */
	public MsdxLpTextModelingFactory(Factory dataframeFactory) {
		super();
		this.orientation= "Row";
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
	public OperatorWithOneArgument makeVariable(final String tableClass, final String tableType) 
	{
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
					.addItem("Variable", Variable.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Variable variable= new Variable((String) parameter.get("Column"), "");
				variable.setBounds(
					parameter.get("LowerBound").toString(), 
					parameter.get("UpperBound").toString());;
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Column")
					.addItem("Variable", variable)
					.build();		
			}//apply			
		}/*OperatorWithOneArgument*/;//return
	}//makeVariable

	@Override
	public OperatorWithOneArgument makeConstraint(
		final String tableClass, 
		final String tableType) 
	{
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
					.addItem("Constraint", Constraint.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Constraint constraint= new Constraint((String) parameter.get("Row"), "");
				constraint.setSenseAndRHS(
					(String) parameter.get("Sense"), 
					parameter.get("RHS").toString());
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Constraint", constraint)
					.addItem("Expression", new Expression())
					.build();		
			}//apply		
		}/*OperatorWithOneArgument*/;//return
	}//makeConstraint

	@Override
	public OperatorWithOneArgument makeObjective(
		final String tableClass, 
		final String tableType) 
	{
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
					.addItem("Objective", Objective.class)
					.addItem("Expression", Expression.class)
					.build();
				return this;
			}

			@Override
			public MsdxContainer<Object> apply(MsdxContainer<Object> parameter) {
				Objective objective= new Objective(
					(String) parameter.get("Row"),
					(String) parameter.get("Sense"), 
					(Double) parameter.get("Constant"));
	
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(parameter, "Name")
					.copyItem(parameter, "Row")
					.addItem("Objective", objective)
					.addItem("Expression", new Expression())
					.build();		
			}//apply	
		}/*OperatorWithOneArgument*/;//return
	}//makeObjective

	@Override
	public OperatorWithTwoArguments multiply(
		final String tableClass, 
		final String tableType) 
	{
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
					MsdxContainer<Class<?>> variablesSchema, 
					String variablesKeyFieldName) 
			{
			    if(this.resultSchema!=null)
			        return this;
				this.leftInputSchema= leftInputSchema;			//{Row, Column, nothing or (Variable and Column2), Coefficient}
				this.leftKeyFieldName= leftKeyFieldName;		//should be "Column"
				this.rightInputSchema= variablesSchema;			//{Column, Name, Variable}
				this.rightKeyFieldName= variablesKeyFieldName;	//should be "Column"
				
				this.resultSchema= MsdxContainer.<Class<?>>builder()
					.copyItems(leftInputSchema.merge(variablesSchema.delete(variablesKeyFieldName)))
					.removeItem("Name")
					.removeItem("Variable")
					.removeItem("Coefficient")
					.addItem("Expression", Expression.class)
					.build();
					// termSchema= {Row, Column, Column2 or nothing, Expression};
				return this;
			}//withResultSchema

			@Override
			protected MsdxContainer<Object> onKeyMatch(MsdxContainer<Object> left, MsdxContainer<Object> variable) {
				Expression product;
				if (tableType.equals("LINEAR"))
					product= Expression.multiply(
						left.get("Coefficient").toString(), 
						(Variable) variable.get("Variable"));
				else //tableType.equals("QUADRATIC")
					product= Expression.multiply(
						left.get("Coefficient").toString(),
						(Variable) left.get("Variable"),
						(Variable) variable.get("Variable"));	//Variable2
				
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
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(left.get(), "Row")
					.copyItem(left.get(), "Column")
					.copyItem(left.get(), "Column2")	//if present
					.addItem("Expression", Expression.empty())
					.build();
			}//noKeyMatch		
		}/*OperatorWithTwoArguments*/;//return	
	}//multiply
	
	@Override
	public OperatorWithTwoArguments add(
		final String tableClass, 
		final String tableType) 
	{
		
		if(!this.orientation().equals("Row"))
			throw new UnsupportedOperationException();	//must override this method for column orientation
		if(!tableClass.equals("TERM"))
			throw new IllegalArgumentException("Illegal table class");
		if(!(tableType.equals("LINEAR") || tableType.equals("QUADRATIC")))
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
				Expression sum= ((Expression) accumulation.get("Expression")).add((Expression) term.get("Expression"));
				
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
				Optional<MsdxContainer<Object>> right) 
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
				MsdxContainer<Class<?>> expressionsSchema,	//not used
				String expressionsKeyFieldName,
				MsdxContainer<Class<?>> allRowsSchema, 
				String allRowsKeyFieldName) 
			{
			    if(this.resultSchema!=null)
			        return this;
				
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
			public MsdxContainer<Object> apply(
				Optional<MsdxContainer<Object>> expressionContainer, 
				Optional<MsdxContainer<Object>> rowContainer) 
			{
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
				Expression sum= ((Expression) rowContainer.get("Expression")).add((Expression) expressionContainer.get("Expression"));
				
				return MsdxRecord.builder(this.getResultSchema())
					.copyItem(rowContainer, "Row")
					.copyItem(rowContainer, "Name")
					.copyItem(rowContainer, "Constraint")	//only one of these will be non-null
					.copyItem(rowContainer, "Objective")	//only one of these will be non-null
					.addItem("Expression", sum)
					.build();
			}//onKeyMatch

			/**
			 * No match can occur in two ways: First, if there is no expression matching a row; 
			 * that just means nothing gets added to the current row's expression.
			 * Second, if there is no row matching an expression; that occurs when a term is mis-specified with a 
			 * non-existent row id. In that case, it's a error that will be reported using the missing method. Also,
			 * the missing row is tagged with a NO_MATCH name so that it can be filtered out from the replacement rows.
			 */
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
		}/*OperatorWithTwoArguments*/;//return
	}//attach

	/**
	 * Writes the lp file.
	 * 
	 * @param model
	 * @param out
	 */
	@Override
	public void generate(MsdxModel model, PrintStream out) {
		
		if(out==null)
			throw new IllegalArgumentException("Missing output stream");
		
		model.getSolverObjects("OBJECTIVE")
			.flatMap(modelingObject -> modelingObject.getBridge().apply())
			.forEach(item -> out.println(Objective.format(item)));			
		
		if(!model.getNames("CONSTRAINT").isEmpty()) {
			out.println("Subject To");		
			model.getSolverObjects("CONSTRAINT")
				.flatMap(modelingObject -> modelingObject.getBridge().apply())
				.forEach(item -> out.println(Constraint.format(item)));			
		}
		
		out.println("Bounds");
		model.getSolverObjects("VARIABLE")
			.flatMap(modelingObject -> modelingObject.getBridge().apply())
			.forEach(item -> out.println(((Variable)item.get("Variable")).getBounds()));
		
		if(!model.getNames("VARIABLE", "INTEGER").isEmpty()) {
			out.println("General");
			model.getSolverObjects("VARIABLE", "INTEGER")
				.flatMap(modelingObject -> modelingObject.getBridge().apply())
				.forEach(item -> out.print((String)item.get("Column") + " "));					
			out.println();
		}
		
		if(!model.getNames("VARIABLE", "BINARY").isEmpty()) {
			out.println("Binary");
			model.getSolverObjects("VARIABLE", "BINARY")
				.flatMap(modelingObject -> modelingObject.getBridge().apply())
				.forEach(item -> out.print((String)item.get("Column") + " "));					
			out.println();
		}
		
		out.println("End");		
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
	 * This class is part of the API for the .lp "solver".
	 */
	public static class Variable {
		
		Object columnId;
		String bounds;
	
		Variable(Object columnId, String bounds) {
			super();
			this.columnId = columnId;
			this.bounds = bounds;
		}

		Object getColumnId() {
			return columnId;
		}

		void setColumnId(Object name) {
			this.columnId = name;
		}

		String getBounds() {
			return bounds;
		}

		void setBounds(String lb, String ub) {
			StringBuffer bnds= new StringBuffer();
			bnds.append(lb);
			bnds.append(" <= ");
			bnds.append(this.getColumnId());
			bnds.append(" <= ");
			bnds.append(ub); 
			this.bounds = bnds.toString();
		}

		@Override
		public String toString() {
			return bounds;
		}

	}//class Variable

	/**
	 * This class is part of the API for the .lp "solver"
	 */
	public static class Constraint {
		
		Object rowId;
		String senseAndRHS;
	
		Constraint(Object rowId, String senseAndRHS) {
			super();
			this.rowId = rowId;
			this.senseAndRHS = senseAndRHS;
		}

		Object getRowId() {
			return rowId;
		}

		void setRowId(Object rowId) {
			this.rowId = rowId;
		}

		String getSenseAndRHS() {
			return senseAndRHS;
		}

		void setSenseAndRHS(String sense, String RHS) {
			StringBuffer sns = new StringBuffer();
			sns.append(sense);
			sns.append(" ");
			sns.append(RHS);
			this.senseAndRHS = sns.toString();		
		}

		@Override
		public String toString() {
			return senseAndRHS;
		}
	
		/**
		 * Creates a string representation of a constraint in .lp format.
		 * 
		 * @param item an element of the the modeling object span representing a constraint
		 * @return a string
		 */
		public static String format(MsdxContainer<Object> item) {
			return new StringBuilder()
				.append(item.get("Row")).append(": ")
				.append(trim(((Expression) item.get("Expression")).get()))
				.append(' ')
				.append(((Constraint)item.get("Constraint")).getSenseAndRHS())
				.toString();
		}
		
	}//class Constraint

	/**
	 * This class is part of the API for the .lp "solver"
	 */
	public static class Objective {
		
		Object rowId;
		String sense;
		String constant;
	
		Objective(Object rowId, String sense, Double constant) {
			super();
			this.rowId = rowId;
			this.sense = sense;
			this.setConstant(constant);
		}

		Object getRowId() {
			return rowId;
		}

		void setRowId(Object rowId) {
			this.rowId = rowId;
		}

		String getSense() {
			return sense;
		}

		void setSense(String sense) {
			this.sense = sense;		
		}

		String getConstant() {
			return constant;
		}

		void setConstant(Double constant) {
			this.constant = (Math.abs(constant) < 2.0 * Double.MIN_VALUE) ? 
				"" :								//constant == 0
				constant.toString();				//constant != 0
			if(constant > 2.0 * Double.MIN_VALUE)	//constant > 0
				this.constant= '+' + this.constant;
		}

		@Override
		public String toString() {
			StringBuffer sns = new StringBuffer();
			sns.append(sense);
			sns.append(" ");
			sns.append(constant);
			return sns.toString();
		}
		
		/**
		 * Creates a string representation of an objective in .lp format.
		 * 
		 * @param item an element of the modeling object span representing an objective
		 * @return a string
		 */
		public static String format(MsdxContainer<Object> item) {
			return new StringBuilder()
				.append(((Objective)item.get("Objective")).getSense())
				.append(System.lineSeparator())
				.append(item.get("Row")).append(": ")
				.append(trim(((Expression) item.get("Expression")).get()))
				.append(' ')
				.append(((Objective)item.get("Objective")).getConstant())
				.toString();
			}
	
	}//class Objective
	
	/**
	 * This class is part of the API for the .lp "solver"
	 */
	public static class Term {
		
		String type;
		String rowId;
		String columnId;
		String columnId1;
		String columnId2;
		String coefficient;
		
		Term(String type, String row, String column, String coefficient) {
			super();
			if(!type.equals("LINEAR"))
				throw new IllegalArgumentException("Not a linear term");
			this.type = type;
			this.rowId = row;
			this.columnId = column;
			this.coefficient = coefficient;
			this.columnId1= "";
			this.columnId2= "";
		} 
		
		Term(String type, String row, String column1, String column2, String coefficient) {
			super();
			if(!type.equals("QUADRATIC"))
				throw new IllegalArgumentException("Not a quadratic term");
			this.type = type;
			this.rowId = row;
			this.columnId1= column1;
			this.columnId2= column2;
			this.coefficient = coefficient;
			this.columnId = "";
		}

		String getRowId() {
			return rowId;
		}

		void setRowId(String row) {
			this.rowId = row;
		}

		String getColumnId() {
			return columnId;
		}

		void setColumnId(String column) {
			this.columnId = column;
		}

		String getColumnId1() {
			return columnId1;
		}

		void setColumnId1(String column1) {
			this.columnId1 = column1;
		}

		String getColumnId2() {
			return columnId2;
		}

		void setColumnId2(String column2) {
			this.columnId2 = column2;
		}

		String getCoefficient() {
			return coefficient;
		}

		void setCoefficient(String coefficient) {
			this.coefficient = coefficient;
		}

		void setCoefficient(Integer coefficient) {
			this.coefficient = coefficient.toString();
		}

		void setCoefficient(Double coefficient) {
			this.coefficient = coefficient.toString();
		}

		void setCoefficient(IEEEDouble coefficient) {
			this.coefficient = coefficient.toString();
		}

		String getType() {
			return type;
		} 
		
		
		
		
	}//class Term

	/**
	 * This class is part of the API for the .lp "solver"
	 */
	public static class Expression {
		
		StringBuilder contents;
	
		public Expression(StringBuilder contents) {
			this.contents= contents;
		}

		public Expression() {
			this(new StringBuilder());
		}

		public Expression(Expression expression) {
			this(expression.get());
		}

		public Expression(String contents) {
			this(new StringBuilder(contents));
		}
		
		public static Expression empty() {
			return new Expression();
		}

		public Expression append(String str) {
			return new Expression(contents.append(str));
		}
		
		public Expression append(Expression expr) {
			return new Expression(contents.append(expr.get()));
		}
		
		public Expression append(StringBuilder str) {
			return new Expression(contents.append(str));
		}
		
		public static Expression multiply(Object coefficient, Variable variable) {
			StringBuilder product= new StringBuilder();
			return new Expression(product
				.append(coefficient.toString())
				.append('*')
				.append(variable.getColumnId()));	
		}

		public static Expression multiply(Object coefficient, Variable variable, Variable variable2) {
			StringBuilder product= new StringBuilder();
			if(variable.getColumnId().equals(variable2.getColumnId())) //it's a square
				return new Expression(product
				.append(coefficient.toString())
				.append("*(")
				.append(variable.getColumnId())
				.append(")^2"));
			//else it's a product
			return new Expression(product
				.append(coefficient.toString())
				.append('*')
				.append(variable.getColumnId())
				.append('*')
				.append(variable2.getColumnId()));	
		}

		public Expression add(Expression terms) {
			return new Expression(this
				.append((this.length()==0 || terms.length()==0 || terms.get().charAt(0)=='-') ? "" : " + ")
				.append(terms.get()));
		}

		public int length() {
			return contents.length();
		}

		StringBuilder get() {
			return contents;
		}

		void set(StringBuilder contents) {
			this.contents = contents;
		}

		public String toString() {
			return contents.length()>0 ? contents.toString() : "empty";
		}
	
	}//class Expression
	

}//class MsdxLpTextModelingFactory
