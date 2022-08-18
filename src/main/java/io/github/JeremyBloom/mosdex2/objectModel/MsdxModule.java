/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.JeremyBloom.mosdex2.json.MsdxReader;


/**
 * Represents a MOSDEX MODULE in Java. 
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * Copyright © 2022 Jeremy A. Bloom
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxModule extends MsdxObject {

    /**Holds the TABLES array of this MODULE.*/
    protected Map<String, MsdxTable> tables;
    
    /**
     * Constructs a new MODULE object. 
     * Ordinarily, a MODULE should be deserialized from JSON using methods of the 
     * MsdxObject.Factory.
     * 
     * @param thisNode Tree Model representation of this object
     * @param parent FILE object containing this MODULE
     * @param factory that created this object
     * @param tables of this MODULE
     */
    public MsdxModule(JsonNode thisNode, MsdxObject parent, Factory factory, Map<String, MsdxTable> tables) {
        super(thisNode, parent, factory);
        this.tables = tables;
    }
    
    protected MsdxModule() {
    	this(MsdxReader.createObjectNode(), null, null, new LinkedHashMap<String, MsdxTable>());
    }

	/**@return the NAME field of this MODULE*/
    public String getName() {
		return this.getFieldAsNode("NAME").asText();
	}

	/**@return the CLASS field of this MODULE*/
    public String getModuleClass() {
		return this.getFieldAsNode("CLASS").asText();
	}

	/**@return the type (KIND) field of this MODULE*/
	public String getType() {
		return this.getFieldAsNode("KIND").asText();
	}

	/**@return the TABLES array of this MODULE*/
	public Map<String, MsdxTable> getTables() {
		return this.tables;
	}
	
	/**@return the TABLE object of the given name if it is present among the TABLES of this MODULE, null otherwise*/
	public MsdxTable getTable(String name) {
		return this.tables.get(name);
	}
	

}//class MsdxModule
