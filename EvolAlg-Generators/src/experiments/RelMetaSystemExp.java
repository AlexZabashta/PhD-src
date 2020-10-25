package experiments;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization.StandardPSO2011;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import clsf.Dataset;
import clsf.ndse.DataSetSolution;
import clsf.ndse.GDSProblem;
import clsf.ndse.gen_op.DatasetCrossover;
import clsf.ndse.gen_op.DatasetMutation;
import clsf.vect.Converter;
import clsf.vect.DirectConverter;
import clsf.vect.GMMConverter;
import clsf.vect.SimpleProblem;
import fitness_function.DataDiversity;
import fitness_function.Limited;
import fitness_function.MahalanobisDistance;
import fitness_function.MetaVariance;
import mfextraction.CMFExtractor;
import mfextraction.RelLandMark;
import mfextraction.SvmMetaSystem;
import utils.ArrayUtils;
import utils.EndSearch;
import utils.FolderUtils;
import utils.MatrixUtils;
import utils.StatUtils;
import utils.ToDoubleArrayFunction;

public class RelMetaSystemExp {
    static int get(String name, String[] args, int index, int defaultValue) {
        try {
            int value = Integer.parseInt(args[index]);
            System.out.println(name + " = " + value);
            return value;
        } catch (RuntimeException e) {
            System.out.println(name + " = default = " + defaultValue);
            return defaultValue;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        System.out.println("args = " + Arrays.toString(args));
        final int limit = get("limit", args, 0, 3000);
        final int threads = get("threads", args, 1, 6);
        final int repeats = get("repeats", args, 2, 5);

        double[][] metaData = new double[2048][];
        List<Dataset> datasets = DataReader.readData("data.csv", new File("data"));
        final int numData = datasets.size();
        CMFExtractor extractor = new CMFExtractor();
        int numMF = extractor.length();
        for (int i = 0; i < numData; i++) {
            metaData[i] = extractor.apply(datasets.get(i));
        }
        double[][] cov = StatUtils.covarianceMatrix(numData, numMF, metaData);
        ArrayUtils.print(cov);
        double[][] invCov = MatrixUtils.inv(numMF, cov);
        ArrayUtils.print(invCov);
        double[] invSigma = new double[numMF];
        for (int i = 0; i < numMF; i++) {
            invSigma[i] = invCov[i][i];
        }
        final double[] min = new double[numMF];
        final double[] max = new double[numMF];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < numData; i++) {
            for (int j = 0; j < numMF; j++) {
                min[j] = Math.min(min[j], metaData[i][j]);
                max[j] = Math.max(max[j], metaData[i][j]);
            }
        }
        MahalanobisDistance distance = new MahalanobisDistance(numMF, invCov);
        String res = FolderUtils.buildPath(false, Long.toString(System.currentTimeMillis()));
        DatasetCrossover crossover = new DatasetCrossover();
        int minNumObjects = Integer.MAX_VALUE;
        int maxNumObjects = 0;
        int minNumFeatures = Integer.MAX_VALUE;
        int maxNumFeatures = 0;
        int minNumClasses = Integer.MAX_VALUE;
        int maxNumClasses = 0;
        Set<Integer> numObjectsDistributionList = new TreeSet<>();
        Set<Integer> numFeaturesDistributionList = new TreeSet<>();
        Set<Integer> numClassesDistributionList = new TreeSet<>();
        for (Dataset dataset : datasets) {
            minNumObjects = Math.min(minNumObjects, dataset.numObjects);
            maxNumObjects = Math.max(maxNumObjects, dataset.numObjects);
            minNumFeatures = Math.min(minNumFeatures, dataset.numFeatures);
            maxNumFeatures = Math.max(maxNumFeatures, dataset.numFeatures);
            minNumClasses = Math.min(minNumClasses, dataset.numClasses);
            maxNumClasses = Math.max(maxNumClasses, dataset.numClasses);
            for (int sub : dataset.classDistribution) {
                numObjectsDistributionList.add(sub);
            }
            numFeaturesDistributionList.add(dataset.numFeatures);
            numClassesDistributionList.add(dataset.numClasses);
        }
        int[] numObjectsDistribution = numObjectsDistributionList.stream().mapToInt(x -> x.intValue()).toArray();
        int[] numFeaturesDistribution = numFeaturesDistributionList.stream().mapToInt(x -> x.intValue()).toArray();
        int[] numClassesDistribution = numClassesDistributionList.stream().mapToInt(x -> x.intValue()).toArray();
        System.out.println(Arrays.toString(numObjectsDistribution));
        System.out.println(Arrays.toString(numFeaturesDistribution));
        System.out.println(Arrays.toString(numClassesDistribution));
        DatasetMutation mutation = new DatasetMutation(minNumObjects, maxNumObjects, minNumFeatures, maxNumFeatures, minNumClasses, maxNumClasses);
        Converter direct = new DirectConverter(numObjectsDistribution, numFeaturesDistribution, numClassesDistribution);
        Converter gmmcon = new GMMConverter(numObjectsDistribution, numFeaturesDistribution, numClassesDistribution);
        Collections.sort(datasets, Comparator.comparing(d -> d.name));
        Collections.shuffle(datasets, new Random(42));
        ToDoubleFunction<Dataset> relScore = new RelLandMark();
        ToDoubleArrayFunction<Dataset> empty = new ToDoubleArrayFunction<Dataset>() {
            @Override
            public double[] apply(Dataset dataset) {
                return new double[length()];
            }

            @Override
            public int length() {
                return 1;
            }
        };
        List<Supplier<Function<List<Dataset>, Dataset>>> strategies = new ArrayList<>();

        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    Random random = new Random();

                    @Override
                    public Dataset apply(List<Dataset> train) {
                        return train.get(random.nextInt(train.size()));
                    }
                };
            }

            @Override
            public String toString() {
                return "RAND_DATA";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    SimpleProblem problem = new SimpleProblem(direct, empty, null);

                    @Override
                    public Dataset apply(List<Dataset> train) {
                        return direct.convert(problem.createSolution());
                    }
                };
            }

            @Override
            public String toString() {
                return "RAND_DIRECT";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    SimpleProblem problem = new SimpleProblem(gmmcon, empty, null);

                    @Override
                    public Dataset apply(List<Dataset> train) {
                        return gmmcon.convert(problem.createSolution());
                    }
                };
            }

            @Override
            public String toString() {
                return "RAND_GMM";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        return mutation.generate(new Random());
                    }
                };
            }

            @Override
            public String toString() {
                return "RAND_NDSE";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);
                        Limited limited = new Limited(diversity, diversity, limit);
                        SimpleProblem problem = new SimpleProblem(direct, limited, train);
                        Algorithm<?> algorithm = new DifferentialEvolutionBuilder(problem).setMaxEvaluations(10000000).build();
                        try {
                            algorithm.run();
                        } catch (EndSearch e) {
                        }
                        return Objects.requireNonNull(limited.dataset);
                    }
                };
            }

            @Override
            public String toString() {
                return "DIV_DIRECT";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);
                        Limited limited = new Limited(diversity, diversity, limit);
                        SimpleProblem problem = new SimpleProblem(gmmcon, limited, train);
                        Algorithm<?> algorithm = new StandardPSO2011(problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());
                        try {
                            algorithm.run();
                        } catch (EndSearch e) {
                        }
                        return Objects.requireNonNull(limited.dataset);
                    }
                };
            }

            @Override
            public String toString() {
                return "DIV_GMM";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);
                        Limited limited = new Limited(diversity, diversity, limit);
                        GDSProblem problem = new GDSProblem(mutation, limited, train);
                        Algorithm<?> algorithm = new MOCellBuilder<DataSetSolution>(problem, crossover, mutation).setMaxEvaluations(10000000).build();
                        try {
                            algorithm.run();
                        } catch (EndSearch e) {
                        }
                        return Objects.requireNonNull(limited.dataset);
                    }
                };
            }

            @Override
            public String toString() {
                return "DIV_NDSE";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        MetaVariance variance = new MetaVariance(new SvmMetaSystem(train, extractor, relScore));
                        Limited limited = new Limited(variance, variance, limit);
                        SimpleProblem problem = new SimpleProblem(direct, limited, train);
                        Algorithm<?> algorithm = new DifferentialEvolutionBuilder(problem).setMaxEvaluations(10000000).build();
                        try {
                            algorithm.run();
                        } catch (EndSearch e) {
                        }
                        return Objects.requireNonNull(limited.dataset);
                    }
                };
            }

            @Override
            public String toString() {
                return "VAR_DIRECT";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        MetaVariance variance = new MetaVariance(new SvmMetaSystem(train, extractor, relScore));
                        Limited limited = new Limited(variance, variance, limit);
                        SimpleProblem problem = new SimpleProblem(gmmcon, limited, train);
                        Algorithm<?> algorithm = new StandardPSO2011(problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());
                        try {
                            algorithm.run();
                        } catch (EndSearch e) {
                        }
                        return Objects.requireNonNull(limited.dataset);
                    }
                };
            }

            @Override
            public String toString() {
                return "VAR_GMM";
            }
        });
        strategies.add(new Supplier<Function<List<Dataset>, Dataset>>() {
            @Override
            public Function<List<Dataset>, Dataset> get() {
                return new Function<List<Dataset>, Dataset>() {
                    @Override
                    public Dataset apply(List<Dataset> train) {
                        MetaVariance variance = new MetaVariance(new SvmMetaSystem(train, extractor, relScore));
                        Limited limited = new Limited(variance, variance, limit);
                        GDSProblem problem = new GDSProblem(mutation, limited, train);
                        Algorithm<?> algorithm = new MOCellBuilder<DataSetSolution>(problem, crossover, mutation).setMaxEvaluations(10000000).build();
                        try {
                            algorithm.run();
                        } catch (EndSearch e) {
                        }
                        return Objects.requireNonNull(limited.dataset);
                    }
                };
            }

            @Override
            public String toString() {
                return "VAR_NDSE";
            }
        });

        List<Callable<Double>> experiments = new ArrayList<>();
        int count = experiments.size();
        double[] rmse = new double[count];

        for (Supplier<Function<List<Dataset>, Dataset>> strategy : strategies) {
            for (int repeat = 0; repeat < repeats; repeat++) {
                List<Dataset> train = new ArrayList<>();
                List<Dataset> test = new ArrayList<>();
                Random random = new Random(repeat + 42);
                for (Dataset dataset : datasets) {
                    if (random.nextInt(3) == 0) {
                        train.add(dataset);
                    } else {
                        test.add(dataset);
                    }
                }

                Function<List<Dataset>, Dataset> function = strategy.get();

                experiments.add(new Callable<Double>() {

                    @Override
                    public Double call() throws Exception {
                        Dataset dataset = function.apply(train);
                        train.add(dataset);
                        SvmMetaSystem system = new SvmMetaSystem(train, extractor, relScore);
                        return system.rmse(test, relScore);
                    }

                    @Override
                    public String toString() {
                        return strategy.toString();
                    }

                });
            }
        }

        System.out.println(experiments.size());

        try (PrintWriter writer = new PrintWriter(new File(res + "names.txt"))) {
            for (Callable<Double> experiment : experiments) {
                writer.println(experiment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int step = 0;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        while (true) {
            List<Future<Double>> futures = new ArrayList<>();

            for (Callable<Double> experiment : experiments) {
                futures.add(executor.submit(experiment));
            }

            List<Double> result = new ArrayList<>();

            for (Future<Double> future : futures) {
                result.add(future.get());
            }

            synchronized (rmse) {
                try (PrintWriter writer = new PrintWriter(new File(res + (step++) + ".txt"))) {
                    for (Double value : result) {
                        writer.println(value);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(step);
            System.out.flush();
        }
    }
}
