package clusterization.direct;

import java.util.Arrays;
import java.util.Random;

import org.uma.jmetal.operator.MutationOperator;

import clusterization.Dataset;
import clusterization.MetaFeaturesExtractor;
import clusterization.direct.fun.RandomFunction;
import utils.ArrayUtils;
import utils.RandomUtils;

public class Mutation implements MutationOperator<DataSetSolution> {

    final MetaFeaturesExtractor extractor;
    final long seed;
    final int minNumObjects, maxNumObjects;
    final int minNumFeatures, maxNumFeatures;

    public Mutation(int minNumObjects, int maxNumObjects, int minNumFeatures, int maxNumFeatures, MetaFeaturesExtractor extractor) {
        this(minNumObjects, maxNumObjects, minNumFeatures, maxNumFeatures, extractor, 0);
    }

    public Mutation(int minNumObjects, int maxNumObjects, int minNumFeatures, int maxNumFeatures, MetaFeaturesExtractor extractor, long seed) {
        this.extractor = extractor;
        this.seed = seed;
        this.minNumObjects = minNumObjects;
        this.maxNumObjects = maxNumObjects;
        this.minNumFeatures = minNumFeatures;
        this.maxNumFeatures = maxNumFeatures;
    }

    public static void main(String[] args) {
        Dataset.normalize = false;
        MetaFeaturesExtractor extractor = new MetaFeaturesExtractor() {
            @Override
            public int lenght() {
                return 2;
            }

            @Override
            public double[] extract(Dataset dataset) {
                return new double[] { dataset.numObjects, dataset.numFeatures };
            }
        };

        Random random = new Random();

        int numObjects = random.nextInt(10) + 1;
        int numFeatures = random.nextInt(10) + 1;

        double[][] data = new double[numObjects][numFeatures];

        for (int i = 0; i < numObjects; i++) {
            for (int j = 0; j < numFeatures; j++) {
                data[i][j] = i + j / 10.0;
            }
        }

        Dataset dataset = new Dataset(data, extractor);

        for (int rep = 0; rep < 10; rep++) {
            Mutation mutation = new Mutation(1, 2, 1, 2, extractor, random.nextLong());
            System.out.println(Arrays.toString(dataset.metaFeatures()));
            ArrayUtils.print(dataset.data());
            dataset = mutation.execute(dataset);
        }

    }

    public Dataset execute(Dataset dataset) {
        double[][] data = dataset.data();
        Random random = new Random(seed ^ dataset.hashCode);
        double[][] newData;
        int numObjects = dataset.numObjects;
        int numFeatures = dataset.numFeatures;

        if (random.nextBoolean() && (minNumObjects != maxNumObjects || numObjects != minNumObjects)) {
            if (numObjects < minNumObjects) {
                newData = RelationsGenerator.changeNumObjects(data, minNumObjects, numFeatures, random);
            } else {
                if (numObjects > maxNumObjects) {
                    newData = RelationsGenerator.changeNumObjects(data, maxNumObjects, numFeatures, random);
                } else {
                    int newNumObjects = RandomUtils.randomLocal(random, numObjects, 5, minNumObjects, maxNumObjects);
                    newData = RelationsGenerator.changeNumObjects(data, newNumObjects, numFeatures, random);
                }
            }
        } else {
            if (numFeatures < minNumFeatures) {
                newData = RelationsGenerator.changeNumFeatures(data, numFeatures, minNumFeatures, random);
            } else {
                if (numFeatures > maxNumFeatures) {
                    newData = RelationsGenerator.changeNumFeatures(data, numFeatures, maxNumFeatures, random);
                } else {

                    if (minNumFeatures == maxNumFeatures) {
                        newData = data;
                        int d = random.nextInt(4) + 3;
                        for (int rep = 0; rep < 5; rep++) {
                            int fid = random.nextInt(numFeatures);
                            RelationsGenerator.apply(RandomFunction.generate(random, numFeatures, d), data, fid);
                        }

                    } else {
                        int newNumFeatures = RandomUtils.randomLocal(random, numFeatures, 5, minNumFeatures, maxNumFeatures);
                        newData = RelationsGenerator.changeNumFeatures(data, numFeatures, newNumFeatures, random);
                    }

                }
            }
        }
        return new Dataset(newData, extractor);
    }

    @Override
    public DataSetSolution execute(DataSetSolution source) {
        return new DataSetSolution(execute(source.getDataset()));
    }

}
