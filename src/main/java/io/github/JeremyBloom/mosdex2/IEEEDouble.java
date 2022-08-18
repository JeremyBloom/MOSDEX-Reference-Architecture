/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.util.regex.Pattern;

import org.apache.commons.math3.util.Precision;

/**
 * Represents a Double value by its IEEE 754 hex value. 
 * This class enables accessing values computed by a solver with no loss of precision.
 * This class uses the Apache Commons Math Precision class to implement equals and compareTo 
 * methods within a user-defined tolerance (default value 0.000001d).
 * In all other ways, IEEEDouble values behave like ordinary Java Doubles.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 *
 */
@SuppressWarnings("serial")
public final class IEEEDouble extends Number {
	
	private Double value;
	
	/**
	 * Comparison tolerance.
	 */
	private double epsilon = 0.000001d;
	
	public static final Class<IEEEDouble> TYPE= IEEEDouble.class;
	
	private static final Pattern DOUBLE_PATTERN = Pattern.compile(
		    "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
		    "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|" +
		    "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
		    "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");
	
	public static final IEEEDouble POSITIVE_INFINITY= new IEEEDouble(Double.POSITIVE_INFINITY);

	public static final IEEEDouble NEGATIVE_INFINITY= new IEEEDouble(Double.NEGATIVE_INFINITY);

	/**
	 * 
	 */
	public IEEEDouble() {
		super();
	}
	
	public IEEEDouble(Double value) {
		super();
		this.value = value;
	}

	public IEEEDouble(double v) {
		super();
		this.value = Double.valueOf(v);
	}

	public IEEEDouble(String s) {
		super();
		this.value = Double.valueOf(s);
	}

	/**
	 * @return the current value of the comparison tolerance.
	 */
	public double getEpsilon() {
		return epsilon;
	}

	/**
	 * Set the comparison tolerance.
	 * 
	 * @param epsilon
	 * @return this IEEEDouble
	 */
	public IEEEDouble withEpsilon(double epsilon) {
		this.epsilon = epsilon;
		return this;
	}

	/**
	 * @return the value
	 */
	public Double get() {
		return value;
	}

	@Override
	public int intValue() {
		return value.intValue();
	}

	@Override
	public long longValue() {
		return value.longValue();
	}

	@Override
	public float floatValue() {
		return value.floatValue();
	}

	@Override
	public double doubleValue() {
		return value.doubleValue();
	}
	
	public Double exposeDoubleValue() {
		return value;
	}
	
	/**
	 * Returns an IEEEDouble object holding the double value represented by the argument string s.
	 * The argument can be a hex string representation of an IEEE Double.
	 * 
	 * @param s
	 * @return an IEEEDouble
	 */
	public static IEEEDouble valueOf(String s) {
		return new IEEEDouble(Double.valueOf(s));
	}
	
	public static IEEEDouble valueOf(double d) {
		return new IEEEDouble(Double.valueOf(d));
	}

	/**
	 * Tests the argument for equality with this number. 
	 * Any type of argument except for Double or IEEE Double returns false.
	 * A proper argument returns equality within the tolerance (epsilon) for this number.
	 * Uses org.apache.commons.math3.util.Precision.equals(double, double, double);
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof IEEEDouble || obj instanceof Double))
			return false;
		if (this.isNaN() && (obj instanceof Double) ? ((Double) obj).isNaN() : ((IEEEDouble) obj).isNaN())
			return true;	//conforms to Double.equals
		if (this.isNaN() || (obj instanceof Double) ? ((Double) obj).isNaN() : ((IEEEDouble) obj).isNaN())
			return false;	//conforms to Double.equals
		double other = (obj instanceof Double) ? ((Double) obj).doubleValue() : ((IEEEDouble) obj).doubleValue() ;
		return Precision.equals(this.doubleValue(), other, epsilon);
	}

	/**
	 * Compares this number to another.
	 * Returns the comparison within the tolerance (epsilon) for this number.
	 * Uses org.apache.commons.math3.util.Precision.compareTo(double, double, double).
	 * 
	 * @param other
	 * @return <ul>
	 * <li>0 if equals(x, y, eps)</li>
	 * <li>&lt 0 if !equals(x, y, eps) &amp x &lt y</li>
	 * <li>&gt 0 if !equals(x, y, eps) &amp x &gt y</li>
	 * </ul>
	 */
	public int compareTo(IEEEDouble other) {
		return Precision.compareTo(this.doubleValue(), other.doubleValue(), epsilon);
	}

	public int compareTo(Double other) {
		return Precision.compareTo(this.doubleValue(), other.doubleValue(), epsilon);
	}

	public int compareTo(double other) {
		return Precision.compareTo(this.doubleValue(), other, epsilon);
	}

	public String toString() {
		return String.valueOf(value);
	}
	
	/**
	 * Inverse of valueOf(string).
	 * 
	 * @return a hexadecimal string representation of this double value
	 */
	public String toHexString() {
		return Double.toHexString(value);
	}
	
	/**
	 * Checks whether a string is a valid IEEE Double.
	 * 
	 * @param s
	 * @return true if s  is a valid IEEE Double, false otherwise
	 */
	public static boolean isValid(String s) {	
		return DOUBLE_PATTERN.matcher(s).matches();
	}
	
	public String toJSON() {
		return Double.toHexString(value);
	}

	public boolean isNaN() {
		return value.isNaN();
	}

	public boolean isInfinite() {
		return value.isInfinite();
	}

	public byte byteValue() {
		return value.byteValue();
	}

	public short shortValue() {
		return value.shortValue();
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}


}//class IEEEDouble
