package org.fabri1983.eternity2.core.testutil;

import java.util.Random;

/*
	See the rationale for BlackholeL1..BlackholeL4 classes below.
*/

abstract class BlackholeL0 {

}

abstract class BlackholeL1 extends BlackholeL0 {

}

abstract class BlackholeL2 extends BlackholeL1 {
    public volatile byte b1;
    public volatile boolean bool1;
    public volatile char c1;
    public volatile short s1;
    public volatile int i1;
    public volatile long l1;
    public volatile float f1;
    public volatile double d1;
    public byte b2;
    public boolean bool2;
    public char c2;
    public short s2;
    public int i2;
    public long l2;
    public float f2;
    public double d2;
    public volatile Object obj1;
    public volatile Object[] objs1;
    public volatile BlackholeL2 nullBait = null;
    public int tlr;
    public volatile int tlrMask;

    public BlackholeL2() {
        Random r = new Random(System.nanoTime());
        tlr = r.nextInt();
        tlrMask = 1;
        obj1 = new Object();
        objs1 = new Object[]{new Object()};

        b1 = (byte) r.nextInt(); b2 = (byte) (b1 + 1);
        bool1 = r.nextBoolean(); bool2 = !bool1;
        c1 = (char) r.nextInt(); c2 = (char) (c1 + 1);
        s1 = (short) r.nextInt(); s2 = (short) (s1 + 1);
        i1 = r.nextInt(); i2 = i1 + 1;
        l1 = r.nextLong(); l2 = l1 + 1;
        f1 = r.nextFloat(); f2 = f1 + Math.ulp(f1);
        d1 = r.nextDouble(); d2 = d1 + Math.ulp(d1);

        if (b1 == b2) {
            throw new IllegalStateException("byte tombstones are equal");
        }
        if (bool1 == bool2) {
            throw new IllegalStateException("boolean tombstones are equal");
        }
        if (c1 == c2) {
            throw new IllegalStateException("char tombstones are equal");
        }
        if (s1 == s2) {
            throw new IllegalStateException("short tombstones are equal");
        }
        if (i1 == i2) {
            throw new IllegalStateException("int tombstones are equal");
        }
        if (l1 == l2) {
            throw new IllegalStateException("long tombstones are equal");
        }
        if (f1 == f2) {
            throw new IllegalStateException("float tombstones are equal");
        }
        if (d1 == d2) {
            throw new IllegalStateException("double tombstones are equal");
        }
    }
}

abstract class BlackholeL3 extends BlackholeL2 {

}

abstract class BlackholeL4 extends BlackholeL3 {

}

/**
 * Black Hole.
 *
 * <p>Black hole "consumes" the values, conceiving no information to JIT whether the
 * value is actually used afterwards. This can save from the dead-code elimination
 * of the computations resulting in the given values.</p>
 */
public class Blackhole extends BlackholeL4 {


    public Blackhole() {
    }

    /*
     * Need to clear the sinks to break the GC from keeping the
     * consumed objects forever.
     */
    public void clearSinks() {
        obj1 = new Object();
        objs1 = new Object[]{new Object()};
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param obj object to consume.
     */
    public final void consume(Object obj) {
        int tlrMask = this.tlrMask; // volatile read
        int tlr = (this.tlr = (this.tlr * 1664525 + 1013904223));
        if ((tlr & tlrMask) == 0) {
            // SHOULD ALMOST NEVER HAPPEN IN MEASUREMENT
            this.obj1 = obj;
            this.tlrMask = (tlrMask << 1) + 1;
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param objs objects to consume.
     */
    public final void consume(Object[] objs) {
        int tlrMask = this.tlrMask; // volatile read
        int tlr = (this.tlr = (this.tlr * 1664525 + 1013904223));
        if ((tlr & tlrMask) == 0) {
            // SHOULD ALMOST NEVER HAPPEN IN MEASUREMENT
            this.objs1 = objs;
            this.tlrMask = (tlrMask << 1) + 1;
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param b object to consume.
     */
    public final void consume(byte b) {
        byte b1 = this.b1; // volatile read
        byte b2 = this.b2;
        if (b == b1 & b == b2) {
            // SHOULD NEVER HAPPEN
            nullBait.b1 = b; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param bool object to consume.
     */
    public final void consume(boolean bool) {
        boolean bool1 = this.bool1; // volatile read
        boolean bool2 = this.bool2;
        if (bool == bool1 & bool == bool2) {
            // SHOULD NEVER HAPPEN
            nullBait.bool1 = bool; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param c object to consume.
     */
    public final void consume(char c) {
        char c1 = this.c1; // volatile read
        char c2 = this.c2;
        if (c == c1 & c == c2) {
            // SHOULD NEVER HAPPEN
            nullBait.c1 = c; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param s object to consume.
     */
    public final void consume(short s) {
        short s1 = this.s1; // volatile read
        short s2 = this.s2;
        if (s == s1 & s == s2) {
            // SHOULD NEVER HAPPEN
            nullBait.s1 = s; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param i object to consume.
     */
    public final void consume(int i) {
        int i1 = this.i1; // volatile read
        int i2 = this.i2;
        if (i == i1 & i == i2) {
            // SHOULD NEVER HAPPEN
            nullBait.i1 = i; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param l object to consume.
     */
    public final void consume(long l) {
        long l1 = this.l1; // volatile read
        long l2 = this.l2;
        if (l == l1 & l == l2) {
            // SHOULD NEVER HAPPEN
            nullBait.l1 = l; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param f object to consume.
     */
    public final void consume(float f) {
        float f1 = this.f1; // volatile read
        float f2 = this.f2;
        if (f == f1 & f == f2) {
            // SHOULD NEVER HAPPEN
            nullBait.f1 = f; // implicit null pointer exception
        }
    }

    /**
     * Consume object. This call provides a side effect preventing JIT to eliminate dependent computations.
     *
     * @param d object to consume.
     */
    public final void consume(double d) {
        double d1 = this.d1; // volatile read
        double d2 = this.d2;
        if (d == d1 & d == d2) {
            // SHOULD NEVER HAPPEN
            nullBait.d1 = d; // implicit null pointer exception
        }
    }

}

