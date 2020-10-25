package clsf.ndse.gen_op.fun.rat;

public class Sum implements RatFunction {
    public final RatFunction left, right;
    public final double min, max;

    public Sum(RatFunction left, RatFunction right) {
        this.left = left;
        this.right = right;

        this.min = left.min() + right.min();
        this.max = left.max() + right.max();

    }

    @Override
    public double applyAsDouble(int object) {
        return left.applyAsDouble(object) + right.applyAsDouble(object);
    }

    @Override
    public double max() {
        return max;
    }

    @Override
    public double min() {
        return min;
    }

}
