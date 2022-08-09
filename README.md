# MOSDEX-Reference-Architecture
The Reference Architecture is a recommendation for how MOSDEX data flows from its source in enterprise systems to an optimization solver and back again.
<b>Introduction</b><br>
This project comprises a Java 
implementation of the MOSDEX Reference Architecture (RefArc). MOSDEX, or 
<u>M</u>athematical <u>O</u>ptimization <u>S</u>olver <u>D</u>ata 
<u>EX</u>change, itself refers to the standard for representing data for 
optimization problems.
<p>
The RefArc includes the following components:
<ol>
<li>parsing MOSDEX JSON and creating the MOSDEX object model. 
(packages json and objectModel). </li>
<li>managing queries and instance data (package dataframe).</li>
<li>creating a representation of an optimization model and the bridges that 
transform MOSDEX data into solver-specific modeling objects, 
invoke the solver, and retrieve the solution data into MOSDEX (packages modeling and  span).</li>
<li>demonstrating MOSDEX on actual problem instances (package examples).</li>
</ol>
<p>
The syntax of this version of MOSDEX (2.0) is defined at MOSDEX/MOSDEX
v2-0/MOSDEXSchemaV2-0.json, located in this package.
<p>
<b>Dependencies</b><br>
In order to use the code in this project, you will need to link to several other code packages:
<ul>
<li>Jackson Java JSON library (<a>https://github.com/FasterXML/jackso</a>n), usually included with Apache Spark.</li>
<li>Apache Spark (<a>https://spark.apache.org/</a>), a unified analytics engine for large-scale data processing. </li>
<li>IBM CPLEX Optimizer <a>https://www.ibm.com/analytics/cplex-optimizer</a>, an optimization solver.</li>
</ul>
See the attached pom.xml file for the exact dependencies on these packages.
<p>
Both Jackson and Spark are open source projects, while CPLEX is a proprietary product of IBM. You must secure a license for CPLEX in order to use it with MOSDEX; CPLEX is not available through Maven Central.<br>
Note: This MOSDEX RefArc implementation relies only on the published, public APIs of these packages and requires no customization of them.
<p>
Other resources for MOSDEX can be found at <a>https://github.com/coin-modeling-dev</a>.
<p>
<b>License and Copyright</b><br>
This software is made available under the terms of the Eclipse Public License - v 2.0. 
<a>https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt</a><br>
The code of this project has copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
