package clsf.ndse.gen_op.fun.cat;

import java.util.Random;

import utils.RandomUtils;

public class SumMod implements CatFunction {

    public final CatFunction lft, rgt;
    private final int[] p, q, r;

    public SumMod(CatFunction lft, CatFunction rgt, int range, Random random) {
        this.lft = lft;
        this.rgt = rgt;

        this.p = RandomUtils.randomPermutation(lft.range(), random);
        this.q = RandomUtils.randomPermutation(rgt.range(), random);
        this.r = RandomUtils.randomPermutation(range, random);
    }

    @Override
    public int applyAsInt(int object) {
        int x = p[lft.applyAsInt(object)];
        int y = q[rgt.applyAsInt(object)];
        return r[(x + y) % range()];
    }

    @Override
    public int range() {
        return r.length;
    }

}
