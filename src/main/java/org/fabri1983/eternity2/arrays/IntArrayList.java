/*
Copyright (c) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/

package org.fabri1983.eternity2.arrays;

public class IntArrayList extends AbstractIntList {

    protected int[] elements;

    public IntArrayList()
    {
        this(10);
    }

    public IntArrayList(int paramInt)
    {
        this.elements = new int[paramInt];
        this.size = 0;
    }

    public void add(int paramInt)
    {
        if (this.size == this.elements.length)
            ensureCapacity(this.size + 1);
        this.elements[(this.size++)] = paramInt;
    }

    public int[] elements()
    {
        return this.elements;
    }

    public AbstractIntList elements(int[] paramArrayOfInt)
    {
        this.elements = paramArrayOfInt;
        this.size = paramArrayOfInt.length;
        return this;
    }

    public void ensureCapacity(int paramInt)
    {
        this.elements = Arrays.ensureCapacity(this.elements, paramInt);
    }

    public int get(int paramInt)
    {
        return this.elements[paramInt];
    }

    public void set(int paramInt1, int paramInt2)
    {
        this.elements[paramInt1] = paramInt2;
    }


    public void trimToSize()
    {
        this.elements = Arrays.trimToCapacity(this.elements, size());
    }

    public void clear(){
        this.elements = new int[0];

    }
}
