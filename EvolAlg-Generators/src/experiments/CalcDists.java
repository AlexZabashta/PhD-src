package experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Random;

import fitness_function.MahalanobisDistance;
import mfextraction.CMFExtractor;
import utils.ArrayUtils;
import utils.MatrixUtils;
import utils.StatUtils;

public class CalcDists {

    public static void main(String[] args) {
        CMFExtractor extractor = new CMFExtractor();

        int numMF = extractor.length();
        int numData = 0;

        double[][] metaData = new double[512][];

        for (File file : new File("pdata").listFiles()) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
                int n = objectInputStream.readInt();
                int m = objectInputStream.readInt();
                double[][] data = (double[][]) objectInputStream.readObject();

                // TODO calc and print MF
                // ClDataset dataset = new ClDataset(data, extractor);
                // double[] mf = dataset.metaFeatures();
                extractor.apply(null);
                // if (mf != null && mf.length == numMF) {
                // metaData[numData++] = mf;
                // }

                System.out.println(file.getName() + " " + n + " " + m);
                System.out.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double[][] cov = StatUtils.covarianceMatrix(numData, numMF, metaData);
        ArrayUtils.print(cov);
        double[][] invCov = MatrixUtils.inv(numMF, cov);
        ArrayUtils.print(invCov);

        MahalanobisDistance distance = new MahalanobisDistance(numMF, invCov);

        Random random = new Random();

        for (int rep1 = 0; rep1 < 5; rep1++) {
            int i = random.nextInt(numData);

            int j = i;
            for (int rep2 = 0; rep2 < 5; rep2++) {

                System.out.println(distance.distance(metaData[i], metaData[j]) + " " + distance.distance(metaData[j], metaData[i]));

                j = random.nextInt(numData);
            }
        }

    }

}
