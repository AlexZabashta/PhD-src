package tasks.svm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;

public class Plot {
	static final int root = 12;
	static final int size = root * root;
	static final int elen = size * 25;

	public static void main(String[] args) throws IOException {
		double[] id = new double[elen];
		for (int i = 0; i < elen; i++) {
			id[i] = i;
		}

		String[] names = { "DIRECT", "GMMK", "CORP" };

		final XYChart chart = new XYChartBuilder().width(800).height(450).title("F-score").xAxisTitle("Iteration").yAxisTitle("Error").build();

		for (String name : names) {
			double[] sum = new double[elen];
			int cnt = 0;

			for (File file : new File(name).listFiles()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					for (int i = 0; i < elen; i++) {
						sum[i] += Double.parseDouble(reader.readLine());
					}
				}
				++cnt;
			}

			for (int i = 0; i < elen; i++) {
				sum[i] /= cnt;
			}
			chart.addSeries(name, id, sum);

		}

		BitmapEncoder.saveBitmapWithDPI(chart, "plot", BitmapFormat.PNG, 300);
		// BitmapEncoder.saveBitmap(chart, "plot", BitmapFormat.PNG);

	}
}
