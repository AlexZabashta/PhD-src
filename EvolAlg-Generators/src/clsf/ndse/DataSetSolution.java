package clsf.ndse;

import java.util.HashMap;
import java.util.Map;

import org.uma.jmetal.solution.Solution;

import clsf.Dataset;

public class DataSetSolution implements Solution<Dataset> {

    private static final long serialVersionUID = 1L;
    private Dataset dataset;
    private double[] error;

    final Map<Object, Object> map = new HashMap<>();

    public DataSetSolution(Dataset dataset) {
        this.dataset = dataset;
    }

    public DataSetSolution(Dataset dataset, double[] error) {
        this.dataset = dataset;
        this.error = error;
    }

    @Override
    public Solution<Dataset> copy() {
        return new DataSetSolution(dataset, error);
    }

    @Override
    public Object getAttribute(Object id) {
        return map.get(id);
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public int getNumberOfObjectives() {
        return 1;
    }

    @Override
    public int getNumberOfVariables() {
        return 1;
    }

    @Override
    public double getObjective(int index) {
        return error[index];
    }

    @Override
    public double[] getObjectives() {
        return error.clone();
    }

    @Override
    public Dataset getVariableValue(int index) {
        return dataset;
    }

    @Override
    public String getVariableValueString(int index) {
        return dataset.name + "_" + dataset.hashCode();
    }

    @Override
    public void setAttribute(Object id, Object value) {
        map.put(id, value);
    }

    @Override
    public void setObjective(int index, double value) {
        error[index] = value;
    }

    public void setObjectives(double[] error) {
        this.error = error;
    }

    @Override
    public void setVariableValue(int index, Dataset value) {
        dataset = value;
    }

}
