package clusterization.direct.fun;

import java.util.function.ToDoubleFunction;

public class AttributeValue implements ToDoubleFunction<double[]> {
    public final int index;

    public AttributeValue(int index) {
        this.index = index;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return object[index];
    }

}
