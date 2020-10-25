package fitness_function;

import java.util.Arrays;
import java.util.List;

import clsf.Dataset;
import utils.ToDoubleArrayFunction;

public class MultiDiversity implements ToDoubleArrayFunction<Dataset> {

    final List<Dataset> datasets;

    final ToDoubleArrayFunction<Dataset> extractor;
    final int length;
    final double[] min, max, invSigma;

    public MultiDiversity(double[] min, double[] max, List<Dataset> datasets, ToDoubleArrayFunction<Dataset> extractor, double[] invSigma) {
        this.datasets = datasets;
        this.extractor = extractor;
        this.min = min;
        this.max = max;
        this.invSigma = invSigma;
        this.length = extractor.length();

        if (length != min.length) {
            throw new IllegalArgumentException("extractor.length != min.length");
        }

        if (length != max.length) {
            throw new IllegalArgumentException("extractor.length != max.length");
        }
        if (length != invSigma.length) {
            throw new IllegalArgumentException("extractor.length != invSigma.length");
        }
    }

    @Override
    public double[] apply(Dataset dataset) {
        double[] u = extractor.apply(dataset);
        double[] minDist = new double[length];

        Arrays.fill(minDist, Double.POSITIVE_INFINITY);

        for (Dataset d : datasets) {
            double[] v = extractor.apply(d);
            for (int i = 0; i < length; i++) {
                minDist[i] = Math.min(minDist[i], Math.abs(v[i] - u[i]));
            }
        }

        double[] e = new double[length];
        for (int i = 0; i < length; i++) {
            if (u[i] < min[i]) {
                e[i] += min[i] - u[i];
            }

            if (u[i] > max[i]) {
                e[i] += u[i] - max[i];
            }

            e[i] -= minDist[i];
            e[i] *= invSigma[i];
        }
        return e;
    }

    @Override
    public int length() {
        return length;
    }

}
