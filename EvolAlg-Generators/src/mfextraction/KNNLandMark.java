package mfextraction;

import java.util.Random;
import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import clsf.WekaConverter;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class KNNLandMark implements ToDoubleFunction<Dataset> {
    @Override
    public double applyAsDouble(Dataset dataset) {
        if (dataset.emptyKNN) {
            synchronized (dataset.metaFeatures) {
                if (dataset.emptyKNN) {
                    Random random = new Random();
                    Instances instances = WekaConverter.convert(dataset);
                    double mean = 0;
                    for (int rep = 0; rep < 10; rep++) {
                        mean += getFscore(random, instances);
                    }
                    dataset.metaFeatures[30] = mean / 10;
                    dataset.emptyKNN = false;
                }
            }
        }
        return dataset.metaFeatures[30];
    }

    double getFscore(Random random, Instances instances) {
        try {
            Evaluation evaluation = new Evaluation(instances);
            evaluation.crossValidateModel(new IBk(), instances, 10, random);
            double f = evaluation.weightedFMeasure();
            if (Double.isFinite(f)) {
                return f;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "KNN";
    }
}
