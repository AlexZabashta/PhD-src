package clsf.ndse.gen_op;

import java.util.Arrays;
import java.util.Random;

import clsf.Dataset;
import utils.Permutation;
import utils.RandomUtils;

public class ChangeNumObjects {

    public static Dataset addObjects(Dataset dataset, Random random, int newNumObjects) {
        int numFeatures = dataset.numFeatures;
        int oldNumObjects = dataset.numObjects;

        double[][] values = new double[newNumObjects][];
        int[] labels = Arrays.copyOf(dataset.labels, newNumObjects);

        for (int oid = 0; oid < oldNumObjects; oid++) {
            values[oid] = dataset.data[oid].clone();
        }

        for (int oid = oldNumObjects; oid < newNumObjects; oid++) {
            values[oid] = new double[numFeatures];
            int label = labels[random.nextInt(oldNumObjects)];
            labels[oid] = label;
            int[] p = Permutation.random(numFeatures, random);

            double[][] dpc = dataset.dataPerClass[label];

            int offset = 0;
            while (offset < numFeatures) {
                double[] src = dpc[random.nextInt(dpc.length)];

                int len = random.nextInt(numFeatures - offset) + 1;

                for (int k = 0; k < len; k++) {
                    int fid = p[offset + k];
                    values[oid][fid] = src[fid];
                }

                offset += len;
            }
        }

        return new Dataset(dataset.name, true, values, false, labels);
    }

    public static Dataset apply(Dataset dataset, Random random) {
        int numObjects = dataset.numObjects;
        return apply(dataset, random, random.nextInt(numObjects * 2) + 2);
    }

    public static Dataset apply(Dataset dataset, Random random, int newNumObjects) {
        int oldNumObjects = dataset.numObjects;

        if (oldNumObjects == newNumObjects) {
            return dataset;
        }

        if (oldNumObjects < newNumObjects) {
            return addObjects(dataset, random, newNumObjects);
        } else {
            return removeObjects(dataset, random, newNumObjects);
        }
    }

    public static Dataset removeObjects(Dataset dataset, Random random, int newNumObjects) {
        int numFeatures = dataset.numFeatures;
        int oldNumObjects = dataset.numObjects;

        boolean[] selection = RandomUtils.randomBinarySelection(oldNumObjects, newNumObjects, random);

        double[][] values = new double[newNumObjects][numFeatures];
        int[] labels = new int[newNumObjects];

        for (int noid = 0, oid = 0; oid < oldNumObjects; oid++) {
            if (selection[oid]) {
                values[noid] = dataset.data[oid].clone();
                labels[noid] = dataset.labels[oid];
                ++noid;
            }
        }

        return new Dataset(dataset.name, true, values, true, labels);
    }

}
