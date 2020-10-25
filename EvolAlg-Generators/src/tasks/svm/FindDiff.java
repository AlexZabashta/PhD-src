package tasks.svm;
import static java.lang.Math.exp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.ToDoubleBiFunction;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.randomsearch.RandomSearch;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization.StandardPSO2011;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

public class FindDiff {

	static final int root = 16;
	static final int size = root * root;
	static final int elen = size * 25;

	static final int v = 100;
	static final int n = v * 2;
	static final int d = n * 2;
	static final int m = 10;

	public static void main(String[] args) throws FileNotFoundException {

		int kid = 3;

		// ToDoubleBiFunction<double[], double[]> kernel = Generator.KERNELS.get(kid);

		ToDoubleBiFunction<double[], double[]> kernel = new ToDoubleBiFunction<double[], double[]>() {

			@Override
			public double applyAsDouble(double[] a, double[] b) {
				double sum = 0;
				for (int f = 0; f < m; f++) {
					double diff = (a[f] - b[f]);
					sum += diff * diff;
				}
				return exp(-sum / 1.61);
			}
		};

		DoubleProblem problem = new DoubleProblem() {

			double best = Double.POSITIVE_INFINITY;
			int iteration = 0;
			double[] errorLog = new double[elen];
			// int[][][] bestDataset = null;

			@Override
			public int getNumberOfObjectives() {
				return 3;
			}

			@Override
			public int getNumberOfConstraints() {
				return 0;
			}

			@Override
			public String getName() {
				return "SVM";
			}

			@Override
			public int getNumberOfVariables() {
				return d * m + 1;
			}

			@Override
			public void evaluate(DoubleSolution solution) {

				Random random = new Random();
				int[] order = new int[n];
				for (int i = 0; i < n; i++) {
					int j = random.nextInt(i + 1);
					order[i] = order[j];
					order[j] = i;
				}

				int c = (int) Math.round((solution.getVariableValue(0) - getLowerBound(0)) * 100);

				int[] label = new int[d];
				for (int i = 0; i < d; i++) {
					label[i] = ((i / v) % 2) * 2 - 1;
				}

				int sp = 1;

				double[][] dataset = new double[d][m];

				for (int i = 0; i < n; i++) {
					int p = order[i];
					if (p < v) {
						for (int j = 0; j < m; j++) {
							dataset[p + 0][j] = solution.getVariableValue(sp++);
						}
					} else {
						for (int j = 0; j < m; j++) {
							dataset[p + v][j] = solution.getVariableValue(sp++);
						}
					}
				}

				for (int i = 0; i < n; i++) {
					int p = order[i];
					if (p < v) {
						for (int j = 0; j < m; j++) {
							dataset[p + v][j] = solution.getVariableValue(sp++);
						}
					} else {
						for (int j = 0; j < m; j++) {
							dataset[p + n][j] = solution.getVariableValue(sp++);
						}
					}
				}

				int[][] k = new int[d][n];

				for (int i = 0; i < d; i++) {
					for (int j = 0; j < n; j++) {
						k[i][j] = (int) Math.round(kernel.applyAsDouble(dataset[i], dataset[j]) * 1000);
					}
				}

				TestCase testCase = new TestCase(n, d, k, label, c);

				double tr = testCase.fscore(new Solution(), Solution.lr, Solution.dec, false);
				double nr = testCase.fscore(new Solution(), Solution.lr, Solution.dec, true);

				// double error = (nr - tr + 2) * (tr) * (nr);
				double error = (nr - tr);

				solution.setObjective(0, error);
				if (getNumberOfObjectives() == 3) {
					solution.setObjective(1, tr);
					solution.setObjective(2, nr);
				}

				synchronized (this) {

					if (random.nextInt(100) < 10 || error < best) {
						if (error < best) {
							System.out.printf(Locale.ENGLISH, "< %.4f %.4f%n", tr, nr);
						} else {
							System.out.printf(Locale.ENGLISH, "> %.4f %.4f%n", tr, nr);
						}
						System.out.flush();
					}

					if (error < best) {
						best = error;

						// if (nr < tr) {

						// try (PrintWriter writer = new PrintWriter(String.format(Locale.ENGLISH, "%.4f %.4f.txt", tr, nr))) {
						//
						// for (int[] object : k) {
						// boolean sep = false;
						// for (int val : object) {
						// if (sep) {
						// writer.print(' ');
						// }
						// writer.print(val);
						// sep = true;
						// }
						// writer.println();
						// }
						// } catch (FileNotFoundException e) {
						// e.printStackTrace();
						// }

						// }
					}

					errorLog[iteration++] = best;

					// System.err.println(p + " " + ef.length);

					if (iteration >= elen) {
						try (PrintWriter writer = new PrintWriter(System.currentTimeMillis() + "log.txt")) {
							for (double val : errorLog) {
								writer.println(val);
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						System.exit(0);
					}
				}

			}

			@Override
			public DoubleSolution createSolution() {
				DoubleSolution solution = new DefaultDoubleSolution(this);

				Random random = new Random();

				double[][][] dataset = new double[2][n][m];

				double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;

				for (int k = 0; k < 2; k++) {
					double[] mu = new double[m];
					for (int j = 0; j < m; j++) {
						mu[j] = random.nextGaussian();
					}

					double[][] scale = new double[m][m];

					for (int x = 0; x < m; x++) {
						for (int y = 0; y < m; y++) {
							scale[x][y] = random.nextGaussian();
						}
					}

					for (int i = 0; i < n; i++) {
						double[] r = new double[m];
						for (int j = 0; j < m; j++) {
							r[j] = random.nextGaussian();
						}

						for (int x = 0; x < m; x++) {
							dataset[k][i][x] += mu[x];
							for (int y = 0; y < m; y++) {
								dataset[k][i][x] += scale[x][y] * r[y];
							}

							min = Math.min(min, dataset[k][i][x]);
							max = Math.max(max, dataset[k][i][x]);
						}
					}
				}

				for (int sp = 1, k = 0; k < 2; k++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < m; j++) {
							solution.setVariableValue(sp++, ((dataset[k][i][j] - min) / (max - min)) * 2 - 1);
						}
					}
				}

				return solution;
			}

			@Override
			public Double getUpperBound(int index) {
				return +1.0;
			}

			@Override
			public Double getLowerBound(int index) {
				return -1.0;
			}
		};
		final ExecutorService executor = Executors.newFixedThreadPool(14);
		SolutionListEvaluator<DoubleSolution> evaluator = new SolutionListEvaluator<DoubleSolution>() {

			@Override
			public void shutdown() {

			}

			@Override
			public List<DoubleSolution> evaluate(List<DoubleSolution> list, Problem<DoubleSolution> problem) {
				List<Future<DoubleSolution>> futures = new ArrayList<>();

				for (DoubleSolution solution : list) {
					futures.add(executor.submit(new Callable<DoubleSolution>() {
						@Override
						public DoubleSolution call() throws Exception {
							problem.evaluate(solution);
							return solution;
						}
					}));
				}

				for (Future<DoubleSolution> future : futures) {
					try {
						future.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}

				synchronized (problem) {
					System.err.println("done");
					System.err.flush();
				}
				return list;
			}
		};

		GeneticAlgorithmBuilder<DoubleSolution> builder = new GeneticAlgorithmBuilder<DoubleSolution>(problem, new DatasetCrossover(problem), new PolynomialMutation());

		// new NSGAIIBuilder<DoubleSolution>(problem, new DatasetCrossover(problem), new PolynomialMutation()).setMaxEvaluations(10000000);
		// NSGAIIBuilder<DoubleSolution> builder = new NSGAIIBuilder<DoubleSolution>(problem, new DatasetCrossover(problem), new PolynomialMutation()).setMaxEvaluations(10000000);

		// MOCellBuilder<DoubleSolution> builder = new MOCellBuilder<DoubleSolution>(problem, new DatasetCrossover(problem), new PolynomialMutation(1.0, 20));

		builder.setSolutionListEvaluator(evaluator);
		builder.setPopulationSize(size);
		builder.setMaxEvaluations(100000000);

		 RandomSearch<?> algorithm = new RandomSearch<>(problem, 100000000);
		// Algorithm<?> algorithm = new StandardPSO2011(problem, size, 10000000, root, evaluator);
	//	Algorithm<?> algorithm = builder.build();
		algorithm.run();
	}

}
