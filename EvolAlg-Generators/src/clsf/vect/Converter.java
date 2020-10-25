package clsf.vect;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import clsf.Dataset;

public interface Converter {

    public int getNumberOfVariables();

    public DoubleSolution convert(DoubleProblem problem, Dataset dataset);

    public Dataset convert(DoubleSolution solution);

    public Double getLowerBound();

    public Double getUpperBound();

}
