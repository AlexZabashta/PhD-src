package experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import clsf.Dataset;
import clsf.WekaConverter;
import clsf.ndse.gen_op.ChangeNumClasses;
import clsf.ndse.gen_op.ChangeNumFeatures;
import clsf.ndse.gen_op.ChangeNumObjects;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class DataReader {

    public static final int MAX_FEATURES = 16;
    public static final int MAX_OBJECTS = 256;
    public static final int MAX_CLASSES = 5;

    public static List<Dataset> readData(String description, File folder) throws IOException {

        Map<String, String> target = new HashMap<>();

        try (CSVParser parser = new CSVParser(new FileReader(description), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                target.put(record.get("id") + ".arff", record.get("target"));
            }
        }

        List<Dataset> datasets = new ArrayList<>();

        for (File file : folder.listFiles()) {

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

                if (instances.numClasses() < 2 || instances.numInstances() < 20 || instances.numAttributes() < 2) {
                    continue;
                }

                if (instances.numAttributes() > MAX_FEATURES * 10 || instances.numInstances() > MAX_OBJECTS * 10) {
                    continue;
                }

                Filter rmv = new ReplaceMissingValues();
                rmv.setInputFormat(instances);
                instances = Filter.useFilter(instances, rmv);

                Filter ntb = new NominalToBinary();
                ntb.setInputFormat(instances);
                instances = Filter.useFilter(instances, ntb);

                if (instances.numAttributes() > MAX_FEATURES * 10 || instances.numInstances() > MAX_OBJECTS * 10) {
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

                if (dataset.numClasses > MAX_CLASSES) {
                    dataset = ChangeNumClasses.removeRareClasses(dataset, new Random(dataset.hashCode()), MAX_CLASSES);
                }

                if (dataset.numObjects > MAX_OBJECTS) {
                    dataset = ChangeNumObjects.apply(dataset, new Random(dataset.hashCode()), MAX_OBJECTS);
                }

                if (dataset.numFeatures > MAX_FEATURES) {
                    dataset = ChangeNumFeatures.apply(dataset, new Random(dataset.hashCode()), MAX_FEATURES);
                }

                boolean cnt = false;
                for (int sub : dataset.classDistribution) {
                    cnt |= sub < 5;
                }
                if (cnt) {
                    continue;
                }

                datasets.add(dataset);

                System.out.println(file.getName());
                // System.out.println(Arrays.toString(mf));
                System.out.flush();

            } catch (Exception e) {
                System.err.println(file.getName());
                e.printStackTrace();
            }
        }

        return datasets;
    }

    static class Result implements Comparable<Result> {
        public final String opt, prob, data, opt_prob;
        public final double value;
        public final long time;

        final Dataset dataset;

        public Result(File file, boolean readInstances) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                this.data = reader.readLine().substring(2);

                String singleObjective = reader.readLine().substring(2);
                String realInitialPopulation = reader.readLine().substring(2);
                String problem = reader.readLine().substring(2);
                String algo = reader.readLine().substring(2);

                this.prob = realInitialPopulation + "_" + problem;
                this.opt = singleObjective + "_" + algo;
                this.time = Long.parseLong(reader.readLine().substring(2));
                this.value = Double.parseDouble(reader.readLine().substring(2));

                reader.readLine(); // skip meta-features

                if (readInstances) {
                    this.dataset = WekaConverter.convert(data, new Instances(reader));
                } else {
                    this.dataset = null;
                }
            }
            this.opt_prob = opt + "_" + prob;
        }

        @Override
        public int compareTo(Result r) {
            return opt_prob.compareTo(r.opt_prob);
        }
    }

    public static List<Result> readResults(String folder, boolean readInstances) throws IOException {
        List<Result> list = new ArrayList<>();
        for (File file : new File(folder).listFiles()) {
            if (file.getName().contains("arff.txt")) {
                continue;
            }
            list.add(new Result(file, readInstances));
        }

        return list;
    }

}
