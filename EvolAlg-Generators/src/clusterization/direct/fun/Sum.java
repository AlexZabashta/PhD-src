package clusterization.direct.fun;

import java.util.function.ToDoubleFunction;

public class Sum implements ToDoubleFunction<double[]> {
    public final ToDoubleFunction<double[]> left, right;

    public Sum(ToDoubleFunction<double[]> left, ToDoubleFunction<double[]> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return left.applyAsDouble(object) + right.applyAsDouble(object);
    }

}
