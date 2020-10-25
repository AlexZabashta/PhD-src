package mfextraction;

import java.util.Arrays;

import clsf.Dataset;
import clsf.WekaConverter;
import mfextraction.decisiontree.WrappedC45DecisionTree;
import mfextraction.decisiontree.WrappedC45ModelSelection;
import mfextraction.decisiontree.pruned.PrunedTreeDevAttr;
import mfextraction.decisiontree.pruned.PrunedTreeDevBranch;
import mfextraction.decisiontree.pruned.PrunedTreeDevClass;
import mfextraction.decisiontree.pruned.PrunedTreeDevLevel;
import mfextraction.decisiontree.pruned.PrunedTreeHeight;
import mfextraction.decisiontree.pruned.PrunedTreeLeavesNumber;
import mfextraction.decisiontree.pruned.PrunedTreeMaxAttr;
import mfextraction.decisiontree.pruned.PrunedTreeMaxBranch;
import mfextraction.decisiontree.pruned.PrunedTreeMaxClass;
import mfextraction.decisiontree.pruned.PrunedTreeMaxLevel;
import mfextraction.decisiontree.pruned.PrunedTreeMeanAttr;
import mfextraction.decisiontree.pruned.PrunedTreeMeanBranch;
import mfextraction.decisiontree.pruned.PrunedTreeMeanClass;
import mfextraction.decisiontree.pruned.PrunedTreeMeanLevel;
import mfextraction.decisiontree.pruned.PrunedTreeMinClass;
import mfextraction.decisiontree.pruned.PrunedTreeNodeNumber;
import mfextraction.decisiontree.pruned.PrunedTreeWidth;
import utils.MatrixUtils;
import utils.StatUtils;
import utils.StatisticalUtils;
import weka.classifiers.trees.j48.ModelSelection;
import weka.core.Instances;

public class MetaFeatures {

    public static void evaluate(Dataset dataset) {
        if (dataset.emptyMF) {
            synchronized (dataset.metaFeatures) {
                if (dataset.emptyMF) {
                    int numFeatures = dataset.numFeatures;
                    int numObjects = dataset.numObjects;

                    double[] mean = new double[numFeatures];
                    double[][] tdata = MatrixUtils.transpose(numObjects, numFeatures, dataset.data);

                    calcCov(dataset, mean);
                    calcStat(dataset, tdata, mean);
                    calcClassVar(dataset);
                    calcClassDist(dataset, tdata);
                    calcTreeMF(dataset);
                    dataset.emptyMF = false;
                }
            }
        }
    }

    private static void calcCov(Dataset dataset, double[] mean) {
        if (dataset.numFeatures <= 1) {
            return;
        }

        double[][] cov = StatUtils.covarianceMatrix(dataset.numObjects, dataset.numFeatures, dataset.data, mean);
        double meanCorrelation = 0;

        for (int i = 0; i < dataset.numFeatures; i++) {
            for (int j = 0; j < i; j++) {
                meanCorrelation += cov[i][j];
            }
        }
        dataset.metaFeatures[0] = 2 * meanCorrelation / dataset.numFeatures / (dataset.numFeatures - 1);
    }

    private static void calcStat(Dataset dataset, double[][] tdata, double[] mean) {
        double meanKurtosis = 0;
        double meanSkewness = 0;

        for (int fid = 0; fid < dataset.numFeatures; fid++) {
            double variance = StatisticalUtils.variance(tdata[fid], mean[fid]);
            if (variance < 1e-7) {
                meanKurtosis += 1.5;
                meanSkewness += 1 / Math.sqrt(2);
            } else {
                meanKurtosis += StatisticalUtils.centralMoment(tdata[fid], 4, mean[fid]) / Math.pow(variance, 2);
                meanSkewness += StatisticalUtils.centralMoment(tdata[fid], 3, mean[fid]) / Math.pow(variance, 1.5);
            }
        }
        dataset.metaFeatures[1] = meanKurtosis / dataset.numFeatures;
        dataset.metaFeatures[2] = meanSkewness / dataset.numFeatures;
    }

    private static void calcClassVar(Dataset dataset) {
        double meanClassVar = 0;
        double minClassVar = Double.POSITIVE_INFINITY;
        double maxClassVar = Double.NEGATIVE_INFINITY;

        for (int fid = 0; fid < dataset.numFeatures; fid++) {
            double classVar = 0;

            for (int label = 0; label < dataset.numClasses; label++) {
                double[][] dpc = dataset.dataPerClass[label];

                double sum0 = 0;
                double sum1 = 0;
                double sum2 = 0;

                for (double[] object : dpc) {
                    double v = object[fid];
                    double p = 1;
                    sum0 += p;
                    p *= v;
                    sum1 += p;
                    p *= v;
                    sum2 += p;
                }

                if (sum0 > 0) {
                    double var = (sum2 - sum1 * sum1 / sum0) / sum0;
                    classVar += var * dataset.classDistribution[label];
                }
            }

            classVar /= dataset.numObjects;
            meanClassVar += classVar;
            minClassVar = Math.min(minClassVar, classVar);
            maxClassVar = Math.max(maxClassVar, classVar);
        }

        dataset.metaFeatures[3] = meanClassVar / dataset.numFeatures;
        dataset.metaFeatures[4] = minClassVar;
        dataset.metaFeatures[5] = maxClassVar;
    }

    private static void calcClassDist(Dataset dataset, double[][] tdata) {
        double meanInClassDist = 0;
        double minInClassDist = Double.POSITIVE_INFINITY;
        double maxInClassDist = Double.NEGATIVE_INFINITY;

        double meanOutClassDist = 0;
        double minOutClassDist = Double.POSITIVE_INFINITY;
        double maxOutClassDist = Double.NEGATIVE_INFINITY;

        for (int fid = 0; fid < dataset.numFeatures; fid++) {
            double inClassDist = 0;

            double cntIn = 0;

            for (int label = 0; label < dataset.numClasses; label++) {
                double[][] dpc = dataset.dataPerClass[label];

                double[] v = new double[dpc.length];

                for (int oid = 0; oid < v.length; oid++) {
                    v[oid] = dpc[oid][fid];
                }
                inClassDist += dist(v);

                cntIn += dpc.length * (dpc.length - 1) / 2;
            }

            double allDist = dist(tdata[fid].clone());
            double outClassDist = allDist - inClassDist;

            if (cntIn > 0) {
                inClassDist /= cntIn;
            }

            double cntOut = dataset.numObjects * (dataset.numObjects - 1) / 2;
            if (cntOut > 0) {
                outClassDist /= cntOut;
            }

            meanInClassDist += inClassDist;
            minInClassDist = Math.min(minInClassDist, inClassDist);
            maxInClassDist = Math.max(maxInClassDist, inClassDist);

            meanOutClassDist += outClassDist;
            minOutClassDist = Math.min(minOutClassDist, outClassDist);
            maxOutClassDist = Math.max(maxOutClassDist, outClassDist);

        }

        dataset.metaFeatures[6] = meanInClassDist / dataset.numFeatures;
        dataset.metaFeatures[7] = minInClassDist;
        dataset.metaFeatures[8] = maxInClassDist;

        dataset.metaFeatures[9] = meanOutClassDist / dataset.numFeatures;
        dataset.metaFeatures[10] = minOutClassDist;
        dataset.metaFeatures[11] = maxOutClassDist;
    }

    private static void calcTreeMF(Dataset dataset) {
        int mfid = 12;

        try {

            Instances instances = WekaConverter.convert(dataset);
            ModelSelection modelSelection = new WrappedC45ModelSelection(instances);
            WrappedC45DecisionTree tree = new WrappedC45DecisionTree(modelSelection, true);
            tree.buildClassifier(instances);

            dataset.metaFeatures[mfid++] = (new PrunedTreeDevAttr()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeDevBranch()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeDevLevel()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeHeight()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeLeavesNumber()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMaxAttr()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMaxBranch()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMaxLevel()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMeanAttr()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMeanBranch()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMeanLevel()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeNodeNumber()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeWidth()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeDevClass()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMaxClass()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMinClass()).extractValue(tree);
            dataset.metaFeatures[mfid++] = (new PrunedTreeMeanClass()).extractValue(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double dist(double[] x) {
        Arrays.sort(x);

        double dist = 0;

        double sum = 0;
        double cnt = 0;

        for (double v : x) {
            dist += v * cnt - sum;
            sum += v;
            cnt += 1;
        }

        return 2 * dist;
    }

}
