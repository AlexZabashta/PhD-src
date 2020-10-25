package clusterization.direct.fun;

import java.util.function.ToDoubleFunction;

public class ConstValue implements ToDoubleFunction<double[]> {

    public final double value;

    public ConstValue(double value) {
        this.value = value;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return value;
    }
}
