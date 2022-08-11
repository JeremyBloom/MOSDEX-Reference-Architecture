/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import org.apache.spark.SparkConf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.github.JeremyBloom.mosdex2.DisplayWindow.ExtendedPrintStream;

/**
 * <a href="MsdxSparkDataframe.Factory"></a>Singleton class for global objects
 * in Msdx.
 * <p>
 * This class creates and manages a number of global services for MOSDEX,
 * objects that need to be accessible across a variety of classes. They are
 * generally, but not always, constants that do not change according to context.
 * The global objects include:
 * <ul>
 * <li>a Display Window to which supplementary output can be written using a
 * globally visible output stream;</li>
 * <li>the Jackson Object Mapper and its associated JSON Factory;</li>
 * <li>the Apache Spark Configuration (other Spark objects reside in
 * MsdxSparkDataframe.Factory).
 * </ul>
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class Msdx {
		
	/**
	 * The sole instance of class Msdx. All global objects are accessed through this instance.
	 */
	public static final Msdx GLOBAL= new Msdx();
	
	/**
	 * Jackson Object Mapper for serializing and deserializing Java objects to and from JSON.
	 * Predefined, constant attribute.
	 */
	public final ObjectMapper mapper;
	
	/**
	 * Configuration for Apache Spark applications using MOSDEX.
	 * Predefined, constant attribute.
	 */
	public final SparkConf sparkConfiguration;
	
	/**
	 * Display window for supplementary output.
	 * Predefined, constant attribute.
	 */
	public DisplayWindow window;
	
	/**
	 * Output stream to the display window. 
	 * Predefined, constant attribute.
	 */
	public ExtendedPrintStream out;
		
	/**
	 * Private constructor to create a new instance.
	 * Access through the static GLOBAL field. 
	 */
	private Msdx() {

		mapper= new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		sparkConfiguration = new SparkConf().setAppName("MOSDEX").setMaster("local[*]");
		//The following now live in MsdxSparkDataframe.Factory
/*		sparkContext= new JavaSparkContext(sparkConfiguration);
		sparkContext.setLogLevel("ERROR");
		session= SparkSession.builder()
				.config(sparkConfiguration)
				.getOrCreate();
*/		
		window= new DisplayWindow("");
		out= window.printStream();
	}
	
	/**
	 * Sets the display title.
	 * @param title
	 */
	public void setDisplayTitle(String title) {
		window.setTitle(title);
	}
	
	/**
	 * Shows the display window.
	 */
	public void showDisplay() {
		window.show();
	}
	
	
}//class Msdx
