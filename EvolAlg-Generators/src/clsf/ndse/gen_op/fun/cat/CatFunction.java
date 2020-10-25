package clsf.ndse.gen_op.fun.cat;

import java.util.Random;
import java.util.function.IntUnaryOperator;

import clsf.Dataset;
import clsf.ndse.gen_op.fun.rat.RatFunction;

public interface CatFunction extends IntUnaryOperator {
    public static CatFunction random(Dataset dataset, Random random, int maxDepth) {
        return random(dataset, random, maxDepth, randomRange(random));
    }

    public static CatFunction random(Dataset dataset, Random random, int maxDepth, int range) {
        if (maxDepth <= 0) {
            return randomLeaf(dataset, random, range);
        } else {
            return randomNode(dataset, random, maxDepth, range);
        }
    }

    public static CatFunction randomLeaf(Dataset dataset, Random random, int range) {
        if (random.nextBoolean()) {
            if (random.nextBoolean()) {
                return new CatConst();
            } else {
                return new RandomCat(range, random);
            }
        } else {
            return new ClassValue(dataset);
        }
    }

    public static CatFunction randomNode(Dataset dataset, Random random, int maxDepth, int range) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth <= 0");
        }

        if (random.nextBoolean()) {
            RatFunction ratFunction = RatFunction.random(dataset, random, maxDepth - 1);
            return new FromRat(ratFunction, range);
        } else {
            CatFunction a = random(dataset, random, maxDepth - 1, range);
            CatFunction b = random(dataset, random, maxDepth - 1, range);
            return new SumMod(a, b, range, random);
        }
    }

    public static int randomRange(Random random) {
        return random.nextInt(10) + 1;
    }

    public int range();
}
