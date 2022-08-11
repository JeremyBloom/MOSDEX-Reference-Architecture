/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Provides a standard source for reading a MOSDEX JSON File.
 * Static factories are provided with different signatures for different types of input:
 * stream, file, path to file, URL, or resource.
 * The constructors and static factories set up the input source. 
 * The MOSDEX Object Factory creates a JSON parser from the Input Source and uses it in the 
 * Factory's read methods to create the MOSDEX Object Model from the JSON.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxInputSource extends InputStream {
	
	private InputStream sourceStream;

	/**
	 * Creates a new Input Source instance. 
	 * Prefer using the static from... methods to create an Input Source.
	 * @param sourceStream
	 */
	protected MsdxInputSource(InputStream sourceStream) {
		super();
		this.sourceStream = sourceStream;
	}

	/**@return the source input stream*/
	public InputStream getStream() {
		return sourceStream;
	}

	/**Reads the next byte of data from the input stream.*/
	@Override
	public int read() throws IOException {
		return sourceStream.read();
	}
	
	/**Closes this input source and releases any associated system resources.*/
	@Override
	public void close() throws IOException {
		sourceStream.close();
	}

	/**
	 * @param sourceStream
	 * @return a new Input Source from the source input stream
	 */
	public static MsdxInputSource fromStream(InputStream sourceStream) {
		return new MsdxInputSource(sourceStream);
	}//fromStream
	
	/**
	 * @param sourceFile
	 * @return a new Input Source from the source file
	 */
	public static MsdxInputSource fromFile(File sourceFile) {
		FileInputStream src= null;
		try {
			src= new FileInputStream(sourceFile);
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		return MsdxInputSource.fromStream(src);
	}//fromFile
	
	/**
	 * @param sourcePath
	 * @return a new Input Source from the file designated by the source path
	 */
	public static MsdxInputSource fromPath(String sourcePath) {
		return MsdxInputSource.fromFile(Paths.get(sourcePath).toFile());	
	}//fromPath
	
	/**
	 * @param sourceURL
	 * @return a new Input Source from the file designated by the source URL
	 */
	public static MsdxInputSource fromURL(URL sourceURL) {
		InputStream src= null;
		try {
			src= sourceURL.openStream();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		return MsdxInputSource.fromStream(src);
	}//fromURL
	
	/**
	 * @param sourcePath
	 * @param resourceClass
	 * @return a new Input Source from the file designated by the resource path 
	 */
	public static MsdxInputSource fromResource(String sourcePath, Class<?> resourceClass) {
		ClassLoader loader= resourceClass.getClassLoader();
		InputStream resource= loader.getResourceAsStream(sourcePath);
		if(resource == null) {
			throw new IllegalArgumentException("Resource file " + sourcePath + " not found");
		}
		return MsdxInputSource.fromStream(resource);
	}//fromResource
	
	/**
	 * @param sourceString
	 * @return a new Input Source from the source string
	 */
	public static MsdxInputSource fromString(String sourceString) {
		return MsdxInputSource.fromStream(new ByteArrayInputStream(sourceString.getBytes()));						
	}//fromString
	

}//class MsdxInputSource
