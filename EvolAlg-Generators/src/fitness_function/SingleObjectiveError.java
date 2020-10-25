package fitness_function;

import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import utils.ToDoubleArrayFunction;

public class SingleObjectiveError implements ToDoubleArrayFunction<Dataset>, ToDoubleFunction<Dataset> {

    final ToDoubleBiFunction<double[], double[]> distance;

    final ToDoubleArrayFunction<Dataset> extractor;
    final double[] target;

    public SingleObjectiveError(ToDoubleBiFunction<double[], double[]> distance, ToDoubleArrayFunction<Dataset> extractor, double[] target) {
        if (target.length != extractor.length()) {
            throw new IllegalArgumentException("target.length != extractor.length()");
        }

        this.target = target;
        this.extractor = extractor;
        this.distance = distance;
    }

    @Override
    public double[] apply(Dataset dataset) {
        return new double[] { applyAsDouble(dataset) };
    }

    @Override
    public double applyAsDouble(Dataset dataset) {
        try {
            return distance.applyAsDouble(extractor.apply(dataset), target);
        } catch (RuntimeException exception) {
            return 100;
        }
    }

    @Override
    public int length() {
        return 1;
    }

}
