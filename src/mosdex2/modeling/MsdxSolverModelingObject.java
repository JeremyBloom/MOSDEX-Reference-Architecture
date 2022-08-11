/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.modeling;

import java.io.Serializable;

import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;

/**
 * This class represents the common elements across all solver-specific modeling objects.
 * <p>
 * The fundamental concept of this class is that it provides 
 * a bridge between the MOSDEX representation of an optimization problem and the solver's 
 * internal objects. The bridge in turn consists of a sequence of Spans that process data
 * as it streams from the MOSDEX source to the solver and back again. 
 * <p>
 * This class hides the identities of the solver's API classes inside Containers, so 
 * that no solver-specific classes are exposed to MOSDEX.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxSolverModelingObject implements Serializable {

	private static final long serialVersionUID = 7013764326287752937L;

	/**The object's name.*/
	private String tableName;

	/**The object's class -- VARIABLE, CONSTRAINT, OBJECTIVE, or TERM.*/
	private String tableClass;

	/**The object's type (called KIND in MOSDEX) which differs according to class.*/
	private String tableType;
	
	/**
	 * The solver-specific modeling objects constructed in this instance. 
	 * The bridge consists of a sequence of Spans that transform the MOSDEX Object Model 
	 * into classes of the solver’s API. 
	 * <p>
	 * Bridge is a Span of Containers of solver-specific modeling objects.
	 */
	protected MsdxSpan bridge;

	/**
	 * Constructs a new solver modeling object (Variable, Constraint, or Objective).
	 * This class is used in the MsdxModel class, which constructs instances of 
	 * this class from the MOSDEX modeling object Tables.
	 * 
	 * @param tableName
	 * @param tableClass
	 * @param tableType
	 * @param bridge
	 */
	protected MsdxSolverModelingObject(
		String tableName, 
		String tableClass, 
		String tableType, 
		MsdxSpan bridge) 
	{
		super();
		this.tableName= tableName;
		this.tableClass= tableClass;
		this.tableType= tableType;
		this.bridge= bridge;		
	}//MsdxSolverModelingObject
	
	/**@return the MOSDEX Table name*/
	public String getTableName() {
		return this.tableName;
	}
	
	/**@return the MOSDEX Table class*/
	public String getTableClass() {
		return this.tableClass;
	}
	
	/**@return the MOSDEX Table type*/
	public String getTableType() {
		return this.tableType;
	}

	/**
	 * This method provides a means to access a bridge that connects a 
	 * MOSDEX modeling object with its presentation to a solver.
	 * 
	 * @return the bridge
	 */
	public MsdxSpan getBridge() {
		return this.bridge;
	}
	
	/**@return the schema of the Containers at the terminal end of the bridge*/
	public MsdxContainer<Class<?>> getSchema() {
		return this.getBridge().getSchema();
	}


}//class MsdxSolverModelingObject
