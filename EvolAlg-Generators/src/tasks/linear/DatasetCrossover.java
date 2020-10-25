package tasks.linear;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

public class DatasetCrossover implements CrossoverOperator<DoubleSolution> {

	private static final long serialVersionUID = 1L;
	final DoubleProblem problem;

	public DatasetCrossover(DoubleProblem problem) {
		this.problem = problem;
	}

	DoubleSolution stab(DoubleSolution solution) {

		Stabilization stabilization = new Stabilization();

		double[][][] dataset = new double[2][][];

		int n = FindDiff.n;
		int v = FindDiff.v;
		int m = FindDiff.m;

		int sp = 0;

		dataset[0] = new double[n][m + 1];
		dataset[1] = new double[v][m + 1];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= m; j++) {
				dataset[0][i][j] = (solution.getVariableValue(sp++));
			}
		}

		for (int i = 0; i < v; i++) {
			for (int j = 0; j <= m; j++) {
				dataset[1][i][j] = (solution.getVariableValue(sp++));
			}
		}

		// dataset = stabilization.process(dataset);
		DoubleSolution result = problem.createSolution();
		sp = 0;
		for (double[][] part : dataset) {
			for (double[] object : part) {
				for (double value : object) {
					result.setVariableValue(sp++, value);
				}
			}
		}

		return result;
	}

	@Override
	public List<DoubleSolution> execute(List<DoubleSolution> source) {
		Random random = new Random();
		DoubleSolution parent1 = stab(source.get(0)), parent2 = stab(source.get(1));
		DoubleSolution offspring1 = problem.createSolution(), offspring2 = problem.createSolution();

		for (int i = 0; i < problem.getNumberOfVariables(); i++) {

			// if (2 * i < problem.getNumberOfVariables()) {
			if (random.nextBoolean()) {
				offspring1.setVariableValue(i, parent1.getVariableValue(i));
				offspring2.setVariableValue(i, parent2.getVariableValue(i));
			} else {
				offspring1.setVariableValue(i, parent2.getVariableValue(i));
				offspring2.setVariableValue(i, parent1.getVariableValue(i));
			}
		}

		return Arrays.asList(offspring1, offspring2);
	}

	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		return 2;
	}

}
