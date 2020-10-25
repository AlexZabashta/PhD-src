package experiments;

import java.io.File;
import java.util.Random;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import weka.clusterers.Canopy;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;

public class ClustTest {
	public static void main(String[] args) throws Exception {
		// Instances instances = new Instances(new FileReader(""));

		Random random = new Random();

		Instances instances;

		{
			CSVLoader loader = new CSVLoader();
			loader.setFile(new File("clust\\data_numbers.csv"));
			instances = loader.getDataSet();
		}
		{
			ArffLoader loader = new ArffLoader();

			File[] files = new File("data").listFiles();

			File file = files[random.nextInt(files.length)];

			loader.setFile(file);
			instances = loader.getDataSet();
			System.out.println(file);
		}

		Filter filter = new Standardize();
		filter.setInputFormat(instances);
		instances = Filter.useFilter(instances, filter);

		System.out.println(instances.numInstances());
		System.out.println(instances.numAttributes());

		int n = instances.numInstances();

		int p = 10;

		final XYChart chart = new XYChartBuilder().width(800).height(450).title("SMAPE").xAxisTitle("Iteration").yAxisTitle("Error").build();
		for (int i = 0; i < 1; i++) {
			double t1 = (i + 1.0) / 100;

			double[] x = new double[p];
			double[] y = new double[p];

			for (int j = 0; j < p; j++) {

				SimpleKMeans kMeans = new SimpleKMeans();
				kMeans.setNumClusters(7);

				SelectedTag method = new SelectedTag(SimpleKMeans.CANOPY, SimpleKMeans.TAGS_SELECTION);
				kMeans.setInitializationMethod(method);

				kMeans.setSeed(42);

				Canopy canopy = new Canopy();

				canopy.setNumClusters(7);

				double t2 = (j + 1.0) / p * 5;

				x[j] = t2;

				//kMeans.setCanopyT1(t1);
				kMeans.setCanopyT2(t2);

				kMeans.buildClusterer(instances);
				y[j] = kMeans.getSquaredError();
				//
				// ClusterEvaluation evaluation = new ClusterEvaluation();
				// evaluation.setClusterer(kMeans);
				// evaluation.evaluateClusterer(instances);

				// y[i] = evaluation.getNumClusters();

			}
			System.out.println(i);

			chart.addSeries("t1=" + t1, x, y);
		}

		BitmapEncoder.saveBitmapWithDPI(chart, "plot", BitmapFormat.PNG, 300);

	}
}
