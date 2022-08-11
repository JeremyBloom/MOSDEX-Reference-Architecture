/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
/**
 * This package includes Java classes representing MOSDEX objects. 
 * Only the classes that are manipulated by the Application are defined. 
 * Other objects are represented by nodes of the JSON Tree Model.
 * In particular, most (but not all) MOSDEX classes derive from the base class,   
 * which provides the template for almost all 
 * of the Java functionality of the MOSDEX object model. Subclasses 
 * are used to create Java objects for 
 * the components of the MOSDEX object model that need a more functional representation 
 * than the JSON Tree Model alone can provide: 
 * <ul style="list-style-type:bullet">
 * <li> MsdxFile</li>
 * <li> MsdxModule</li>
 * <li> MsdxTable</li>
 * <li> MsdxSchema</li>
 * <li> MsdxInstance</li>
 * <li> MsdxRecord</li>
 * <li> MsdxQuery</li>
 * </ul> 
 * <p>
 * The MsdxObject class has a static member class Factory that includes methods 
 * to read and write the object model from MOSDEX JSON.
 * <p>
 * Generated from MOSDEX syntax v2.0 by
 * http://www.jsonschema2pojo.org/
 * 
 * @author Jeremy A. Bloom (jeremyblmca@gmail.com)
 * Copyright © 2022 Jeremy A. Bloom
 * 
 */
package io.github.JeremyBloom.mosdex2.objectModel;