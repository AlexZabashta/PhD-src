package clsf.vect;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import clsf.Dataset;

public class FixedConverter implements Converter {

    final static Double lowerBound = -10.0;
    final static Double upperBound = +10.0;

    final int numClasses;
    final int numFeatures;
    final int numObjects;
    final int numObjectsPerClass;

    public FixedConverter(int numObjectsPerClass, int numClasses, int numFeatures) {
        this.numClasses = numClasses;
        this.numFeatures = numFeatures;
        this.numObjectsPerClass = numObjectsPerClass;
        this.numObjects = numObjectsPerClass * numClasses;
    }

    @Override
    public DoubleSolution convert(DoubleProblem problem, Dataset dataset) {
        DoubleSolution solution = new DefaultDoubleSolution(problem);

        for (int sp = 0, label = 0; label < numClasses; label++) {
            for (int sid = 0; sid < numObjectsPerClass; sid++) {
                for (int fid = 0; fid < numFeatures; fid++) {
                    solution.setVariableValue(sp++, Math.min(upperBound, Math.max(lowerBound, dataset.dataPerClass[label][sid][fid])));
                }
            }
        }
        return solution;
    }

    @Override
    public Dataset convert(DoubleSolution solution) {

        double[][] data = new double[numObjects][numFeatures];
        int[] labels = new int[numObjects];

        for (int oid = 0, sp = 0, label = 0; label < numClasses; label++) {
            for (int sid = 0; sid < numObjectsPerClass; sid++) {
                for (int fid = 0; fid < numFeatures; fid++) {
                    data[oid][fid] = Double.parseDouble(solution.getVariableValueString(sp++));
                }
                labels[oid++] = label;
            }
        }

        return new Dataset("synthetic_direct", true, data, false, labels);
    }

    @Override
    public Double getLowerBound() {
        return lowerBound;
    }

    @Override
    public int getNumberOfVariables() {
        return numObjects * numFeatures;
    }

    @Override
    public Double getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return "FIXED";
    }
}
