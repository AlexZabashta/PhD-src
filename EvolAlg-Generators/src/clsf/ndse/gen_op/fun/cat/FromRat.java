package clsf.ndse.gen_op.fun.cat;

import clsf.ndse.gen_op.fun.rat.RatFunction;

public class FromRat implements CatFunction {

    public final RatFunction node;
    public final int range;
    public final double scale, offset;

    public FromRat(RatFunction node, int range) {
        this.node = node;
        if (range < 1) {
            throw new IllegalArgumentException("range = " + range + " < 1");
        }

        this.offset = node.min();
        this.scale = 1 / Math.max((node.max() - node.min()), 1e-3);
        this.range = range;
    }

    @Override
    public int applyAsInt(int objectId) {
        int catValue = (int) (((node.applyAsDouble(objectId) - offset) * scale) * (range - 1));
        return Math.max(0, Math.min(range - 1, catValue));
    }

    @Override
    public int range() {
        return range;
    }

}
