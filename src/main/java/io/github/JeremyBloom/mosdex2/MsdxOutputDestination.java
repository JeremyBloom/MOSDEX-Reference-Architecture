/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Provides a standard destination for writing a MOSDEX JSON File.
 * Static factories are provided with different signatures for different types of output:
 * stream, file, path to file, URL, or resource.
 * The constructors and static factories set up the output destination. 
 * The MOSDEX Object Factory creates a JSON generator from the Output Destination and uses it in the 
 * Factory's write methods to create the JSON from MOSDEX Object Model.
 * <p>
 * Note: it is possible to generate the output as a JSON string with the following calling sequence
 * <p>
 * <code>
 * ByteArrayOutputStream json= new ByteArrayOutputStream();
 * <br>
 * msdxFactory.writeFile(msdxFile, MsdxOutputDestination.toStream(json));
 * <br>
 * String jsonFile= json.toString();
 * <br>
 * json.close();
 * </code>
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
public class MsdxOutputDestination extends OutputStream {

	private OutputStream destinationStream;
	
	/**
	 * Creates a new Output Destination instance. 
	 * Prefer using the static to... methods to create an Output Destination.
	 * @param destinationStream
	 */
	protected MsdxOutputDestination(OutputStream destinationStream) {
		super();
		this.destinationStream = destinationStream;
	}

	/**@return the destination output stream*/
	public OutputStream getStream() {
		return this.destinationStream;
	}

	/**Writes a character to the destination.*/
	@Override
	public void write(int b) throws IOException {
		destinationStream.write(b);
	}//write

	/**Writes a character to the destination.*/
	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
	}

	/**Writes an array of characters to the destination.*/
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
	}

	/**Flushes this Output Destination and forces any buffered output bytes to be written out.*/
	@Override
	public void flush() throws IOException {
		super.flush();
	}

	/**Closes this Output Destination and releases any associated system resources.*/
	@Override
	public void close() throws IOException {
		super.close();
		destinationStream.close();
	}
	
	/**
	 * @param destinationStream
	 * @return a new Output Destination to the destination output stream
	 */
	public static MsdxOutputDestination toStream(OutputStream destinationStream) {
		return new MsdxOutputDestination(destinationStream);
	}//toStream
	
	/**
	 * @param destinationFile
	 * @return a new Output Destination to the destination file
	 */
	public static MsdxOutputDestination toFile(File destinationFile) {
		OutputStream dst= null;
		if(destinationFile.exists())
			destinationFile.delete();
		try {
			dst= Files.newOutputStream(destinationFile.toPath(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		return MsdxOutputDestination.toStream(dst);
	}//toFile
	
	/**
	 * @param destinationPath
	 * @return a new Output Destination to the file designated by the destination path
	 */
	public static MsdxOutputDestination toPath(String destinationPath) {
		return MsdxOutputDestination.toFile(Paths.get(destinationPath).toFile());
	}
	
	/**
	 * @param destinationURL
	 * @return a new Output Destination to the file designated by the destination URL
	 */
	public static MsdxOutputDestination toURL(URL destinationURL) {
        URLConnection connection= null;
		try {
			connection = destinationURL.openConnection();
	        connection.setDoOutput(true);
	        return MsdxOutputDestination.toStream(connection.getOutputStream());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		return null;
	}//toURL
	
	/**
	 * @param sourcePath
	 * @param resourceClass
	 * @return a new Output Destination to the file designated by the resource path
	 */
	public static MsdxOutputDestination toResource(String sourcePath, Class<?> resourceClass) {
		return MsdxOutputDestination.toPath(resourceClass.getResource(sourcePath).getPath());
	}//toResource

	/**
	 * Provides a hash code for using this class as the key in a LinkedHashMap
	 */
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	/**
	 * Permits using this class as the key in a LinkedHashMap
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	
}//class MsdxOutputDestination
