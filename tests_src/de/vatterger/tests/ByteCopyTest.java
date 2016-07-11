package de.vatterger.tests;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.math.MathUtils;

import de.vatterger.engine.util.Profiler;

public class ByteCopyTest {
	public static void main(String[] args) {
		byte[][] target = new byte[10000][32];
		for (int r = 0; r < 100; r++) {
			byte[] bytes0 = new byte[32];
			byte[] bytes1 = new byte[32];

			for (int i = 0; i < bytes0.length; i++) {
				bytes0[i] = (byte) MathUtils.random((int) Byte.MAX_VALUE);
				bytes1[i] = (byte) MathUtils.random((int) Byte.MAX_VALUE);
			}

			Profiler p = new Profiler("copy - "+r, TimeUnit.MICROSECONDS);

			for (int i = 0; i < target.length; i++) {
				target[i] = Arrays.copyOf(MathUtils.randomBoolean() ? bytes0 : bytes1, 32);
			}

			p.log();
		}
	}
}
