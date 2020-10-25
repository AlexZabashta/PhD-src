package clsf.ndse.gen_op.fun.rat;

import clsf.Dataset;

public class RatValue implements RatFunction {
    public final Dataset dataset;
    public final int feature;

    public RatValue(int feature, Dataset dataset) {
        this.feature = feature;
        this.dataset = dataset;
    }

    @Override
    public double applyAsDouble(int object) {
        return dataset.data[object][feature];
    }

    @Override
    public double max() {
        return dataset.max[feature];
    }

    @Override
    public double min() {
        return dataset.min[feature];
    }

}
