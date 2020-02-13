package org.fabri1983.eternity2.experimental.mph.recsplit;

/**
 * Evaluator for the hybrid mechanism.
 */
public class IntRecSplitEvaluator {

	private final Settings settings;
    private final BitBuffer buffer;
    private final long size;
    private final int bucketCount;
    private final int minStartDiff;
    private final MonotoneList startList;
    private final int minOffsetDiff;
    private final MonotoneList offsetList;
    private final int startBuckets;
    private final int endHeader;
    private final int endOffsetList;
    private final IntBDZ alternative;

    public IntRecSplitEvaluator(BitBuffer buffer, Settings settings, boolean eliasFanoMonotoneLists) {
        this.settings = settings;
        this.buffer = buffer;
        this.size = (int) (buffer.readEliasDelta() - 1);
        this.bucketCount = Settings.getBucketCount(size, settings.getAverageBucketSize());
        boolean alternative = buffer.readBit() != 0;
        this.minOffsetDiff = (int) (buffer.readEliasDelta() - 1);
        this.minStartDiff = (int) (buffer.readEliasDelta() - 1);
        this.endHeader = buffer.position();
        this.offsetList = MonotoneList.load(buffer, eliasFanoMonotoneLists);
        this.endOffsetList = buffer.position();
        this.startList = MonotoneList.load(buffer, eliasFanoMonotoneLists);
        this.startBuckets = buffer.position();
        if (alternative) {
            int b = bucketCount;
            int offset = offsetList.get(b);
            int pos = startBuckets + getMinBitCount(offset) + startList.get(b) + b * minStartDiff;
            buffer.seek(pos);
            this.alternative = IntBDZ.load(buffer);
        } else {
            this.alternative = null;
        }
    }
    
    public int getHeaderSize() {
        return endHeader;
    }

    public int getOffsetListSize() {
        return endOffsetList - endHeader;
    }

    public int getStartListSize() {
        return startBuckets - endOffsetList;
    }
    
    public int evaluate(int key) {
        int b;
        int hashCode = IntHash.universalHash(key, 0);
        if (bucketCount == 1) {
            b = 0;
        } else {
            b = Settings.reduce(hashCode, bucketCount);
        }
        int startPos;
        long offsetPair = offsetList.getPair(b);
        int offset = (int) (offsetPair >>> 32) + b * minOffsetDiff;
        int offsetNext = ((int) offsetPair) + (b + 1) * minOffsetDiff;
        if (offsetNext == offset) {
            if (alternative == null) {
                // entry not found
                return 0;
            }
            offset = offsetList.get(bucketCount) + bucketCount * minOffsetDiff;
            return offset + alternative.evaluate(key);
        }
        int bucketSize = offsetNext - offset;
        startPos = startBuckets + getMinBitCount(offset) +
                startList.get(b) + b * minStartDiff;
        return evaluate(startPos, key, hashCode, 0, offset, bucketSize);
    }

    private int evaluate(int pos, int key, int hashCode,
            int index, int add, int size) {
        while (true) {
            if (size < 2) {
                return add;
            }
            int shift = settings.getGolombRiceShift(size);
            int q = buffer.readUntilZero(pos);
            pos += q + 1;
            long value = (q << shift) | buffer.readNumber(pos, shift);
            pos += shift;
            int oldX = Settings.getUniversalHashIndex(index);
            index += value + 1;
            int x = Settings.getUniversalHashIndex(index);
            if (x != oldX) {
                hashCode = IntHash.universalHash(key, x);
            }
            if (size <= settings.getLeafSize()) {
                int h = Settings.supplementalHash(hashCode, index);
                h = Settings.reduce(h, size);
                return add + h;
            }
            int split = settings.getSplit(size);
            int firstPart, otherPart;
            if (split < 0) {
                firstPart = -split;
                otherPart = size - firstPart;
                split = 2;
            } else {
                firstPart = size / split;
                otherPart = firstPart;
            }
            int h = Settings.supplementalHash(hashCode, index);
            if (firstPart != otherPart) {
                h = Settings.reduce(h, size);
                if (h < firstPart) {
                    size = firstPart;
                    continue;
                }
                pos = skip(pos, firstPart);
                add += firstPart;
                size = otherPart;
                continue;
            }
            h = Settings.reduce(h, split);
            for (int i = 0; i < h; i++) {
                pos = skip(pos, firstPart);
                add += firstPart;
            }
            size = firstPart;
        }
    }
    
    private int skip(int pos, int size) {
        if (size < 2) {
            return pos;
        }
        pos = buffer.skipGolombRice(pos, settings.getGolombRiceShift(size));
        if (size <= settings.getLeafSize()) {
            return pos;
        }
        int split = settings.getSplit(size);
        int firstPart, otherPart;
        if (split < 0) {
            firstPart = -split;
            otherPart = size - firstPart;
            split = 2;
        } else {
            firstPart = size / split;
            otherPart = firstPart;
        }
        int s = firstPart;
        for (int i = 0; i < split; i++) {
            pos = skip(pos, s);
            s = otherPart;
        }
        return pos;
    }
    
	private static int getMinBitCount(int size) {
	    // at least 1.375 bits per key (if it is less, fill with zeroes)
	    return (size  * 11 + 7) >>> 3;
	}
    
}
