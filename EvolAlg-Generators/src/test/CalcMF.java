package test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import clsf.Dataset;
import mfextraction.CMFExtractor;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class CalcMF {

    public static final int A_LIMIT = 128;
    public static final int I_LIMIT = 256;
    public static final int C_LIMIT = 5;

    public static void main(String[] args) throws IOException {

        Map<String, String> target = new HashMap<>();

        try (CSVParser parser = new CSVParser(new FileReader("data.csv"), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                target.put(record.get("id") + ".arff", record.get("target"));
            }
        }

        CMFExtractor extractor = new CMFExtractor();

        for (File file : new File("data").listFiles()) {

            String className = target.get(file.getName());
            if (className == null) {
                continue;
            }
            className = className.toLowerCase();

            try (FileReader reader = new FileReader(file)) {
                Instances instances = new Instances(reader);

                for (int i = 0; i < instances.numAttributes(); i++) {
                    if (instances.attribute(i).name().toLowerCase().equals(className)) {
                        instances.setClassIndex(i);
                    }
                }

                if (instances.classIndex() < 0) {
                    continue;
                }

                if (instances.numClasses() < 2 || instances.numClasses() > C_LIMIT) {
                    continue;
                }

                Filter rmv = new ReplaceMissingValues();
                rmv.setInputFormat(instances);
                instances = Filter.useFilter(instances, rmv);

                if (instances.numAttributes() > A_LIMIT || instances.numInstances() > I_LIMIT) {
                    continue;
                }

                Filter ntb = new NominalToBinary();
                ntb.setInputFormat(instances);
                instances = Filter.useFilter(instances, ntb);

                if (instances.numAttributes() > A_LIMIT || instances.numInstances() > I_LIMIT) {
                    continue;
                }

                int numObjects = instances.numInstances();
                int numFeatures = instances.numAttributes() - 1;

                double[][] data = new double[numObjects][numFeatures];
                int[] labels = new int[numObjects];

                for (int oid = 0; oid < numObjects; oid++) {
                    Instance instance = instances.get(oid);
                    for (int fid = 0, aid = 0; aid < instances.numAttributes(); aid++) {
                        if (aid == instances.classIndex()) {
                            continue;
                        }
                        data[oid][fid++] = instance.value(aid);
                    }

                    labels[oid] = (int) instance.classValue();
                }

                Dataset dataset = new Dataset(file.getName(), Dataset.defaultNormValues, data, Dataset.defaultNormLabels, labels);

                System.out.println(file.getName());
                System.out.println(Arrays.toString(extractor.apply(dataset)));
                // System.out.println(Arrays.toString(mf));
                System.out.flush();

            } catch (Exception e) {
                System.err.println(file.getName());
                e.printStackTrace();
            }
        }
    }

}
