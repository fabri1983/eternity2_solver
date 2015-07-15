package eternity_faster_pkg_arrays;

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

/* Location:           C:\colt-1.1.2\lib\colt.jar
 * Qualified Name:     cern.colt.list.AbstractIntList
 * JD-Core Version:    0.5.4
 */