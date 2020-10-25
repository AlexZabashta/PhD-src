package raw;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ConvertReg {
	public static void main(String[] args) throws IOException {

		Map<String, Integer> kw = new HashMap<>();
		int ba = 97;

		try (BufferedReader reader = new BufferedReader(new FileReader("raw\\kw.txt"))) {
			for (int i = 0; i < ba; i++) {
				kw.put(reader.readLine().split(" ")[0], i);
			}
		}

		try (CSVParser parser = new CSVParser(new FileReader("raw\\data_price_l.csv"), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
			try (PrintWriter out = new PrintWriter("out.csv")) {
				for (int i = 0; i < ba + 17; i++) {
					if (i != 0) {
						out.print(',');
					}
					out.print("f_" + i);
				}
				out.println();

				for (CSVRecord record : parser) {

					try {
						double[] array = new double[7 + 10 + ba];
						int ap = 0;

						array[ap++] = Double.parseDouble(record.get("Cena"));
						array[ap++] = Double.parseDouble(record.get("Massa DES"));
						array[ap++] = Double.parseDouble(record.get("Massa zagotovki"));
						array[ap++] = Double.parseDouble(record.get("Summarnoe vremy izgotovleniy dni"));

						String[] size = record.get("Razmer").toLowerCase().split("h");

						array[ap++] = Double.parseDouble(size[0]);
						array[ap++] = Double.parseDouble(size[1]);
						array[ap++] = Double.parseDouble(size[2]);

						String text = (record.get("Material") + " " + record.get("Nomenklatura")).toLowerCase();

						int l = 0;

						while (l < text.length()) {
							while (l < text.length() && !Character.isDigit(text.charAt(l)) && !Character.isLetter(text.charAt(l))) {
								++l;
							}
							int r = l;
							while (r < text.length() && Character.isDigit(text.charAt(r)) == Character.isDigit(text.charAt(l)) && Character.isLetter(text.charAt(r)) == Character.isLetter(text.charAt(l))) {
								r++;
							}

							if (l < r) {
								while (r - l > 1 && text.charAt(l) == '0') {
									++l;
								}
								String string = text.substring(l, r);

								try {
									double value = Double.parseDouble(string);
									if (ap < 17) {
										array[ap++] = value;
									}
								} catch (NumberFormatException nan) {
									Integer id = kw.get(string);
									if (id != null) {
										array[id] = 1;
									}
								}
							}

							l = r;
						}

						for (int i = 0; i < ba + 17; i++) {
							if (i != 0) {
								out.print(',');
							}
							out.print(array[i]);
						}
						out.println();

						// System.out.println(text);
						// System.out.println(Arrays.toString(array));
						// System.out.println(w + " " + l + " " + h + " " + md + " " + mz + " " + cost);
						// System.out.println(text);
					} catch (RuntimeException e) {
						// e.printStackTrace();
					}
				}
			}

		}

	}
}
