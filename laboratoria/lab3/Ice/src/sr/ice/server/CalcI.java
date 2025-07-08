package sr.ice.server;

import Demo.A;
import Demo.Calc;
import Demo.EmptySeq;
import com.zeroc.Ice.Current;

import java.util.Arrays;

public class CalcI implements Calc {
	private static final long serialVersionUID = -2448962912780867770L;
	long counter = 0;

	@Override
	public long add(int a, int b, Current __current) {
		System.out.println("ADD: a = " + a + ", b = " + b + ", result = " + (a + b));

		// punkt 2.2.10
		System.out.println("Call from client " + __current.id);

		if (a > 1000 || b > 1000) {
			try {
				Thread.sleep(6000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		if (__current.ctx.values().size() > 0) {
			System.out.println("There are some properties in the context");
		}

		return a + b;
	}

	// punkt 2.2.8 - uzupełniłem implementację
	@Override
	public long subtract(int a, int b, Current __current) {
		System.out.println("SUBTRACT: a = " + a + ", b = " + b + ", result = " + (a - b));

		// punkt 2.2.10
		System.out.println("Call from client " + __current.id);

		if (a > 1000 || b > 1000) {
			try {
				Thread.sleep(6000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		if (__current.ctx.values().size() > 0) {
			System.out.println("There are some properties in the context");
		}

		return a - b;
	}

	// punkt 2.2.14
	@Override
	public float avg(long[] s, Current __current) throws EmptySeq {
		if(s.length == 0) {
			throw new EmptySeq();
		}

		float average = (float)Arrays.stream(s).average().getAsDouble();

		System.out.println("AVG: s = " + Arrays.toString(s) + ", result = " + average);

		System.out.println("Call from client " + __current.id);

		if (__current.ctx.values().size() > 0) {
			System.out.println("There are some properties in the context");
		}

		return average;
	}


	@Override
	public /*synchronized*/ void op(A a1, short b1, Current current) {
		System.out.println("OP" + (++counter));
		try {
			Thread.sleep(500);
		} catch (java.lang.InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}