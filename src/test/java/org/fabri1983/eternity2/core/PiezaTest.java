package org.fabri1983.eternity2.core;

import org.junit.Assert;
import org.junit.Test;

public class PiezaTest {

	@Test
	public void testRotation() {
		String piezaString = "0 12 16 5";
		short piezaNum = (short) 68;
		
		Pieza p0 = PiezaFactory.from(piezaString, piezaNum);
		
		Pieza p90 = PiezaFactory.from(piezaString, piezaNum);
		Pieza.rotar90(p90);
		
		Pieza p180 = PiezaFactory.from(piezaString, piezaNum);
		Pieza.rotar180(p180);
		
		Pieza p270 = PiezaFactory.from(piezaString, piezaNum);
		Pieza.rotar270(p270);
		
		Pieza pTargetA = PiezaFactory.from(piezaString, piezaNum);
		Pieza.llevarArotacion(pTargetA, (byte) 0);
		assertEquals(p0, pTargetA);
		Pieza.llevarArotacion(pTargetA, (byte) 1);
		assertEquals(p90, pTargetA);
		Pieza.llevarArotacion(pTargetA, (byte) 2);
		assertEquals(p180, pTargetA);
		Pieza.llevarArotacion(pTargetA, (byte) 3);
		assertEquals(p270, pTargetA);
		
		Pieza pTargetB = PiezaFactory.from(piezaString, piezaNum);
		Pieza.llevarArotacion(pTargetB, (byte) 3);
		assertEquals(p270, pTargetB);
		Pieza.llevarArotacion(pTargetB, (byte) 2);
		assertEquals(p180, pTargetB);
		Pieza.llevarArotacion(pTargetB, (byte) 1);
		assertEquals(p90, pTargetB);
		Pieza.llevarArotacion(pTargetB, (byte) 0);
		assertEquals(p0, pTargetB);
		
		Pieza pTargetC = PiezaFactory.from(piezaString, piezaNum);
		Pieza.llevarArotacion(pTargetC, (byte) 3);
		assertEquals(p270, pTargetC);
		Pieza.llevarArotacion(pTargetC, (byte) 0);
		assertEquals(p0, pTargetC);
		Pieza.llevarArotacion(pTargetC, (byte) 1);
		assertEquals(p90, pTargetC);
		Pieza.llevarArotacion(pTargetC, (byte) 2);
		assertEquals(p180, pTargetC);
	}

	@Test
	public void testRotationBenchmark() {
		String piezaString = "0 12 16 5";
		short piezaNum = (short) 68;
		Pieza pTarget = PiezaFactory.from(piezaString, piezaNum);
		
		System.out.print("benchmarking rotation... ");
		int loops=50, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (byte i=0; i < 4; ++i) {
				Pieza.llevarArotacion(pTarget, i);
			}
		}
		long timeBench = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (byte i=0; i < 4; ++i) {
				Pieza.llevarArotacion(pTarget, i);
			}
		}
		long nanosBench = System.nanoTime() - timeBench;
		long nanosPerRot = nanosBench/(4*loops);
		System.out.println("done. " + nanosPerRot + " nanos/rot");
	}
	
	private void assertEquals(Pieza expected, Pieza actual) {
		Assert.assertEquals("top doesn't match", expected.top, actual.top);
		Assert.assertEquals("right doesn't match", expected.right, actual.right);
		Assert.assertEquals("bottom doesn't match", expected.bottom, actual.bottom);
		Assert.assertEquals("left doesn't match", expected.left, actual.left);
	}
}
