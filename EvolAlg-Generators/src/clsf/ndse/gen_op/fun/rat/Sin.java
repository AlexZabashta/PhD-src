package clsf.ndse.gen_op.fun.rat;

public class Sin implements RatFunction {
    public final RatFunction node;

    public Sin(RatFunction node) {
        this.node = node;
    }

    @Override
    public double applyAsDouble(int object) {
        return Math.sin(node.applyAsDouble(object));
    }

    @Override
    public double max() {
        return +1.0;
    }

    @Override
    public double min() {
        return -1.0;
    }

}
