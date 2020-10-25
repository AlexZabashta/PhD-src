package tasks.svm;
import java.util.Arrays;

public class TestCase {
	final int n, m;
	final int[][] kernel;
	final int[] label;
	final int c;

	public TestCase(int n, int m, int[][] kernel, int[] label, int c) {
		this.n = n;
		this.m = m;
		this.kernel = kernel;
		this.label = label;
		this.c = c;
	}

	public double fMeasure(int numClasses, int[][] confusionMatrix) {
		double[] classDistribution = new double[numClasses];

		double f1 = 0;

		int sumC = 0;
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				sumC += confusionMatrix[i][j];
				classDistribution[i] += confusionMatrix[i][j];
			}
		}

		for (int c = 0; c < numClasses; c++) {
			classDistribution[c] /= sumC;
		}

		for (int c = 0; c < numClasses; c++) {

			double localF1;

			if (confusionMatrix[c][c] == 0) {
				localF1 = 0.0;
			} else {
				double sumRow = 0;
				for (int j = 0; j < numClasses; j++) {
					sumRow += confusionMatrix[c][j];
				}

				double sumCol = 0;
				for (int i = 0; i < numClasses; i++) {
					sumCol += confusionMatrix[i][c];
				}

				double precision = confusionMatrix[c][c] / sumRow;
				double recall = confusionMatrix[c][c] / sumCol;
				localF1 = 2 * precision * recall / (precision + recall);
			}

			f1 += classDistribution[c] * localF1;
		}

		return f1;
	}

	double fscore(double[] lambda) {

		int[][] cm = new int[2][2];

		for (int i = n; i < m; i++) {
			double sum = lambda[n];
			for (int j = 0; j < n; j++) {
				sum += lambda[j] * kernel[i][j] * label[j];
			}

			if (label[i] < 0) {
				if (sum >= 0) {
					cm[0][1] += 1;
				} else {
					cm[0][0] += 1;
				}
			} else {
				if (sum <= 0) {
					cm[1][0] += 1;
				} else {
					cm[1][1] += 1;
				}
			}
		}

		// System.out.println(n + " " + m);
		// System.out.println(Arrays.deepToString(cm));

		return fMeasure(2, cm);
	}

	double margin(double[] lambda) {
		double error = 0;
		for (int i = n; i < m; i++) {
			double sum = lambda[n];
			for (int j = 0; j < n; j++) {
				sum += lambda[j] * kernel[i][j] * label[j];
			}

			// System.out.println(sum + " " + label[i]);

			error += Math.max(0, -sum * label[i]);

		}
		return error;
	}

	int errors(double[] lambda) {
		int errors = 0;
		for (int i = n; i < m; i++) {
			double sum = lambda[n];
			for (int j = 0; j < n; j++) {
				sum += lambda[j] * kernel[i][j] * label[j];
			}

			// System.out.println(sum + " " + label[i]);

			if (sum * label[i] <= 0) {
				++errors;
			}
		}
		return errors;
	}

	double[] lambda(Solution solution, double lr, double dec, boolean baseline) {
		return solution.solve(n, kernel, label, c, lr, dec, baseline);
	}

	int errors(Solution solution, double lr, double dec, boolean baseline) {
		return errors(lambda(solution, lr, dec, baseline));
	}

	double margin(Solution solution, double lr, double dec, boolean baseline) {
		return margin(lambda(solution, lr, dec, baseline));
	}

	double fscore(Solution solution, double lr, double dec, boolean baseline) {
		return fscore(lambda(solution, lr, dec, baseline));
	}

	public double errorRate(Solution solution, double lr, double dec, boolean baseline) {
		return 1.0 * errors(solution, lr, dec, baseline) / (m - n);
	}
}
