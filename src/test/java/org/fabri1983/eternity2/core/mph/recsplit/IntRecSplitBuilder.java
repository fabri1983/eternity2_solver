package org.fabri1983.eternity2.core.mph.recsplit;

/**
 * A builder to generate a MPHF description, or to get an evaluator of a description.
 *
 */
public class IntRecSplitBuilder {

    private int averageBucketSize = 256;
    private int leafSize = 10;
    private boolean eliasFanoMonotoneLists = true;

    private IntRecSplitBuilder() {
    }

    /**
     * Create a new instance of the builder, with the given universal hash implementation.
     *
     * @param <T> the type
     * @param hash the universal hash function
     * @return the builder
     */
    public static IntRecSplitBuilder newInstance() {
        return new IntRecSplitBuilder();
    }

    public IntRecSplitBuilder averageBucketSize(int averageBucketSize) {
        if (averageBucketSize < 4 || averageBucketSize > 64 * 1024) {
            throw new IllegalArgumentException("averageBucketSize out of range: " + averageBucketSize);
        }
        this.averageBucketSize = averageBucketSize;
        return this;
    }

    public IntRecSplitBuilder leafSize(int leafSize) {
        if (leafSize < 1 || leafSize > 25) {
            throw new IllegalArgumentException("leafSize out of range: " + leafSize);
        }
        this.leafSize = leafSize;
        return this;
    }

    public IntRecSplitBuilder eliasFanoMonotoneLists(boolean eliasFano) {
        this.eliasFanoMonotoneLists = eliasFano;
        return this;
    }

    public IntRecSplitEvaluator buildEvaluator(BitBuffer description) {
        Settings s = new Settings(leafSize, averageBucketSize);
        return new IntRecSplitEvaluator(new BitBuffer(description), s, eliasFanoMonotoneLists);
    }

}
