package experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MetaResults {

    final static int MAX_NAMES = 512;
    final static int MAX_STEPS = 512;

    public static void main(String[] args) throws IOException {
        String folder = "result\\experiments.MetaSystemExp\\1554319303746\\";

        int n = 0;
        String[] names = new String[MAX_NAMES];
        double[][] data = new double[MAX_NAMES][MAX_STEPS];

        try (BufferedReader reader = new BufferedReader(new FileReader(folder + "names.txt"))) {

        }

    }
}
