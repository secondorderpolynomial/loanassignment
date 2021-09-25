package loanassignment;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static loanassignment.Utils.csvReader;
import static loanassignment.Utils.csvWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class Application {
  public static void assignLoansAndComputeYields(final String banksCsv, final String facilitiesCsv,
      final String covenantsCsv, final String loansCsv, final String assignmentsCsv, final String yieldsCsv)
          throws UnsupportedEncodingException, FileNotFoundException, IOException {

    final FundingHelper helper = new FundingHelper()
        .loadBanks(banksCsv)
        .loadFacilities(facilitiesCsv)
        .loadCovenants(covenantsCsv);

    try (final CSVParser records = csvReader(loansCsv);
        final CSVPrinter assignments = csvWriter(assignmentsCsv, "loan_id", "facility_id");
        final CSVPrinter yields = csvWriter(yieldsCsv, "loan_id", "facility_id");) {

      for (final CSVRecord record : records) {
        final LoanRequest loan = LoanRequest.of(
            parseDouble(record.get("amount")),
            parseFloat(record.get("interest_rate")),
            parseFloat(record.get("default_likelihood")),
            record.get("state"));
        final int facilityId = helper.assignLoan(loan);
        if (facilityId != -1) {
          // System.out.println(record.get("id") + "," + facilityId);
          assignments.printRecord(record.get("id"), facilityId);
        } else {
          assignments.printRecord(record.get("id"), "");
          System.err.println("Not funded loan_id: " + record.get("id"));
          // throw new IllegalArgumentException(record.get("id"));
        }
      }
    }

    System.out.println("Assignments written to: " + assignmentsCsv);
    helper.outputYields(yieldsCsv);
    System.out.println("Yields written to: " + yieldsCsv);
    System.out.println();
    helper.printBalances();
  }

  public static void main(final String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
    // Input files
    String banksCsv = "./banks.csv";
    String facilitiesCsv = "./facilities.csv";
    String covenantsCsv = "./covenants.csv";
    String loansCsv = "./loans.csv";

    // Output files
    String assignmentsCsv = "./assignments.csv";
    String yieldsCsv = "./yields.csv";

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-b":
          i++;
          banksCsv = args[i];
          break;
        case "-f":
          i++;
          facilitiesCsv = args[i];
          break;
        case "-c":
          i++;
          covenantsCsv = args[i];
          break;
        case "-l":
          i++;
          loansCsv = args[i];
          break;
        case "-a":
          i++;
          assignmentsCsv = args[i];
          break;
        case "-y":
          i++;
          yieldsCsv = args[i];
          break;
        case "-h":
          System.out.println("The following options are available. All options are optional:\n"
              + "\n"
              + "   -b csv_file      Banks csv file (input). Default: ./banks.csv\n"
              + "   -f csv_file      Facilities csv file (input). Default: ./facilities.csv\n"
              + "   -c csv_file      Covenants csv file (input). Default: ./covenants.csv\n"
              + "   -l csv_file      Loans csv file (input). Default: ./loans.csv\n"
              + "   -a csv_file      Assignments csv file (output). Default: ./assignments.csv\n"
              + "   -y csv_file      Yields csv file (output). Default: ./yields.csv");
          System.exit(0);
          break;
        default:
      }
    }

    assignLoansAndComputeYields(banksCsv, facilitiesCsv, covenantsCsv, loansCsv, assignmentsCsv, yieldsCsv);
  }
}
