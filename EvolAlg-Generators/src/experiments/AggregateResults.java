package experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.MoreObjects;

import experiments.DataReader.Result;
import utils.FolderUtils;

public class AggregateResults {

    public static void main(String[] args) throws IOException {

        String folder = "result\\experiments.GenerationExp\\r1554731018458result";
        List<Result> results = DataReader.readResults(folder, false);
        Set<String> opts = new TreeSet<>();
        Set<String> probs = new TreeSet<>();
        Set<String> datas = new TreeSet<>();

        for (Result result : results) {
            opts.add(result.opt);
            probs.add(result.prob);
            datas.add(result.data);
        }

        // for (String data : datas) {
        // System.out.print('"' + data + '"' + ", ");
        // }

        Collections.sort(results);
        System.out.println(datas.size());
        int n = results.size();

        Map<String, String> table = new HashMap<>();

        int l = 0;
        while (l < n) {
            Result result = results.get(l);

            int r = l;
            while (r < n && result.compareTo(results.get(r)) == 0) {
                ++r;
            }

            double avg = 0;

            for (int i = l; i < r; i++) {
                avg += results.get(i).value;
            }

            table.put(result.opt_prob, String.format(Locale.ENGLISH, "%.4f", avg / (r - l)));
            System.out.println(result.opt_prob + " " + (r - l));

            l = r;
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
