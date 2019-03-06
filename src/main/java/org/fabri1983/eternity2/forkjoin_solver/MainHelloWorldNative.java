package org.fabri1983.eternity2.forkjoin_solver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * This class intended for testing use of fork-join actions in Graal native image generation.
 */
public class MainHelloWorldNative extends RecursiveAction {
	
	private static final long serialVersionUID = 1L;
	
	final static int NUM_PROCESSES = 4;

	int tag;
	CountDownLatch startSignal;
	CountDownLatch doneSignal;
	
	public MainHelloWorldNative(int tag, CountDownLatch startSignal, CountDownLatch doneSignal) {
		super();
		this.tag = tag;
		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
	}
	
	@Override
	protected void compute() {
		try {
			// await for starting signal
			startSignal.await();

			// start doing some work
			System.out.println("Start task " + tag);
			long count = 0L;
	        for (long x=0, c=Integer.MAX_VALUE; x < c; x++) {
	            count+=1;
	        }
	        System.out.println("Finish task " + tag + " with count " + count);
		} catch (InterruptedException e) {
			System.out.println("Exception on compute()");
			e.printStackTrace();
		} finally {
			doneSignal.countDown();
		}
	}
	
	// arguments are passed using the text field below this editor
	public static void main(String[] args) {
		
		ForkJoinPool fjpool = new ForkJoinPool(NUM_PROCESSES, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

		// a start signal that prevents any ExplorationAction from proceeding until the orchestrator (this thread) is
		// ready for them to proceed
		final CountDownLatch startSignal = new CountDownLatch(1);

		// a completion signal that allows the driver orchestrator (this thread) to wait until all ExplorationAction
		// have completed
		final CountDownLatch doneSignal = new CountDownLatch(NUM_PROCESSES);

		// submit all fork join tasks
		for (int i = 0; i < NUM_PROCESSES; ++i) {

			System.out.println("RecursiveAction " + i + " submitted");

			fjpool.submit(new MainHelloWorldNative(i, startSignal, doneSignal));
		}

		// let all threads proceed
		startSignal.countDown();

		// wait for all tasks to finish
		try {
			doneSignal.await();
			System.out.println("All tasks have finished!");
		} catch (InterruptedException e) {
			System.out.println("Exception on doneSignal.await()");
			e.printStackTrace();
		}
	}

}
