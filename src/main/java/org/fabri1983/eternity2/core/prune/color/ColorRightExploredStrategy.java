package org.fabri1983.eternity2.core.prune.color;

public interface ColorRightExploredStrategy {

	boolean usarPodaColorRightExpl();

	int get(int i);

	void set(int i, int val);

	void compareAndSet(int i, int val);

}
