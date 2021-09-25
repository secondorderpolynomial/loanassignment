package loanassignment;

public class LoanRequest {
  double amount;
  float interestRate;
  float defaultLikelihood;
  String state;

  public static LoanRequest of(final double amount, final float interestRate, final float defaultLikelihood,
      final String state) {
    final LoanRequest l = new LoanRequest();
    l.amount = amount;
    l.interestRate = interestRate;
    l.defaultLikelihood = defaultLikelihood;
    l.state = state;
    return l;
  }
}
