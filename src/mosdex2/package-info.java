/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
/**
 * <u></u>This package and its subpackages together comprise a Java
 * implementation of the MOSDEX Reference Architecture (RefArc). MOSDEX, or
 * <u>M</u>athematical <u>O</u>ptimization <u>S</u>olver <u>D</u>ata 
 * <u>EX</u>change, itself refers to the standard for representing data for
 * optimization problems. The RefArc is a recommendation for how MOSDEX data
 * flows from its source in enterprise systems to an optimization solver and
 * back again.
 * <p>
 * The RefArc includes the following components:
 * <ol>
 * <li>parsing MOSDEX JSON and creating the MOSDEX object model. 
 * (packages json and objectModel). </li>
 * <li>managing queries and instance data (package dataframe).</li>
 * <li>creating a representation of an optimization model and the bridges that
 * transform MOSDEX data into solver-specific modeling objects, 
 * invoke the solver, and retrieve the solution data into MOSDEX (packages modeling and span).</li>
 * <li>demonstrating MOSDEX on actual problem instances (package examples).</li>
 * </ol>
 * <p>
 * The syntax of this version of MOSDEX (2.0) is defined at MOSDEX/MOSDEX
 * v2-0/MOSDEXSchemaV2-0.json, located in this package.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2022 Jeremy A. Bloom /
 * 
 */
package io.github.JeremyBloom.mosdex2;