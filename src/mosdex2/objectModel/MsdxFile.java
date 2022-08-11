/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.JeremyBloom.mosdex2.MsdxInputSource;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;

/**
 * Represents a MOSDEX FILE in Java. 
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * Copyright © 2022 Jeremy A. Bloom
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxFile extends MsdxObject {
	
    /**Holds the MODULES array of this FILE*/
    protected Map<String, MsdxModule> modules;
    
    /**
     * Constructs a new FILE object. 
     * Ordinarily, a FILE should be deserialized from JSON using methods of the 
     * MsdxObject.Factory.
     * 
     * @param thisNode Tree Model representation of this object
     * @param parent normally null
     * @param factory that created this object
     * @param modules of this FILE
     */
    public MsdxFile(JsonNode thisNode, MsdxObject parent, MsdxObject.Factory factory, /*MsdxModules*/ Map<String, MsdxModule> modules) {
        super(thisNode, parent, factory);
        this.modules = modules;
    }
    
	protected MsdxFile() {
		this(MsdxReader.createObjectNode(), null, null, new LinkedHashMap<String, MsdxModule>());
	}
	
	/**
	 * Calls the Factory readFile method.
	 * 
	 * @param src
	 * @param factory 
	 * @return a new MOSDEX File object
	 */
	public static MsdxFile read(MsdxInputSource src, MsdxObject.Factory factory) {
		return factory.readFile(src);
	}
	
	/**Calls the Factory writeFile method.*/
	public void write(MsdxOutputDestination dst) {
		this.getFactory().writeFile(this, dst);
	}

	/**@return the SYNTAX field of this FILE*/
	public URI getSyntax() {
		URI syntax= null;
		if(!this.hasField("SYNTAX") || this.getFieldAsNode("SYNTAX").asText().isEmpty())
			return null;
		try {
			syntax= new URI(this.getFieldAsNode("SYNTAX").asText(""));
		} catch (URISyntaxException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return syntax;
	}

	/**@return the MODULES array of this FILE*/
	public Map<String, MsdxModule> getModules() {
		return modules;
	}
	
	/**@return the MODULE object of the given name of this FILE or null if there is none*/
	public MsdxModule getModule(String name) {
		return modules.get(name);
	}
	
	/**
	 * @return the TABLE objects in all MODULES of this FILE
	 * @throws IllegalArgumentException if there is any duplicate table name 
	 */
	public Map<String, MsdxTable> getTables() {
		return this.getModules().values().stream()
			.flatMap(module -> module.getTables().entrySet().stream())
			.collect(Collectors.toMap(
				table -> table.getKey(), 
				table -> table.getValue(), 
				(name1, name2) -> {throw new IllegalArgumentException("Duplicate table name " + name1);},
				LinkedHashMap<String, MsdxTable>::new));	
	}
	
	/**@return returns the TABLE object of the given name if it is present among the MODULES of this FILE, null otherwise*/
	public MsdxTable getTable(String name) {
		MsdxTable table= null;
		while(table==null) {
			for(Iterator<Entry<String, MsdxModule>> module= modules.entrySet().iterator(); module.hasNext();) {
				table= module.next().getValue().getTable(name);
				if(table!=null) break;
			}
		}
		return table;
	}
	

}//class MsdxFile
