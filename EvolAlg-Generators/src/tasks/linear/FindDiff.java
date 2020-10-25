package tasks.linear;
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

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
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

	static final int root = 12;
	static final int size = root * root;
	static final int elen = size * 25;
	static final int n = 4096;
	static final int v = 256;
	static final int m = 64;

	public static void main(String[] args) throws FileNotFoundException {

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
				return "LR";
			}

			@Override
			public int getNumberOfVariables() {
				return (n + v) * (m + 1);
			}

			@Override
			public void evaluate(DoubleSolution solution) {

				int sp = 0;

				int f = 2 * m;
				int[] fp = new int[f];
				int[] scale = new int[f];
				int[] shift = new int[f];
				Random random = new Random();
				for (int i = 0; i < f; i++) {
					int j = random.nextInt(i + 1);
					fp[i] = fp[j];
					fp[j] = i;
					scale[i] = random.nextInt(100) + 1;
					shift[i] = random.nextInt(2000001) - 1000000;
				}

				int[][] train = new int[n][f + 1];
				int[][] test = new int[v][f + 1];

				for (int i = 0; i < n; i++) {
					for (int j = 0; j < m; j++) {
						train[i][fp[j]] = (int) Math.round(solution.getVariableValue(sp++));
					}
					train[i][f] = (int) Math.round(solution.getVariableValue(sp++));
				}

				for (int i = 0; i < v; i++) {
					for (int j = 0; j < m; j++) {
						test[i][fp[j]] = (int) Math.round(solution.getVariableValue(sp++));
					}
					test[i][f] = (int) Math.round(solution.getVariableValue(sp++));
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

				double sum = 0;
				for (int i = 0; i < getNumberOfVariables(); i++) {
					double rel = solution.getVariableValue(i) / getUpperBound(i);
					sum += rel * rel;
				}
				sum /= getNumberOfVariables();

				LRSolution naive = new SolutionMSE();
				LRSolution smape = new SolutionSMAPE();

				double[] ts = smape.solve(n, f, train);
				double[] ns = naive.solve(n, f, train);
				// double[] ns2 = naive2.solve(n, f, train);

				double tr = SMAPE.calc(v, f, ts, test);
				// double nr = Math.min(SMAPE.calc(v, f, ns1, test), SMAPE.calc(v, f, ns2, test));
				double nr = SMAPE.calc(v, f, ns, test);

				// double error = Math.max(tr - nr, Math.max(0.0, Math.max(tr, nr) - 0.5));
				// double error = tr - nr + Math.max(0.0, Math.max(tr, nr) - 0.5);
				// double error = (tr - nr + 1) * (tr + 1) * (sum + 10);
				// double error = (tr - nr + 2) * (tr + 1) * (sum + 10);
				// double error = (tr - nr + 4) * (tr + 1) * (sum + 10);
				// double error = ((tr + 0.01) * (1.01 - nr) + 1) * (sum + 5);
				// double error = (tr) + Math.max(0, nr - 0.4);
				// double error = 2 * tr + nr;
				// double error = Math.sqrt(tr) + nr;
				double error = tr - nr;

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

						if (tr < nr & false) {

							int[][][] bestDataset = new int[][][] { train, test };

							try (PrintWriter writer = new PrintWriter(String.format(Locale.ENGLISH, "%.4f %.4f.txt", tr, nr))) {
								writer.println(f);

								for (int[][] part : bestDataset) {
									writer.println(part.length);
									for (int[] object : part) {
										boolean sep = false;
										for (int val : object) {
											if (sep) {
												writer.print(' ');
											}
											writer.print(val);
											sep = true;
										}
										writer.println();
									}
								}
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

						}
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
				return new DefaultDoubleSolution(this);
			}

			@Override
			public Double getUpperBound(int index) {
				return +100000.0;
			}

			@Override
			public Double getLowerBound(int index) {
				return -100000.0;
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

		// Algorithm<?> algorithm = new StandardPSO2011(problem, size, 10000000, root, evaluator);
		Algorithm<?> algorithm = builder.build();
		algorithm.run();
	}

}
