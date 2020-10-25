package test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static utils.MatrixUtils.*;
import static utils.ArrayUtils.*;

public class DecompTest {

    public static void main(String[] args) {

        int n = 5;

        double[][] rndm = new double[n][n];

        Random random = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                rndm[i][j] = random.nextGaussian() + i / (1.0 + j);
            }
        }

        print(rndm);

        double[][] matrix = mul(n, rndm, transpose(n, n, rndm));

        print(matrix);

        double[][] cd = sqrt(n, matrix);
        print(matrix);

        print(cd);
        print(mul(n, cd, transpose(n, n, cd)));

        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            int j = random.nextInt(i + 1);
            p[i] = p[j];
            p[j] = i;
        }

        // System.out.println(Arrays.toString(p));

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }

        Arrays.sort(order, new Comparator<Integer>() {

            @Override
            public int compare(Integer i, Integer j) {
                return Double.compare(matrix[i][i], matrix[j][j]);
            }
        });

        double[][] pm = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int oi = order[i];
                int oj = order[j];

                pm[i][j] = cd[oi][oj];
            }
        }

        print(pm);
        print(mul(n, pm, transpose(n, n, pm)));

    }

}
