package fitness_function;

import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import utils.EndSearch;
import utils.ToDoubleArrayFunction;

public class Limited implements ToDoubleArrayFunction<Dataset> {

    public final ToDoubleArrayFunction<Dataset> baseFunction;
    public double best = Double.POSITIVE_INFINITY;

    public final ToDoubleFunction<Dataset> cmpFunction;
    public Dataset dataset = null;
    public final double[] log;
    public int qid = 0;

    public Limited(ToDoubleArrayFunction<Dataset> baseFunction, ToDoubleFunction<Dataset> cmpFunction, int limit) {
        this.baseFunction = baseFunction;
        this.cmpFunction = cmpFunction;
        log = new double[limit];
    }

    @Override
    public double[] apply(Dataset dataset) {
        if (qid >= log.length) {
            throw new EndSearch();
        }

        double cmpValue = cmpFunction.applyAsDouble(dataset);

        if (cmpValue < best) {
            best = cmpValue;
            this.dataset = dataset;
        }
        log[qid++] = cmpValue;

        return baseFunction.apply(dataset);
    }

    public Limited clone() {
        return new Limited(baseFunction, cmpFunction, log.length);
    }

    @Override
    public int length() {
        return baseFunction.length();
    }

}
