package clusterization.direct.fun;

import java.util.function.ToDoubleFunction;

public class Abs implements ToDoubleFunction<double[]> {
    public final ToDoubleFunction<double[]> node;

    public Abs(ToDoubleFunction<double[]> node) {
        this.node = node;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return Math.abs(node.applyAsDouble(object));
    }

}
