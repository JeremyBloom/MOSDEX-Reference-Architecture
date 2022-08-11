/*
 * This code is made available under the terms of the Eclipse Public License - v 2.0.<br>
 * 
 * Copyright 2022 by Dr. Jeremy A. Bloom (<a>jeremyblmca@gmail.com</a>)
 */
package io.github.JeremyBloom.mosdex2;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class holds heterogeneous objects and serves a fundamental role as general purpose data carriers 
 * that are used the bridging stream operations for creating solver-specific modeling objects. 
 * This class provides uniformity in handling those streams while avoiding creating many context-specific classes. 
 * Container is used in Record, Schema, Instance, Dataframe and Span.  
 * <p>
 * This class is designed to handle either data as a Record, having a prespecified schema, or 
 * a Schema itself. For data, use the type parameter T= Object; 
 * for a schema, use the type parameter T= Class&lt?&gt.
 *
 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
 */
public class MsdxContainer<T> implements Serializable {
	
	private static final long serialVersionUID = -6901729464171054592L;

	/**
	 * Holds the content of this Container.
	 * Should use a LinkedHashMap to assure iteration order.
	 */
	private Map<String, T> items;
	
	/**
	 * Creates a container from a map of items.
	 * Alternatively, use a Builder to add items one-by-one.
	 * 
	 * @param items
	 */
	public MsdxContainer(Map<String, T> items) {
		super();
		this.items = items;
	}

	/**Creates an empty container.*/
	public MsdxContainer() {
		this(Collections.emptyMap());
	}
	
	/**Creates an empty container.*/
	public static <T> MsdxContainer<T> empty() {
		return new MsdxContainer<T>();
	}

	/**
	 * Creates a container from a stream of items. 
	 * Checks whether the stream items have duplicate names with different contents.
	 * 
	 * @param <T> the type of content (Class<?> for a Schema Container, Object for a data Container)
	 * @param items a stream of map entries
	 * @throws IllegalArgumentException if the items have duplicate names with different contents. 
	 */
	public MsdxContainer(Stream<Map.Entry<String, T>> items) {
		this(fromStream(items));
	}
	
	/**
	 * Copies a container. 
	 * Modifying the copy does not affect the original, provided the items themselves are immutable.
	 * 
	 * @param original
	 */
	public MsdxContainer(MsdxContainer<T> original) {
		this(new LinkedHashMap<String, T>(original.items));
	}
	
	/**@return true if this Container is empty, false otherwise*/
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	/**@return the number of items in this Container*/
	public int size() {
		return items.size();
	}

	/**@return the names of the fields in this Container*/
	public Set<String> itemNames() {
		return Collections.unmodifiableSet(this.getItems().keySet());
	}
	
	/**@return true if itemName is among the field names of this Container, false otherwise*/
	public boolean containsField(String itemName) {
		return this.getItems().containsKey(itemName);
	}
	
	/**@return an unmodifiable view of the data items*/
	public Map<String, T> getItems() {
		return Collections.unmodifiableMap(items);
	}
	
	/**@return an unmodifiable view of the data item values*/
	public Collection<T> getContent() {
		return Collections.unmodifiableCollection(this.getItems().values());
	}
	
	/**
	 * Enables reordering the items according the provided names.
	 * 
	 * @param itemNames
	 * @return the items in the order specified by itemNames
	 * @throws IllegalArgumentException if the item name is not present among the names in this instance
	 */
	public Collection<T> getContentInOrder(Collection<String> itemNames) {
		List<T> result= new ArrayList<T>();
		for(String itemName: itemNames) {
			if(!this.itemNames().contains(itemName))
				throw new IllegalArgumentException(itemName + " is not present");
			result.add(this.get(itemName));
		}
		return result; 
	}
	
	/**@return the value of an item*/
	public T get(String itemName) {
		if(!items.containsKey(itemName))
			throw new IllegalArgumentException(itemName + " is missing from " + items.keySet().toString());
		return items.get(itemName);
	}
	
	/**@return the value of an item, if it is present, or a default value if it is not*/
	public T getOrDefault(String itemName, T defaultValue) {
		return items.getOrDefault(itemName, defaultValue);
	}
	
	/**@return the item with its name*/
	public Map.Entry<String, T> getEntry(String itemName) {
		return newItem(itemName, items.get(itemName));
	}

	/**
	 * @param <T> the type of content (Class<?> for a Schema Container, Object for a data Container)
	 * @return a Builder instance to create a new Container
	 */
	public static <T> MsdxContainer.Builder<T> builder() {
		return new MsdxContainer.Builder<T>();
	}
	
	/**A convenience method to create a new item.*/
	public static <T> AbstractMap.SimpleImmutableEntry<String, T> newItem(String name, T content) {
		return new AbstractMap.SimpleImmutableEntry<String, T>(name, content);
	}
	
	/**
	 * Tests whether the content of this Container conforms to the specified Schema.
	 * Throws an exception if it is not.  
	 * For data Containers (MsdxContainer<Object>), this method assures that the 
	 * types of the items are instances of the classes specified in the Schema. 
	 * For Schema Containers (MsdxContainer<Class<?>>), this method assures that the 
	 * types of the items are assignment compatible with the classes specified in the Schema. 
	 * 
	 * @param schema
	 * @return this Container if one of the exceptions is not thrown
	 * @throws IllegalStateException if any of the following violations occurs:
	 * <ul>
	 * <li>the Container is missing a field of the Schema; or</li>
	 * <li>the Container has an extra item not in the Schema; or</li>
	 * <li>the type of an item is not consistent with or not assignment compatible with the type specified in the Schema.</li>
	 * </ul>
	 */
	public MsdxContainer<T> verify(MsdxContainer<Class<?>> schema) {
		
		Set<String> missing= new LinkedHashSet<String>(schema.itemNames());
		missing.removeAll(this.itemNames());
		if(!missing.isEmpty()) 
			throw new IllegalStateException("Missing field(s) " + missing.toString());
		
		Set<String> extra= new LinkedHashSet<String>(this.itemNames());
		extra.removeAll(schema.itemNames());
		if(!extra.isEmpty()) 
			throw new IllegalStateException("Extra field(s) " + extra.toString());
		
		if(this.items.values().stream().allMatch(v -> Class.class.isInstance(v))) {
			//it's schema container
			for(String itemName: this.itemNames()) {
				if(!schema.get(itemName).isAssignableFrom((Class<?>) this.get(itemName)))
					throw new IllegalStateException("Type mismatch on " + itemName);
			}		
		}	
		else { 
			//it's a data container
			for(String itemName: this.itemNames()) {
				if(this.get(itemName) != null && !schema.get(itemName).isInstance(this.get(itemName)))
					throw new IllegalStateException("Type mismatch on " + itemName);
			}
		}
		//if no exception is thrown
		return this;
	}

	/**@return the content of this Container as a stream of items*/
	public Stream<Map.Entry<String, T>> toStream() {
		return this.items.entrySet().stream();
	}
	
	/**
	 * Creates a Container from a stream of items. 
	 * Checks whether the stream items have duplicate names with different contents.
	 * 
	 * @param <T> the type of content (Class<?> for a Schema Container, Object for a data Container)
	 * @param items a stream of map entries
	 * @return a map of the entries
	 * @throws IllegalArgumentException if the items have duplicate names with different contents. 
	 */
	protected static <T> MsdxContainer<T> fromStream(Stream<Map.Entry<String, T>> items) {
		MsdxContainer.Builder<T> result= MsdxContainer.builder();
		items.forEach(item -> 
			result.addItem(item.getKey(), item.getValue()));	
		return result.build();	
	}//fromStream

	/**Applies an action to each field of this Container.*/
	public void forEach(BiConsumer<? super String, ? super T> action) {
		items.forEach(action);
	}

	/**
	 * Creates a new Container by deleting the items that are not among given names.
	 * Does not check whether the item names actually exist in the Container.
	 * 
	 * @param itemNames
	 * @return a new Container
	 */
	public MsdxContainer<T> select(Collection<String> itemNames) {
		return new MsdxContainer<T>(this.toStream()
			.filter(e -> itemNames.contains(e.getKey())));
	}

	/**
	 * Creates a new Container by deleting the items that are not among given names.
	 * Does not check whether the item names actually exist in the Container.
	 * 
	 * @param itemNames
	 * @return a new Container
	 */
	public MsdxContainer<T> select(String... itemNames) {
		return select(Arrays.asList(itemNames));
	}

	/**
	 * Creates a new Container by deleting the items with the given names.
	 * Does not check whether the item names actually exist in the Container.
	 * 
	 * @param itemNames
	 * @return a new Container
	 */
	public MsdxContainer<T> delete(Collection<String> itemNames) {
		return new MsdxContainer<T>(this.toStream()
			.filter(e -> !itemNames.contains(e.getKey())));
	}

	/**
	 * Creates a new Container by deleting the items with the given names.
	 * Does not check whether the item names actually exist in the Container.
	 * 
	 * @param itemNames
	 * @return a new Container
	 */
	public MsdxContainer<T> delete(String... itemNames) {
		return delete(Arrays.asList(itemNames));
	}
	
	/**
	 * Replaces the value of a field in this Container.
	 * Warning -- this method allows changing the values in a Container and 
	 * violates the general immutability of a Container; use it sparingly.
	 * 
	 * @param itemName
	 * @param newValue
	 * @return this Container
	 * @throws IllegalArgumentException if the item name is not present or 
	 * if the new value is not type compatible with the existing value
	 */
	public MsdxContainer<T> replace(String itemName, T newValue) {
		if(!this.containsField(itemName))
			throw new IllegalArgumentException(itemName + " is missing from " + items.keySet().toString());
		if(!newValue.getClass().isInstance(this.get(itemName).getClass()))
			throw new IllegalArgumentException("Incompatible type");
		items.put(itemName, newValue);
		return this;
	}
	
	/**@return true if the values of all items are null, false otherwise*/
	public boolean allNull() {
		return this.toStream()
			.allMatch(e -> e.getValue()==null);
	}
	
	/**
	 * @param itemNames
	 * @return true if the contents of all the given item names are null, false otherwise
	 */
	public boolean allNull(Collection<String> itemNames) {
		return this.toStream()
			.filter(e -> itemNames.contains(e.getKey()))
			.allMatch(e -> e.getValue()==null);
	}
	
	/**
	 * Creates a new Container by merging two containers.
	 * If both containers have items with the same name, 
	 * the contents of the items must match.
	 * 
	 * @param other
	 * @return a new Container
	 * @throws IllegalArgumentException if both containers have fields 
	 * with the same name but their contents do not match.
	 */
	public MsdxContainer<T> merge(MsdxContainer<T> other) {
		return new MsdxContainer<T>(Stream.concat(this.toStream(), other.toStream()));
	}
	
	/**
	 * Creates a new Container in which the designated item has a new name. 
	 * The order of the items is preserved.
	 * 
	 * @param oldName
	 * @param newName
	 * @return a new Container
	 */
	public MsdxContainer<T> renameField(String oldName, String newName) {
		if(!containsField(oldName))
			throw new IllegalArgumentException("Missing name " + oldName);
		if(containsField(newName))
			throw new IllegalArgumentException("Duplicate name " + newName);

		return new MsdxContainer<T>(			
			this.toStream()
				.map(e -> e.getKey().equals(oldName) ? 
					newItem(newName, e.getValue()) : 
					e));		
	}//renameField
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MsdxContainer))
			return false;
		MsdxContainer<?> other = (MsdxContainer<?>) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}
	
	/**
	 * Finds all entries in both this and the other container that either 
	 * do not have matching keys or
	 * have matching keys but different values. 
	 * 
	 * @param other
	 * @return the mismatched items
	 */
	public Set<Map.Entry<String, T>> mismatchedItems(MsdxContainer<T> other) {
		Set<Map.Entry<String, T>> matched= this.items.entrySet().stream()
			.filter(entry -> other.items.entrySet().contains(entry))
			.collect(Collectors.toSet());
		return Stream.concat(this.items.entrySet().stream(), other.items.entrySet().stream())
			.filter(entry -> !matched.contains(entry))
			.collect(Collectors.toSet());
	}//mismatchedItems
	
	/**
	 * Makes a string of the form:
	 * <pre><code>
	 * [
	 *   {field name: field content},
	 *    ...
	 *   {field name: field content}
	 * ]
	 * </code></pre>
	 * Using this method is discouraged; prefer the show method in MsdxSchema or MsdxRecord.
	 */
	@Override
	public String toString() {
		StringBuilder result= new StringBuilder();
		result.append("{\n");	
		result.append(toString(this.itemNames(), "", "Fields", name -> name.toString()));
		result.append("\n}");		
		result.append(toString(this.getContentInOrder(this.itemNames()), "", "", item -> String.valueOf(item)));
		result.append("\n}");		
		return result.toString();
	}//toString
	
	/**
	 * Creates a string of the form
	 * <pre><code>
	 * "label": [content[0], ..., content[n]]
	 * </code></pre>
	 * Use this for Containers representing Records in an Instance array.
	 * If you want the item names, call this method twice, once for the names and then for the content.
	 * <p>
	 * Using this method is discouraged; prefer the show method in MsdxSchema or MsdxRecord.
	 * 
	 * @param <X> String for item names, Class<?> for a schema container or Object for a data container
	 * @param content this.content
	 * @param indent white space prepended to the content of this container
	 * @param label for the content, FIELDS for item names, TYPEs for a schema container or DATA for a data container
	 * @param formatter item-&gtitem.getSimpleName() for a schema container, item-&gt String.valueOf(item) for item names or a data container
	 * @return a string
	 */
	public static <X> String toString(Collection<X> content, String indent, String label, Function<X, String> formatter) {
		StringBuilder result= new StringBuilder(indent);
		if(!label.trim().isEmpty()) 
			result.append('"').append(label).append('"').append(": ");
		result.append(content.stream().collect(Collectors.mapping(
			item -> item != null ? formatter.apply(item) : "null", 
			Collectors.joining(", ", "[", "]")))
		);
		return result.toString();
	}//toString

	/**
	 * Builder for MsdxContainer.
	 * The builder can be used to verify the data against a predefined Schema or
	 * to create the Schema. 
	 * Normally, for data Containers, the Schema is specified by the constructor.
	 * If the Container is used for a Schema, do not specify a Schema.
	 * <p>
	 * Direct use of this builder is discouraged. Instead, use the builders in MsdxSchema or MsdxRecord.
	 *
	 * @author Dr. Jeremy A. Bloom (jeremyblmca@gmail.com) ©2019 Jeremy A. Bloom
	 *
	 */
	public static class Builder<T> {
	
		protected Map<String, T> items;
		
		public Builder() {
			super();
			this.items= new LinkedHashMap<String, T>();
		}
		
		/**
		 * Adds the item to this container.
		 * 
		 * @param itemName
		 * @param content
		 * @return this builder
		 * @throws IllegalArgumentException if the item name duplicates a field already in the container 
		 * and the content is not equal to the existing field content (allows duplicate fields to be replaced).
		 */
		public Builder<T> addItem(String itemName, T content) {
			if(this.items.containsKey(itemName) && !content.equals(this.items.get(itemName)))
				throw new IllegalArgumentException("Duplicate item name " + itemName);
			this.items.put(itemName, content);
			return this;
		}
		
		/**
		 * Adds a item only if the selector is true.
		 * 
		 * @param selector
		 * @param itemName
		 * @param content
		 * @return this builder
		 */
		public Builder<T> addItemIf(boolean selector, String itemName, T content) {
			if(!selector)
				return this;
			this.addItem(itemName, content);
			return this;
		}
		
		/**
		 * Copies an item from the other container if it is present.
		 * 
		 * @param other
		 * @param itemName
		 * @return this builder
		 */
		public Builder<T> copyItem(MsdxContainer<T> other, String itemName) {
			if(other.containsField(itemName))
				this.addItem(itemName, other.get(itemName));	
			return this;
		}
		
		/**
		 * Copies a group of items from the other container.
		 * 
		 * @param other
		 * @param itemNames
		 * @return this builder
		 */
		public Builder<T> copyItems(MsdxContainer<T> other, Collection<String> itemNames) {
			for(String itemName: itemNames)
				this.addItem(itemName, other.get(itemName));
			return this;
		}
		
		/**
		 * Copies a group of items from the other container.
		 * 
		 * @param other
		 * @param itemNames
		 * @return this builder
		 */
		public Builder<T> copyItems(MsdxContainer<T> other, String... itemNames) {
			return copyItems(other, Arrays.asList(itemNames));
		}
		
		/**
		 * Copies all items from the other container.
		 * 
		 * @param other
		 * @return this builder
		 */
		public Builder<T> copyItems(MsdxContainer<T> other) {
			return copyItems(other, other.itemNames());
		}
		
		/**
		 * Removes the item if it is present.
		 * 
		 * @param itemName
		 * @return this builder
		 */
		public Builder<T> removeItem(String itemName) {
			this.items.remove(itemName);
			return this;
		}
		
		/**
		 * Removes the item if it is present and the selector is true
		 * 
		 * @param selector
		 * @param itemName
		 * @return this builder
		 */
		public Builder<T> removeItemIf(boolean selector, String itemName) {
			if(selector)
				this.items.remove(itemName);
			return this;
		}
		
		/**@return the item names at the current build stage*/
		public Set<String> itemNames() {
			return Collections.unmodifiableSet(items.keySet());
		}
		
		/**
		 * Concludes the build process.
		 * 
		 * @return the new Container
		 */
		public MsdxContainer<T> build() {
			return new MsdxContainer<T>(this.items);
		}

	}//class MsdxContainer.Builder
	

}//class MsdxContainer
