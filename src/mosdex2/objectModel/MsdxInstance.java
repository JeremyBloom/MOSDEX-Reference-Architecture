/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.MsdxOutputDestination;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;

/**
 * Represents a MOSDEX Instance in Java. 
 * Potentially, an Instance can be a very large object; for that reason,
 * the actual data is held in a Dataframe, which can be implemented 
 * with specialized data structures and methods to support large data objects 
 * (for example an Apache Spark Dataset or a JDBC database table).
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * Copyright © 2022 Jeremy A. Bloom
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxInstance extends MsdxObject {
	
	/**The Schema of the parent Table.*/
	MsdxSchema tableSchema;
	
	/**Holds the actual data of this Instance.*/
	MsdxDataframe dataframe;

	/**
     * Constructs a new Instance object. 
     * Ordinarily, an Instance should be deserialized from JSON using methods of the 
     * MsdxObject.Factory or constructed from a Query.
     * 
     * @param thisNode Tree Model representation of this object (contains one representative Record)
     * @param parent Table object of this Instance
     * @param objectFactory that created this object
	 * @param dataframe
	 * @param tableSchema
	 */
	public MsdxInstance(
		JsonNode thisNode, 
		MsdxObject parent, 
		MsdxObject.Factory objectFactory,
		MsdxDataframe dataframe, 
		MsdxSchema tableSchema) 
	{
		super(thisNode, parent, objectFactory);
		this.dataframe = dataframe;
		this.tableSchema = tableSchema;
	}
	
	/**
     * Constructs a new Instance object. 
     * Ordinarily, an Instance should be deserialized from JSON using methods of the 
     * MsdxObject.Factory or constructed from a Query.
     * 
     * @param thisNode Tree Model representation of this object (contains one representative Record)
     * @param parent Table object of this Instance
     * @param objectFactory that created this object
	 * @param records data stream to create a Dataframe
	 * @param tableSchema
	 * @param dataframeFactory to construct the Dataframe
	 */
	public MsdxInstance(
		JsonNode thisNode, 
		MsdxObject parent, 
		MsdxObject.Factory objectFactory, 
		Stream<MsdxContainer<Object>> records, 
		MsdxSchema tableSchema, 
		MsdxDataframe.Factory dataframeFactory ) 
	{
		this(
			thisNode, 
			parent, 
			objectFactory, 
			dataframeFactory.create(((MsdxTable) parent).getName(), records, tableSchema.asContainer()),
			tableSchema);
	}//MsdxInstance
	
	protected MsdxInstance() {
		this(MsdxReader.createArrayNode(), null, null, null, null);
	}
	
	/**
	 * Replaces the Dataframe of this Instance with the argument and its Schema. 
	 * Replaces the node consistent with the new content.
	 * 
	 * @param dataframe
	 * @param tableSchema
	 * @return this instance
	 */
	public MsdxInstance replace(MsdxDataframe dataframe, MsdxSchema tableSchema) {
		this.dataframe= dataframe;
		if(this.getSchema()==null)
			this.tableSchema= tableSchema;

		ArrayNode representativeRecord= MsdxRecord.toNode(dataframe.first(), tableSchema.asContainer());
    	((ArrayNode) this.thisNode).remove(0);
		((ArrayNode) this.thisNode).add(representativeRecord);
		return this;
	}//replace
	
    /**
     * @return a JSON node representation of this instance;
     * because of the potentially large size of an instance, the node includes only 
     * one representative record; 
     * used for debugging and validation of the MOSDEX against its JSON schema.
     */
    @Override
	public JsonNode getAsNode() {
		return super.getAsNode();
	}

	/**@return the Dataframe of this Instance*/
	public MsdxDataframe getDataframe() {
		return this.dataframe;
	}
	
	/**@return the Dataframe of this Instance as a stream of records*/
	public Stream<MsdxContainer<Object>> asContainers() {
		return this.dataframe.toStream().map(record -> (MsdxContainer<Object>)record);		
	}

	/**@return the Schema of this Instance*/
	public MsdxSchema getSchema() {
		return tableSchema;
	}
	
	/**
	 * Writes an Instance as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * Primarily used for displaying the contents of the Instance.
	 * Note: the various write operations are adapted from the corresponding operations 
	 * in MsdxObject.Factory.
	 * 
	 * @param dst destination
	 * @param limit maximum number of records written (null for no limit)
	 */
	public void show(MsdxOutputDestination dst, Long limit) {
		
		//Dummy nodes for use by the writer
		ObjectNode instanceNode= MsdxReader.createObjectNode();
		ArrayNode recordsNode= instanceNode.putArray("INSTANCE");
		recordsNode.add(new String("Dummy").repeat(10));	//so that the instance is not written in-line
		
		MsdxObject.Factory objectFactory= new MsdxObject.Factory(null, Msdx.GLOBAL.mapper, false);
		MsdxWriter.Generator generator= MsdxWriter.Generator.create(dst);
		MsdxWriter writer= new MsdxWriter(generator) {

			@Override
			protected boolean specialHandling(JsonNode node, String keyword) throws IOException {
				if(keyword.equals("INSTANCE")) {
					objectFactory.writeRecords(
						MsdxInstance.this.asContainers() 
							.limit(limit!=null ? limit : Long.MAX_VALUE),
							MsdxInstance.this.getSchema().asContainer(), 
						generator);
					return true;
				}
				return false;
			}
    	}/*MsdxWriter*/;
	    	
		try {
			/*
			generator.writeStartObject(false);
	    	writer.objectToJson(instanceNode);
	    	generator.writeEndObject(false);	
	    	generator.linefeed();
			generator.flush();
			*/	    	
			generator.writeStartObject(false);
	    	writer.objectToJson(instanceNode);
	    	generator.writeEndObject(false);	
	    	generator.linefeed();
			generator.flush();	    	
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}				
	}//show
	
	/**
	 * Writes an Instance as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * 
	 * @param dst destination
	 */
	public void show(MsdxOutputDestination dst) {
		show(dst, null);
	}//show
	
	/**
	 * Writes an Instance as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * 
	 * @param out destination
	 * @param limit maximum number of records written (null for no limit)
	 */
	public void show(PrintStream out, Long limit) {
		show(MsdxOutputDestination.toStream(out), limit);
	}//show
	
	/**
	 * Writes an Instance as a JSON Object.
	 * Generally conforms to MOSDEX conventions.
	 * 
	 * @param out destination
	 */
	public void show(PrintStream out) {
		show(MsdxOutputDestination.toStream(out), null);
	}//show
	
	
}//class MsdxInstance
