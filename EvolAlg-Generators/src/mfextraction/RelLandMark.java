package mfextraction;

import java.util.Random;
import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import clsf.WekaConverter;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class RelLandMark implements ToDoubleFunction<Dataset> {
    @Override
    public double applyAsDouble(Dataset dataset) {
        if (dataset.emptyREL) {
            synchronized (dataset.metaFeatures) {
                if (dataset.emptyREL) {
                    if (dataset.numClasses <= 1) {
                        dataset.metaFeatures[32] = 0;
                    } else {
                        Instances instances = WekaConverter.convert(dataset);
                        double knn = getKnnFscore(new Random(42), instances);
                        double svm = getSvmFscore(new Random(42), instances);
                        dataset.metaFeatures[32] = (knn - svm) / (knn + svm + 1e-3);
                    }
                    dataset.emptyREL = false;
                }
            }
        }
        return dataset.metaFeatures[32];
    }

    double getKnnFscore(Random random, Instances instances) {
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

    double getSvmFscore(Random random, Instances instances) {
        try {
            Evaluation evaluation = new Evaluation(instances);
            evaluation.crossValidateModel(new SMO(), instances, 10, random);
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
        return "REL";
    }
}
