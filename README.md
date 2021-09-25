## Loan Assignment

Usage:

```
./gradlew run
```

You can specify input and output file arguments like this:

```
./gradlew run --args="-c ./covenants.csv -a ./assignments.csv"
```

You can get help:

```
./gradlew run --args="-h"

The following options are available. All options are optional:

   -b csv_file      Banks csv file (input). Default: ./banks.csv
   -f csv_file      Facilities csv file (input). Default: ./facilities.csv
   -c csv_file      Covenants csv file (input). Default: ./covenants.csv
   -l csv_file      Loans csv file (input). Default: ./loans.csv
   -a csv_file      Assignments csv file (output). Default: ./assignments.csv
   -y csv_file      Yields csv file (output). Default: ./yields.csv
```

### Primary classes

- `FundingHelper.java`
	- Loads Banks, Facilities, Covenants into an in-memory model
	- Holds loan assignment logic
- `Covenant.java`
	- Abstract class implemented by each type of restriction
	- Has 2 implementations: `MaxDefaultLikelihoodCovenant`, `BannedStateCovenant`
- `Application.java`: Main class that runs the application


### Coding considerations

I made some simplifications to err on the side of better code redability:

- Using structures-like classes instead of full-blown class features
- Redundancy in variable names, static imports
- Assumed non-negative bank/facility/loan ids so it's easy to model missing values
- Simplistic language features and some redundancy over more complex language features like abstraction and inheritance
- Fewer files, hopefully without cluttering code
- Lesser error handling. Production code will certainly contain extensive validation and error handling.
- No test cases. Production code will certainly contain extensive unit and integration tests.
 
 
## Implementation considerations

- Statically implemented Covenants `MaxDefaultLikelihoodCovenant` and `BannedStateCovenant`. I discussed more about how we can add flexibility here in the submitted write-up.