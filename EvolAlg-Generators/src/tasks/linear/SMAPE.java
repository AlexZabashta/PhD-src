package tasks.linear;

public class SMAPE {
	public static double calc(int k, int m, double[] a, int[][] xy) {

		for (double val : a) {
			if (Double.isInfinite(val)) {
				return 2;
			}

			if (Double.isNaN(val)) {
				return 2;
			}

			if (Math.abs(val) > 1e18) {
				return 2;
			}
		}

		double sum = 0;

		for (int i = 0; i < k; i++) {
			double result = a[m];
			for (int j = 0; j < m; j++) {
				result += a[j] * xy[i][j];
			}
			double expected = xy[i][m];
			double num = Math.abs(expected - result);
			double den = Math.abs(expected) + Math.abs(result);

			if (den < 1e-9) {
				if (num < 1e-9) {
					sum += 0;
				} else {
					sum += 1;
				}
			} else {
				sum += num / den;
			}
		}

		return sum / k;

	}

	public static void main(String[] args) {
//		double[] x = { 30.9381215619778, -60295.28400816625 };
		double[] x = { 31, -60420 };

		int k = 1024;

		int m = x.length - 1;
		int[][] data = new int[k][m + 1];

		for (int i = 0; i < k; i++) {
			if (i % 2 == 0) {
				data[i][0] = 2015;
				data[i][1] = 2045;
			} else {
				data[i][0] = 2016;
				data[i][1] = 2076;
			}
		}

		System.out.println(calc(2, 1, x, data));

	}
}
