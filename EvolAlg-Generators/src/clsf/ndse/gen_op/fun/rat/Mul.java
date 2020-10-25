package clsf.ndse.gen_op.fun.rat;

public class Mul implements RatFunction {
    public final RatFunction left, right;
    public final double min, max;

    public Mul(RatFunction left, RatFunction right) {
        this.left = left;
        this.right = right;
        double ll = left.min() * right.min();
        double lr = left.min() * right.max();
        double rl = left.max() * right.min();
        double rr = left.max() * right.max();

        this.min = Math.min(Math.min(ll, lr), Math.min(rl, rr));
        this.max = Math.max(Math.max(ll, lr), Math.max(rl, rr));

    }

    @Override
    public double applyAsDouble(int object) {
        return left.applyAsDouble(object) * right.applyAsDouble(object);
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
