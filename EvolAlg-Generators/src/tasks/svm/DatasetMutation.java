package tasks.svm;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

public class DatasetMutation implements MutationOperator<DoubleSolution> {

	private static final long serialVersionUID = 1L;
	double probability = 0.1;

	@Override
	public DoubleSolution execute(DoubleSolution solution) {
		Random random = new Random();

		return solution;
	}

}
