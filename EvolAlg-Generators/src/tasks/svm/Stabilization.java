package tasks.svm;
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

	static double cov(int j, double[][][] dataset) {
		double mean = mean(j, dataset);
		double cnt = 0;

		double cov = 0;
		int s = -1;
		for (double[][] subset : dataset) {
			for (double[] object : subset) {
				cov += (object[j] - mean) * s;
				cnt += 1;
			}
			s += 2;

		}
		return cov / cnt;
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
		int m = dataset[0][0].length;

		double[] cor = new double[m];

		for (int j = 0; j < m; j++) {
			cor[j] = cov(j, dataset);
		}

		// System.out.println(Arrays.toString(cor));

		Integer[] order = new Integer[m];
		for (int j = 0; j < m; j++) {
			order[j] = j;
		}
		Arrays.sort(order, Comparator.comparingDouble(i -> cor[i]));

		double[][][] sdataset = new double[k][][];

		for (int c = 0; c < k; c++) {
			int n = dataset[c].length;
			sdataset[c] = new double[n][m];

			Arrays.sort(dataset[c], Comparator.comparingDouble(object -> dot(object, cor)));

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					sdataset[c][i][j] = dataset[c][i][order[j]];
				}
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
		dataset[1] = new double[5][7];

		Stabilization stabilization = new Stabilization();

		fill(dataset);

		print(dataset);

		dataset = stabilization.process(dataset);
		print(dataset);

		dataset = stabilization.process(dataset);
		print(dataset);
	}
}
