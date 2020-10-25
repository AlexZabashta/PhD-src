package clsf.vect;

import java.util.Arrays;
import java.util.Random;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import clsf.Dataset;
import utils.MatrixUtils;
import utils.RandomUtils;
import utils.StatUtils;

public class GMMConverter implements Converter {

    final static Double lowerBound = -10.0;
    final static Double upperBound = +10.0;

    final int maxClasses;
    final int maxFeatures;
    final int maxObjectsPerClass;
    final int[] numClassesDistribution;
    final int[] numFeaturesDistribution;
    final int[] numObjectsDistribution;

    public GMMConverter(int[] numObjectsDistribution, int[] numFeaturesDistribution, int[] numClassesDistribution) {
        this.numObjectsDistribution = numObjectsDistribution;
        this.maxObjectsPerClass = numObjectsDistribution[numObjectsDistribution.length - 1];

        this.numFeaturesDistribution = numFeaturesDistribution;
        this.maxFeatures = numFeaturesDistribution[numFeaturesDistribution.length - 1];

        this.numClassesDistribution = numClassesDistribution;
        this.maxClasses = numClassesDistribution[numClassesDistribution.length - 1];

    }

    public int toInt(double doubleValue, int[] distribution) {
        int index = (int) Math.round((distribution.length - 1) * (doubleValue + 10) * 0.05);
        if (index < 0) {
            index = 0;
        }
        if (index >= distribution.length) {
            index = distribution.length - 1;
        }
        return distribution[index];
    }

    public double toDouble(int intValue, int[] distribution) {
        int index = Arrays.binarySearch(distribution, intValue);
        if (index < 0) {
            index = ~index;
        }
        return Math.min(1.0, 2.0 * index / (distribution.length - 1.0) - 1.0) * 9.99;
    }

    public double denormalize(double value, double min, double max) {
        if (value < min) {
            return -10;
        }
        if (value > max) {
            return +10;
        }
        return (2.0 * (value - min) / (max - min) - 1) * 9.99;
    }

    @Override
    public DoubleSolution convert(DoubleProblem problem, Dataset dataset) {
        DoubleSolution solution = new DefaultDoubleSolution(problem);
        int sp = 0;

        Random random = new Random();

        solution.setVariableValue(sp++, toDouble(dataset.numFeatures, numFeaturesDistribution));
        solution.setVariableValue(sp++, toDouble(dataset.numClasses, numClassesDistribution));

        int[] selClass = RandomUtils.randomSelection(dataset.numClasses, maxClasses, random);
        int[] selFeatures = RandomUtils.randomSelection(dataset.numFeatures, maxFeatures, random);

        for (int label : selClass) {
            solution.setVariableValue(sp++, toDouble(dataset.classDistribution[label], numObjectsDistribution));
        }

        for (int label : selClass) {
            double[][] data = dataset.dataPerClass[label];

            double[] offset = StatUtils.mean(data.length, dataset.numFeatures, data);
            double[][] cov = StatUtils.covarianceMatrix(data.length, dataset.numFeatures, data, offset);
            for (int i = 0; i < dataset.numFeatures; i++) {
                cov[i][i] += 1e-3;
            }

            double[][] scale = MatrixUtils.sqrt(cov);

            for (int i = 0; i < maxFeatures; i++) {
                int fid1 = selFeatures[i];
                for (int j = 0; j < i; j++) {
                    int fid2 = selFeatures[j];
                    solution.setVariableValue(sp++, denormalize(scale[fid1][fid2], -10, +10));
                }
            }

            for (int i = 0; i < maxFeatures; i++) {
                int fid1 = selFeatures[i];
                solution.setVariableValue(sp++, denormalize(offset[fid1], -10, +10));
            }
        }

        return solution;
    }

    @Override
    public Dataset convert(DoubleSolution solution) {

        Random random = new Random();

        int sp = 0;
        int numFeatures = toInt(solution.getVariableValue(sp++), numFeaturesDistribution);
        int numClasses = toInt(solution.getVariableValue(sp++), numClassesDistribution);

        int numObjects = 0;
        int[] numObjectsPerClass = new int[numClasses];

        for (int label = 0; label < maxClasses; label++) {
            int value = toInt(solution.getVariableValue(sp++), numObjectsDistribution);

            if (label < numClasses) {
                numObjectsPerClass[label] = Math.max(value, 10);
                numObjects += numObjectsPerClass[label];
            }
        }

        double[][] data = new double[numObjects][];
        int[] labels = new int[numObjects];
        int oid = 0;

        double[][] rvec = new double[1][numFeatures];
        double[][] scale = new double[numFeatures][numFeatures];
        double[] offset = new double[numFeatures];

        for (int label = 0; label < numClasses; label++) {

            for (int i = 0; i < maxFeatures; i++) {
                for (int j = 0; j <= i; j++) {
                    double value = solution.getVariableValue(sp++);
                    if (i < numFeatures) {
                        scale[i][j] = value;
                    }
                }
            }

            for (int i = 0; i < maxFeatures; i++) {
                double value = solution.getVariableValue(sp++);
                if (i < numFeatures) {
                    offset[i] = value;
                }
            }

            for (int object = 0; object < numObjectsPerClass[label]; object++) {
                for (int i = 0; i < numFeatures; i++) {
                    rvec[0][i] = random.nextGaussian();
                }

                double[] vector = MatrixUtils.mul(1, numFeatures, numFeatures, rvec, scale)[0];
                for (int i = 0; i < numFeatures; i++) {
                    vector[i] += offset[i];
                }

                data[oid] = vector;
                labels[oid++] = label;
            }
        }

        return new Dataset("synthetic_gmm", true, data, false, labels);
    }

    @Override
    public Double getLowerBound() {
        return lowerBound;
    }

    @Override
    public int getNumberOfVariables() {
        return 2 + maxClasses * (1 + maxFeatures * (maxFeatures + 3) / 2);
    }

    @Override
    public Double getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return "GMM";
    }
}
