package experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.gde3.GDE3Builder;
import org.uma.jmetal.algorithm.multiobjective.ibea.IBEABuilder;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder.Variant;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.omopso.OMOPSOBuilder;
import org.uma.jmetal.algorithm.multiobjective.paes.PAESBuilder;
import org.uma.jmetal.algorithm.multiobjective.randomsearch.RandomSearchBuilder;
import org.uma.jmetal.algorithm.multiobjective.smsemoa.SMSEMOABuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.algorithm.singleobjective.coralreefsoptimization.CoralReefsOptimizationBuilder;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolutionBuilder;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.CovarianceMatrixAdaptationEvolutionStrategy;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization.StandardPSO2011;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BestSolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import clsf.ndse.DataSetSolution;
import clsf.ndse.GDSProblem;
import clsf.ndse.gen_op.DatasetCrossover;
import clsf.ndse.gen_op.DatasetMutation;
import clsf.vect.GMMConverter;
import clsf.vect.SimpleProblem;

public class Algorithms {

    public static List<Function<Problem<?>, Algorithm<?>>> list(boolean singleObjective, DatasetCrossover crossover, DatasetMutation mutation) {
        List<Function<Problem<?>, Algorithm<?>>> list = new ArrayList<>();
        list.add(getCMAES(singleObjective));
        list.add(getCR(singleObjective, crossover, mutation));
        list.add(getDE(singleObjective));
        list.add(getGA(singleObjective, crossover, mutation));
        list.add(getGDE3());
        // list.add(getIBEA(singleObjective));
        list.add(getMOCell(crossover, mutation));
        list.add(getMOEAD());
        list.add(getNSGAII(crossover, mutation));
        list.add(getOMOPSO());
        // list.add(getPAES(crossover, mutation));
        list.add(getPSO(singleObjective, crossover, mutation));
        list.add(getRAND(crossover, mutation));
        list.add(getSMSEMOA(crossover, mutation));
        list.add(getSPEA2(crossover, mutation));

        return list;
    }

    public static Function<Problem<?>, Algorithm<?>> getCMAES(boolean singleObjective) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                if (singleObjective) {
                    try {
                        SimpleProblem simpleProblem = (SimpleProblem) problem;
                        if (simpleProblem.converter instanceof GMMConverter) {
                            return new CovarianceMatrixAdaptationEvolutionStrategy.Builder(simpleProblem).setMaxEvaluations(10000000).build();
                        }
                    } catch (ClassCastException cce) {
                    }
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getCR(boolean singleObjective, DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                if (singleObjective) {
                    try {
                        return new CoralReefsOptimizationBuilder<DoubleSolution>((DoubleProblem) problem, new BestSolutionSelection<>(new DominanceComparator<DoubleSolution>()), new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setMaxEvaluations(10000000).build();
                    } catch (ClassCastException cce) {
                    }
                    try {
                        return new CoralReefsOptimizationBuilder<DataSetSolution>((GDSProblem) problem, new BestSolutionSelection<>(new DominanceComparator<DataSetSolution>()), crossover, mutation).setMaxEvaluations(10000000).build();
                    } catch (ClassCastException cce) {
                    }
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getDE(boolean singleObjective) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                if (singleObjective) {
                    try {
                        return new DifferentialEvolutionBuilder((DoubleProblem) problem).setMaxEvaluations(10000000).build();
                    } catch (ClassCastException cce) {
                    }
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getGA(boolean singleObjective, DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                if (singleObjective) {
                    try {
                        return new GeneticAlgorithmBuilder<DoubleSolution>((DoubleProblem) problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setMaxEvaluations(10000000).build();
                    } catch (ClassCastException cce) {
                    }
                    try {
                        return new GeneticAlgorithmBuilder<DataSetSolution>((GDSProblem) problem, crossover, mutation).setMaxEvaluations(10000000).build();
                    } catch (ClassCastException cce) {
                    }
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getGDE3() {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new GDE3Builder((DoubleProblem) problem).setMaxEvaluations(10000000).setPopulationSize(32).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getIBEA(boolean singleObjective) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                if (singleObjective) {
                    try {
                        return new IBEABuilder((DoubleProblem) problem).setMaxEvaluations(10000000).setPopulationSize(32).build();
                    } catch (ClassCastException cce) {
                    }
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getMOCell(DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new MOCellBuilder<DataSetSolution>((GDSProblem) problem, crossover, mutation).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                try {
                    return new MOCellBuilder<DoubleSolution>((DoubleProblem) problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getMOEAD() {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new MOEADBuilder((DoubleProblem) problem, Variant.MOEAD).setMaxEvaluations(10000000).setPopulationSize(32).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getNSGAII(DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new NSGAIIBuilder<DataSetSolution>((GDSProblem) problem, crossover, mutation).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                try {
                    return new NSGAIIBuilder<DoubleSolution>((DoubleProblem) problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getOMOPSO() {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new OMOPSOBuilder((DoubleProblem) problem, new SequentialSolutionListEvaluator<>()).setMaxIterations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getPAES(DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new PAESBuilder<DataSetSolution>((GDSProblem) problem).setMutationOperator(mutation).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                try {
                    return new PAESBuilder<DoubleSolution>((DoubleProblem) problem).setMutationOperator(new PolynomialMutation()).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getPSO(boolean singleObjective, DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                if (singleObjective) {
                    try {
                        return new StandardPSO2011((DoubleProblem) problem, 100, 10000000, 10, new SequentialSolutionListEvaluator<DoubleSolution>());
                    } catch (ClassCastException cce) {
                    }
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getRAND(DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new RandomSearchBuilder<DataSetSolution>((GDSProblem) problem).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                try {
                    return new RandomSearchBuilder<DoubleSolution>((DoubleProblem) problem).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getSMSEMOA(DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new SMSEMOABuilder<DataSetSolution>((GDSProblem) problem, crossover, mutation).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                try {
                    return new SMSEMOABuilder<DoubleSolution>((DoubleProblem) problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setMaxEvaluations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }

    public static Function<Problem<?>, Algorithm<?>> getSPEA2(DatasetCrossover crossover, DatasetMutation mutation) {
        return new Function<Problem<?>, Algorithm<?>>() {
            @Override
            public Algorithm<?> apply(Problem<?> problem) {
                try {
                    return new SPEA2Builder<DataSetSolution>((GDSProblem) problem, crossover, mutation).setMaxIterations(10000000).build();
                } catch (ClassCastException cce) {
                }
                try {
                    return new SPEA2Builder<DoubleSolution>((DoubleProblem) problem, new SBXCrossover(1.0, 10.0), new PolynomialMutation()).setMaxIterations(10000000).build();
                } catch (ClassCastException cce) {
                }
                return null;
            }
        };
    }
}
