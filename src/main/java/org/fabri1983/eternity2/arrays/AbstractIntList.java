/*
Copyright (c) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/

package org.fabri1983.eternity2.arrays;

public abstract class AbstractIntList extends AbstractCollection
{
    protected int size;

    public abstract void ensureCapacity(int paramInt);

    protected abstract int get(int paramInt);

    protected abstract void set(int paramInt1, int paramInt2);

    public int size()
    {
        return this.size;
    }
}
