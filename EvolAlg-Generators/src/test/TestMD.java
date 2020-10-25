package test;

import java.util.Random;

import utils.ArrayUtils;
import utils.MatrixUtils;

public class TestMD {
    public static void main(String[] args) {
        int n = 10;
        int m = 20;
        double[][] pcov = new double[m][n];

        Random random = new Random();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                pcov[i][j] = random.nextGaussian() + Math.sin(i) + Math.cos(j);
            }
        }

        double[][] cov = MatrixUtils.mul(MatrixUtils.transpose(pcov), pcov);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cov[i][j] /= m;
            }
            cov[i][i] += 1e-7;
        }

        ArrayUtils.print(cov);
        double[][] inv = MatrixUtils.inv(n, ArrayUtils.copy(cov));
        ArrayUtils.print(inv);

        double[] v = new double[n];
        double[] u = new double[n];

        for (int i = 0; i < n; i++) {
            u[i] = random.nextDouble() * n;
            v[i] = i;
        }
        ArrayUtils.print(v);
        System.out.println();
        ArrayUtils.print(u);
        System.out.println();

        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            d[i] = u[i] - v[i];
        }
        ArrayUtils.print(d);
        System.out.println();

        double s = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                s += d[i] * inv[i][j] * d[j];
            }
        }

        System.out.println(s);

        double[][] cinv = ArrayUtils.copy(inv);

        for (int i = 0; i < n; i++) {
            cinv[i][i] += 1e-7;
        }

        double[][] sqr = MatrixUtils.sqrt(cinv);
        ArrayUtils.print(sqr);

        double[] us = new double[n];
        double[] vs = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                us[i] += u[j] * sqr[j][i];
                vs[i] += v[j] * sqr[j][i];
            }
        }

        double[] ds = new double[n];
        for (int i = 0; i < n; i++) {
            ds[i] = us[i] - vs[i];
        }
        double t = 0;

        for (int i = 0; i < n; i++) {
            t += ds[i] * ds[i];
        }
        System.out.println(t);

    }
}
