package clusterization.direct.fun;

import java.util.Random;
import java.util.function.ToDoubleFunction;

public class RandomFunction {

    public static ToDoubleFunction<double[]> generate(Random random, int numFeatures, int level) {
        double p = random.nextDouble();

        if (level <= 0) {
            if (p < 0.5 && numFeatures > 0) {
                return new AttributeValue(random.nextInt(numFeatures));
            } else {
                if (p < 0.75) {
                    return new ConstValue(random.nextGaussian());
                } else {
                    return new NoiesValue(random);
                }
            }
        } else {

            if (p < 0.5) {
                if (p < 0.25) {
                    return new Sin(generate(random, numFeatures, level - 1));
                } else {
                    return new Abs(generate(random, numFeatures, level - 1));
                }
            } else {
                if (p < 0.75) {
                    return new Sum(generate(random, numFeatures, level - 1), generate(random, numFeatures, level - 1));
                } else {
                    return new Mul(generate(random, numFeatures, level - 1), generate(random, numFeatures, level - 1));
                }
            }
        }
    }
}
