package clsf.vect;

import java.util.Arrays;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import clsf.Dataset;
import utils.RandomUtils;
import weka.core.Debug.Random;

public class DirectConverter implements Converter {

    final static Double lowerBound = -10.0;
    final static Double upperBound = +10.0;

    final int maxClasses;
    final int maxFeatures;
    final int maxObjectsPerClass;
    final int[] numClassesDistribution;
    final int[] numFeaturesDistribution;
    final int[] numObjectsDistribution;

    public DirectConverter(int[] numObjectsDistribution, int[] numFeaturesDistribution, int[] numClassesDistribution) {
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

        double[][][] dpc = dataset.dataPerClass;

        for (int label : selClass) {
            solution.setVariableValue(sp++, toDouble(dpc[label].length, numObjectsDistribution));
        }

        for (int label : selClass) {
            int[] selObjects = RandomUtils.randomSelection(dpc[label].length, maxObjectsPerClass, random);

            for (int oid : selObjects) {
                for (int fid : selFeatures) {
                    solution.setVariableValue(sp++, denormalize(dpc[label][oid][fid], dataset.min[fid], dataset.max[fid]));
                }
            }
        }

        return solution;
    }

    @Override
    public Dataset convert(DoubleSolution solution) {
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

        double[][] data = new double[numObjects][numFeatures];
        int[] labels = new int[numObjects];
        int oid = 0;

        for (int label = 0; label < numClasses; label++) {
            for (int object = 0; object < maxObjectsPerClass; object++) {
                for (int feature = 0; feature < maxFeatures; feature++) {
                    double value = solution.getVariableValue(sp++);

                    if (object < numObjectsPerClass[label] && feature < numFeatures) {
                        data[oid][feature] = value;
                    }
                }

                if (object < numObjectsPerClass[label]) {
                    labels[oid++] = label;
                }
            }
        }

        return new Dataset("synthetic_direct", true, data, false, labels);
    }

    @Override
    public Double getLowerBound() {
        return lowerBound;
    }

    @Override
    public int getNumberOfVariables() {
        return 2 + maxClasses * (1 + maxFeatures * maxObjectsPerClass);
    }

    @Override
    public Double getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return "DIRECT";
    }
}
