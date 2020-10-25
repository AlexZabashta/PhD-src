package clsf;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Standardize;

public class WekaConverter {
    static Instances normalize(Instances instances) {
        try {
            Standardize standardize = new Standardize();
            standardize.setInputFormat(instances);
            return Filter.useFilter(instances, standardize);
        } catch (Exception e0) {
            System.err.println(e0.getLocalizedMessage());
            try {
                Normalize normalize = new Normalize();
                normalize.setScale(2);
                normalize.setTranslation(-1);
                normalize.setInputFormat(instances);
                return Filter.useFilter(instances, normalize);
            } catch (Exception e1) {
                System.err.println(e1.getLocalizedMessage());
                return instances;
            }
        }
    }

    public static Dataset convert(Instances instances) {
        return convert(instances.relationName(), instances);
    }

    public static Dataset convert(String name, Instances instances) {
        int classIndex = instances.classIndex();

        for (int a = 0; classIndex < 0 && a < instances.numAttributes(); a++) {
            if (instances.attribute(a).name().equals("class")) {
                instances.setClassIndex(a);
                classIndex = a;
            }
        }
        if (classIndex < 0) {
            throw new IllegalArgumentException("classIndex not set");
        }

        int numObjects = instances.numInstances();
        int numFeatures = instances.numAttributes() - 1;

        double[][] data = new double[numObjects][numFeatures];
        int[] labels = new int[numObjects];

        for (int oid = 0; oid < numObjects; oid++) {
            Instance instance = instances.get(oid);
            for (int fid = 0, aid = 0; aid < instances.numAttributes(); aid++) {
                if (aid == instances.classIndex()) {
                    continue;
                }
                data[oid][fid++] = instance.value(aid);
            }

            labels[oid] = (int) instance.classValue();
        }

        return new Dataset(name, Dataset.defaultNormValues, data, Dataset.defaultNormLabels, labels);
    }

    public static Instances convert(Dataset dataset) {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(dataset.numFeatures + 1);

        for (int j = 0; j < dataset.numFeatures; j++) {
            attributes.add(new Attribute("x" + j));
        }

        ArrayList<String> classNames = new ArrayList<String>(dataset.numClasses);

        for (int k = 0; k < dataset.numClasses; k++) {
            classNames.add("c" + k);
        }

        attributes.add(new Attribute("class", classNames));

        Instances instances = new Instances(dataset.name, attributes, dataset.numObjects);

        instances.setClassIndex(dataset.numFeatures);

        for (int oid = 0; oid < dataset.numObjects; oid++) {
            Instance instance = new DenseInstance(dataset.numFeatures + 1);
            instance.setDataset(instances);

            for (int fid = 0; fid < dataset.numFeatures; fid++) {
                instance.setValue(fid, dataset.data[oid][fid]);
            }

            instance.setClassValue(dataset.labels[oid]);
            instances.add(instance);
        }

        return (instances);
    }
}
