/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2.objectModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.JeremyBloom.mosdex2.Msdx;
import io.github.JeremyBloom.mosdex2.MsdxContainer;
import io.github.JeremyBloom.mosdex2.json.MsdxReader;

/**
 * Represents a MOSDEX Query in Java. 
 * Query also is the type for the Table fields INITIALIZE, APPEND and REVISE. 
 * The primary content of this class is the JSON Tree Model node thisNode (inherited from MsdxObject).
 * <p>
 * A Query is represented by a JSON object of clause fields.
 * A clause is a JSON field, where the field key is a directive, an SQL operation such as 
 * SELECT, FROM, WHERE, JOIN, etc. The field value in a clause is the predicate of the directive;
 * usually it's a array of values and/or Query objects, but it could also be a stand-alone value or Query.
 * Typically, a predicate  is simply a list (of strings). However, SQL also allows predicates that are subqueries.
 * In that case, an item of the predicate may itself be a Query. 
 * Furthermore, potentially (but not commonly) predicate may be nested to any number of levels.
 * <p>
 * A dependency occurs when a Table's Query references another Table.  
 * Amongst all the clauses, dependencies occur in the predicates of the following directives:
 * FROM, JOIN, NATURAL JOIN, LEFT/RIGHT/FULL OUTER JOIN, CROSS JOIN.
 * Subqueries can arise in SELECT, FROM, and WHERE directives and 
 * also in set operations UNION, UNION ALL, INTERSECT, and MINUS.
 * <p>
 * Note: JSON generally requires that objects have unique keys. In the case of a Query object, that means 
 * that the directives must be unique, which would preclude having, for instance, 
 * multiple Joins in a single query. 
 * While such multi-join queries are probably rare in MOSDEX, they must be accommodated.
 * So for MOSDEX syntax, the directives can be appended with a number to distinguish them, 
 * as say "JOIN#1" "JOIN#2", etc. MOSDEX will strip off the # and anything 
 * after it before presenting the query to the SQL engine.
 * <p>
 * The following is an example Query: 
 * <pre><code>
 * "QUERY": {                                                         
 *    "SELECT": [                                                     
 *      "'operatingCost' AS Row                            -- STRING",
 *      "ship.Column AS Column                             -- STRING",
 *      "routes.shippingCost*demands.amount AS Coefficient -- DOUBLE" 
 *    ],                                                              
 *    "FROM": [ "routes" ],                                           
 *    "JOIN": [ "ship" ], "USING": [ "(location, store)" ],           
 *    "JOIN#2": [ "demands" ], "USING#2": [ "(store)" ]               
 *  }                                                                 
 * </code></pre>                                                                 
 * The schema of the Query is defined by field types appended to each field in a SELECT Clause 
 * as an SQL comment, denoted by -- surrounded by white space.
 * Note that if a Query has multiple Directives of the same kind (e.g. multiple JOIN clauses), 
 * you can append a #n suffix (n an integer) to distinguish them (because field names in a JSON object must be unique);
 * the query processing methods of this class will strip off the suffixes.
 * <p>
 * The class provides helper methods to create SQL query strings and to derive the schema from the query.
 * The toSQL methods parse the Query node to create a string that can be read by the create method of
 * MsdxDataframe.Factory. 
 * The static member class Resolver handles dependencies among the Tables. 
 * <p>
 * The syntax of MOSDEX: Mathematical Optimization Solver Data EXchange, v 2-0. 
 * Copyright © 2022 Jeremy A. Bloom
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxQuery extends MsdxObject {
	
	/**Holds the set of Table names upon which this Query depends.*/
	private Set<String> dependencies;
	
    /**
     * Constructs a new Query object.
     * Ordinarily, a QUERY should be deserialized from JSON using methods of the 
     * MsdxObject.Factory.
     * 
	 * @param thisNode Tree model representation of this object
     * @param parent the Table object containing this Query
     * @param factory that created this object
	 */
	public MsdxQuery(JsonNode thisNode, MsdxObject parent, MsdxObject.Factory factory) {
		super(thisNode, parent, factory);
		this.dependencies= new LinkedHashSet<String>();
	}
	
	protected MsdxQuery(MsdxObject parent) {
		this(MsdxReader.createObjectNode(), parent, null);
	}
	
	/**@return the set of Table names upon which this Query depends*/
	public Set<String> getDependencies() {
		return this.dependencies;
	}
	
	/**
	 * Parses the node associated with this Query. 
	 * This method is parent of the recursive toSQL method, 
	 * which parses the children nodes of this Query. 
	 * 
	 * @return the SQL representation of this Query
	 */
	public String toSQL() {
		String sql= toSQL(thisNode).toString();
		return sql.substring(1, sql.length()-2);	//remove surrounding parentheses and terminal new line
	}
	
	/**
	 * Recursively parses the clauses of a Query node to append 
	 * to the SQL string being built. If the current node is a single value, 
	 * this method appends its text representation to the SQL string being built. 
	 * If it is an array node, this method calls itself on each element. 
	 * If it is an object node, it represents a subquery, and  this method calls itself. 
	 * <p>
	 * The method also edits the text to insert a comma after each element 
	 * containing a comment (that is, a data type), so that the SQL processor 
	 * in the Dataframe Factory reads the query string correctly.
	 * <p>
	 * This method leaves the query string surrounded by parentheses, 
	 * which are removed by the parent toSQL method.
	 * 
	 * @param node contains the Query or subquery being parsed
	 * @return the SQL string being built
	 */
	protected StringBuilder toSQL(JsonNode node) {
		StringBuilder sql= new StringBuilder();
		
		Iterator<Map.Entry<String, JsonNode>> fields;
		Map.Entry<String, JsonNode> field;
		String directive;
		JsonNode predicate;
		Iterator<JsonNode> elements;
		JsonNode element;
		int location;
		if(node.isValueNode()) {
			sql.append(node.asText().replace(" --", ", --")); //replacement applies when there is a comment at the end of the element
		}
		else if(node.isArray()) {
			elements= node.elements();
			while(elements.hasNext()) {
				element= elements.next();
				sql.append(toSQL(element));
				if(elements.hasNext()) { 
					if(sql.indexOf(", --") >= 0)
						sql.append("\n");						
					else 
						sql.append(", ");
				}
				else {	//last element in array
					location= sql.lastIndexOf(", --");
					if(location >= 0) 									//replacement applies when there is a comment at the end of the element
						sql.replace(location, location+1, "");
				}
			}//while elements
			sql.append('\n');				
		}//query is array
		else if(node.isObject()) {	//node is a subquery
			fields= node.fields();	
			sql.append("(");
			while (fields.hasNext()) {
				field= fields.next();
				directive= field.getKey().split("#")[0];	//strips any differentiator from the directive key
				sql.append(directive).append(" ");		
				predicate= field.getValue();
				sql.append(toSQL(predicate));
				if(fields.hasNext() && sql.charAt(sql.length()-1)!='\n')
					sql.append('\n');
			}//while fields
			sql.append(")\n");
		}//query is object
		return sql;
	}//toSQL
	
	/**
	 * Matches a field name or field type in a SELECT clause.
	 * Used in the getSchema method.
	 */
	private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_$][a-zA-Z\\d_$]*");
	
	/**
	 * Matches the "field name -- field type end" parts in a SELECT clause.
	 * The field name may or may not be preceded by the SQL keyword "AS".
	 * Used in the getSchema method.
	 */
	private static final Pattern FIELD_AS_COMMENT= Pattern.compile("(" + IDENTIFIER.toString() + ")" + "\\s+--\\s+" + "(" + IDENTIFIER.toString() +")" + "\\s*" + "\\z");
	
	/**
	 * This method creates a Table Schema from a Query in MOSDEX JSON.
	 * It uses the fields defined in the SELECT clause of the Query. 
	 * The type of each field is annotated as a SQL comment, denoted by " -- ", 
	 * after the field name or alias name in the "AS" portion of the field.
	 * <p> 
	 * The following is an example Query with data type annotations: 
	 * <pre><code>
     * "QUERY": {                                                                                        
     *  "SELECT":[																				 
     *  "'ship' AS Name     											-- STRING",          
     *         "routes.origin AS origin									-- STRING",           
     *         "routes.destination AS destination						-- STRING",          
     *         "CONCAT('ship','_', origin, '_', destination) AS Column	-- STRING",          
     *         "CAST(0.0 AS DOUBLE) AS LowerBound						-- DOUBLE",          
     *         "routes.capacity AS UpperBound							-- DOUBLE",          
     *         "'PrimalValue(Column)' AS Value							-- DOUBLE_FUNCTION"],
     *  "FROM": "routes"         																	 
     * }         																						 
     * </code></pre>
     * 
     * All fields must have unique names. 
	 * If the FROM or JOIN clauses specify more than one Table, 
	 * all columns must be fully qualified with the name of its source Table and 
	 * must have an AS clause to define a unique name; 
	 * unqualified names are allowed when referring to the single Table in a FROM clause.
	 * Each field must be specified in its own string in the predicate array, and  
	 * the comment symbol -- must be surrounded by whitespace, and 
	 * the JSON delimiters , and ] appear outside the quotes separating the array elements.
	 * 
	 * @return a Schema container
	 */
	public MsdxContainer<Class<?>> getSchema() {	
		if(parent.hasField("SCHEMA"))
			return ((MsdxTable)parent).getSchema().asContainer();
		
		Matcher fieldDef;

		MsdxContainer.Builder<Class<?>> schema= MsdxContainer.<Class<?>>builder();
		String fieldName;
		String fieldType;
		for(JsonNode field: thisNode.get("SELECT")) {
			fieldDef= FIELD_AS_COMMENT.matcher(field.asText());
			if(!fieldDef.find())
				throw new IllegalArgumentException("Missing field definition in " + field.asText());
			fieldName= fieldDef.group(1);
			fieldType= fieldDef.group(2);
			if(schema.itemNames().contains(fieldName))
				throw new IllegalArgumentException("Duplicate field name " + fieldName);
			schema.addItem(fieldName, MsdxSchema.typeOf(fieldType));			
		}//for field		
		return schema.build();
	}//getSchema

	/**
	 * This class resolves dependencies among the Tables in a Model, i.e. a Module or a set of Modules. 
	 * Dependencies arise when a Query Table refers to other Tables through a FROM, JOIN, or 
	 * UNION clause; Instance Tables do not have dependencies, although a Query Table 
	 * might have dependencies on Instance Tables. 
	 * <p>
	 * Resolving the dependencies requires executing the Queries to create Instance data 
	 * in the Table. Clearly, the Queries must be executed in a particular order 
	 * so that the Instance data for each Table is available by the time a Query 
	 * that depends on it is executed; moreover, circular dependencies (in which Tables 
	 * have mutual dependencies) are not allowed and must be detected with an exception thrown. 
	 * The resolveDependencies method of this class executes an algorithm that fulfills 
	 * these functions.
	 * <p>
	 * Note: There is one Resolver instance for each MOSDEX Application, since Table dependencies may cross 
	 * File, Module, and Model boundaries. 
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 */
	public static class Resolver {
		
		/**Holds the Model's Tables*/
		Map<String, MsdxTable> collector;
	
		/**
		 * The set of Tables whose dependencies have been resolved so that their Queries can be executed.
		 * The insertion order of this set specifies the execution order. 
		 */
		private Set<String> resolved;
		
		/**The set of Tables whose dependencies have not yet been resolved.*/
		private Set<String> unresolved;
		
		/**
		 * Creates a new Resolver object.
		 * 
		 * @param collector the set of Tables to be resolved.
		 */
		public Resolver(Map<String, MsdxTable> collector) {
			super();
			this.collector= collector;
			this.unresolved= new LinkedHashSet<String>();
			this.resolved= new LinkedHashSet<String>();	
		}

		/**
		 * The set of SQL directives that potentially can create dependencies.
		 * <br>
		 * <ul>
		 * <li>FROM</li> 
		 * <li>JOIN</li> 
		 * <li>NATURAL JOIN</li> 
		 * <li>LEFT OUTER JOIN</li> 
		 * <li>RIGHT OUTER JOIN</li> 
		 * <li>FULL OUTER JOIN</li> 
		 * <li>CROSS JOIN</li>
		 * </ul>
		 * ANSI SQL:1999 Standard.
		 */
		static final private Set<String> DIRECTIVES= new HashSet<String>(Arrays.asList(
			"FROM", "JOIN", "NATURAL JOIN", "LEFT OUTER JOIN", "RIGHT OUTER JOIN", "FULL OUTER JOIN", "CROSS JOIN"));

		/**
		 * Calls the findDependecies method for each Table.
		 * 
		 * @return this resolver
		 */
		public MsdxQuery.Resolver findDependencies() {
			MsdxTable table;
			for(String tableName: this.collector.keySet()) {
				table= this.collector.get(tableName);
				if(table.hasField("QUERY"))
					findDependenciesOf(table.getFieldAsNode("QUERY"), table, 0);				
			}//for tableName	
			return this;
		}//findDependencies

		/**
		 * This method recursively searches for dependencies in the clause predicates 
		 * of FROM, JOIN, NATURAL JOIN, LEFT/RIGHT/FULL OUTER JOIN, CROSS JOIN directives 
		 * as well as the set operations UNION, UNION ALL, INTERSECT, and MINUS.
		 * 
		 * @param queryNode find its dependencies 
		 * @param table containing the query
		 * @param depth of the recursion
		 * @throws IllegalStateException if the depth reaches 7 (to prevent infinite recursion)
		 */
		public void findDependenciesOf(JsonNode queryNode, MsdxTable table, int depth) {
			if(depth>7)
				throw new IllegalStateException(depth + " too deep for table " + table.getName() + ": " + String.valueOf(table.getQuery().getDependencies()));
			if(queryNode==null)
				return;

			if(!queryNode.isObject())
				throw new IllegalStateException("Syntax error in query:\n" + queryNode.toPrettyString() + "\nexpected a JSON object");
			
			String directive;
			JsonNode predicate;		
			Iterable<Map.Entry<String, JsonNode>> clauses= new Iterable<Map.Entry<String, JsonNode>>() {
				@Override public Iterator<Map.Entry<String, JsonNode>> iterator() {return queryNode.fields();}
			};
			for(Map.Entry<String, JsonNode> clause: clauses) {
				directive= clause.getKey().split("#")[0];	//strips any differentiator from the directive key
				predicate= clause.getValue();
				//at this point we have a predicate node
				//for a value node, it needs to be added to the dependencies
				if(predicate.isValueNode()) {
					addIfDependent(directive, predicate, table.getQuery());
					continue;	//clause loop
				}
				//for an object node, it's a subquery and 
				//needs to be submitted to find dependencies recursively
				if(predicate.isObject()) {
					findDependenciesOf(predicate, table, depth+1);	
					continue;	//clause loop
				}
				if(!predicate.isArray())
					throw new IllegalStateException("Syntax error in query:\n" + predicate.toPrettyString() + "\nexpected an array");
				//for an array node, the same logic applies to each item of the array
				for(JsonNode item: predicate) {	//item is either a value or a subquery
					if(item.isObject())	{		//it's a subquery
						findDependenciesOf(item, table, depth);	
						continue;	//item loop
					}
					//it's a value
					addIfDependent(directive, item, table.getQuery());
				}//for item
			}//for clause
			
		}//findDependenciesOf
		
		/**
		 * Gathers the dependencies of all the tables in the collector. 
		 * Should be called after the findDepedencies method, but could be called in 
		 * the findDepedenciesOf method to track progress.
		 * 
		 * @return the dependencies of each Table in the collector
		 */
		public Map<String, Set<String>> getTableDependencies() {		
			Map<String, Set<String>> tableDependencies= collector.entrySet().stream()
				.map(entry -> {
					String tableName= entry.getKey();
					MsdxTable table= (MsdxTable)entry.getValue();
					Set<String> dependencies= table.hasField("QUERY") ? 
						((MsdxQuery)table.getQuery()).getDependencies() :
						Set.of();
					return Map.entry(tableName, dependencies);
				})
				.collect(Collectors.toMap(
					entry->entry.getKey(), 
					entry->entry.getValue()));		
			return tableDependencies;			
		}//getTableDependencies
		
		/**
		 * Determines whether the value node associated with the directive introduces a 
		 * dependency and if so adds it to the dependencies set for the table.
		 * 
		 * @param directive SQL keyword
		 * @param item of the predicate
		 * @param query find its dependencies 
		 * @return true if a dependency is added, false otherwise.
		 */
		private boolean addIfDependent(String directive, JsonNode item, MsdxQuery query) {
			if(!item.isValueNode())		
				throw new IllegalStateException("Syntax error in query\n" + query.toSQL() + "\nin "+ directive +": expected a value");
			if(!DIRECTIVES.contains(directive)) 
				return false;
			query.dependencies.add(item.asText());
			return true;
		}//addIfDependent

		/**
		 * Calls the resolveDependecies method for each table.
		 */
		public void resolveDependencies() {
			for(String tableName: this.collector.keySet()) {
				if(resolved.contains(tableName))
					continue;
				resolveDependenciesFor(this.collector.get(tableName), 0);
			}//for table			
		}//resolveDependencies
		
		/**
		 * This method resolves the dependencies for its Table argument. 
		 * At any point in the process, each Table is in one of three lists, resolved, unresolved, or 
		 * not yet considered. Initially, all Tables have not been considered. 
		 * The process considers each Table in turn, in no particular order. 
		 * It then operates recursively by resolving the dependencies of the Tables upon 
		 * which the argument table depends before the argument is itself considered resolved. 
		 * As each Table's dependencies are resolved, it is added to the resolved list. 
		 * When the process completes, all Tables' dependencies have been resolved, and
		 * the resolved list provides the order in which they are resolved. This order then 
		 * can be used to actually execute the Tables' queries.
		 * <p>
		 * If at some point, a dependent Table is encountered that is unresolved 
		 * (as opposed to either resolved or not yet considered), 
		 * that indicates that a circular dependency exists, which implies that the dependencies 
		 * cannot be resolved, and an exception is thrown.
		 * 
		 * @param table resolve its dependencies 
		 * @param depth of the recursion
		 * @throws IllegalStateException if a circular reference is detected
		 * @throws IllegalStateException if the depth reaches 7 (to prevent infinite recursion)
		 */
		public void resolveDependenciesFor(MsdxTable table, int depth) {
			if(depth>7)
				throw new IllegalStateException(depth + " too deep for table " + table.getName() + ": " + String.valueOf(table.getQuery().getDependencies()));
			Set<String> dependencies= table.hasField("QUERY") ? 
				table.getQuery().getDependencies() :
				new LinkedHashSet<String>(); 
			if(resolved.contains(table.getName()))
				return;
			unresolved.add(table.getName());
			for(String dependentTable: dependencies) {
				if(!this.resolved.contains(dependentTable)) {
					if(unresolved.contains(dependentTable)) 
						throw new IllegalStateException("Circular reference detected: " 
							+ table.getName() + "-> " + dependentTable);
					resolveDependenciesFor(/*table*/collector.get(dependentTable), depth+1);
				}
			}//for dependentTable
			this.resolved.add(table.getName());
			this.unresolved.remove(table.getName());		
		}//resolveDependenciesFor

		/**
		 * @return the resolution order of the tables in the collector. 
		 * Used by the createModelingObjects and createSolutionObjects methods in MsdxModel 
		 */
		public Set<String> getResolutionOrder() {
			return this.resolved;
		}
		
	}//class MsdxQuery.Resolver 
	
	
}//class MsdxQuery
