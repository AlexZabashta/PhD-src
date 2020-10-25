package mfextraction;

import java.util.Arrays;

import clsf.Dataset;
import utils.ToDoubleArrayFunction;

public class CMFExtractor implements ToDoubleArrayFunction<Dataset> {
    final int lenght = 29;

    @Override
    public double[] apply(Dataset dataset) {
        MetaFeatures.evaluate(dataset);
        return Arrays.copyOf(dataset.metaFeatures, lenght);
    }

    @Override
    public int length() {
        return lenght;
    }

}
