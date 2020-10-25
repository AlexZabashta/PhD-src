package clusterization.direct.fun;

import java.util.Random;
import java.util.function.ToDoubleFunction;

public class NoiesValue implements ToDoubleFunction<double[]> {
    public final Random random;

    public NoiesValue(Random random) {
        this.random = random;
    }

    @Override
    public double applyAsDouble(double[] object) {
        return random.nextGaussian();
    }

}
