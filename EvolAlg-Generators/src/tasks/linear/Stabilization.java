package tasks.linear;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

public class Stabilization {

	static double mean(int j, double[][][] dataset) {
		double mean = 0;
		double cnt = 0;
		for (double[][] subset : dataset) {
			for (double[] object : subset) {
				mean += object[j];
				cnt += 1;
			}
		}
		return mean / cnt;
	}

	static double cov(int x, int y, double[][][] dataset) {
		double meanX = mean(x, dataset);
		double meanY = mean(y, dataset);
		double cnt = 0;

		double mean2 = 0;
		for (double[][] subset : dataset) {
			for (double[] object : subset) {
				mean2 += (object[x] - meanX) * (object[y] - meanY);
				cnt += 1;
			}
		}
		return mean2 / cnt;
	}

	static double dot(double[] object, double[] cor) {
		double sum = 0;
		for (int j = 0; j < cor.length; j++) {
			sum += object[j] * cor[j];
		}
		return sum;
	}

	public double[][][] process(double[][][] dataset) {
		int k = dataset.length;
		int m = dataset[0][0].length - 1;

		double[] cor = new double[m];

		for (int j = 0; j < m; j++) {
			cor[j] = cov(j, m, dataset);
		}

		// System.out.println(Arrays.toString(cor));

		Integer[] forder = new Integer[m];
		for (int j = 0; j < m; j++) {
			forder[j] = j;
		}
		Arrays.sort(forder, Comparator.comparingDouble(i -> cor[i]));
		// System.out.println(Arrays.toString(forder));

		double[][][] sdataset = new double[k][][];

		for (int c = 0; c < k; c++) {
			int n = dataset[c].length;
			sdataset[c] = new double[n][m + 1];

			Arrays.sort(dataset[c], Comparator.comparingDouble(object -> object[m]));

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					sdataset[c][i][j] = dataset[c][i][forder[j]];
				}
				sdataset[c][i][m] = dataset[c][i][m];
			}

		}

		return sdataset;

	}

	public static void print(double[][][] dataset) {
		for (double[][] subset : dataset) {
			for (double[] object : subset) {
				for (double val : object) {
					System.out.printf(Locale.ENGLISH, "%5.2f ", val);

				}
				System.out.println();
			}
			System.out.println();
		}
		System.out.println();
	}

	public static void fill(double[][][] dataset) {
		Random random = new Random(0);
		for (double[][] subset : dataset) {
			for (double[] object : subset) {
				for (int i = 0; i < object.length; i++) {
					object[i] = random.nextGaussian();
				}
			}
		}
	}

	public static void main(String[] args) {

		double[][][] dataset = new double[2][][];

		dataset[0] = new double[5][7];
		dataset[1] = new double[2][7];

		Stabilization stabilization = new Stabilization();

		fill(dataset);

		print(dataset);

		dataset = stabilization.process(dataset);
		print(dataset);

		dataset = stabilization.process(dataset);
		print(dataset);
	}
}
