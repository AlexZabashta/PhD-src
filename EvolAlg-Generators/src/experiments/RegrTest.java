package experiments;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;

import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class RegrTest {

	public static double mape(Instances instances, int seed, double s) {
		try {
			Random random = new Random(seed);

			Instances train = new Instances(instances);
			Instances test = new Instances(instances);

			for (Instance instance : instances) {
				if (random.nextBoolean()) {
					train.add(instance);
				} else {
					test.add(instance);
				}
			}
			AdditiveRegression boosting = new AdditiveRegression();
			boosting.setMinimizeAbsoluteError(true);

			boosting.buildClassifier(train);
			boosting.setShrinkage(s);

			double mape = 0;

			for (Instance instance : test) {
				double pred = boosting.classifyInstance(instance);
				double real = instance.classValue();
				mape += Math.abs(real - pred) / real;
			}
			return mape / test.size();
		} catch (Exception e) {
			e.printStackTrace();
			return 1.0;
		}
	}

	public static void main(String[] args) throws Exception {
		Instances instances;

		{
			CSVLoader loader = new CSVLoader();
			loader.setFile(new File("regr\\data_numbers.csv"));
			instances = loader.getDataSet();

			for (int aid = 0; aid < instances.numAttributes(); aid++) {
				if (instances.attribute(aid).name().equals("verified_emb_company_location")) {
					instances.setClassIndex(aid);
				}
			}
		}
		{
			// CSVLoader loader = new CSVLoader();
			// loader.setFile(new File("regr\\data_price.csv"));
			// instances = loader.getDataSet();
			// instances.setClassIndex(0);

		}

		for (int i = 0; i < 10; i++) {
			if (i != 0) {
				System.out.print(',');
			}
			System.out.print(mape(instances, i, 1.0 - Math.exp(-0.001)));
		}

		// System.out.println(mape(instances, 1.0));
		// System.out.println(mape(instances, 1.0 - Math.exp(-3)));
		// System.out.println(mape(instances, 1.0 - Math.exp(-2)));

		final XYChart chart = new XYChartBuilder().width(800).height(450).title("MAPE(decay)").xAxisTitle("log(1 - decay)").yAxisTitle("MAPE").build();
		int p = 20;
		double[] x = new double[p];
		double[] y = new double[p];

		for (int j = 0; j < p; j++) {
			double ls = (j - p + 1) / Math.E;
			double s = 1 - Math.exp(ls);
			x[j] = ls;
			y[j] = mape(instances, j, s);

		}
		chart.addSeries("GB", x, y);
		BitmapEncoder.saveBitmapWithDPI(chart, "plot", BitmapFormat.PNG, 300);

	}
}
