/*
Copyright (c) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/

package org.fabri1983.eternity2.arrays;

public class Arrays
{
	public static int[] ensureCapacity(int[] paramArrayOfInt, int paramInt)
	{
		int i = paramArrayOfInt.length;
		int[] arrayOfInt;
		if (paramInt > i)
		{
			int j = i * 3 / 2 + 1;
			if (j < paramInt)
				j = paramInt;
			arrayOfInt = new int[j];
			System.arraycopy(paramArrayOfInt, 0, arrayOfInt, 0, i);
		}
		else
		{
			arrayOfInt = paramArrayOfInt;
		}
		return arrayOfInt;
	}

	public static Object[] ensureCapacity(Object[] paramArrayOfObject, int paramInt)
	{
		int i = paramArrayOfObject.length;
		Object[] arrayOfObject;
		if (paramInt > i)
		{
			int j = i * 3 / 2 + 1;
			if (j < paramInt)
				j = paramInt;
			arrayOfObject = new Object[j];
			System.arraycopy(paramArrayOfObject, 0, arrayOfObject, 0, i);
		}
		else
		{
			arrayOfObject = paramArrayOfObject;
		}
		return arrayOfObject;
	}

	public static String toString(int[] paramArrayOfInt)
	{
		return "";
	}

	public static String toString(Object[] paramArrayOfObject)
	{
		return "";
	}

	public static int[] trimToCapacity(int[] paramArrayOfInt, int paramInt)
	{
		if (paramArrayOfInt.length > paramInt)
		{
			int[] arrayOfInt = paramArrayOfInt;
			paramArrayOfInt = new int[paramInt];
			System.arraycopy(arrayOfInt, 0, paramArrayOfInt, 0, paramInt);
		}
		return paramArrayOfInt;
	}

	public static Object[] trimToCapacity(Object[] paramArrayOfObject, int paramInt)
	{
		if (paramArrayOfObject.length > paramInt)
		{
			Object[] arrayOfObject = paramArrayOfObject;
			paramArrayOfObject = new Object[paramInt];
			System.arraycopy(arrayOfObject, 0, paramArrayOfObject, 0, paramInt);
		}
		return paramArrayOfObject;
	}
}
