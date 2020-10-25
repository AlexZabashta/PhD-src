package utils;

import java.util.Random;

public class StatUtils {

    public static double[][] covarianceMatrix(int n, int m, double[][] data) {
        return covarianceMatrix(n, m, data, mean(n, m, data));

    }

    public static double[][] covarianceMatrix(int n, int m, double[][] data, double[] mean) {
        double[][] covariance = new double[m][m];

        for (int i = 0; i < n; i++) {
            for (int x = 0; x < m; x++) {
                for (int y = 0; y < m; y++) {
                    covariance[x][y] += (data[i][x] - mean[x]) * (data[i][y] - mean[y]);
                }
            }
        }
        for (int x = 0; x < m; x++) {
            for (int y = 0; y < m; y++) {
                covariance[x][y] /= n;
            }
        }

        return covariance;
    }

    public static void main(String[] args) {
        Random random = new Random();

        double scale = 4, shift = -23;

        int n = 1000;

        double[] array = new double[n];

        for (int i = 0; i < n; i++) {
            array[i] = random.nextInt(2);
        }

        System.out.println(var(array));

    }

    public static double mean(double[] array) {
        if (array.length == 0) {
            return 0;
        } else {
            return sum(array) / array.length;
        }
    }

    public static double sum(double[] array) {
        double sum = 0;
        for (double value : array) {
            sum += value;
        }
        return sum;
    }

    public static double mean(double sum0, double sum1) {
        if (sum0 < 1e-9) {
            return 0;
        } else {
            return sum1 / sum0;
        }
    }

    public static double[] mean(int n, int m, double[][] data) {
        double[] mean = new double[m];
        for (int i = 0; i < n; i++) {
            double[] vector = data[i];
            for (int j = 0; j < m; j++) {
                mean[j] += vector[j];
            }
        }
        for (int j = 0; j < m; j++) {
            mean[j] /= n;
        }
        return mean;
    }

    public static double std(double sum0, double mean, double sum2) {
        if (sum0 < 1e-9) {
            return 1;
        } else {
            return Math.sqrt(Math.max(0, sum2 / sum0 - mean * mean));
        }
    }

    public static double var(double sum0, double sum1, double sum2) {
        if (sum0 < 1e-9) {
            return 0;
        } else {
            return (sum2 - sum1 * sum1 / sum0) / sum0;
        }
    }

    public static double std(double[] array) {
        return Math.sqrt(var(array));
    }

    public static double var(double[] array) {
        if (array.length < 2) {
            return 0;
        }
        double sum0 = 0, sum1 = 0, sum2 = 0;

        for (double value : array) {
            double power = 1;
            sum0 += power;
            power *= value;
            sum1 += power;
            power *= value;
            sum2 += power;
        }
        return var(sum0, sum1, sum2);
    }
}