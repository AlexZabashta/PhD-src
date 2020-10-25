package fitness_function;

import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import mfextraction.TreeMetaSystem;
import utils.StatUtils;
import utils.ToDoubleArrayFunction;

public class MetaVariance implements ToDoubleFunction<Dataset>, ToDoubleArrayFunction<Dataset> {

    final ToDoubleArrayFunction<Dataset> base;

    public MetaVariance(ToDoubleArrayFunction<Dataset> base) {
        this.base = base;
    }

    @Override
    public double applyAsDouble(Dataset dataset) {
        if (dataset.numClasses == 1) {
            return 10;
        }
        return 1 - StatUtils.var(base.apply(dataset));
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
