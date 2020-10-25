package experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.uma.jmetal.problem.Problem;

import clsf.Dataset;
import clsf.ndse.GDSProblem;
import clsf.ndse.gen_op.DatasetMutation;
import clsf.vect.Converter;
import clsf.vect.SimpleProblem;
import fitness_function.Limited;

public class Problems {

    public static List<Function<Limited, Problem<?>>> list(Converter dir, Converter gmm, DatasetMutation mutation, List<Dataset> initPopulation) {
        List<Function<Limited, Problem<?>>> list = new ArrayList<>();

        list.add(new Function<Limited, Problem<?>>() {
            @Override
            public Problem<?> apply(Limited limited) {
                return new GDSProblem(mutation, limited, initPopulation);
            }
        });

        list.add(new Function<Limited, Problem<?>>() {
            @Override
            public Problem<?> apply(Limited limited) {
                return new SimpleProblem(dir, limited, initPopulation);
            }
        });

        list.add(new Function<Limited, Problem<?>>() {
            @Override
            public Problem<?> apply(Limited limited) {
                return new SimpleProblem(gmm, limited, initPopulation);
            }
        });

        return list;
    }

}
