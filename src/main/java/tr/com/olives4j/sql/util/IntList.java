/*******************************************************************************
 *   Copyright (c) 2016, Omer Dogan.  All rights reserved.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *    
 *******************************************************************************/
package tr.com.olives4j.sql.util;
/*
 * IntList.java Created Aug 310, 2010 by Andrew Butler, PSL
 */

/**
 * Acts like an {@link java.util.ArrayList} but for primitive int values
 */
public class IntList {
	private int[] elementData;

	private int size;

	/**
	 * Creates a list with a capacity of 5
	 */
	public IntList() {
		this(5);
	}

	/**
	 * Creates a list with a set capacity
	 * 
	 * @param size
	 *            The initial capacity of the list
	 */
	public IntList(int size) {
		elementData = new int[size];
	}

	/**
	 * Creates a list with a set of values
	 * 
	 * @param values
	 *            The values for the list
	 */
	public IntList(int[] values) {
		elementData = values;
		size = values.length;
	}

	/**
	 * @return The number of elements in the list
	 */
	public int size() {
		return size;
	}

	/**
	 * @return Whether this list is empty of elements
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Clears this list, setting its size to 0
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * Gets the value in the list at the given index
	 * 
	 * @param index
	 *            The index of the value to get
	 * @return The value at the given index
	 */
	public int get(int index) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException(index);
		return elementData[index];
	}

	/**
	 * Adds a value to the end of this list
	 * 
	 * @param value
	 *            The value to add to the list
	 */
	public void add(int value) {
		ensureCapacity(size + 1);
		elementData[size++] = value;
	}

	/**
	 * Adds a value to this list at the given index
	 * 
	 * @param index
	 *            The index to add the value at
	 * @param value
	 *            The value to add to the list
	 */
	public void add(int index, int value) {
		if (index < 0 || index > size)
			throw new ArrayIndexOutOfBoundsException(index);
		ensureCapacity(size + 1);
		for (int i = size; i > index; i--)
			elementData[i] = elementData[i - 1];
		elementData[index] = value;
		size++;
	}

	/**
	 * Adds an array of values to the end of this list
	 * 
	 * @param value
	 *            The values to add
	 */
	public void addAll(int[] value) {
		ensureCapacity(size + value.length);
		for (int i = 0; i < value.length; i++)
			elementData[size + i] = value[i];
		size += value.length;
	}

	/**
	 * Adds a list of values to the end of this list
	 * 
	 * @param list
	 *            The list of values to add
	 */
	public void addAll(IntList list) {
		ensureCapacity(size + list.size);
		for (int i = 0; i < list.size; i++)
			elementData[size + i] = list.elementData[i];
		size += list.size;
	}

	/**
	 * Replaces a value in this list with another value
	 * 
	 * @param index
	 *            The index of the value to replace
	 * @param value
	 *            The value to replace the old value with
	 * @return The old value at the given index
	 */
	public int set(int index, int value) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException(index);
		int ret = elementData[index];
		elementData[index] = value;
		return ret;
	}

	/**
	 * Removes a value from this list
	 * 
	 * @param index
	 *            The index of the value to remove
	 * @return The value that was removed
	 */
	public int remove(int index) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException(index);
		int ret = elementData[index];
		for (int i = index; i < size - 1; i++)
			elementData[i] = elementData[i + 1];
		size--;
		return ret;
	}

	/**
	 * Removes a value from this list
	 * 
	 * @param value
	 *            The value to remove
	 * @return Whether the value was found and removed
	 */
	public boolean removeValue(int value) {
		for (int i = 0; i < size; i++)
			if (elementData[i] == value) {
				remove(i);
				return true;
			}
		return false;
	}

	/**
	 * Removes all instances of the given value from this list
	 * 
	 * @param value
	 *            The value to remove
	 * @return The number of times the value was removed
	 */
	public int removeAll(int value) {
		int ret = 0;
		for (int i = 0; i < size; i++)
			if (elementData[i] == value) {
				remove(i);
				i--;
				ret++;
			}
		return ret;
	}

	/**
	 * Determines if this list contains a given value
	 * 
	 * @param value
	 *            The value to find
	 * @return Whether this list contains the given value
	 */
	public boolean contains(int value) {
		return indexOf(value) >= 0;
	}

	/**
	 * Counts the number of times a value is represented in this list
	 * 
	 * @param value
	 *            The value to count
	 * @return The number of times the value appears in this list
	 */
	public int instanceCount(int value) {
		int ret = 0;
		for (int i = 0; i < size; i++)
			if (elementData[i] == value)
				ret++;
		return ret;
	}

	/**
	 * Finds a value in this list
	 * 
	 * @param value
	 *            The value to find
	 * @return The first index whose value is the given value
	 */
	public int indexOf(int value) {
		for (int i = 0; i < size; i++)
			if (elementData[i] == value)
				return i;
		return -1;
	}

	/**
	 * Finds a value in this list
	 * 
	 * @param value
	 *            The value to find
	 * @return The last index whose value is the given value
	 */
	public int lastIndexOf(int value) {
		for (int i = size - 1; i >= 0; i--)
			if (elementData[i] == value)
				return i;
		return -1;
	}

	/**
	 * @return The list of values currently in this list
	 */
	public int[] toArray() {
		int[] ret = new int[size];
		System.arraycopy(elementData, 0, ret, 0, size);
		return ret;
	}

	/**
	 * Similary to {@link #toArray()} but creates an array of {@link Integer}
	 * wrappers
	 * 
	 * @return The list of values currently in this list
	 */
	public Integer[] toObjectArray() {
		Integer[] ret = new Integer[size];
		for (int i = 0; i < ret.length; i++)
			ret[i] = new Integer(elementData[i]);
		return ret;
	}

	/**
	 * Trims this list so that it wastes no space and its capacity is equal to its
	 * size
	 */
	public void trimToSize() {
		if (elementData.length == size)
			return;
		int[] oldData = elementData;
		elementData = new int[size];
		System.arraycopy(oldData, 0, elementData, 0, size);
	}

	/**
	 * Ensures that this list's capacity is at list the given value
	 * 
	 * @param minCapacity
	 *            The minimum capacity for the list
	 */
	public void ensureCapacity(int minCapacity) {
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			int oldData[] = elementData;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			elementData = new int[newCapacity];
			System.arraycopy(oldData, 0, elementData, 0, size);
		}
	}
}