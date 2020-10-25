package clusterization;

import java.util.function.Function;

public interface MetaFeaturesExtractor extends Function<Dataset, double[]> {
    int lenght();

    @Override
    default double[] apply(Dataset dataset) {
        return extract(dataset);
    }

    double[] extract(Dataset dataset);

    default String name(int index) {
        return "MF" + index;
    }
}
