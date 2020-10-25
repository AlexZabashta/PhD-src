package fitness_function;

import java.util.function.ToDoubleBiFunction;

import utils.MatrixUtils;
import utils.StatUtils;

public class MahalanobisDistance implements ToDoubleBiFunction<double[], double[]> {
    final int d;
    final double[][] invCov;

    public MahalanobisDistance(int d, double[][] invCov) {
        this.d = d;
        this.invCov = invCov;
    }

    public MahalanobisDistance(int n, int m, double[][] data) {
        this(m, MatrixUtils.inv(m, StatUtils.covarianceMatrix(n, m, data)));
    }

    @Override
    public double applyAsDouble(double[] u, double[] v) {
        return distance(u, v);
    }

    public int dim() {
        return d;
    }

    public double distance(double[] diff) {
        if (diff.length != d) {
            throw new IllegalArgumentException("u.length != d");
        }
        double sum = 0;
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                sum += diff[i] * invCov[i][j] * diff[j];
            }
        }
        return Math.sqrt(sum);
    }

    public double distance(double[] u, double[] v) {
        if (u.length != d) {
            throw new IllegalArgumentException("u.length != d");
        }
        if (v.length != d) {
            throw new IllegalArgumentException("v.length != d");
        }

        double[] diff = new double[d];

        for (int i = 0; i < d; i++) {
            diff[i] = u[i] - v[i];
        }

        return distance(diff);
    }
}