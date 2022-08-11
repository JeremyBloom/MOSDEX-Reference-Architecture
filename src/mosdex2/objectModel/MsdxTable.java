/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;

/**
 * Represents a MOSDEX TABLE in Java. 
 * Note, TABLE includes the subclasses DATA, VARIABLE, CONSSTRAINT, OBJECTIVE, and TERM, 
 * but these have no separate Java representations.
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * Copyright © 2022 Jeremy A. Bloom
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxTable extends MsdxObject {

    /**The SCHEMA field of this TABLE.*/
    protected MsdxSchema schema;

    /**The INSTANCE field of this TABLE.*/
    protected MsdxInstance instance;

    /**
     * The QUERY or INITIALIZE field of this TABLE; 
     * only one such field can be present in MOSDEX.
     */
    protected MsdxQuery query;

    /**The APPEND query field of this TABLE (not yet implemented).*/
    protected MsdxQuery append;

    /**The REVISE query field of this TABLE (not yet implemented).*/
    protected MsdxQuery revise;

    /**
     * Constructs a new TABLE object. 
     * Ordinarily, a TABLE should be deserialized from JSON using methods of the 
     * MsdxObject.Factory.
     * 
     * @param node Tree Model representation of this object
     * @param parent MODULE object containing this TABLE
     * @param factory that created this object
     * @param schema
     * @param instance
     * @param query
     * @param append
     * @param revise
     */
    public MsdxTable(
		JsonNode node, 
		MsdxObject parent, 
		Factory factory, 
		MsdxSchema schema, 
		MsdxInstance instance, 
		MsdxQuery query, 
		MsdxQuery append, 
		MsdxQuery revise) 
    {
        super(node, parent, factory);
        this.schema = schema;
        this.instance = instance;
        this.query = query;
        this.append = append;
        this.revise = revise;
    }//MsdxTable
    
    protected MsdxTable() {
    	this(MsdxReader.createObjectNode(), null, null, null, null, null, null, null);
    }
    
    /**
     * This method is used when revising an Instance to retrieve solution items from a modeling object.
     * It replaces the Table's Schema and Instance with the Dataframe argument.
	 * 
     * @param dataframe
     * @return this Table
     * @throws IllegalStateException if the Table has both an Instance and a Query
     */
    public MsdxTable replaceInstance(MsdxDataframe dataframe) {
    	if(!this.hasField("INSTANCE")) 
    		throw new IllegalStateException("Table " + this.getName() + " does not have an instance"); 	
    	if(this.hasField("QUERY") || this.hasField("INITIALIZE")) 
    		throw new IllegalStateException("Table " + this.getName() + " cannot have an instance and a query simultaneously"); 	
    	this.schema.replace(dataframe.getSchema());
    	this.instance.replace(dataframe, this.schema);
    	
    	((ObjectNode) this.getAsNode()).replace("INSTANCE", this.instance.getAsNode());  	
    	((ObjectNode) this.getAsNode()).replace("SCHEMA", schema.getAsNode());  	
    	return this;
    }//replaceInstance
    
    /**
     * Creates an Instance from the Table's Query or Initialize field. 
     * Removes the Query or Initialize field, since a Table cannot have both an Instance and a Query.
     * If the Table does not have a Schema, one is created from the Query;
     * if the Table already has a Schema, it must be the same as the one generated from the Query.
     * 
     * @return this table
     * @throws IllegalStateException if this Table has no Query or if it already has an Instance
     * @throws IllegalArgumentException if the Table's Schema does not match the Query's
     */
    public MsdxTable createInstance() {
    	if(!(this.hasField("QUERY") || this.hasField("INITIALIZE"))) 
    		throw new IllegalStateException("Table has no query"); 	
    	if(this.hasField("INSTANCE")) 
    		throw new IllegalStateException("Cannot have an instance and a query simultaneously");
    	MsdxContainer<Class<?>> querySchema= this.query.getSchema();
    	if(this.hasField("SCHEMA")) {
        	Set<Map.Entry<String, Class<?>>> mismatchedFields= querySchema.mismatchedItems(this.getSchema().asContainer());
        	if(!mismatchedFields.isEmpty())
        		throw new IllegalArgumentException("Mismatched fields " + mismatchedFields.toString());
    	}
    	else {
    		this.schema= new MsdxSchema(MsdxSchema.toNode(querySchema), this, factory, querySchema);
	    	((ObjectNode) this.getAsNode()).set("SCHEMA", this.schema.getAsNode());
    	}

    	MsdxDataframe dataframe= this.getFactory().getDataframeFactory().create(this.getName(), this.query, this.getSchema().asContainer());
    	this.instance= new MsdxInstance(MsdxReader.createArrayNode(), this, factory, dataframe, this.getSchema());
    	this.instance.replace(dataframe, this.getSchema());
    	
    	((ObjectNode) this.getAsNode()).set("INSTANCE", this.instance.getAsNode());
    	((ObjectNode) this.getAsNode()).remove("QUERY");
    	((ObjectNode) this.getAsNode()).remove("INITIALIZE");
    	
    	return this;
    }//createInstance
    
	/**@return the NAME field of this TABLE*/
    public String getName() {
		return this.getFieldAsNode("NAME").asText();
	}

	/**@return the CLASS field of this TABLE*/
	public String getTableClass() {
		return this.getFieldAsNode("CLASS").asText();
	}

	/**@return the type (KIND) field of this TABLE*/
	public String getTableType() {
		return this.getFieldAsNode("KIND").asText();
	}

	/**@return this Table's Schema object*/
	public MsdxSchema getSchema() {
		return this.schema;
	}

	/**@return this Table's Instance object*/
	public MsdxInstance getInstance() {
		return this.instance;
	}
	
	/**@return this Table's Query object*/
	public MsdxQuery getQuery() {
		return this.query;
	}

	/**@return this Table's Append query object*/
	public MsdxQuery getAppendQuery() {
		return append;
	}
	
	/**@return this Table's Revise query object*/
	public MsdxQuery getReviseQuery() {
		return revise;
	}

	
}//class MsdxTable
