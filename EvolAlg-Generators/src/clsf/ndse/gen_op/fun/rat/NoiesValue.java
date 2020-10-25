package clsf.ndse.gen_op.fun.rat;

import java.util.Random;

public class NoiesValue implements RatFunction {
    public final Random random;

    public NoiesValue(Random random) {
        this.random = random;
    }

    @Override
    public double applyAsDouble(int objectId) {
        return Math.max(-10, Math.min(+10, random.nextGaussian()));
    }

    @Override
    public double max() {
        return +10.0;
    }

    @Override
    public double min() {
        return -10.0;
    }

}
