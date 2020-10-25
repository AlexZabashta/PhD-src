package experiments;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import utils.FolderUtils;
import utils.StatUtils;

public class MetaSystemResults {

    public static void main(String[] args) throws IOException {
        String result = FolderUtils.buildPath(false, Long.toString(System.currentTimeMillis()));

        final int n = 9;

        String folder = "result\\experiments.MetaSystemExp\\1554397984421\\";
        final int m = 5;
        final int s = 339;

        // String folder = "result\\experiments.MetaSystemExp\\r1554396210193\\";
        // final int m = 7;
        // final int s = 51;

        final int k = n * m;

        String[] name = new String[n];
        Map<String, Integer> ids = new HashMap<>(n);
        double[][][] data = new double[n][s][m];

        try (BufferedReader reader = new BufferedReader(new FileReader(folder + "names.txt"))) {
            for (int i = 0; i < k; i++) {
                String line = reader.readLine();
                if (i % m == 0) {
                    name[i / m] = line;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            ids.put(name[i], i);
        }

        for (int t = 0; t < s; t++) {
            try (BufferedReader reader = new BufferedReader(new FileReader(folder + t + ".txt"))) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        String line = reader.readLine();
                        data[i][t][j] = Double.parseDouble(line);
                    }
                }
            }
        }

        for (String prefix : Arrays.asList(null, "RAND", "DIV", "VAR")) {
            System.out.printf(Locale.ENGLISH, "%7s ", prefix);
            for (String suffix : Arrays.asList("DIRECT", "GMM", "NDSE")) {
                if (prefix == null) {
                    System.out.printf(Locale.ENGLISH, " & %7s", suffix);
                } else {
                    double[][] sub = data[ids.get(prefix + "_" + suffix)];
                    double minAvg = Double.POSITIVE_INFINITY;
                    for (double[] step : sub) {
                        minAvg = Math.min(minAvg, StatUtils.mean(step));
                    }
                    System.out.printf(Locale.ENGLISH, " & %7.5f", minAvg);
                }
            }
            System.out.println(" \\\\");
        }
    }
}
