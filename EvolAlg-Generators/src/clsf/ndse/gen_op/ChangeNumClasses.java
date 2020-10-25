package clsf.ndse.gen_op;

import java.util.Random;

import clsf.Dataset;
import clsf.ndse.gen_op.fun.cat.CatFunction;
import clsf.ndse.gen_op.fun.cat.ClassValue;
import clsf.ndse.gen_op.fun.cat.SumMod;
import utils.ArrayUtils;

public class ChangeNumClasses {

    public static Dataset apply(Dataset dataset, Random random, int newNumClasses) {
        int oldNumClasses = dataset.numClasses;

        if (oldNumClasses == newNumClasses) {
            return dataset;
        }

        int numObjects = dataset.numObjects;
        CatFunction randomFun = CatFunction.random(dataset, random, 3, newNumClasses);
        CatFunction classVal = new ClassValue(dataset);

        CatFunction classMaper = new SumMod(randomFun, classVal, newNumClasses, random);

        int[] labels = new int[numObjects];

        for (int oid = 0; oid < numObjects; oid++) {
            labels[oid] = classMaper.applyAsInt(oid);
        }

        return dataset.changeLabels(true, labels);
    }

    public static Dataset removeRareClasses(Dataset dataset, Random random, int newNumClasses) {
        int[] classDistribution = dataset.classDistribution;
        int[] order = ArrayUtils.order(classDistribution);

        int newNumObjects = 0;
        for (int i = 0; i < newNumClasses; i++) {
            newNumObjects += classDistribution[order[i]];
        }

        double[][] data = new double[newNumObjects][];
        int[] labels = new int[newNumObjects];

        for (int oid = 0, i = 0; i < newNumClasses; i++) {
            for (double[] object : dataset.dataPerClass[order[i]]) {
                data[oid] = object.clone();
                labels[oid] = i;
                ++oid;
            }
        }

        return new Dataset(dataset.name, true, data, false, labels);
    }

    public static void main(String[] args) {
        Random random = new Random();
        System.out.println(random.nextInt(2) * 2 - 1);
    }

}
