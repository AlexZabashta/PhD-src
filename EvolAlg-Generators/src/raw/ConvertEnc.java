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

		map['é'] = 'i';
		map['ö'] = 'c';
		map['ó'] = 'u';
		map['ê'] = 'k';
		map['å'] = 'e';
		map['í'] = 'n';
		map['ã'] = 'g';
		map['ø'] = 's';
		map['ù'] = 's';
		map['ç'] = 'z';
		map['õ'] = 'h';
		map['ú'] = 't';
		map['ô'] = 'f';
		map['û'] = 'y';
		map['â'] = 'v';
		map['à'] = 'a';
		map['ï'] = 'p';
		map['ð'] = 'r';
		map['î'] = 'o';
		map['ë'] = 'l';
		map['ä'] = 'd';
		map['æ'] = 'j';
		map['ý'] = 'e';
		map['ÿ'] = 'y';
		map['÷'] = 'c';
		map['ñ'] = 's';
		map['ì'] = 'm';
		map['è'] = 'i';
		map['ò'] = 't';
		map['ü'] = 'e';
		map['á'] = 'b';
		map['þ'] = 'u';
		map['É'] = 'I';
		map['Ö'] = 'C';
		map['Ó'] = 'U';
		map['Ê'] = 'K';
		map['Å'] = 'E';
		map['Í'] = 'N';
		map['Ã'] = 'G';
		map['Ø'] = 'S';
		map['Ù'] = 'S';
		map['Ç'] = 'Z';
		map['Õ'] = 'H';
		map['Ú'] = 'T';
		map['Ô'] = 'F';
		map['Û'] = 'Y';
		map['Â'] = 'V';
		map['À'] = 'A';
		map['Ï'] = 'P';
		map['Ð'] = 'R';
		map['Î'] = 'O';
		map['Ë'] = 'L';
		map['Ä'] = 'D';
		map['Æ'] = 'J';
		map['Ý'] = 'E';
		map['ß'] = 'Y';
		map['×'] = 'C';
		map['Ñ'] = 'S';
		map['Ì'] = 'M';
		map['È'] = 'I';
		map['Ò'] = 'T';
		map['Ü'] = 'E';
		map['Á'] = 'B';
		map['Þ'] = 'U';
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
