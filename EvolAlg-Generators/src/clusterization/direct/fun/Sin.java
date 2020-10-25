package clusterization.direct.fun;

import java.util.function.ToDoubleFunction;

public class Sin implements ToDoubleFunction<double[]> {
    public final ToDoubleFunction<double[]> node;

    public Sin(ToDoubleFunction<double[]> node) {
        this.node = node;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return Math.sin(node.applyAsDouble(object));
    }

}
