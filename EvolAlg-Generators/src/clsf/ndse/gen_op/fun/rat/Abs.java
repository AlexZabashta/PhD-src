package clsf.ndse.gen_op.fun.rat;

public class Abs implements RatFunction {
    public final double max;

    public final RatFunction node;

    public Abs(RatFunction node) {
        this.node = node;
        this.max = Math.max(-node.min(), node.max());
    }

    @Override
    public double applyAsDouble(int object) {
        return Math.abs(node.applyAsDouble(object));
    }

    @Override
    public double max() {
        return max;
    }

    @Override
    public double min() {
        return 0;
    }

}
