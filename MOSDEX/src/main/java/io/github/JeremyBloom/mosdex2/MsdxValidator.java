/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.json.MsdxWriter;

/**
 * This class validates a MOSDEX JSON file against the MOSDEX JSON Schema. 
 * This class has not been fully implemented.
 * 
 * @author Dr. Jeremy Bloom (jeremyblmca@gmail.com)
 */
public class MsdxValidator {
	
	private JsonNode syntaxTree;
	private ProcessingReport report;	

	/**@param syntaxTree MOSDEX syntax as a Jackson Tree model*/
	public MsdxValidator(JsonNode syntaxTree) {
		super();
		this.syntaxTree= syntaxTree;
		this.report= new ListProcessingReport();
	}
	
	/**
	 * Checks a MOSDEX File object against the MOSDEX JSON Schema.
	 * @param mosdexTree
	 * @return true if the MOSDEX File conforms the the schema, false otherwise.
	 */
	public boolean validate(JsonNode mosdexTree) {
        this.report= JsonSchemaFactory.byDefault().getValidator().validateUnchecked(this.syntaxTree, mosdexTree);
        if(!report.isSuccess()) {
            for (ProcessingMessage processingMessage : report)
                System.err.println(getMessagesAsJson(processingMessage));
            throw new IOError(new Throwable("JSON MsdxSchema validation exception"));    
        }
		return report.isSuccess();
	}

	/**
	 * Writes a pretty JSON string representation of a node to an output stream.
	 * 
	 * @param node
	 * @param out
	 */
	public static void show(JsonNode node, OutputStream out) {
		MsdxWriter.Generator generator= MsdxWriter.Generator.create(MsdxOutputDestination.toStream(out));
		MsdxWriter writer= new MsdxWriter(generator);
		try {
			if(node.isValueNode()) {
				writer.valueToJson(node, null);
			}
			else if (node.isArray()) {
				writer.arrayToJson(node, null);
			}
			else if(node.isObject()) {
				writer.objectToJson(node);
			}
			else
				throw new IllegalArgumentException("Unsupported node type " + node.getNodeType().toString());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}//show
	
	public ProcessingReport getReport() {
		return this.report;
	}
	
	/**
	 * Provides a pretty JSON string for the processing messages.
	 * 
	 * @param messages
	 * @return a pretty JSON string
	 */
	public String getMessagesAsJson(ProcessingMessage messages) {	
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		JsonNode messageNode= messages.asJson();
		show(messageNode, out);
	    return out.toString();
	}//getMessageAsJson
	

}//class MsdxValidator
