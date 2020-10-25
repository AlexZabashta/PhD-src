package fitness_function;

import java.util.List;
import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import utils.ToDoubleArrayFunction;

public class DataDiversity implements ToDoubleFunction<Dataset>, ToDoubleArrayFunction<Dataset> {

    final List<Dataset> datasets;

    final ToDoubleArrayFunction<Dataset> extractor;
    final MahalanobisDistance metric;
    final int length;
    final double[] min, max;

    public DataDiversity(double[] min, double[] max, List<Dataset> datasets, ToDoubleArrayFunction<Dataset> extractor, MahalanobisDistance metric) {
        this.datasets = datasets;
        this.extractor = extractor;
        this.metric = metric;
        this.min = min;
        this.max = max;
        this.length = extractor.length();

        if (length != min.length) {
            throw new IllegalArgumentException("extractor.length != min.length");
        }

        if (length != max.length) {
            throw new IllegalArgumentException("extractor.length != max.length");
        }

        if (length != metric.dim()) {
            throw new IllegalArgumentException("extractor.length != metric.dim");
        }

    }

    @Override
    public double applyAsDouble(Dataset dataset) {
        if (dataset.numClasses == 1) {
            return 1000;
        }

        double[] u = extractor.apply(dataset);
        double[] e = new double[length];

        for (int i = 0; i < length; i++) {
            if (u[i] < min[i]) {
                e[i] += min[i] - u[i];
            }

            if (u[i] > max[i]) {
                e[i] += u[i] - max[i];
            }
        }

        double minDist = Double.POSITIVE_INFINITY;

        for (Dataset d : datasets) {
            minDist = Math.min(minDist, metric.distance(u, extractor.apply(d)));
        }

        return length + metric.distance(e) - minDist;
    }

    @Override
    public double[] apply(Dataset dataset) {
        return new double[] { applyAsDouble(dataset) };
    }

    @Override
    public int length() {
        return 1;
    }

}
