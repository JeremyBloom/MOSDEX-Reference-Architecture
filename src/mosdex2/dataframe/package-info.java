/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
/**
 * This package represents alternative implementations of the Dataframe interface.
 * <p>
 * A Dataframe is a fundamental component of MOSDEX, representing a data set of
 * records that supports SQL queries. A Table's Instance field contains a Dataframe 
 * that holds its Records. Since Instances can potentially hold very large data sets, 
 * a basic challenge in implementing MOSDEX is how to efficiently read and process Instances. 
 * Our design philosophy is to use data structures that support big data operations from the outset 
 * to insure scalability of MOSDEX. 
 * <p>
 * Currently, we are using Apache Spark to implement Dataframe, but we believe our design is 
 * adaptable to FLINK, or JDBC databases as well. 
 */
package io.github.JeremyBloom.mosdex2.dataframe;
