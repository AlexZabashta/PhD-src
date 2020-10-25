package clsf.ndse.gen_op.fun.cat;

import clsf.Dataset;

public class ClassValue implements CatFunction {

    public final Dataset dataset;

    public ClassValue(Dataset dataset) {
        this.dataset = dataset;
    }

    @Override
    public int applyAsInt(int object) {
        return dataset.labels[object];
    }

    @Override
    public int range() {
        return dataset.numClasses;
    }

}
