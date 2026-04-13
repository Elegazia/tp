# RishabhShenoy03 - Project Portfolio Page

## Project: CG2StocksTracker

CG2StocksTracker is a desktop command-line portfolio tracker built for typing-oriented users who want to manage holdings quickly.
The user interacts with it through a CLI, and it is written in Java.

Given below are my contributions to the project.

### New Features: Implemented and refined parsing for `/set`, `/value`, and `/list`

- What:
	- Implemented parser support for `/set` in both typed update mode (`--type --ticker --price`) and ticker-wide update mode (`--ticker --price`).
	- Implemented parser support for `/value` as a read-only valuation command with strict no-extra-arguments behavior.
	- Implemented parser support for `/list` variants, including default listing, type filters (`--stock`, `--etf`, `--bond`), and portfolio summary mode (`--portfolios`).
	- Added validation to reject invalid flag combinations and malformed inputs early, before command execution.
- Why:
	- These commands represent high-frequency user workflows: updating market prices, viewing holdings, and checking portfolio-level value/performance.
	- A robust parser was necessary to keep command behavior predictable as signatures evolved from positional style to flag-based syntax.
	- Early validation reduced downstream failures in execution code and improved user-facing error clarity.
- How:
	- Extended `Parser` option extraction and usage checks to enforce required flags, disallow unsupported flag mixes, and normalize command inputs.
	- Mapped validated inputs into `ParsedCommand` fields so execution handlers receive complete and unambiguous command data.
	- Coordinated parsing contracts with execution-layer expectations in `CG2StocksTracker` and model-facing methods to avoid parser/executor mismatches.
	- Backed changes with parser and integration test updates so valid and invalid command paths remained explicitly covered.

### Enhancement: Extended optional-input support for `/set`, `/add`, and `/remove`

- What:
	- Extended `/set` to support both scoped and broad updates by allowing optional `--type` (typed update vs ticker-wide update).
	- Extended `/add` to support optional fee inputs (`--brokerage`, `--fx`, `--platform`) while preserving required core inputs.
	- Extended `/remove` to support optional `--qty`, optional `--price`, and optional fee inputs for realistic sell workflows.
	- Added fallback behavior where `/remove` uses the holding's saved last price when `--price` is omitted.
- Why:
	- Users need flexible command forms for everyday workflows without losing input safety.
	- Optional pricing fallback makes `/remove` practical after prices were previously updated using `/set`.
	- Explicit validation was needed to prevent invalid sells and inconsistent P&L calculations.
- How:
	- Implemented conditional parsing paths in `Parser` so optional fields are accepted only for compatible commands.
	- Coordinated parser outputs with execution logic so omitted inputs trigger the correct defaults.
	- For `/remove`, enforced these validation constraints:
		- `QTY` (if provided) must be `> 0` and cannot exceed current quantity.
		- `PRICE` (if provided) must be `> 0`.
		- Fee values (if provided) must be `>= 0`.
	- Implemented the no-price fallback rule for `/remove`: if `--price` is omitted, use saved last price; if no saved last price exists (e.g., no prior `/set` or equivalent price context), command fails clearly.

### Enhancement: Refactored parser and tests after command-signature changes

- What:
	- Refactored parser logic, command dispatch assumptions, and related tests each time command signatures were updated.
	- Updated command parsing to match finalized flag conventions and consistent usage rules across commands.
- Why:
	- Frequent signature changes introduced risk of silent behavior drift between parser, UI messaging, and execution flow.
	- Continuous refactoring was required to keep behavior consistent while still allowing command-surface improvements.
- How:
	- Revised parser branches and usage messages to match new command contracts immediately after each change.
	- Expanded and updated `ParserTest` and affected test cases to cover both successful parsing and validation failures.
	- Revalidated text-ui behavior where needed to ensure user-facing command flows remained stable. [#24](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/24)

**Quality Practices:**
- Defensive programming with null checks to prevent NullPointerExceptions.
- Edge-case handling: holdings without prices, negative P&L, extreme return values.
- Consistent numeric formatting across all output.
- Comprehensive testing: 7 UI unit tests + text-ui-test golden file validation.

### Code contributed

- [RepoSense contribution link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=w09-4&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~other~test-code&since=2026-02-20T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=RishabhShenoy03&tabRepo=AY2526S2-CS2113-W09-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~other~test-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

### Documentation

- User Guide:
	- Updated command formats and examples to reflect finalized flag-based usage, especially for `/set`, `/list`, and `/value`.
	- Revised wording for command variants so users can clearly distinguish when to use filtered listings, portfolio summary view, and typed vs ticker-wide updates.
	- Aligned examples and constraints with actual parser validation behavior to reduce ambiguity and usage errors.
- Developer Guide:
	- Updated command-flow documentation so implementation details remain aligned with the real parsing to execution pipeline.
	- Added and refined UML diagrams for `/set`, `/value`, and `/list` to reflect current class interactions and sequence flow.
	- Revised DG explanations after parser refactors to remove outdated command assumptions and keep maintenance guidance accurate.
- [#34](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/34), [#51](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/51)

### Contributions to team-based tasks

- Helped prepare and release `v1.0` and `v2.1`
- Helped drive UI and output consistency during periods of frequent command evolution.
- Supported team integration by keeping parser, tests, and documentation aligned during refactoring-heavy phases.
- Took initiative for grouping and delegating tasks, often leading discussions and keeping the team on track for deadlines
- Helped teammates with generating UML diagrams using
PlantUML, showed them how to embed the images within
the Developer Guide.

### Review and mentoring contributions

- Reviewed teammates' pull requests and gave feedback on parser behavior and command consistency.
- Helped teammates troubleshoot command-related refactoring and test failures.

### Contributions beyond the project team

- Shared implementation/refactoring insights with peers where relevant.
