package clusterization.direct;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.CrossoverOperator;

import clusterization.Dataset;
import clusterization.MetaFeaturesExtractor;
import utils.RandomUtils;

public class Crossover implements CrossoverOperator<DataSetSolution> {

	final MetaFeaturesExtractor extractor;

	public Crossover(MetaFeaturesExtractor extractor) {
		this.extractor = extractor;
	}

	@Override
	public List<DataSetSolution> execute(List<DataSetSolution> source) {
		if (source.size() != 2) {
			throw new IllegalArgumentException("Source should have two datasets.");
		}

		Dataset objX = source.get(0).getDataset(), objY = source.get(1).getDataset();

		Random random = new Random(objX.hashCode + objY.hashCode);

		double[][] dataX = objX.data();
		double[][] dataY = objY.data();

		int sumNumFeatures = objX.numFeatures + objY.numFeatures;

		int newNumObjects = RandomUtils.randomFromSegment(random, objX.numObjects, objY.numObjects);

		dataX = RelationsGenerator.changeNumObjects(dataX, newNumObjects, objX.numFeatures, random);
		dataY = RelationsGenerator.changeNumObjects(dataY, newNumObjects, objY.numFeatures, random);

		int numFeaturesA = RandomUtils.randomFromSegment(random, objX.numFeatures, objY.numFeatures);
		int numFeaturesB = sumNumFeatures - numFeaturesA;

		boolean[] f = RandomUtils.randomSelection(sumNumFeatures, numFeaturesA, random);

		double[][] newDataA = new double[newNumObjects][numFeaturesA];
		double[][] newDataB = new double[newNumObjects][numFeaturesB];

		for (int oid = 0; oid < newNumObjects; oid++) {
			for (int a = 0, b = 0, fid = 0; fid < sumNumFeatures; fid++) {
				double val;
				if (fid < objX.numFeatures) {
					val = dataX[oid][fid];
				} else {
					val = dataY[oid][fid - objX.numFeatures];
				}

				if (f[fid]) {
					newDataA[oid][a++] = val;
				} else {
					newDataB[oid][b++] = val;
				}
			}
		}

		DataSetSolution offspringA = new DataSetSolution(new Dataset(newDataA, extractor));
		DataSetSolution offspringB = new DataSetSolution(new Dataset(newDataB, extractor));

		return Arrays.asList(offspringA, offspringB);
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		return 2;
	}

	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}

}
