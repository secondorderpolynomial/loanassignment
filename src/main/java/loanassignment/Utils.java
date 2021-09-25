package loanassignment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class Utils {
  @SuppressWarnings("resource")
  public static CSVParser csvReader(final String csvFile) throws IOException {
    final Reader is = new InputStreamReader(new FileInputStream(csvFile), "utf-8");
    return CSVFormat.DEFAULT
        .builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()
        .parse(is);
  }

  @SuppressWarnings("resource")
  public static CSVPrinter csvWriter(final String csvFile, final String... headers) throws IOException {
    final Appendable fileWriter = new OutputStreamWriter(new FileOutputStream(csvFile));
    final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(headers).build();
    return new CSVPrinter(fileWriter, format);
  }

  public static boolean isEmpty(final String s) {
    return s == null || s.isEmpty();
  }

  public static int toId(final String s) {
    if (isEmpty(s)) {
      return -1;
    }

    return Integer.parseInt(s);
  }
}
