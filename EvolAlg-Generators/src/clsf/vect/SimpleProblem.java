package clsf.vect;

import java.util.List;
import java.util.Random;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import clsf.Dataset;
import utils.ToDoubleArrayFunction;

public class SimpleProblem implements DoubleProblem {

    private static final long serialVersionUID = 1L;

    public final Converter converter;
    final List<Dataset> datasets;
    final ToDoubleArrayFunction<Dataset> errorFunction;

    final Random random = new Random();

    public SimpleProblem(Converter converter, ToDoubleArrayFunction<Dataset> errorFunction, List<Dataset> datasets) {
        this.converter = converter;
        this.errorFunction = errorFunction;
        this.datasets = datasets;
    }

    @Override
    public DoubleSolution createSolution() {
        if (datasets == null || datasets.isEmpty()) {
            return new DefaultDoubleSolution(this);
        } else {
            return converter.convert(this, datasets.get(random.nextInt(datasets.size())));
        }
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        int length = errorFunction.length();
        double[] error = errorFunction.apply(converter.convert(solution));
        for (int i = 0; i < length; i++) {
            solution.setObjective(i, error[i]);
        }
    }

    @Override
    public Double getLowerBound(int index) {
        return converter.getLowerBound();
    }

    @Override
    public String getName() {
        return converter.toString();
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return errorFunction.length();
    }

    @Override
    public int getNumberOfVariables() {
        return converter.getNumberOfVariables();
    }

    @Override
    public Double getUpperBound(int index) {
        return converter.getUpperBound();
    }

}
