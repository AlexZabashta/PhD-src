package clusterization;

import java.util.ArrayList;
import java.util.Arrays;

import utils.ArrayUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class Dataset {

    public final int numObjects;
    public final int numFeatures;
    public final int hashCode;

    private final double[][] data;
    private final double[] metaFeatures;
    public final MetaFeaturesExtractor extractor;

    public static boolean normalize = true;

    public Dataset(double[][] data, MetaFeaturesExtractor extractor) {
        this(data, extractor, Dataset.normalize);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public Dataset(double[][] data, MetaFeaturesExtractor extractor, boolean normalize) {
        this.data = ArrayUtils.copy(data);
        this.numObjects = data.length;

        if (numObjects <= 0) {
            throw new IllegalArgumentException("data.length == 0");
        }

        this.extractor = extractor;
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
        this.numFeatures = numFeatures;

        if (numFeatures <= 0) {
            throw new IllegalArgumentException("Number of features must be positive");
        }

        if (normalize) {
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

        this.hashCode = Arrays.deepHashCode(data);
        this.metaFeatures = extractor.extract(this);
    }

    public Instances toInstances() {
        ArrayList<Attribute> attributes = new ArrayList<>();

        for (int fid = 0; fid < numFeatures; fid++) {
            attributes.add(new Attribute("f" + fid));
        }

        Instances instances = new Instances("rel", attributes, numObjects);

        for (int oid = 0; oid < numObjects; oid++) {
            DenseInstance instance = new DenseInstance(1.0, data[oid].clone());
            instance.setDataset(instances);
            instances.add(instance);
        }

        return instances;
    }

    public double[] metaFeatures() {
        return metaFeatures.clone();
    }

    public double[][] data() {
        return ArrayUtils.copy(data);
    }

}
