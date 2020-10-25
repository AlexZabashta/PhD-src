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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization.StandardPSO2011;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.DoubleProblem;
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
import mfextraction.KNNLandMark;
import mfextraction.TreeMetaSystem;
import utils.ArrayUtils;
import utils.EndSearch;
import utils.FolderUtils;
import utils.MatrixUtils;
import utils.StatUtils;
import utils.ToDoubleArrayFunction;

public class MetaSystemExp {
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

    public static void main(String[] args) throws IOException {
        System.out.println("args = " + Arrays.toString(args));
        final int limit = get("limit", args, 0, 300);
        final int threads = get("threads", args, 1, 5);
        final int repeats = get("repeats", args, 2, 2);

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

        List<Runnable> experiments = new ArrayList<>();

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
        ToDoubleFunction<Dataset> knnScore = new KNNLandMark();

        double[] rmse = new double[512];
        String[] names = new String[512];
        int expId = 0;

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

            int id = expId++;
            names[id] = "RAND_DATA";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    train.add(train.get(random.nextInt(train.size())));
                    
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }
                }
            });
        }

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

            int id = expId++;
            names[id] = "RAND_DIRECT";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    SimpleProblem problem = new SimpleProblem(direct, empty, null);
                    Dataset dataset = direct.convert(problem.createSolution());
                    train.add(dataset);

                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }
                }
            });
        }

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

            int id = expId++;
            names[id] = "RAND_GMM";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    SimpleProblem problem = new SimpleProblem(gmmcon, empty, null);
                    Dataset dataset = gmmcon.convert(problem.createSolution());
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }
                }
            });
        }

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

            int id = expId++;
            names[id] = "RAND_NDSE";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    Dataset dataset = mutation.generate(new Random());
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "DIV_DIRECTP";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);
                    Limited limited = new Limited(diversity, diversity, limit);
                    SimpleProblem problem = new SimpleProblem(direct, limited, train);

                    Algorithm<?> algorithm = new StandardPSO2011(problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "DIV_DIRECTD";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);
                    Limited limited = new Limited(diversity, diversity, limit);
                    SimpleProblem problem = new SimpleProblem(direct, limited, train);

                    Algorithm<?> algorithm = new DifferentialEvolutionBuilder(problem).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "DIV_GMMP";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);

                    Limited limited = new Limited(diversity, diversity, limit);
                    SimpleProblem problem = new SimpleProblem(gmmcon, limited, train);
                    Algorithm<?> algorithm = new StandardPSO2011(problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());
                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }
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

            int id = expId++;
            names[id] = "DIV_GMMS";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);

                    Limited limited = new Limited(diversity, diversity, limit);
                    SimpleProblem problem = new SimpleProblem(gmmcon, limited, train);
                    Algorithm<?> algorithm = new SMSEMOABuilder<DoubleSolution>(problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setPopulationSize(100).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "DIV_NDSEM";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);

                    Limited limited = new Limited(diversity, diversity, limit);
                    GDSProblem problem = new GDSProblem(mutation, limited, train);

                    Algorithm<?> algorithm = new MOCellBuilder<DataSetSolution>(problem, crossover, mutation).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }
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

            int id = expId++;
            names[id] = "DIV_NDSEN";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    DataDiversity diversity = new DataDiversity(min, max, train, extractor, distance);

                    Limited limited = new Limited(diversity, diversity, limit);
                    GDSProblem problem = new GDSProblem(mutation, limited, train);

                    Algorithm<?> algorithm = new NSGAIIBuilder<DataSetSolution>(problem, crossover, mutation).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "VAR_DIRECTP";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    MetaVariance variance = new MetaVariance(new TreeMetaSystem(train, extractor, knnScore));

                    Limited limited = new Limited(variance, variance, limit);
                    SimpleProblem problem = new SimpleProblem(direct, limited, train);
                    Algorithm<?> algorithm = new StandardPSO2011(problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }
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

            int id = expId++;
            names[id] = "VAR_DIRECTD";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    MetaVariance variance = new MetaVariance(new TreeMetaSystem(train, extractor, knnScore));

                    Limited limited = new Limited(variance, variance, limit);
                    SimpleProblem problem = new SimpleProblem(direct, limited, train);
                    Algorithm<?> algorithm = new DifferentialEvolutionBuilder(problem).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "VAR_GMMP";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    MetaVariance variance = new MetaVariance(new TreeMetaSystem(train, extractor, knnScore));

                    Limited limited = new Limited(variance, variance, limit);
                    SimpleProblem problem = new SimpleProblem(gmmcon, limited, train);
                    Algorithm<?> algorithm = new StandardPSO2011(problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());
                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }
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

            int id = expId++;
            names[id] = "VAR_GMMS";

            experiments.add(new Runnable() {
                @Override
                public void run() {

                    MetaVariance variance = new MetaVariance(new TreeMetaSystem(train, extractor, knnScore));

                    Limited limited = new Limited(variance, variance, limit);
                    SimpleProblem problem = new SimpleProblem(gmmcon, limited, train);

                    Algorithm<?> algorithm = new SMSEMOABuilder<DoubleSolution>(problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setPopulationSize(100).setMaxEvaluations(10000000).build();
                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

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

            int id = expId++;
            names[id] = "VAR_NDSEM";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    MetaVariance variance = new MetaVariance(new TreeMetaSystem(train, extractor, knnScore));

                    Limited limited = new Limited(variance, variance, limit);
                    GDSProblem problem = new GDSProblem(mutation, limited, train);
                    Algorithm<?> algorithm = new MOCellBuilder<DataSetSolution>(problem, crossover, mutation).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }
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

            int id = expId++;
            names[id] = "VAR_NDSEN";

            experiments.add(new Runnable() {
                @Override
                public void run() {
                    MetaVariance variance = new MetaVariance(new TreeMetaSystem(train, extractor, knnScore));

                    Limited limited = new Limited(variance, variance, limit);
                    GDSProblem problem = new GDSProblem(mutation, limited, train);
                    Algorithm<?> algorithm = new NSGAIIBuilder<DataSetSolution>(problem, crossover, mutation).setMaxEvaluations(10000000).build();

                    try {
                        algorithm.run();
                    } catch (EndSearch e) {
                    }

                    Dataset dataset = Objects.requireNonNull(limited.dataset);
                    train.add(dataset);
                    TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                    double score = system.rmse(test, knnScore);
                    synchronized (rmse) {
                        rmse[id] = score;
                    }

                }
            });
        }

        System.out.println(experiments.size());
        System.out.println(expId);
        System.out.println(Arrays.toString(Arrays.copyOf(names, expId)));

        try (PrintWriter writer = new PrintWriter(new File(res + "names.txt"))) {
            for (int i = 0; i < expId; i++) {
                writer.println(names[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int step = 0;

        while (true) {
            synchronized (rmse) {
                Arrays.fill(rmse, Double.NaN);
            }

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            for (Runnable experiment : experiments) {
                executor.submit(experiment);
            }
            executor.shutdown();
            try {
                executor.awaitTermination(2, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(step);
            System.out.flush();

            synchronized (rmse) {
                try (PrintWriter writer = new PrintWriter(new File(res + (step++) + ".txt"))) {
                    for (int i = 0; i < expId; i++) {
                        writer.println(rmse[i]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
