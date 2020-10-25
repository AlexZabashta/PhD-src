package tasks.svm;
import static java.lang.Math.exp;
import static java.lang.Math.pow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleBiFunction;

public class Generator {

	public static final List<ToDoubleBiFunction<double[], double[]>> KERNELS = new ArrayList<>();

	static {
		for (int pow = 1; pow <= 3; pow++) {
			for (int delta = 0; delta <= 1; delta++) {

				double costDelta = delta;
				double constPow = pow;

				KERNELS.add(new ToDoubleBiFunction<double[], double[]>() {

					@Override
					public double applyAsDouble(double[] a, double[] b) {
						double sum = costDelta;
						int m = Math.min(a.length, b.length);
						for (int f = 0; f < m; f++) {
							sum += a[f] * b[f];
						}
						return pow(sum, constPow);
					}

					@Override
					public String toString() {
						return "(a*b + " + costDelta + ")^" + constPow;
					}
				});
			}
		}
		KERNELS.add(new ToDoubleBiFunction<double[], double[]>() {

			@Override
			public double applyAsDouble(double[] a, double[] b) {
				double sum = 0;
				int m = Math.min(a.length, b.length);
				for (int f = 0; f < m; f++) {
					sum += Math.abs((a[f] - b[f]));
				}
				return exp(-sum / 2);
			}

			@Override
			public String toString() {
				return "e^(|a-b|_1)";
			}
		});
		KERNELS.add(new ToDoubleBiFunction<double[], double[]>() {

			@Override
			public double applyAsDouble(double[] a, double[] b) {
				double sum = 0;
				int m = Math.min(a.length, b.length);
				for (int f = 0; f < m; f++) {
					double diff = (a[f] - b[f]);
					sum += diff * diff;
				}
				return exp(-sum / 2);
			}

			@Override
			public String toString() {
				return "e^(|a-b|_2)";
			}
		});

		KERNELS.add(new ToDoubleBiFunction<double[], double[]>() {

			@Override
			public double applyAsDouble(double[] a, double[] b) {
				double max = 0;
				int m = Math.min(a.length, b.length);
				for (int f = 0; f < m; f++) {
					max += Math.max(max, Math.abs((a[f] - b[f])));
				}
				return exp(-max / 2);
			}

			@Override
			public String toString() {
				return "e^(|a-b|_inf)";
			}
		});

	}

	public static final int VALID = 256;

	public static TestCase generate(long seed, ToDoubleBiFunction<double[], double[]> kernel) {
		int n = 100;
		int m = 100 + VALID;

		double scale = 10000;

		Random random = new Random(kernel.toString().hashCode() * seed);

		int f = random.nextInt(10) + 3;

		int c = notRandomInt(100000, random);

		double[][] data = new double[m][f];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < f; j++) {
				data[i][j] = random.nextGaussian();
			}
		}

		double[][] matrix = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				matrix[i][j] += kernel.applyAsDouble(data[i], data[j]);
			}
		}

		int k = notRandomInt(n - 10, random) + 10;
		double[] lambda = new double[n];
		for (int i = 0; i < k; i++) {
			lambda[random.nextInt(n)] = (random.nextGaussian());
		}

		double[] score = new double[m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				score[i] += lambda[j] * matrix[i][j];
			}
		}

		double[] sortedValues = score.clone();
		Arrays.sort(sortedValues);

		int mid = m / 2;
		double bias = (sortedValues[mid - 1] + sortedValues[mid]) / 2;

		int[] label = new int[m];

		for (int i = 0; i < m; i++) {
			if (score[i] < bias) {
				label[i] = -1;
			} else {
				label[i] = +1;
			}
		}

		for (int i = 0; i < n; i++) {
			int index = Math.abs(Arrays.binarySearch(sortedValues, score[i]));
			if (Math.abs(mid - 0.5 - index) < n * 0.1 && random.nextBoolean()) {
				label[i] *= -1;
			}
		}

		double sigma = random.nextGaussian() * scale;
		double mu = random.nextGaussian() * scale;

		int[][] rounded = new int[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				long value = Math.round(matrix[i][j] * sigma + mu);
				if (Math.abs(value) > 1_000_000_000) {
					throw new IllegalArgumentException(value + ">10^9");
				}
				rounded[i][j] = (int) value;
			}
		}

		return new TestCase(n, m, rounded, label, c);
	}

	public static int[] randomPermutation(int n, Random random) {
		int[] p = new int[n];

		for (int i = 0; i < n; i++) {
			int j = random.nextInt(i + 1);
			p[i] = p[j];
			p[j] = i;
		}

		return p;
	}

	public static String testName(int test) {
		return (test / 10) + "" + (test % 10);
	}

	public static int notRandomInt(int max, Random random) {
		if (random.nextBoolean()) {
			if (random.nextBoolean()) {
				return random.nextInt(max) + 1;
			} else {
				return Math.min(max, random.nextInt(10) + 1);
			}
		} else {
			return Math.max(1, max - random.nextInt(10));
		}
	}

	public static void writeTest(String prefix, int test, TestCase testCase) throws FileNotFoundException {
		try (PrintWriter writer = new PrintWriter(prefix + testName(test) + ".txt")) {
			writer.println(testCase.n);
			for (int i = 0; i < testCase.n; i++) {
				for (int j = 0; j < testCase.n; j++) {
					writer.print(testCase.kernel[i][j]);
					writer.print(' ');
				}
				writer.println(testCase.label[i]);
			}
			writer.println(testCase.c);
			writer.println(testCase.m - testCase.n);
			for (int i = testCase.n; i < testCase.m; i++) {
				for (int j = 0; j < testCase.n; j++) {
					writer.print(testCase.kernel[i][j]);
					writer.print(' ');
				}
				writer.println(testCase.label[i]);
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {

		String prefix = "tests" + File.separator;
		Random random = new Random(123);

		Solution solution = new Solution();

		int test = 2;

		for (ToDoubleBiFunction<double[], double[]> kernel : KERNELS) {
			int error = 322;
			TestCase testCase = null;
			while (error > 20) {
				testCase = generate(random.nextInt(), kernel);
				error = testCase.errors(solution, Solution.lr, Solution.dec, false);
			}
			System.out.println(test + " " + error);

			writeTest(prefix, test, testCase);
			++test;
		}

	}
}
