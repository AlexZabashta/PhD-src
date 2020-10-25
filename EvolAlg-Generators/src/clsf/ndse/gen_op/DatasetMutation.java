package clsf.ndse.gen_op;

import java.util.Random;

import org.uma.jmetal.operator.MutationOperator;

import clsf.Dataset;
import clsf.WekaConverter;
import clsf.ndse.DataSetSolution;
import utils.ArrayUtils;
import utils.RandomUtils;

public class DatasetMutation implements MutationOperator<DataSetSolution> {
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        Dataset.defaultNormValues = false;

        Random random = new Random();

        DatasetMutation mutation = new DatasetMutation(11, 20, 2, 10, 2, 5);

        Dataset dataset = mutation.generate(random);

        for (int i = 0; i < 10; i++) {

            ArrayUtils.print(dataset.data);
            System.out.println(WekaConverter.convert(dataset));
            dataset = mutation.execute(dataset);
        }

    }

    public final int minNumClasses, maxNumClasses;
    public final int minNumFeatures, maxNumFeatures;
    public final int minNumObjects, maxNumObjects;

    public DatasetMutation(int minNumObjects, int maxNumObjects, int minNumFeatures, int maxNumFeatures, int minNumClasses, int maxNumClasses) {
        this.minNumObjects = minNumObjects;
        this.maxNumObjects = maxNumObjects;
        this.minNumFeatures = minNumFeatures;
        this.maxNumFeatures = maxNumFeatures;
        this.minNumClasses = minNumClasses;
        this.maxNumClasses = maxNumClasses;
    }

    public Dataset execute(Dataset dataset) {
        Random random = new Random(dataset.hashCode());
        Dataset mutant = executeAny(dataset, random);
        if (mutant.numClasses < minNumClasses || mutant.numClasses > maxNumClasses) {
            return dataset;
        }
        if (mutant.numFeatures < minNumFeatures || mutant.numFeatures > maxNumFeatures) {
            return dataset;
        }
        if (mutant.numObjects < minNumObjects || mutant.numObjects > maxNumObjects) {
            return dataset;
        }
        return mutant;
    }

    @Override
    public DataSetSolution execute(DataSetSolution source) {
        return new DataSetSolution(execute(source.getDataset()));
    }

    public Dataset executeAny(Dataset dataset, Random random) {

        if (random.nextBoolean()) {
            int newNumObjects = RandomUtils.randomLocal(random, dataset.numObjects, 10, minNumObjects, maxNumObjects);
            return ChangeNumObjects.apply(dataset, random, newNumObjects);
        } else {
            if (random.nextBoolean()) {
                int newNumFeatures = RandomUtils.randomLocal(random, dataset.numFeatures, 5, minNumFeatures, maxNumFeatures);
                return ChangeNumFeatures.apply(dataset, random, newNumFeatures);
            } else {
                int newNumClasses = RandomUtils.randomLocal(random, dataset.numClasses, 1, minNumClasses, maxNumClasses);
                return ChangeNumClasses.apply(dataset, random, newNumClasses);
            }
        }
    }

    public Dataset generate(Random random) {
        int numObjects = RandomUtils.randomFromSegment(random, minNumObjects, maxNumObjects);
        int numFeatures = RandomUtils.randomFromSegment(random, minNumFeatures, maxNumFeatures);
        int numClasses = RandomUtils.randomFromSegment(random, minNumClasses, maxNumClasses);

        double[][] data = new double[numObjects][2];
        int[] labels = new int[numObjects];

        for (int oid = 0; oid < numObjects; oid++) {
            labels[oid] = random.nextInt(numClasses);
            data[oid][0] = random.nextGaussian();
            data[oid][1] = random.nextGaussian() * data[oid][0] + random.nextGaussian() * labels[oid];
        }

        Dataset dataset = new Dataset("synthetic_ndse", true, data, true, labels);
        dataset = ChangeNumFeatures.apply(dataset, random, numFeatures);

        for (int attemp = 0; attemp < 10; attemp++) {
            if (dataset.numClasses >= minNumClasses) {
                break;
            }
            dataset = ChangeNumClasses.apply(dataset, random, numClasses);
        }

        return dataset;
    }
}
