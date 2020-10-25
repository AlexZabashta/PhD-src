package raw;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ConvertEnc {
	public static void main(String[] args) throws IOException {

		char[] bufer = new char[1 << 20];

		char[] map = new char[8471];

		Arrays.fill(map, ' ');

		for (int i = 0; i < 256; i++) {
			map[i] = (char) i;
		}

		map['�'] = 'i';
		map['�'] = 'c';
		map['�'] = 'u';
		map['�'] = 'k';
		map['�'] = 'e';
		map['�'] = 'n';
		map['�'] = 'g';
		map['�'] = 's';
		map['�'] = 's';
		map['�'] = 'z';
		map['�'] = 'h';
		map['�'] = 't';
		map['�'] = 'f';
		map['�'] = 'y';
		map['�'] = 'v';
		map['�'] = 'a';
		map['�'] = 'p';
		map['�'] = 'r';
		map['�'] = 'o';
		map['�'] = 'l';
		map['�'] = 'd';
		map['�'] = 'j';
		map['�'] = 'e';
		map['�'] = 'y';
		map['�'] = 'c';
		map['�'] = 's';
		map['�'] = 'm';
		map['�'] = 'i';
		map['�'] = 't';
		map['�'] = 'e';
		map['�'] = 'b';
		map['�'] = 'u';
		map['�'] = 'I';
		map['�'] = 'C';
		map['�'] = 'U';
		map['�'] = 'K';
		map['�'] = 'E';
		map['�'] = 'N';
		map['�'] = 'G';
		map['�'] = 'S';
		map['�'] = 'S';
		map['�'] = 'Z';
		map['�'] = 'H';
		map['�'] = 'T';
		map['�'] = 'F';
		map['�'] = 'Y';
		map['�'] = 'V';
		map['�'] = 'A';
		map['�'] = 'P';
		map['�'] = 'R';
		map['�'] = 'O';
		map['�'] = 'L';
		map['�'] = 'D';
		map['�'] = 'J';
		map['�'] = 'E';
		map['�'] = 'Y';
		map['�'] = 'C';
		map['�'] = 'S';
		map['�'] = 'M';
		map['�'] = 'I';
		map['�'] = 'T';
		map['�'] = 'E';
		map['�'] = 'B';
		map['�'] = 'U';
		try (Reader reader = new InputStreamReader(new FileInputStream("raw\\data_price.csv"), Charset.forName("utf8"))) {
			try (Writer writer = new FileWriter("data_price_l.csv")) {

				int len;
				while ((len = reader.read(bufer)) >= 0) {

					for (int i = 0; i < len; i++) {
						bufer[i] = map[bufer[i]];
						if (bufer[i] > 255) {
							bufer[i] = ' ';
						}
					}

					writer.write(bufer, 0, len);
				}

			}
		}

	}
}
