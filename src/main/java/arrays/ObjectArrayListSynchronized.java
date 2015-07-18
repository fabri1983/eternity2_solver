package arrays;

/**
 * Lock free for multi-threading safe use.
 * 
 * WARNING: is uncompleted.
 * 
 * @author Fabricio Lettieri
 */
public class ObjectArrayListSynchronized extends ObjectArrayList
{

	public ObjectArrayListSynchronized()
	{
		this(10);
	}

	public ObjectArrayListSynchronized(int paramInt)
	{
		this.elements = new Object[paramInt];
		this.size = 0;
	}

	public void add(Object paramObject)
	{
		synchronized (this) {
			if (this.size == this.elements.length)
				ensureCapacity(this.size + 1);
			this.elements[(this.size++)] = paramObject;
		}
	}

	public Object[] elements()
	{
		return this.elements;
	}

	public ObjectArrayListSynchronized elements(Object[] paramArrayOfObject)
	{
		this.elements = paramArrayOfObject;
		this.size = paramArrayOfObject.length;
		return this;
	}

}
