package org.fabri1983.eternity2.core.bitset;

/**
 *  This class implements a set of bits that grows as needed. Each bit of the
 *  bit set represents a <code>boolean</code> value. The values of a
 *  <code>SparseBitSet</code> are indexed by non-negative integers.
 *  Individual indexed values may be examined, set, cleared, or modified by
 *  logical operations. One <code>SparseBitSet</code> or logical value may be
 *  used to modify the contents of (another) <code>SparseBitSet</code> through
 *  logical <b>AND</b>, logical <b>inclusive OR</b>, logical <b>exclusive
 *  OR</b>, and <b>And NOT</b> operations over all or part of the bit sets.
 *  <p>
 *  All values in a bit set initially have the value <code>false</code>.
 *  <p>
 *  Every bit set has a current size, which is the number of bits of space
 *  <i>nominally</i> in use by the bit set from the first set bit to just after
 *  the last set bit. The length of the bit set effectively tells the position
 *  available after the last bit of the SparseBitSet.
 *  <p>
 *  The maximum cardinality of a <code>SparseBitSet</code> is
 *  <code>Integer.MAX_VALUE</code>, which means the bits of a
 *  <code>SparseBitSet</code> are labelled <code>
 *  0</code>&nbsp;..&nbsp;<code>Integer.MAX_VALUE&nbsp;&minus;&nbsp;1</code>.
 *  After the last set bit of a <code>SparseBitSet</code>, any attempt to find
 *  a subsequent bit (<i>nextSetBit</i>()), will return an value of &minus;1.
 *  If an attempt is made to use <i>nextClearBit</i>(), and all the bits are
 *  set from the starting position of the search to the bit labelled
 *  <code>Integer.MAX_VALUE&nbsp;&minus;&nbsp;1</code>, then similarly &minus;1
 *  will be returned.
 *  <p>
 *  Unless otherwise noted, passing a null parameter to any of the methods in
 *  a <code>SparseBitSet</code> will result in a
 *  <code>NullPointerException</code>.
 *  <p>
 *  A <code>SparseBitSet</code> is not safe for multi-threaded use without
 *  external synchronization.
 *
 * @author      Bruce K. Haddon
 * @author      Arthur van Hoff
 * @author      Michael McCloskey
 * @author      Martin Buchholz
 * @version     1.0, 2009-03-17
 * @since       1.6
 */
public class SparseBitSet
{
    /*  My apologies for listing all the additional authors, but concepts, code,
        and even comments have been re-used in this class definition from code in
        the JDK that was written and/or maintained by these people. I owe a debt,
        which I acknowledge. But they are in no way responsible for what ever
        misuse I have made of their work.
                                                        Bruce K. Haddon

        The representation of a SparseBitSet is packed into "words", and the words
        are stored in arrays which are here referred to as "blocks." Blocks are
        accessed by two levels of indirection from the master "level 1" (whole)
        set array through second level arrays called "areas" (making the blocks
        "level3"). A "word" is a long, consisting of 64 bits, requiring six address
        bits to select a bit within a word (and this is considered in places to be
        a "level4"). This choice of a long for "word" is determined purely by
        performance concerns, and is built into the implementation in a deep way,
        because the references to blocks are always of the form "long[]." (This
        does not mean that blocks could not be changed to arrays of ints, but it
        would take some extensive work.)

        The fact that there are three levels is also deeply involved in the
        scanning algorithms, meaning that the accesses are always nested three
        deep. Again, the change this might be a large amount of work. On the
        other hand, these three levels have proven, so far, to provide adequate
        speed, and an storage efficient way to deal with sparseness.

        For simplicity, the level3 blocks and the level2 areas are always "full"
        size, i.e., LENGTH3 and LENGTH2 respectively, and for consistency, the
        fourth level is of length LENGTH4. The level1 structure is of variable
        length (as this may save scanning several thousand null pointers, and
        careful consideration must be taken of this at all times, in particular,
        when choosing to increase the size (see resize()). The only place where
        this array is reduced in size is when a clone is made, in which case it is
        created with the smallest size, and allowed to grow as entries are copied
        to it. That all the arrays are kept to power-of-2 sizes is a programming
        convenience (permitting shifts and  masks).

        Whenever possible, a level 3 block that contains no bits (all the words are
        zero, is discarded, and its reference is replaced by a null value.
        Similarly, level 2 areas that contain only null pointers (to level 3
        blocks) are discarded, and their references replaced by null values. This
        is the "normalized" condition, but does not have to be nor is strictly
        enforced. The operations still work if the representation is partially or
        totally "denormalized." In particular, the methods that deal with single
        bits (setting, flipping, clearing, etc.) do not attempt to normalize the
        set, in the interests of speed. However, when a set is scanned as the
        resultant set of some operation, then, in most cases, the set will be
        normalized--the exception being level2 areas that are not completly scanned
        in a particular pass.

        The sizes of the blocks and areas has been the result of some investigation
        with varying sizes, and the sizes selected appear to represent a reasonable
        "sweet" spot. There is, of course, no guarantee that these are the best
        for all possible situations, but, given that not having these the same
        for all bit sets would be hopelessly complex (bad enough as is), these
        values appear to be a fair compromise. */

    /**
     *  The storage for this SparseBitSet. The <i>i</i>th bit is stored in a word
     *  represented by a long value, and is at bit position <code>i % 64</code>
     *  within that word (where bit position 0 refers to the least significant bit
     *  and 63 refers to the most significant bit).
     *  <p>
     *  The words are organized into blocks, and the blocks are accessed by two
     *  additional levels of array indexing.
     */
    private transient long[][][] bits;

    /**
     *  For the current size of the bits array, this is the maximum possible
     *  length of the bit set, i.e., the index of the last possible bit, plus one.
     *  Note: this not the value returned by <i>length</i>().
     * @see #resize(int)
     * @see #length()
     */
    private transient int bitsLength;

    //==============================================================================
    //  The critical parameters. These are set up so that the compiler may
    //  pre-compute all the values as compile-time constants.
    //==============================================================================

    /**
     *  The number of bits in a long value.
     */
    private static final int LENGTH4 = Long.SIZE;

    /**
     *  The number of bits in a positive integer, and the size of permitted index
     *  of a bit in the bit set.
     */
    private static final int INDEX_SIZE = Integer.SIZE - 1;

    /**
     *  The label (index) of a bit in the bit set is essentially broken into
     *  4 "levels". Respectively (from the least significant end), level4, the
     *  address within word, the address within a level3 block, the address within
     *  a level2 area, and the level1 address of that area within the set.
     *
     *  LEVEL4 is the number of bits of the level4 address (number of bits need
     *  to address the bits in a long)
     */
    private static final int LEVEL4 = 6;

    /**
     *  LEVEL3 is the number of bits of the level3 address.
     */
    private static final int LEVEL3 = 5; // Do not change!
    /**
     *  LEVEL2 is the number of bits of the level2 address.
     */
    private static final int LEVEL2 = 5; // Do not change!
    /**
     *  LEVEL1 is the number of bits of the level1 address.
     */
    private static final int LEVEL1 = INDEX_SIZE - LEVEL2 - LEVEL3 - LEVEL4;

    /**
     *  MAX_LENGTH1 is the maximum number of entries in the level1 set array.
     */
    private static final int MAX_LENGTH1 = 1 << LEVEL1;

    /**
     *  LENGTH2 is the number of entries in the any level2 area.
     */
    private static final int LENGTH2 = 1 << LEVEL2;

    /**
     *  LENGTH3 is the number of entries in the any level3 block.
     */
    private static final int LENGTH3 = 1 << LEVEL3;

    /**
     *  The shift to create the word index. (I.e., move it to the right end)
     */
    private static final int SHIFT3 = LEVEL4;

    /**
     *  MASK3 is the mask to extract the LEVEL3 address from a word index
     *  (after shifting by SHIFT3).
     */
    private static final int MASK3 = LENGTH3 - 1;

    /**
     *  SHIFT2 is the shift to bring the level2 address (from the word index) to
     *  the right end (i.e., after shifting by SHIFT3).
     */
    private static final int SHIFT2 = LEVEL3;

    /**
     *  UNIT is the greatest number of bits that can be held in one level1 entry.
     *  That is, bits per word by words per level3 block by blocks per level2 area.
     */
    private static final int UNIT = LENGTH2 * LENGTH3 * LENGTH4;

    /**
     *  MASK2 is the mask to extract the LEVEL2 address from a word index
     *  (after shifting by SHIFT3 and SHIFT2).
     */
    private static final int MASK2 = LENGTH2 - 1;

    /**
     *  SHIFT1 is the shift to bring the level1 address (from the word index) to
     *  the right end (i.e., after shifting by SHIFT3).
     */
    private static final int SHIFT1 = LEVEL2 + LEVEL3;

    /*  Programming notes:

        i, j, and k are used to hold values that are actual bit indices (i.e.,
        the index (label) of the bit within the user's view of the bit set).

        u, v, and w, are used to hold values that refer to the indices of the
        words in the set array that are used to hold the bits (with 64 bits per
        word). These variable names, followed by 1, 2, or 3, refer to the component
        "level" parts of the complete word index.

        word (where used) is a potential entry to or from a block, containing 64
        bits of the bit set. The prefixes a, b, result, etc., refer to the bit
        sets from which these are coming or going. Without a prefix, or with the
        prefix "a," the set in question is "this" set (see next paragraph).

        Operations are conceived to be in the form a.op(b), thus in the discussion
        (not in the public Javadoc documentation) the two sets are referred to a
        "a" and "b", where the set referred to by "this" is usually set a.
        Hence, reference to set a is usually implicit, but set b will usually be
        explicit. Variables beginning with these letters hold values relevant to
        the corresponding set, and, in particular, these letters followed by
        1, 2, and 3 are used to refer to the corresponding (current) level1,
        level3 area, and level3 block, arrays.

        The resizing of the table takes place as necessary. In this regard, it is
        worth noting that the table is grown, but never shrunk (except in a new
        object formed by cloning).

        Similarly, care it taken to ensure that any supplied reference to a bit
        set (other than this) has an opportunity to fail for being null before
        any other set (including this) has its state changed. For the most
        part, this is allowed to happen "naturally," but the Strategies incorporate
        an explicit check when necessary.

        There is a amount of (almost) repetitive scanning code in many of the
        "single bit" methods. The intent is that these methods for SparseBitSet be
        as small and as fast as possible.

        For the scanning of complete sets, or for ranges within complete sets,
        all of the scanning logic is built into one (somewhat enormous) method,
        setScanner(). This contains all the considerations for matching up
        corresponding level 3 blocks (if they exist), and then uses a Strategy
        object to do the processing on those level3 blocks. This keeps all
        the scanning and optimization logic in one place, and the Strategies are
        reasonably simple (see the definition of AbstractStrategy for a discussion
        of the tasks that must be defined therein).

        The test for index i (the first index in all cases) being in range is
        rather perverse, but the idea was to keep the actual number of comparisons
        to a minimum, hence the check is for "(i + 1) < 1". This is almost but not
        quite equivalent to "i < 0", although it is for all values of i except
        i=Integer.MAX_VALUE. In this latter case, (i + 1) "overflows" to
        -(Integer.MAX_VALUE + 1), and thus appears to be less than 1, and thus the
        check picks up the other disallowed case. Let us hope the compiler never
        gets smart enough to try to do the apparent optimization! */


    /**
     *  Constructs an empty bit set with the default initial size.
     *  Initially all bits are effectively <code>false</code>.
     *
     * @since       1.6
     */
    public SparseBitSet()
    {
        /*   By requesting 1 bit, will actually get UNIT number of bits. */
        this(1);
    }

    /**
     *  Creates a bit set whose initial size is large enough to efficiently
     *  represent bits with indices in the range <code>0</code> through
     *  at least <code>nbits-1</code>. Initially all bits are effectively
     *  <code>false</code>.
     *  <p>
     *  No guarantees are given for how large or small the actual object will be.
     *  The setting of bits above the given range is permitted (and will perhaps
     *  eventually cause resizing).
     *
     * @param       nbits the initial provisional length of the SparseBitSet
     * @throws      java.lang.NegativeArraySizeException if the specified initial
     *              length is negative
     * @see         #SparseBitSet()
     * @since       1.6
     */
    public SparseBitSet(int nbits) throws NegativeArraySizeException
    {
    	/*  Array size is computed based on this being a capacity given in bits. */
        if (nbits < 0) // capacity can't be negative -- could only come from
            throw new NegativeArraySizeException( //  an erroneous user given
                                                 "(requested capacity=" + nbits + ") < 0"); // nbits value
        resize(nbits - 1); //  Resize takes last usable index
    }

    /**
     *  Sets the bit at the specified index to <code>false</code>.
     *
     * @param       i a bit index.
     * @exception   IndexOutOfBoundsException if the specified index is negative
     *              or equal to Integer.MAX_VALUE.
     * @since       1.6
     */
    public void clear(int i)
    {
        /*  In the interests of speed, no check is made here on whether the
            level3 block goes to all zero. This may be found and corrected
            in some later operation. */
        if ((i + 1) < 1)
            throw new IndexOutOfBoundsException("i=" + i);
        if (i >= bitsLength)
            return;
        final int w = i >> SHIFT3;
        long[][] a2;
        if ((a2 = bits[w >> SHIFT1]) == null)
            return;
        long[] a3;
        if ((a3 = a2[(w >> SHIFT2) & MASK2]) == null)
            return;
        a3[w & MASK3] &= ~(1L << i); //  Clear the indicated bit
    }

    /**
     *  Sets all of the bits in this <code>SparseBitSet</code> to
     *  <code>false</code>.
     *
     * @since       1.6
     */
    public void clear()
    {
        /*  This simply resets to null all the entries in the set. */
        nullify(0);
    }

    /**
     *  Returns the value of the bit with the specified index. The value is
     *  <code>true</code> if the bit with the index <code>i</code> is currently set
     *  in this <code>SparseBitSet</code>; otherwise, the result is
     *  <code>false</code>.
     *
     * @param       i the bit index
     * @return      the boolean value of the bit with the specified index.
     * @exception   IndexOutOfBoundsException if the specified index is negative
     *              or equal to Integer.MAX_VALUE
     * @since       1.6
     */
    public boolean get(int i)
    {
        if ((i + 1) < 1)
            throw new IndexOutOfBoundsException("i=" + i);
        final int w = i >> SHIFT3;

        long[][] a2;
        long[] a3;
        return i < bitsLength && (a2 = bits[w >> SHIFT1]) != null
                && (a3 = a2[(w >> SHIFT2) & MASK2]) != null
                && ((a3[w & MASK3] & (1L << i)) != 0);
    }

    /**
     *  Returns the index of the first bit that is set to <code>false</code> that
     *  occurs on or after the specified starting index.
     *
     * @param       i the index to start checking from (inclusive)
     * @return      the index of the next clear bit, or -1 if there is no such bit
     * @exception   IndexOutOfBoundsException if the specified index is negative
     * @since       1.6
     */
    public int nextClearBit(int i)
    {
        /*  The index of this method is permitted to be Integer.MAX_VALUE, as this
            is needed to make this method work together with the method
            nextSetBit()--as might happen if a search for the next clear bit is
            started after finding a set bit labelled Integer.MAX_VALUE-1. This
            case is not optimised, the code will eventually return -1 (since
            the Integer.MAX_VALUEth bit does "exist," and is 0. */

        if (i < 0)
            throw new IndexOutOfBoundsException("i=" + i);
        /*  This is the word from which the search begins. */
        int w = i >> SHIFT3;
        int w3 = w & MASK3;
        int w2 = (w >> SHIFT2) & MASK2;
        int w1 = w >> SHIFT1;

        long nword = ~0L << i;
        final int aLength = bits.length;

        long[][] a2;
        long[] a3;
        /*  Is the next clear bit in the same word at the nominated beginning bit
        (including the nominated beginning bit itself). The first check is
        whether the starting bit is within the structure at all. */
        if (w1 < aLength && (a2 = bits[w1]) != null
                && (a3 = a2[w2]) != null
                && ((nword = ~a3[w3] & (~0L << i))) == 0L)
        {
            /*  So now start a search though the rest of the entries for
                a null area or block, or a clear bit (a set bit in the
                complemented value). */
            ++w;
            w3 = w & MASK3;
            w2 = (w >> SHIFT2) & MASK2;
            w1 = w >> SHIFT1;
            nword = ~0L;
            loop: for (; w1 != aLength; ++w1)
            {
                if ((a2 = bits[w1]) == null)
                    break;
                for (; w2 != LENGTH2; ++w2)
                {
                    if ((a3 = a2[w2]) == null)
                        break loop;
                    for (; w3 != LENGTH3; ++w3)
                        if ((nword = ~a3[w3]) != 0)
                            break loop;
                    w3 = 0;
                }
                w2 = w3 = 0;
            }
        }
        final int result = (((w1 << SHIFT1) + (w2 << SHIFT2) + w3) << SHIFT3)
                + Long.numberOfTrailingZeros(nword);
        return (result == Integer.MAX_VALUE ? -1 : result);
    }

    /**
     *  Returns the index of the first bit that is set to <code>true</code> that
     *  occurs on or after the specified starting index. If no such it exists then
     *  -1 is returned.
     *  <p>
     *  To iterate over the <code>true</code> bits in a <code>SparseBitSet
     *  sbs</code>, use the following loop:
     *
     *  <pre>
     *  for( int i = sbbits.nextSetBit(0); i &gt;= 0; i = sbbits.nextSetBit(i+1) )
     *  {
     *      // operate on index i here
     *  }</pre>
     *
     * @param       i the index to start checking from (inclusive)
     * @return      the index of the next set bit
     * @exception   IndexOutOfBoundsException if the specified index is negative
     * @since       1.6
     */
    public int nextSetBit(int i)
    {
        /*  The index value (i) of this method is permitted to be Integer.MAX_VALUE,
            as this is needed to make the loop defined above work: just in case the
            bit labelled Integer.MAX_VALUE-1 is set. This case is not optimised:
            but eventually -1 will be returned, as this will be included with
            any search that goes off the end of the level1 array. */

        if (i < 0)
            throw new IndexOutOfBoundsException("i=" + i);
        /*  This is the word from which the search begins. */
        int w = i >> SHIFT3;
        int w3 = w & MASK3;
        int w2 = (w >> SHIFT2) & MASK2;
        int w1 = w >> SHIFT1;

        long word = 0L;
        final int aLength = bits.length;

        long[][] a2;
        long[] a3;
        /*  Is the next set bit in the same word at the nominated beginning bit
        (including the nominated beginning bit itself). The first check is
        whether the starting bit is within the structure at all. */
        if (w1 < aLength && ((a2 = bits[w1]) == null
                || (a3 = a2[w2]) == null
                || ((word = a3[w3] & (~0L << i)) == 0L)))
        {
            /*  So now start a search though the rest of the entries for a bit. */
            ++w;
            w3 = w & MASK3;
            w2 = (w >> SHIFT2) & MASK2;
            w1 = w >> SHIFT1;
            major: for (; w1 != aLength; ++w1)
            {
                if ((a2 = bits[w1]) != null)
                    for (; w2 != LENGTH2; ++w2)
                    {
                        if ((a3 = a2[w2]) != null)
                            for (; w3 != LENGTH3; ++w3)
                                if ((word = a3[w3]) != 0)
                                    break major;
                        w3 = 0;
                    }
                w2 = w3 = 0;
            }
        }
        return (w1 >= aLength ? -1
                : (((w1 << SHIFT1) + (w2 << SHIFT2) + w3) << SHIFT3)
                        + Long.numberOfTrailingZeros(word));
    }

    /**
     *  Sets the bit at the specified index.
     *
     * @param       i a bit index
     * @exception   IndexOutOfBoundsException if the specified index is negative
     *              or equal to Integer.MAX_VALUE
     * @since       1.6
     */
    public void set(int i)
    {
        if ((i + 1) < 1)
            throw new IndexOutOfBoundsException("i=" + i);
        final int w = i >> SHIFT3;
        final int w1 = w >> SHIFT1;
        final int w2 = (w >> SHIFT2) & MASK2;

        if (i >= bitsLength)
            resize(i);
        long[][] a2;
        if ((a2 = bits[w1]) == null)
            a2 = bits[w1] = new long[LENGTH2][];
        long[] a3;
        if ((a3 = a2[w2]) == null)
            a3 = a2[w2] = new long[LENGTH3];
        a3[w & MASK3] |= 1L << i;
    }

    /**
     *  Sets the bit at the specified index to the specified value.
     *
     * @param       i a bit index
     * @param       value a boolean value to set
     * @exception   IndexOutOfBoundsException if the specified index is negative
     *              or equal to Integer.MAX_VALUE
     * @since       1.6
     */
    public void set(int i, boolean value)
    {
        if (value)
            set(i);
        else
            clear(i);
    }

    /**
     *  Clear out a part of the set array with nulls, from the given start to the
     *  end of the array. If the given parameter is beyond the end of the bits
     *  array, nothing is changed.
     *
     * @param       start word index at which to start (inclusive)
     * @since       1.6
     */
    private final void nullify(int start)
    {
        final int aLength = bits.length;
        if (start < aLength)
        {
            for (int w = start; w != aLength; ++w)
                bits[w] = null;
        }
    }

    /**
     *  Resize the bit array. Moves the entries in the the bits array of this
     *  SparseBitSet into an array whose size (which may be larger or smaller) is
     *  the given bit size (<i>i.e.</i>, includes the bit whose index is one less
     *  that the given value). If the new array is smaller, the excess entries in
     *  the set array are discarded. If the new array is bigger, it is filled with
     *  nulls.
     *
     * @param       index the desired address to be included in the set
     * @since       1.6
     */
    private final void resize(int index)
    {
        /*  Find an array size that is a power of two that is as least as large
            enough to contain the index requested. */
        final int w1 = (index >> SHIFT3) >> SHIFT1;
        int newSize = Integer.highestOneBit(w1);
        if (newSize == 0)
            newSize = 1;
        if (w1 >= newSize)
            newSize <<= 1;
        if (newSize > MAX_LENGTH1)
            newSize = MAX_LENGTH1;
        final int aLength1 = (bits != null ? bits.length : 0);

        if (newSize != aLength1 || bits == null)
        { // only if the size needs to be changed
            final long[][][] temp = new long[newSize][][]; //  Get the new array
            if (aLength1 != 0)
            {
                /*  If it exists, copy old array to the new array. */
                System.arraycopy(bits, 0, temp, 0, Math.min(aLength1, newSize));
                nullify(0); //  Don't leave unused pointers around. */
            }
            bits = temp; //  Set new array as the set array
            bitsLength = //  Index of last possible bit, plus one.
            (newSize == MAX_LENGTH1 ? Integer.MAX_VALUE : newSize * UNIT);
        }
    }

}

