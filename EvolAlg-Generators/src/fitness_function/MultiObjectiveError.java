package fitness_function;

import java.util.Arrays;

import clsf.Dataset;
import utils.ToDoubleArrayFunction;

public class MultiObjectiveError implements ToDoubleArrayFunction<Dataset> {

    final ToDoubleArrayFunction<Dataset> extractor;

    final int length;

    final double[] target, invSigma;

    public MultiObjectiveError(ToDoubleArrayFunction<Dataset> extractor, double[] target, double[] invSigma) {
        this.length = extractor.length();
        if (target.length != this.length) {
            throw new IllegalArgumentException("target.length != extractor.length()");
        }
        if (invSigma.length != this.length) {
            throw new IllegalArgumentException("invSigma.length != extractor.length()");
        }
        this.target = target;
        this.invSigma = invSigma;
        this.extractor = extractor;
    }

    @Override
    public double[] apply(Dataset dataset) {
        double[] diffs = new double[length];
        Arrays.fill(diffs, 100);
        try {
            double[] result = extractor.apply(dataset);
            for (int i = 0; i < length; i++) {
                diffs[i] = Math.abs(target[i] - result[i]) * invSigma[i];
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return diffs;
    }

    @Override
    public int length() {
        return length;
    }

}
