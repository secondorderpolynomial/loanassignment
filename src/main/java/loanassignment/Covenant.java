package loanassignment;

/**
 * Abstract data structure representing a covenant. Concrete implementations are DefaultLikelihoodCovenant and
 * BannedStateCovenant.
 */
public abstract class Covenant {
  final int bankId;
  final int facilityId;

  public Covenant(final int bankId, final int facilityId) {
    this.bankId = bankId;
    this.facilityId = facilityId;
  }

  /**
   * Can the loan be funded based on this covenant?
   */
  abstract boolean canFund(final LoanRequest LoanRequest);



  /**
   * Covenant implementation for DefaultLikelihood.
   */
  public static class MaxDefaultLikelihoodCovenant extends Covenant {
    float maxDefaultLikelihood = Float.MAX_VALUE;

    public MaxDefaultLikelihoodCovenant(final int bankId, final int facilityId, final float maxDefaultLikelihood) {
      super(bankId, facilityId);
      this.maxDefaultLikelihood = maxDefaultLikelihood;
    }

    @Override
    public boolean canFund(final LoanRequest LoanRequest) {
      return LoanRequest.defaultLikelihood <= maxDefaultLikelihood;
    }
  }

  /**
   * Covenant implementation for BannedState.
   */
  public static class BannedStateCovenant extends Covenant {
    String bannedState;

    public BannedStateCovenant(final int bankId, final int facilityId, final String bannedState) {
      super(bankId, facilityId);
      this.bannedState = bannedState;
    }

    @Override
    public boolean canFund(final LoanRequest loanRequest) {
      return !bannedState.equals(loanRequest.state);
    }
  }
}