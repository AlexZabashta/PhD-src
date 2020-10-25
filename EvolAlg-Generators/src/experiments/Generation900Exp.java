package experiments;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization.StandardPSO2011;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import clsf.Dataset;
import clsf.WekaConverter;
import clsf.vect.FixedConverter;
import clsf.vect.SimpleProblem;
import fitness_function.Limited;
import fitness_function.MahalanobisDistance;
import fitness_function.SingleObjectiveError;
import mfextraction.CMFExtractor;
import utils.ArrayUtils;
import utils.BlockingThreadPoolExecutor;
import utils.EndSearch;
import utils.FolderUtils;
import utils.MatrixUtils;
import utils.StatUtils;
import weka.core.Instances;

public class Generation900Exp {

    public static void main(String[] args) throws IOException {

        final int limit = 500;
        final int threads = 5;

        final double[][] metaData = new double[16384][];

        final List<Dataset> datasets = new ArrayList<>();

        final int numFeatures = 16;
        final int numObjectsPerClass = 64;

        String[] classNames = { "zero", "one" };

        final int numClasses = classNames.length;
        final int numObjects = numObjectsPerClass * numClasses;

        for (File datafolder : new File("csv").listFiles()) {
            try {
                double[][] data = new double[numObjects][numFeatures];
                int[] labels = new int[numObjects];

                String[] header = new String[numFeatures];

                for (int f = 0; f < numFeatures; f++) {
                    header[f] = "f" + f;
                }

                for (int oid = 0, label = 0; label < numClasses; label++) {
                    try (CSVParser parser = new CSVParser(new FileReader(datafolder.getPath() + File.separator + classNames[label] + ".csv"), CSVFormat.DEFAULT.withHeader(header))) {
                        for (CSVRecord record : parser) {
                            for (int fid = 0; fid < numFeatures; fid++) {
                                data[oid][fid] = Double.parseDouble(record.get(fid));
                            }
                            labels[oid++] = label;
                        }
                    }
                }

                datasets.add(new Dataset(datafolder.getName(), true, data, false, labels));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        final int numData = datasets.size();

        final CMFExtractor extractor = new CMFExtractor();
        final int numMF = extractor.length();

        for (int i = 0; i < numData; i++) {
            metaData[i] = extractor.apply(datasets.get(i));
        }

        final double[][] cov = StatUtils.covarianceMatrix(numData, numMF, metaData);
        ArrayUtils.print(cov);
        final double[][] invCov = MatrixUtils.inv(numMF, cov);
        ArrayUtils.print(invCov);

        final MahalanobisDistance distance = new MahalanobisDistance(numMF, invCov);

        String commonPath = Long.toString(System.currentTimeMillis());
        String resultPath = FolderUtils.buildPath(false, commonPath + "result");

        ExecutorService executor = new BlockingThreadPoolExecutor(threads, false);

        double sumDists = 0;

        for (int i = 0; i < numData; i++) {
            double[] x = extractor.apply(datasets.get(i));
            for (int j = 0; j < i; j++) {
                double[] y = extractor.apply(datasets.get(j));
                sumDists += distance.applyAsDouble(x, y);
            }
        }

        final double norm = 2.0 * sumDists / (numData * (numData - 1));

        List<Dataset> train = new ArrayList<>();
        List<Dataset> test = new ArrayList<>();

        Collections.sort(datasets, Comparator.comparing(dataset -> dataset.name));

        for (int i = 0; i < numData; i++) {
            if (i % 10 == 0) {
                test.add(datasets.get(i));
            } else {
                train.add(datasets.get(i));
            }
        }

        for (Dataset targetDataset : test) {
            final String targetName = targetDataset.name;
            final double[] target = extractor.apply(targetDataset);

            SingleObjectiveError errorFunction = new SingleObjectiveError(distance, extractor, target);

            Limited limited = new Limited(errorFunction, errorFunction, limit);
            DoubleProblem problem = new SimpleProblem(new FixedConverter(numObjectsPerClass, numClasses, numFeatures), limited, train);

            Algorithm<?> algorithm = new StandardPSO2011((DoubleProblem) problem, 32, 10000000, 8, new SequentialSolutionListEvaluator<DoubleSolution>());

            executor.submit(new Runnable() {
                @Override
                public void run() {

                    String tag = targetName;

                    System.out.println("START " + tag);
                    System.out.flush();

                    try {
                        long startTime = System.currentTimeMillis();
                        try {
                            algorithm.run();
                            algorithm.getResult();
                        } catch (EndSearch e) {
                            System.out.println("FINISH " + tag);
                            System.out.flush();
                        }
                        long finishTime = System.currentTimeMillis();

                        Dataset result = limited.dataset;
                        Instances instances = WekaConverter.convert(result);

                        synchronized (commonPath) {

                            try (PrintWriter writer = new PrintWriter(new File(resultPath + targetName + ".arff"))) {
                                printLine(writer, targetName);
                                printLine(writer, problem.getName());
                                printLine(writer, algorithm.getName());
                                printLine(writer, Long.toString(finishTime - startTime));
                                printLine(writer, Double.toString(limited.best));

                                writer.print("%");
                                double[] mf = extractor.apply(result);
                                for (int i = 0; i < mf.length; i++) {
                                    writer.print(' ');
                                    writer.print(mf[i]);
                                }
                                writer.println();

                                writer.print("%");
                                for (double dist : limited.log) {
                                    writer.print(' ');
                                    writer.print(dist / norm);
                                }
                                writer.println();

                                writer.println(instances);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println();
                            System.out.flush();
                        }
                    } catch (RuntimeException exception) {
                    }
                }

                void printLine(PrintWriter writer, String line) {
                    writer.println("% " + line);
                    System.out.println(line);
                }
            });

        }
        executor.shutdown();
    }

}
