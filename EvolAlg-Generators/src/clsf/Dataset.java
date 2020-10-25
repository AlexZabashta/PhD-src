package clsf;

import java.util.Arrays;

import utils.ArrayUtils;
import utils.CategoryMapper;

public class Dataset {

    public static boolean defaultNormLabels = true;
    public static boolean defaultNormValues = true;

    public final int[] classDistribution;

    public final double[][] data;
    public final double[][][] dataPerClass;
    private final int hashCode;
    public final int[] labels;

    public final double[] max;
    public final double[] min;
    public final String name;

    public boolean emptyMF = true;
    public boolean emptyKNN = true;
    public boolean emptySVM = true;
    public boolean emptyREL = true;

    public final double[] metaFeatures = new double[64];

    public final int numClasses;
    public final int numFeatures;
    public final int numObjects;

    public Dataset(String name, boolean normValues, double[][] data, boolean normLabels, int[] labels) {
        this.name = name;
        this.data = data;

        this.numObjects = data.length;
        if (numObjects <= 3) {
            throw new IllegalArgumentException("numObjects <= 10");
        }

        this.numFeatures = getNumFeatures();
        if (numFeatures <= 1) {
            throw new IllegalArgumentException("numFeatures <= 1");
        }

        if (normValues) {
            normValues();
        }

        if (labels.length != numObjects) {
            throw new IllegalArgumentException("Number of objects must be equal to labels length");
        }
        this.labels = labels;

        if (normLabels) {
            normLabels();
        }

        this.numClasses = ArrayUtils.max(labels) + 1;

        this.hashCode = Arrays.deepHashCode(data) ^ Arrays.hashCode(this.labels);

        this.min = calcMin();
        this.max = calcMax();
        this.classDistribution = calcDistribution();
        this.dataPerClass = calcDataPerClass();
    }

    private double[][][] calcDataPerClass() {
        double[][][] dataPerClass = new double[numClasses][][];
        int[] s = classDistribution.clone();
        for (int label = 0; label < numClasses; label++) {
            dataPerClass[label] = new double[s[label]][];
            s[label] = 0;
        }
        for (int oid = 0; oid < numObjects; oid++) {
            int c = labels[oid];
            dataPerClass[c][s[c]++] = data[oid];
        }
        return dataPerClass;
    }

    private int[] calcDistribution() {
        int[] classDistribution = new int[numClasses];
        for (int oid = 0; oid < numObjects; oid++) {
            ++classDistribution[labels[oid]];
        }
        return classDistribution;
    }

    private double[] calcMax() {
        double[] max = new double[numFeatures];
        Arrays.fill(max, Double.NEGATIVE_INFINITY);

        for (int fid = 0; fid < numFeatures; fid++) {
            for (int oid = 0; oid < numObjects; oid++) {
                max[fid] = Math.max(max[fid], data[oid][fid]);
            }
            max[fid] += 1e-3;
        }
        return max;
    }

    private double[] calcMin() {
        double[] min = new double[numFeatures];
        Arrays.fill(min, Double.POSITIVE_INFINITY);

        for (int fid = 0; fid < numFeatures; fid++) {
            for (int oid = 0; oid < numObjects; oid++) {
                min[fid] = Math.min(min[fid], data[oid][fid]);
            }
            min[fid] -= 1e-3;
        }
        return min;
    }

    public Dataset changeLabels(boolean normLabels, int[] labels) {
        return new Dataset(name, false, data, normLabels, labels);
    }

    public Dataset changeValues(boolean normValues, double[][] data) {
        return new Dataset(name, normValues, data, false, labels);
    }

    private int getNumFeatures() {
        int numFeatures = -1;
        for (double[] object : data) {
            if (numFeatures == -1) {
                numFeatures = object.length;
            }
            if (object.length != numFeatures) {
                throw new IllegalArgumentException("All objects length must be equal");
            }

            for (double value : object) {
                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException("All values must be finite, value = " + value);
                }
            }
        }

        return numFeatures;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private void normLabels() {
        CategoryMapper mapper = new CategoryMapper(labels.clone());
        for (int oid = 0; oid < numObjects; oid++) {
            labels[oid] = mapper.applyAsInt(labels[oid]);
        }
    }

    private void normValues() {
        for (int fid = 0; fid < numFeatures; fid++) {
            double mean = 0;
            for (int oid = 0; oid < numObjects; oid++) {
                mean += data[oid][fid];
            }
            mean /= numObjects;
            for (int oid = 0; oid < numObjects; oid++) {
                data[oid][fid] -= mean;
            }

            double var = 0;
            double asymm = 0;
            for (int oid = 0; oid < numObjects; oid++) {
                double value = data[oid][fid];
                double value2 = value * value;
                var += value2;
                asymm += value * value2;
            }
            var /= numObjects;
            if (var > 1e-6) {
                double scale = Math.sqrt(1 / var);
                if (asymm < 0) {
                    scale *= -1;
                }
                for (int oid = 0; oid < numObjects; oid++) {
                    data[oid][fid] *= scale;
                }
            }
        }
    }
}
