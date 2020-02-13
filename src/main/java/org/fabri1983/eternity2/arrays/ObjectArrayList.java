/*
Copyright (c) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/

package org.fabri1983.eternity2.arrays;

public class ObjectArrayList<T> extends AbstractCollection
{
	protected T[] elements;
	protected int size;

	public ObjectArrayList()
	{
		this(10);
	}

	@SuppressWarnings("unchecked")
	public ObjectArrayList(int paramInt)
	{
		this.elements = (T[]) new Object[paramInt];
		this.size = 0;
	}

	public void add(T paramObject)
	{
		if (this.size == this.elements.length)
			ensureCapacity(this.size + 1);
		this.elements[(this.size++)] = paramObject;
	}

	public T[] elements()
	{
		return this.elements;
	}

	public ObjectArrayList<T> elements(T[] paramArrayOfObject)
	{
		this.elements = paramArrayOfObject;
		this.size = paramArrayOfObject.length;
		return this;
	}

	public void ensureCapacity(int paramInt)
	{
		this.elements = Arrays.ensureCapacity(this.elements, paramInt);
	}

	public T get(int paramInt)
	{
		return this.elements[paramInt];
	}

	public void set(int paramInt, T paramObject)
	{
		this.elements[paramInt] = paramObject;
	}

	public int size()
	{
		return this.size;
	}

	public void trimToSize()
	{
		this.elements = Arrays.trimToCapacity(this.elements, size());
	}

	@SuppressWarnings("unchecked")
	public void clear()
	{
		this.elements = (T[]) new Object[0];
	}

}
