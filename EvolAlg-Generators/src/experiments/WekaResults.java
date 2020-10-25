package experiments;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import clsf.Dataset;
import experiments.DataReader.Result;
import mfextraction.CMFExtractor;
import mfextraction.KNNLandMark;
import mfextraction.TreeMetaSystem;
import utils.FolderUtils;

public class WekaResults {

    public static void main(String[] args) throws IOException {

        String folder = "result\\experiments.GenerationExp\\r1554731018458result";
        List<Result> results = DataReader.readResults(folder, true);

        Set<String> opts = new TreeSet<>();
        Set<String> probs = new TreeSet<>();
        Set<String> datas = new TreeSet<>();

        for (Result result : results) {
            opts.add(result.opt);
            probs.add(result.prob);
            datas.add(result.data);
        }

        final int cores = 5;
        int repeats = 10;

        System.out.println("cores = " + cores);
        System.out.println("repeats = " + repeats);

        List<Dataset> datasets = DataReader.readData("data.csv", new File("data"));
        Collections.sort(datasets, Comparator.comparing(d -> d.name));
        Collections.shuffle(datasets, new Random(42));
        CMFExtractor extractor = new CMFExtractor();
        ToDoubleFunction<Dataset> knnScore = new KNNLandMark();

        Collections.sort(results);
        int resultsSize = results.size();

        Map<String, String> table = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        int l = 0;
        while (l < resultsSize) {
            Result result = results.get(l);

            int r = l;
            while (r < resultsSize && result.compareTo(results.get(r)) == 0) {
                ++r;
            }

            List<Dataset> genData = new ArrayList<>();
            for (int i = l; i < r; i++) {
                genData.add(results.get(i).dataset);
            }

            executor.submit(new Runnable() {

                @Override
                public void run() {
                    double rmse = 0;

                    for (int repeat = 0; repeat < repeats; repeat++) {

                        List<Dataset> tmp = new ArrayList<>(datasets);
                        Collections.shuffle(tmp, new Random(repeat + 42));

                        for (int and = 0; and < 4; and++) {
                            List<Dataset> train = new ArrayList<>();
                            List<Dataset> test = new ArrayList<>();

                            for (Dataset dataset : tmp) {
                                if ((dataset.name.hashCode() & 3) == and) {
                                    test.add(dataset);
                                } else {
                                    train.add(dataset);
                                }
                            }

                            for (Dataset dataset : genData) {
                                if ((dataset.name.hashCode() & 3) == and) {
                                    train.add(dataset);
                                }
                            }

                            TreeMetaSystem system = new TreeMetaSystem(train, extractor, knnScore);
                            rmse += system.rmse(test, knnScore) / repeats;
                        }
                    }

                    synchronized (table) {
                        table.put(result.opt_prob, String.format(Locale.ENGLISH, "%.4f", rmse / 4));
                    }
                    System.out.println(result.opt_prob);
                }
            });
            l = r;
        }
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String res = FolderUtils.buildPath(false, Long.toString(System.currentTimeMillis()));
        try (PrintWriter writer = new PrintWriter(res + "results.tex")) {
            writer.printf("%14s", "");
            for (String prob : probs) {
                writer.print("  &  ");
                writer.printf("%14s", prob);
            }
            writer.println();
            for (String opt : opts) {
                writer.printf("%14s", opt);
                for (String prob : probs) {
                    String value = table.get(opt + "_" + prob);
                    writer.print("  &  ");
                    writer.printf("%14s", value);
                }
                writer.println();
            }
        }
    }
}
