package test;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class TestWeka {
    public static void main(String[] args) {

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("x1"));
        attributes.add(new Attribute("x2"));
        attributes.add(new Attribute("y"));

        int n = 123;

        Instances instances = new Instances("meta", attributes, n);
        instances.setClassIndex(2);

        Random random = new Random();

        for (int i = 0; i < n; i++) {
            Instance instance = new DenseInstance(3);
            instance.setDataset(instances);

            double x1 = random.nextDouble();
            double x2 = random.nextGaussian();
            double y = Math.sin(x1) + Math.cos(x2);

            instance.setValue(0, x1);
            instance.setValue(1, x2);
            instance.setClassValue(y);
            instances.add(instance);
        }

        REPTree tree = new REPTree();

        try {
            tree.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int m = 23;

        System.out.println(instances.size());

        for (int i = 0; i < m; i++) {
            Instance instance = new DenseInstance(3);
            instance.setDataset(instances);

            double x1 = random.nextDouble();
            double x2 = random.nextGaussian();

            double y = Math.sin(x1) + Math.cos(x2);

            instance.setValue(0, x1);
            instance.setValue(1, x2);

            try {
                System.out.println(y + " " + tree.classifyInstance(instance));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // RandomForest forest = new RandomForest();

        // forest.getCapabilities();

        // RandomTree tree = new RandomTree();

    }
}
