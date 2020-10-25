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
import java.util.Map;

import javax.imageio.ImageIO;

import utils.FolderUtils;
import utils.StatUtils;

public class DrawResults {
    public static Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.PINK, Color.CYAN, Color.MAGENTA, Color.BLACK };

    public static BufferedImage convert(double minY, double maxY, int w, int h, int s, int m, double[][]... data) {
        int n = data.length;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }

        for (int i = 0; i < n; i++) {
            Color c = colors[i];

            Graphics2D graphics = (Graphics2D) image.getGraphics();

            graphics.setColor(new Color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 0.3f));
            graphics.setStroke(new BasicStroke(1));

            for (int t = 1; t < s; t++) {

                int lid = t - 1;
                int rid = t - 0;

                int l = (int) Math.floor(w * (lid) / (s - 1));
                int r = (int) Math.ceil(w * (rid) / (s - 1));

                double meanL = StatUtils.mean(data[i][lid]);
                double meanR = StatUtils.mean(data[i][rid]);

                double stdL = StatUtils.std(data[i][lid]);
                double stdR = StatUtils.std(data[i][rid]);

                double botL = meanL - stdL;
                double botR = meanR - stdR;

                double topL = meanL + stdL;
                double topR = meanR + stdR;

                for (int x = l; x < r; x++) {
                    double yb = (x - l) * (botR - botL) / (r - l) + botL;
                    int y1 = (int) Math.round(h * (yb - minY) / (maxY - minY));

                    double yt = (x - l) * (topR - topL) / (r - l) + topL;
                    int y2 = (int) Math.round(h * (yt - minY) / (maxY - minY));

                    graphics.drawLine(x, h - y1 - 1, x, h - y2 - 1);
                }
            }

            graphics.setColor(new Color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f));
            graphics.setStroke(new BasicStroke(3));

            for (int t = 1; t < s; t++) {

                int lid = t - 1;
                int rid = t - 0;

                int l = (int) Math.floor(w * (lid) / (s - 1));
                int r = (int) Math.ceil(w * (rid) / (s - 1));

                int yL = (int) Math.round(h * (StatUtils.mean(data[i][lid]) - minY) / (maxY - minY));
                int yR = (int) Math.round(h * (StatUtils.mean(data[i][rid]) - minY) / (maxY - minY));

                graphics.drawLine(l, h - yL - 1, r, h - yR - 1);
            }

        }

        return image;
    }

    public static void main(String[] args) throws IOException {
        String result = FolderUtils.buildPath(false, Long.toString(System.currentTimeMillis()));

        final int n = 10;

        // String folder = "result\\experiments.MetaSystemExp\\1554397984421\\";
        // final int m = 5;
        // final int s = 339;

        String folder = "result\\experiments.RelMetaSystemExp\\1563975046032\\";
        final int m = 5;
        final int s = 29;

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

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < n; i++) {
            for (int t = 0; t < s; t++) {
                for (int j = 0; j < m; j++) {
                    minY = Math.min(minY, data[i][t][j]);
                    maxY = Math.max(maxY, data[i][t][j]);
                }
            }
        }

        System.out.println(minY + " " + maxY);

        double tmpMin = 2;
        while (tmpMin > minY) {
            tmpMin -= 0.01;
        }
        minY = tmpMin;

        double tmpMax = 0;
        while (tmpMax < maxY) {
            tmpMax += 0.01;
        }
        maxY = tmpMax;

        System.out.println(minY + " " + maxY);

        int w = 1800;
        int h = 450;

        for (String prefix : Arrays.asList("RAND", "DIV", "VAR")) {
            BufferedImage image = convert(0.33, 0.37, w, h, s, m, data[ids.get(prefix + "_DIRECT")], data[ids.get(prefix + "_GMM")], data[ids.get(prefix + "_NDSE")]);
            ImageIO.write(image, "png", new File(result + prefix + ".png"));
        }

    }

}
