/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxDataframe;
import io.github.JeremyBloom.mosdex2.dataframe.MsdxSparkDataframe;
import io.github.JeremyBloom.mosdex2.modeling.MsdxCplexModelingFactory;
import io.github.JeremyBloom.mosdex2.modeling.MsdxLpTextModelingFactory;
import io.github.JeremyBloom.mosdex2.modeling.MsdxModel;
import io.github.JeremyBloom.mosdex2.modeling.MsdxMpsTextModelingFactory;
import io.github.JeremyBloom.mosdex2.modeling.MsdxSolverModelingFactory;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxFile;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxObject;
import io.github.JeremyBloom.mosdex2.objectModel.MsdxTable;
import io.github.JeremyBloom.mosdex2.span.MsdxJavaSpan;
import io.github.JeremyBloom.mosdex2.span.MsdxSpan;

/**
 * This class provides a fully configured optimization application using MOSDEX.
 * This application allows Modules to be spread over multiple MOSDEX Files, but
 * uses a single solver instance to solve them as a single model; typically, the
 * modeling objects would be included in a single Module (this is the
 * recommended practice, as future releases may enforce the requirement that
 * each model module has its own solver instance, in order to support
 * decomposition algorithms). Other modules can serve as data inputs, or
 * possibly as outputs. It does not support decomposition or multiple models.
 * <p>
 * This class is configured with builder-type methods named useXx, where Xx
 * signifies the particular factories to be used for the dataframes, spans, and
 * solver. The MOSDEX content files are specified using the addFile methods. The
 * addSolverResults method enables reporting the native solver output. The show
 * method enables specifying a subset of the modules and tables to report,
 * useful when then input tables are very large.
 * <p>
 * This class has a static factory called example that provides a limited set of
 * configuration options useful for demonstration purposes. It takes its input
 * from a single MOSDEX file and uses the IBM CPLEX solver, Apache Spark
 * dataframes, and Java spans. An example instance does not permit selection of
 * the output tables; all modules and their tables are reported.
 * </p>
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxApplication implements Runnable {
	
	/**
	 * The title of the application. 
	 * The first word should be the short name; the remainder documents the specific use case.
	 */
	protected String title;
	
	/**
	 * Maps pairs of sources and destinations for MOSDEX JSON input and output.
	 * The the output corresponding to the source will be written to the destination. 
	 * If a source is unpaired, use null for the destination.
	 * Note: mosdexOutputs.keySet() is the set of sources.
	 */
	protected Map<MsdxInputSource, MsdxOutputDestination> mosdexOutputs;
	
	/**
	 * You can designate a set of modules and tables to include in the output.
	 * The map has module names as keys and a set of tables as the value for each module. 
	 * If the set is empty, all tables in the module will be included.
	 * If the objectsToShow map is empty, all modules and their tables will be included. 
	 */
	protected Map<String, Set<String>> objectsToShow;
	
	/**
	 * The destination for the native output of the solver.
	 * Null does not report the solver output.
	 */
	protected MsdxOutputDestination solverResults;
	
	/**True if solver results is to include a .lp or .mps representation of the model.*/
	protected boolean includeGeneratedModel;
	
	/**
	 * The status returned by the solver (e.g. Infeasible, Optimal, etc.).
	 * Typically this will be a string representing the solver's native status indicator.
	 */
	protected String solveStatus;
	
	/**JSON strings representing the MOSDEX JSON output expected at each destination (null if not used).*/
	protected Map<MsdxOutputDestination, String> expectedOutputs;
	
	/**The MOSDEX object factory implementation.*/
	protected MsdxObject.Factory objectFactory;
	
	/**The dataframe factory implementation.*/
	protected MsdxDataframe.Factory dataframeFactory;
	
	/**The span factory implementation.*/
	protected MsdxSpan.Factory spans;
	
	/**The solver modeling factory implementation.*/
	protected MsdxSolverModelingFactory solver;
	
	/**The collector holds all the tables from all the Modules in all the Files.*/
	Map<String, MsdxTable> collector;
	
	/**
	 * Constructs a new application instance.
	 * 
	 * @param title for display
	 */
	public MsdxApplication(String title) 
	{
		super();
		this.title = title;
		this.mosdexOutputs = new IdentityHashMap<MsdxInputSource, MsdxOutputDestination>();
		this.objectsToShow = new LinkedHashMap<String, Set<String>>();
		this.solveStatus = "Unknown";
		this.solverResults = null;
		this.includeGeneratedModel = false;
		this.expectedOutputs = new IdentityHashMap<MsdxOutputDestination, String>();
		this.spans = null;
		this.solver = null;
		this.dataframeFactory= null;
		this.objectFactory= null;	//set in the use...Dataframes configuration method, since it depends on the dataframe factory
		this.collector= new LinkedHashMap<String, MsdxTable>();	//collects all the tables in the application, used by MsdxModel
	}
	
	/**
	 * Adds a source and a destination pair for MOSDEX Files.
	 * Solver outputs from the source will be written to the destination.
	 * If the destination is null, only the source File will be used.
	 * You can specify an expected output string to check against the actual output; 
	 * if there is not a match, the run method will throw an exception. 
	 * 
	 * @param src input source
	 * @param dst output destination (null if not used)
	 * @param expectedOutput JSON string representing the MOSDEX JSON output expected at this destination (null if not used)
	 * @return this application instance
	 * @throws IllegalArgumentException if expectedOutput is not null and output destination is not a PrintStream
	 */
	public MsdxApplication addFile(
		MsdxInputSource src, 
		MsdxOutputDestination dst, 
		String expectedOutput) 
	{
		if(this.mosdexOutputs.containsKey(src))
			throw new IllegalArgumentException("Duplicate sources");
		this.mosdexOutputs.put(src, dst);
		
		if(expectedOutput==null)
			return this;
		
		if(this.expectedOutputs.containsKey(dst))
			throw new IllegalArgumentException("Expected output has already been defined");
		if (!(dst.getStream() instanceof PrintStream))
			throw new IllegalArgumentException("Destination must be a PrintStream");
		this.expectedOutputs.put(dst, expectedOutput);
		
		return this;
	}
	
	/**Adds a source and a destination pair for MOSDEX Files, without an expected output.*/
	public MsdxApplication addFile(MsdxInputSource src, MsdxOutputDestination dst) {
		return addFile(src, dst, null);
	}

	/**Adds a source without a destination.*/
	public MsdxApplication addFile(MsdxInputSource src) {
		return addFile(src, null, null);
	}
	
	/**
	 * Specifies modules and tables to include in the output.
	 * This feature may be helpful when dealing with large MOSDEX files. 
	 * Assumes all modules and tables have unique names.
	 * If no modules are specified, all will be included.
	 * If no tables are specified for a module, all will be included.
	 * 
	 * @param module name
	 * @param tables names for this module
	 * @return this application instance
	 */
	public MsdxApplication show(String module, String... tables) {
		if(objectsToShow.containsKey(module))
			throw new IllegalArgumentException("Duplicate names " + module);
		objectsToShow.put(module, Set.of(tables));
		return this;
	}
	
	/**
	 * Sets a destination for the native output of the solver.
	 * 
	 * @param solverResults the solverResults destination to add
	 * @param includeGeneratedModel true if the solver factory produces a .lp or .mps representation of the model 
	 * to be included in the results
	 * @return this application instance
	 */
	public MsdxApplication addSolverResults(MsdxOutputDestination solverResults, boolean includeGeneratedModel) {
		if(this.solverResults!=null)
			throw new IllegalArgumentException("Solver results destination has already been defined");
		if(!(solverResults.getStream() instanceof PrintStream))
			throw new IllegalArgumentException("Solver results must be a PrintStream");
		this.solverResults = solverResults;
		this.includeGeneratedModel= includeGeneratedModel;
		return this;
	}

	/**
	 * Specifies using Apache Spark dataframes.
	 * At present, this is the only option for the dataframe factory; 
	 * however, future releases may allow other dataframes (e.g. a database). 
	 * 
	 * @return this application instance
	 */
	public MsdxApplication useSparkDataframes() {
		if(this.dataframeFactory!=null)
			throw new IllegalArgumentException("Dataframe factory has already been defined");
		this.dataframeFactory= new MsdxSparkDataframe.Factory(Msdx.GLOBAL.sparkConfiguration);
		this.objectFactory= new MsdxObject.Factory(this.dataframeFactory, Msdx.GLOBAL.mapper, false);
		return this;
	}
	
	/**
	 * Specifies using Java spans.
	 * At present, this is the only option for the span factory; 
	 * however, future releases may allow other spans (e.g. Apache Spark distributed datasets). 
	 * 
	 * @return this application instance
	 */
	public MsdxApplication useJavaSpans() {
		if(this.spans!=null)
			throw new IllegalArgumentException("Span factory has already been defined");
		this.spans= new MsdxJavaSpan.Factory();
		return this;
	}

	/**
	 * Specifies use of the IBM CPLEX solver modeling factory.
	 * Specify the dataframe factory before this call. 
	 * At present, this is the only option for the solver; 
	 * however, future releases may allow other spans (e.g. OSI).
	 * Note, configuring the application for CPLEX precludes using any other 
	 * modeling factory (e.g. LP or MPS) 
	 * 
	 * @return this application instance
	 */
	public MsdxApplication useCplex() {
		if(this.dataframeFactory==null)
			throw new IllegalArgumentException("Dataframe factory has not been defined");
		if(this.solver!=null)
			throw new IllegalArgumentException("Solver has already been defined");
		try {
			this.solver= new MsdxCplexModelingFactory(new IloCplex(), dataframeFactory);
		} catch (IloException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Specifies use of the LP solver modeling factory.
	 * Specify the dataframe factory before this call.
	 * This factory does not solve an optimization model; it merely creates 
	 * a .lp representation of the model. 
	 * Note, configuring the application for LP precludes using any other 
	 * modeling factory (e.g. CPLEX or MPS) 
	 * 
	 * @return this application instance
	 */
	public MsdxApplication useLp() {
		if(this.solver!=null)
			throw new IllegalArgumentException("Solver has already been defined");
		this.solver= new MsdxLpTextModelingFactory(dataframeFactory);
		return this;
	}

	/**
	 * Specifies use of the MPS solver modeling factory.
	 * Specify the dataframe factory before this call.
	 * This factory does not solve an optimization model; it merely creates 
	 * a .mps representation of the model. 
	 * Note, configuring the application for MPS precludes using any other 
	 * modeling factory (e.g. CPLEX or LP) 
	 * 
	 * @return this application instance
	 */
	public MsdxApplication useMps() {
		if(this.solver!=null)
			throw new IllegalArgumentException("Solver has already been defined");
		this.solver= new MsdxMpsTextModelingFactory(dataframeFactory);
		return this;
	}
	
	/**
	 * Executes the application.
	 * If expected outputs are defined for the destinations, checks that they match the actual outputs. 
	 * <p>
	 * Throws JSONException if there is a parsing error in the checker (failing the checker test does not generate an exception)
	 */
	@Override
	public void run() {
		
		//Check that all required fields have been set
		if(this.mosdexOutputs.keySet().isEmpty())
			throw new IllegalArgumentException("MsdxApplication: No input source defined");
		if(this.mosdexOutputs.values().stream().allMatch(value -> value==null))
			throw new IllegalArgumentException("MsdxApplication: No output destination defined");
		if(this.objectFactory==null)
			throw new IllegalArgumentException("MsdxApplication: Object Factory is not defined");
		if(this.dataframeFactory==null)
			throw new IllegalArgumentException("MsdxApplication: Dataframe Factory is not defined");
		if(this.spans==null)
			throw new IllegalArgumentException("MsdxApplication: Span Factory is not defined");
		if(this.solver==null)
			throw new IllegalArgumentException("MsdxApplication: Solver Modeling Factory is not defined");
		
		Map<MsdxInputSource, MsdxFile> mosdexFiles= new IdentityHashMap<MsdxInputSource, MsdxFile>();
		MsdxFile msdx;
		for(MsdxInputSource src: this.mosdexOutputs.keySet()) {
			msdx= objectFactory.readFile(src);
			mosdexFiles.put(src, msdx);
		}
		
		//Populate the collector with all tables from all modules
		this.collector= mosdexFiles.values().stream()
			.flatMap(mosdex -> mosdex.getModules().values().stream())
			.flatMap(module -> module.getTables().values().stream())
			.collect(Collectors.toMap(
				table -> table.getName(), 
				table -> table, 
				(name1, name2) -> {throw new IllegalArgumentException("Duplicate table names " + name1);}, 
				LinkedHashMap<String, MsdxTable>::new));
		
		//Create the model and all solver-specific objects
		MsdxModel model= new MsdxModel(this.title, solver, spans, this.dataframeFactory);
		model.createModelingObjects(this.collector);
		
		this.solver.withName(model.getModelName());	
		if(includeGeneratedModel)
			this.solver.generate(model, solverResults);	
		
		//Solve the model
		this.solveStatus= solver.solve(model, solverResults);
		
		if(solveStatus.equalsIgnoreCase("Not supported"))
			return;
		if(solveStatus.equalsIgnoreCase("Failure"))
			System.err.println("MOSDEX application " + this.title + " solve status= " + solveStatus);
		
		//Recover the solution from the solver
		model.createSolutionObjects(collector);
		
		//Check the actual output against the expected output
		Checker checker= new Checker();
		
		MsdxFile file;
		MsdxOutputDestination dst;
		MsdxOutputDestination out= null;
		for(MsdxInputSource src: this.mosdexOutputs.keySet()) {
			file= mosdexFiles.get(src);
			dst= mosdexOutputs.get(src);
			if(dst!=null) {
				out= checker.getRedirect(dst);
				objectFactory.writeFile(
					file, 
					objectsToShow,	//modules (and their tables) to show
					out);			//redirected destination
			}
			
			if(this.expectedOutputs.get(dst)!=null) {
				checker.test(dst, this.expectedOutputs.get(dst));
			}
		}//for each src
	}//run

	/**@return the status returned by the solver (e.g. Infeasible, Optimal, etc.);
	 * typically this will be a string representing the solver's native status indicator.
	 */
	public String getSolveStatus() {
		return solveStatus;
	}
	
	/**
	 * This static factory method provides a specialized application instance 
	 * for a set of examples using MOSDEX with IBM CPLEX and using Apache Spark dataframes and Java spans.
	 * To create a specific example, write a class that provides initialization for 
	 * the values of the three fields : title, fileName, and expectedOutputs.
	 * Then write a main method that creates a MsdxApplication instance using this factory 
	 * and calls its run method.
	 * <p>
	 * The application instance finds the MOSDEX JSON files of the examples 
	 * in a folder directly under the mosdex2 project: <br>
	 * <code>/mosdex2/exampleFiles</code><br>
	 * It writes the MOSDEX results to a display window and the native CPLEX output 
	 * to System.out. 
	 * It throws an exception if the actual MOSDEX does not match the expected output. 
	 *
	 * @param title The display title of the example; 
	 * the first word should be the short name of the example.
	 * @param fileName The name of the MOSDEX JSON file in the example files directory.
	 * @param expectedOutput The JSON string representing the MOSDEX output expected (ignored if null).
	 * @return an application instance
	 */
	public static MsdxApplication example(
		String title, 
		String fileName, 
		String expectedOutput) 
	{
		/**
		 * The name of the directory containing the MOSDEX JSON files of the examples.
		 * This directory should be a folder directly under the mosdex2 project: 
		 * /mosdex2/exampleFiles
		 */
		String exampleFiles= "exampleFiles";
		
		if(title == null || fileName == null)
			throw new IllegalArgumentException("Uninitialized field(s)");
		
		Msdx.GLOBAL.setDisplayTitle(title);
		Msdx.GLOBAL.showDisplay();
		
//		Getting the example file from the source/main/resource/.../examples folder does not appear to work correctly
//		This does not appear to work, as Eclipse is unable to find the resource folder
//		MsdxInputSource mosdex= MsdxInputSource.fromResource(this.fileName, MsdxExample.class);
		
//		Getting the example file from the mosdex2/exampleFiles folder
		Path path= Path.of(findProject().toString(), exampleFiles, fileName);
		MsdxInputSource mosdex= MsdxInputSource.fromFile(path.toFile());

		MsdxApplication application= new MsdxApplication(
			title.split(" (\\b[^\\s]+\\b)")[0]) //first word of title
			.addFile(mosdex, MsdxOutputDestination.toStream(Msdx.GLOBAL.out), expectedOutput) 
			.addSolverResults(MsdxOutputDestination.toStream(System.out), true)
			.useSparkDataframes()
			.useJavaSpans()
			.useCplex();
		
		return application;	
	}//example
	
	/**
	 * Finds the location of the MOSDEX project on the local file system.
	 * 
	 * @return a path of the form root/[local directory hierarchy]/mosdex2
	 */
	public static Path findProject() {
		Path mosdex= null;
		try {
			mosdex = Paths.get(Msdx.class.getResource("msdx.class").toURI());
			//Should be root/.../mosdex2/target/classes/.../msdx.class
		} catch (URISyntaxException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		//Iterate the mosdex path until reaching the target directory containing the class files
		Path path= Path.of(mosdex.getRoot().toString());
		for(Path directory: mosdex) {
			if(directory.toString().equals("target"))
				break;
			else
				path= Path.of(path.toString(), directory.toString());
		}
		//Should be root/.../mosdex2 where the ... represents the information needed to locate the mosdex2 project directory
		return path;
	}//findProject

	/**
	 * This class enables the run method to check whether or not the actual MOSDEX output 
	 * to each destination matches the expected  output.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	protected class Checker {
		
		/**
		 * Maps the actual output destinations (keys) to redirected destinations.
		 * If an expected output to a destination is specified in expectedOutputs, 
		 * the checker redirects the actual output to a byte array (i.e. a string)  
		 * for testing against the expected output.
		 * A destination without an expected output redirects to itself.
		 */
		Map<MsdxOutputDestination, MsdxOutputDestination> redirects;
	
		Checker() {
			super();
			redirects= new IdentityHashMap<MsdxOutputDestination, MsdxOutputDestination>();
			for(MsdxOutputDestination dst: mosdexOutputs.values()) {
				if(dst!=null) {
					redirects.put(
						dst, 
						expectedOutputs.get(dst)==null ?
							dst :
							MsdxOutputDestination.toStream(new ByteArrayOutputStream()));
				}
			}//for dst
		}//constructor
		
		/**
		 * @param dst
		 * @return the redirected output destination of an actual destination dst
		 */
		MsdxOutputDestination getRedirect(MsdxOutputDestination dst) {
			return redirects.get(dst);
		}
		
		/**
		 * Tests whether the expected MOSDEX output to a destination matches the actual output. 
		 * For destinations with an expected output, the actual output is captured as a string and 
		 * sent to the original destination. Then the actual and expected are compared and
		 * the result is reported to the solver results destination.
		 * 
		 * @param dst
		 * @param expected
		 * @return though void, prints a message about success or failure to the solver results destination, 
		 * and if failure, indicates where the mismatches occur.
		 * @throws JSONException if the actual output is not parsable as JSON
		 */
		void test(MsdxOutputDestination dst, String expected) {
			if(expected==null) 
				return;
			
			//Actual output goes to original destination
			String actualOutput= redirects.get(dst).getStream().toString();
			((PrintStream) dst.getStream()).println(actualOutput);	
			
			JSONCompareResult result= null;
			try {
				result=  JSONCompare.compareJSON(expected, actualOutput, JSONCompareMode.LENIENT);
			} catch (JSONException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			if(result.failed()) {
				((PrintStream) solverResults.getStream()).println("MOSDEX output does not match expected");
				result.getFieldFailures().stream()
					.forEach(failure -> ((PrintStream) solverResults.getStream()).println("Failure on " + failure.getField() + ": expected= " + failure.getExpected().toString() + " actual= " + failure.getActual().toString()));
				result.getFieldMissing().stream()
					.forEach(failure -> ((PrintStream) solverResults.getStream()).println("Missing " + failure.getField()));
				result.getFieldUnexpected().stream()
					.forEach(failure -> ((PrintStream) solverResults.getStream()).println("Unexpected " + failure.getField()));
			}
			else
				((PrintStream) solverResults.getStream()).println("MOSDEX output matches expected");
			return;
		}//test
		
	}//class MsdxApplication.Checker
	

}//MsdxApplication
