package clusterization.direct.fun;

import java.util.function.ToDoubleFunction;

public class Mul implements ToDoubleFunction<double[]> {
    public final ToDoubleFunction<double[]> left, right;

    public Mul(ToDoubleFunction<double[]> left, ToDoubleFunction<double[]> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return left.applyAsDouble(object) * right.applyAsDouble(object);
    }

}
