/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
/**
 * This package provides classes and interfaces that support streaming data from 
 * MOSDEX to a solver.
 * <p>
 * A Span is an abstraction representing the data streams which connect the
 * MOSDEX object model with the solver-specific modeling objects. A Span is
 * intended to work as a distributed, parallel pipeline that does not realize
 * its content except at its origin and destination. A Span transforms the data
 * that passes through it according to an Operator. Operators are the means by
 * which a solver Modeling Factory specifies the transformations necessary to
 * create and use the solver-specific API in MOSDEX. The sequence of Spans that
 * transforms MOSDEX data into solver-specific modeling objects is called a
 * bridge.
 * <p>
 * Although the potential exists to achieve the intention that a bridge does not
 * realize its content except at its origin and destination, at present we have not
 * been able to do so fully. Instead, we provide several implementations that
 * simulate Spans and leave it to other developers for further work.
 * <p>
 * The MOSDEX Span interface is implemented with two concrete classes, based on
 * Java streams or on Apache Spark distributed data sets (the latter is under development at present). 
 * The interface specifies the
 * key operations on Spans, which are a subset of the operations available for
 * Spark distributed datasets and Java streams. Apache Spark provides for
 * distributed parallel processing of data, but requires serializability of the
 * solver-specific classes that may not be available for legacy solver APIs. The
 * Java streams do not suffer that limitation, but also do not necessarily
 * support distributed parallel processing (further investigation is needed).
 * <p>
 * The fundamental component of the Span interface is the Container class. A
 * Container holds heterogeneous data together with their class identities, which
 * constitutes the Container's schema. All Containers in a Span must have the
 * same schema.
 * <p>
 * The package also includes two Operators, for one or two Container arguments, 
 * that transform Spans. These Operators behave analogously to Java functional interfaces 
 * in that they are designed to process the Containers in a Span as a stream. 
 * However, because the arguments are Containers, they need to define a Schema 
 * for the result Containers. Thus, they each include a withResultSchema method 
 * to construct it and a getResultSchema method to retrieve it. (This violates 
 * the fundamental requirement of a Java functional interface -- that it has one public method).
 * <p>
 * The classes implementing MsdxSpan do not have public constructors. Instead,
 * each has a companion, static member class implementing the MsdxSpan.Factory
 * interface. The create methods of this companion class provide for
 * construction of Span objects from various input data structures.
 * 
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
package io.github.JeremyBloom.mosdex2.span;
