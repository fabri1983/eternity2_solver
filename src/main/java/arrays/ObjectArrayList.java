package arrays;

import java.lang.reflect.Array;

public class ObjectArrayList extends AbstractCollection
{
	protected Object[] elements;
	protected int size;

	public ObjectArrayList()
	{
		this(10);
	}

	public ObjectArrayList(int paramInt)
	{
		this.elements = new Object[paramInt];
		this.size = 0;
	}

	public void add(Object paramObject)
	{
		if (this.size == this.elements.length)
			ensureCapacity(this.size + 1);
		this.elements[(this.size++)] = paramObject;
	}

	public Object[] elements()
	{
		return this.elements;
	}

	public ObjectArrayList elements(Object[] paramArrayOfObject)
	{
		this.elements = paramArrayOfObject;
		this.size = paramArrayOfObject.length;
		return this;
	}

	public void ensureCapacity(int paramInt)
	{
		this.elements = Arrays.ensureCapacity(this.elements, paramInt);
	}

	public Object get(int paramInt)
	{
		return this.elements[paramInt];
	}

	public void set(int paramInt, Object paramObject)
	{
		this.elements[paramInt] = paramObject;
	}

	public int size()
	{
		return this.size;
	}

	public Object[] toArray(Object[] paramArrayOfObject)
	{
		if (paramArrayOfObject.length < this.size)
			paramArrayOfObject = (Object[])Array.newInstance(paramArrayOfObject.getClass().getComponentType(), this.size);
		Object[] arrayOfObject = this.elements;
		int i = this.size;
		while (--i >= 0)
			paramArrayOfObject[i] = arrayOfObject[i];
		if (paramArrayOfObject.length > this.size)
			paramArrayOfObject[this.size] = null;
		return paramArrayOfObject;
	}

	public void trimToSize()
	{
		this.elements = Arrays.trimToCapacity(this.elements, size());
	}

	public void clear()
	{
		this.elements = new Object[0];
	}

}

/* Location:           C:\colt-1.1.2\lib\colt.jar
 * Qualified Name:     cern.colt.list.ObjectArrayList
 * JD-Core Version:    0.5.4
 */