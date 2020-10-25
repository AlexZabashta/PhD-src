package clusterization.direct;

import java.util.Arrays;
import java.util.Random;
import java.util.function.ToDoubleFunction;

import clusterization.direct.fun.RandomFunction;
import utils.RandomUtils;
import utils.ArrayUtils;
import utils.Permutation;

public class RelationsGenerator {
    public static void main(String[] args) {
        int n = 5;
        int m = 10;

        double[][] values = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                values[i][j] = i + j / 10.0;
            }
        }

        Random random = new Random();

        ArrayUtils.print(changeNumObjects(values, 10, m, random));
    }

    public static double[][] selectFeatures(double[][] values, int numFeatures, int newNumFeatures, boolean[] mask) {
        int numObjects = values.length;
        double[][] result = new double[numObjects][newNumFeatures];

        for (int i = 0; i < numObjects; i++) {
            for (int j = 0, k = 0; j < numFeatures; j++) {
                if (mask[j]) {
                    result[i][k++] = values[i][j];
                }
            }
        }

        return result;
    }

    public static void apply(ToDoubleFunction<double[]> fun, double[][] values, int index) {
        for (double[] array : values) {
            array[index] = fun.applyAsDouble(array);
        }
    }

    public static double[][] changeNumFeatures(double[][] data, int numFeatures, int newNumFeatures, Random random) {
        int numObjects = data.length;
        if (numFeatures == newNumFeatures) {
            return data;
        }

        if (numFeatures < newNumFeatures) { // ADD

            double[][] newData = new double[numObjects][];
            int d = random.nextInt(4) + 3;

            for (int fid = 0; fid < numFeatures; fid++) {
                for (int oid = 0; oid < numObjects; oid++) {
                    newData[oid] = Arrays.copyOf(data[oid], newNumFeatures);
                }
            }

            for (int fid = numFeatures; fid < newNumFeatures; fid++) {
                apply(RandomFunction.generate(random, numFeatures, d), newData, fid);
            }

            return newData;

        } else { // REMOVE
            boolean[] mask = RandomUtils.randomSelection(numFeatures, newNumFeatures, random);
            return selectFeatures(data, numFeatures, newNumFeatures, mask);
        }
    }

    public static double[][] changeNumObjects(double[][] values, int newNumObjects, int numFeatures, Random random) {

        int oldNumObjects = values.length;

        if (oldNumObjects == newNumObjects) {
            return values;
        }

        if (oldNumObjects < newNumObjects) {
            int n = values.length;

            double[][] big = Arrays.copyOf(values, newNumObjects);

            for (int dstObjId = oldNumObjects; dstObjId < newNumObjects; dstObjId++) {
                big[dstObjId] = new double[numFeatures];

                int[] p = Permutation.random(numFeatures, random);

                int offset = 0;
                while (offset < numFeatures) {
                    int srcObjId = random.nextInt(n);
                    int len = random.nextInt(numFeatures - offset) + 1;

                    for (int k = 0; k < len; k++) {
                        int fid = p[offset + k];
                        big[dstObjId][fid] = values[srcObjId][fid];
                    }

                    offset += len;
                }

            }

            return big;

        } else {
            boolean[] b = RandomUtils.randomSelection(oldNumObjects, newNumObjects, random);
            double[][] small = new double[newNumObjects][];

            for (int i = 0, j = 0; i < oldNumObjects; i++) {
                if (b[i]) {
                    small[j++] = values[i];
                }
            }

            return small;
        }
    }

}
