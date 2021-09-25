package loanassignment;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static loanassignment.Utils.csvReader;
import static loanassignment.Utils.csvWriter;
import static loanassignment.Utils.isEmpty;
import static loanassignment.Utils.toId;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import loanassignment.Covenant.BannedStateCovenant;
import loanassignment.Covenant.MaxDefaultLikelihoodCovenant;

/**
 * A proxy for database access. Databases would typically be external.
 */
public class FundingHelper {
  private final Map<Integer, Bank> banks = new TreeMap<>();
  private final Map<Integer, Facility> facilities = new TreeMap<>();

  /**
   * Secondary view of facilities for faster loan assignment. Sorted by interest rate.
   */
  final List<Facility> facilitiesList = new ArrayList<>();

  /**
   * Try to assign a loan to a facility.
   *
   * @return Facility ID if loan was assigned. <code>null</code> otherwise.
   */
  public int assignLoan(final LoanRequest loanRequest) {
    for (final Facility facility : facilitiesList) {
      // System.out.println(facility.facilityId + " - " + (facility.amount - facility.amountFunded - loanAmount));
      final boolean funded = facility.fund(loanRequest);
      if (funded) {
        return facility.facilityId;
      }
    }

    return -1;
  }

  /**
   * Output expected yields to csv.
   */
  public void outputYields(final String yieldsCsv) throws IOException {
    try (final CSVPrinter yields = csvWriter(yieldsCsv, "facility_id", "expected_yield")) {
      for (final Facility facility : facilities.values()) {
        yields.printRecord(facility.facilityId, Math.round(facility.expectedYield));
        // System.out.println(facility.facilityId + "," + Math.round(facility.expectedYield) + " -> balance: "
        // + ((facility.amount - facility.amountFunded) > 88831));
      }
    }
  }

  /**
   * Print facility balances.
   */
  public void printBalances() {
    System.out.println("Facility/Bank\tRate\tFunded\t\tBalance");
    System.out.println("=============\t====\t=======\t\t======");
    for (final Facility facility : facilities.values()) {
      System.out.println(facility.facilityId + "/" + facility.bankId + "\t\t" + facility.facilityInterestRate + "\t"
          + (long) facility.amountFunded + "\t\t" + (long) facility.balance());
    }
  }



  /**
   * Load a banks csv file.
   */
  public FundingHelper loadBanks(final String banksCsvFile)
      throws UnsupportedEncodingException, FileNotFoundException, IOException {
    try (final CSVParser records = csvReader(banksCsvFile)) {
      for (final CSVRecord record : records) {
        final Bank bank = Bank.of(record.get("id"), record.get("name"));
        banks.put(bank.bankId, bank);
      }
    }

    return this;
  }

  /**
   * Load a facilities csv file.
   */
  public FundingHelper loadFacilities(final String facilitiesCsvFile)
      throws UnsupportedEncodingException, FileNotFoundException, IOException {
    try (final CSVParser records = csvReader(facilitiesCsvFile)) {
      for (final CSVRecord record : records) {
        final Facility facility = Facility.of(record.get("id"), record.get("bank_id"), record.get("amount"),
            record.get("interest_rate"));

        banks.get(parseInt(record.get("bank_id"))).addFacility(facility);
        facilities.put(facility.facilityId, facility);
        facilitiesList.add(facility);
      }
    }

    Collections.sort(facilitiesList, (o1, o2) -> Double.compare(o1.facilityInterestRate, o2.facilityInterestRate));
    return this;
  }

  /**
   * Load a covenants csv file.
   */
  public FundingHelper loadCovenants(final String covenantsCsvFile)
      throws UnsupportedEncodingException, FileNotFoundException, IOException {
    try (final CSVParser records = csvReader(covenantsCsvFile)) {
      for (final CSVRecord record : records) {
        final int bankId = toId(record.get("bank_id"));
        final int facilityId = toId(record.get("facility_id"));
        final String maxDefaultLikelihood = record.get("max_default_likelihood");
        final String bannedState = record.get("banned_state");

        if (facilityId != -1) {
          final Facility facility = facilities.get(facilityId);
          if (!isEmpty(maxDefaultLikelihood)) {
            facility.addCovenant(new MaxDefaultLikelihoodCovenant(bankId, facilityId, parseFloat(maxDefaultLikelihood)));
          }
          if (!isEmpty(bannedState)) {
            facility.addCovenant(new BannedStateCovenant(bankId, facilityId, bannedState));
          }
        } else {
          final Bank bank = banks.get(bankId);
          if (!isEmpty(maxDefaultLikelihood)) {
            bank.addCovenant(new MaxDefaultLikelihoodCovenant(bankId, facilityId, parseFloat(maxDefaultLikelihood)));
          }
          if (!isEmpty(bannedState)) {
            bank.addCovenant(new BannedStateCovenant(bankId, facilityId, bannedState));
          }
        }
      }
    }

    return this;
  }



  /**
   * Data structure representing banks. Would typically be external and possibly cached in memory.
   */
  public static class Bank {
    int bankId;
    String name;

    /**
     * Facilities provided by this bank.
     */
    private final List<Facility> facilities = new ArrayList<>();

    static Bank of(final String bankId, final String name) {
      final Bank b = new Bank();
      b.bankId = parseInt(bankId);
      b.name = name;
      return b;
    }

    void addFacility(final Facility facility) {
      facilities.add(facility);
    }

    void addCovenant(final Covenant covenant) {
      for (final Facility facility : facilities) {
        facility.addCovenant(covenant);
      }
    }
  }

  /**
   * Data structure representing facilties. Would typically be external and possibly cached in memory.
   */
  public static class Facility {
    int facilityId;
    int bankId;
    double amount;
    float facilityInterestRate;

    private final List<Covenant> covenants = new ArrayList<>();
    double amountFunded;
    double expectedYield;

    static Facility of(final String facilityId, final String bankId, final String amount, final String interest) {
      final Facility c = new Facility();
      c.facilityId = parseInt(facilityId);
      c.bankId = parseInt(bankId);
      c.amount = parseDouble(amount);
      c.facilityInterestRate = parseFloat(interest);
      return c;
    }

    void addCovenant(final Covenant covenant) {
      covenants.add(covenant);
    }

    boolean fund(final LoanRequest loanRequest) {
      if (amountFunded + loanRequest.amount > amount) {
        // Facility threshold reached
        return false;
      }

      for (final Covenant c : covenants) {
        if (!c.canFund(loanRequest)) {
          // Does not satisfy covenant
          return false;
        }
      }

      amountFunded += loanRequest.amount;
      expectedYield += expectedYield(loanRequest.amount, loanRequest.interestRate, loanRequest.defaultLikelihood);
      return true;
    }

    double expectedYield(final double loanAmount, final float loanInterestRate,
        final float loanDefaultLikelihood) {
      return Math.round((1 - loanDefaultLikelihood) * loanInterestRate * loanAmount - loanDefaultLikelihood * loanAmount
          - facilityInterestRate * loanAmount);
    }

    double balance() {
      return amount - amountFunded;
    }
  }
}
