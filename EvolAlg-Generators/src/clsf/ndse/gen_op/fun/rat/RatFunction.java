package clsf.ndse.gen_op.fun.rat;

import java.util.Random;
import java.util.function.IntToDoubleFunction;

import clsf.Dataset;
import clsf.ndse.gen_op.fun.cat.CatFunction;

public interface RatFunction extends IntToDoubleFunction {
    public static RatFunction random(Dataset dataset, Random random, int maxDepth) {
        if (maxDepth <= 0) {
            return randomLeaf(dataset, random);
        } else {
            return randomNode(dataset, random, maxDepth);
        }
    }

    public static RatFunction randomLeaf(Dataset dataset, Random random) {
        int n = dataset.numFeatures;
        if (n == 0 || random.nextBoolean()) {
            if (random.nextBoolean()) {
                return new NoiesValue(random);
            } else {
                return new RatConst(random.nextGaussian());
            }
        } else {
            return new RatValue(random.nextInt(n), dataset);
        }
    }

    public static RatFunction randomNode(Dataset dataset, Random random, int maxDepth) {

        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth <= 0");
        }

        if (random.nextBoolean()) {
            RatFunction a = random(dataset, random, maxDepth - 1);
            RatFunction b = random(dataset, random, maxDepth - 1);
            if (random.nextBoolean()) {
                return new Sum(a, b);
            } else {
                return new Mul(a, b);
            }
        } else {
            if (random.nextBoolean()) {
                CatFunction catFunction = CatFunction.random(dataset, random, maxDepth - 1);
                return new FromCat(catFunction, random);
            } else {
                RatFunction ratFuction = RatFunction.random(dataset, random, maxDepth - 1);

                if (random.nextBoolean()) {
                    return new Abs(ratFuction);
                } else {
                    return new Sin(ratFuction);
                }
            }
        }
    }

    public double max();

    public double min();

}
