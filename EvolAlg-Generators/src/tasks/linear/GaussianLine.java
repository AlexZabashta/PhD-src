package tasks.linear;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GaussianLine {
	static final int root = 12;
	static final int size = root * root;
	static final int elen = size * 25;
	static final int n = 4096;
	static final int v = 256;
	static final int m = 64;

	static double process() {

		int f = 2 * m;
		int[] fp = new int[f];
		int[] scale = new int[f];
		int[] shift = new int[f];
		Random random = new Random();
		double noize = random.nextDouble();
		for (int i = 0; i < f; i++) {
			int j = random.nextInt(i + 1);
			fp[i] = fp[j];
			fp[j] = i;
			scale[i] = random.nextInt(100) + 1;
			shift[i] = random.nextInt(2000001) - 1000000;
		}

		int[][] train = new int[n][f + 1];
		int[][] test = new int[v][f + 1];

		double[] eq = new double[m + 1];
		for (int j = 0; j <= m; j++) {
			eq[j] = random.nextGaussian();
		}

		for (int i = 0; i < n; i++) {
			int y = (int) Math.round((eq[m] + random.nextGaussian() * noize) * 1000);
			for (int j = 0; j < m; j++) {
				y += train[i][fp[j]] = (int) Math.round(random.nextGaussian() * 1000);

			}
			train[i][f] = y;
		}

		for (int i = 0; i < v; i++) {
			int y = (int) Math.round((eq[m]) * 1000);
			for (int j = 0; j < m; j++) {
				y += test[i][fp[j]] = (int) Math.round(random.nextGaussian() * 1000);

			}
			test[i][f] = y;
		}

		for (int j = m + 8; j < f; j++) {
			int x = random.nextInt(m);
			int y = random.nextInt(m);

			for (int i = 0; i < n; i++) {
				train[i][fp[j]] = train[i][fp[x]] + train[i][fp[y]];
			}

			for (int i = 0; i < v; i++) {
				test[i][fp[j]] = test[i][fp[x]] + test[i][fp[y]];
			}
		}

		for (int j = 0; j < f; j++) {
			for (int i = 0; i < n; i++) {
				train[i][j] *= scale[j];
				train[i][j] += shift[j];
			}

			for (int i = 0; i < v; i++) {
				test[i][j] *= scale[j];
				test[i][j] += shift[j];
			}

		}

		LRSolution naive = new SolutionMSE();
		LRSolution smape = new SolutionSMAPE();

		double[] ts = smape.solve(n, f, train);
		double[] ns = naive.solve(n, f, train);

		double tr = SMAPE.calc(v, f, ts, test);
		double nr = SMAPE.calc(v, f, ns, test);

		double error = tr - nr;

		return error;

	}

	public static void main(String[] args) {

		final ExecutorService executor = Executors.newFixedThreadPool(10);

		Object mutex = new Object();

		int rep = 10;

		for (int i = 0; i < rep; i++) {
			executor.submit(() -> {

				double best = Double.POSITIVE_INFINITY;
				double[] errorLog = new double[elen];

				for (int iteration = 0; iteration < elen; iteration++) {
					best = Math.min(best, process());
					errorLog[iteration] = best;
				}

				synchronized (mutex) {
					try (PrintWriter writer = new PrintWriter(System.currentTimeMillis() + "log.txt")) {
						for (double val : errorLog) {
							writer.println(val);
						}
						Thread.sleep(10);
					} catch (FileNotFoundException | InterruptedException e) {
						e.printStackTrace();
					}
				}

			});
		}

		executor.shutdown();

		System.out.println(process());
	}
}
