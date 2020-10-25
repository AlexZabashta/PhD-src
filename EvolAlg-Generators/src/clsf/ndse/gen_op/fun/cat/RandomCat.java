package clsf.ndse.gen_op.fun.cat;

import java.util.Random;

public class RandomCat implements CatFunction {

    public final Random random;
    public final int range;

    public RandomCat(int range, Random random) {
        if (range < 1) {
            throw new IllegalArgumentException("range = " + range + " < 1");
        }
        this.range = range;
        this.random = random;
    }

    @Override
    public int applyAsInt(int object) {
        return random.nextInt(range);
    }

    @Override
    public int range() {
        return range;
    }

}
