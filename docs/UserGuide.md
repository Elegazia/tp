# User Guide

## Introduction

CG2 Stocks Tracker is a command-line portfolio tracker for recording holdings, updating market prices, and viewing realized/unrealized P&L.

## Quick Start

1. Ensure Java 17 or above is installed.
2. Build and run from the project root:
	 - Windows: `gradlew.bat run`
	 - macOS/Linux: `./gradlew run`
3. Use `/help` in the app to list all commands.

## Features

### Create portfolio: `/create`
Creates a portfolio.

Format: `/create NAME`

Example:
`/create retirement`

### Switch active portfolio: `/use`
Switches the active portfolio.

Format: `/use NAME`

Example:
`/use retirement`

### List portfolios/holdings: `/list`

Formats:
- `/list`
- `/list --stock`
- `/list --etf`
- `/list --bond`
- `/list --portfolios`

Notes:
- `/list` shows all holdings in the active portfolio.
- Type filters show only that asset type in the same tabular format.
- `/list --portfolios` shows each portfolio's realized and unrealised P&L in alphabetical order.

### Add holding: `/add`
Adds units to a holding and requires purchase price per unit.

Format:
`/add --type TYPE --ticker TICKER --qty QTY --price PRICE`

Notes:
- `TYPE` must be one of `STOCK`, `ETF`, `BOND`.
- `QTY > 0`, `PRICE > 0`.
- If holding already exists, quantity is increased and average buy price is recalculated using weighted average.

Example:
`/add --type STOCK --ticker VOO --qty 1 --price 300`

Output contains:
- Added/updated holding details.
- Quantity.
- Average buy price.

### Remove holding units (partial or full): `/remove`
Sells part or all of a holding and records realized P&L.

Format:
`/remove --type TYPE --ticker TICKER --qty QTY --price PRICE`

Optional behavior:
- If `--qty` is omitted: sell all units of the holding.
- If `--price` is omitted: use the last price set via `/set` for that ticker.
- If both are omitted: sell all units using last `/set` price.

Notes:
- If no `--price` is provided and there is no last `/set` price, command fails.
- `QTY` (when provided) must be `> 0` and not exceed current holding quantity.
- If `/set` was never used for a holding, remove falls back to the initial add price for that holding.

Examples:
- Sell specific qty at explicit price:
	`/remove --type STOCK --ticker VOO --qty 0.5 --price 620`
- Sell specific qty at last `/set` price:
	`/remove --type STOCK --ticker VOO --qty 0.5`
- Sell all at explicit price:
	`/remove --type STOCK --ticker VOO --price 590`
- Sell all at last `/set` price:
	`/remove --type STOCK --ticker VOO`

Output contains:
- Sold quantity.
- Effective sell price used.
- Realized P&L for that sale (signed + / -).

### Set market price: `/set`
Sets the latest market price for a ticker. This does not sell anything.

Format:
`/set --ticker TICKER --price PRICE`

Notes:
- `PRICE > 0`.
- Updates all holdings in active portfolio with matching ticker.
- Used by `/value` for unrealized P&L and by `/remove` as fallback sell price.

Example:
`/set --ticker VOO --price 600`

Output contains:
- Price update confirmation only.

### Bulk set prices from CSV: `/setmany`
Loads prices from CSV into active portfolio.

Format:
`/setmany --file FILEPATH`

CSV header must be:
`ticker,price`

### View valuation and P&L: `/value`
Shows portfolio-level current total value, realized P&L, and unrealized P&L by holding.

Format:
`/value`

Output contains:
- `Current total value`: sum of `quantity * current unit price` across holdings in the active portfolio.
- `Realized P&L`: cumulative P&L from completed sells (positive or negative).
- `Unrealised P&L by holding`: one row per holding.
- `Total unrealised P&L`: sum across holdings.

Example scenario:
1. `/add --type STOCK --ticker VOO --qty 1 --price 300`
2. `/set --ticker VOO --price 600`
3. `/value`

Expected result summary:
- Realized P&L = `+0.00` (nothing sold yet)
- Unrealized P&L for VOO = `+300.00`
- Total unrealized P&L = `+300.00`

### Help: `/help`
Shows the command list.

### Exit: `/exit`
Exits the application.

## FAQ

**Q**: Why does `/remove` fail when I omit `--price`?

**A**: If you omit `--price`, the app first uses the last `/set` price for that ticker. If no `/set` price exists yet, it uses the holding's initial add price.

**Q**: How is average buy price calculated?

**A**: Weighted average cost basis is used:
`newAvg = (oldQty * oldAvg + addedQty * addedPrice) / (oldQty + addedQty)`

## Command Summary

- `/create NAME`
- `/use NAME`
- `/list`
- `/list --stock`
- `/list --etf`
- `/list --bond`
- `/list --portfolios`
- `/add --type TYPE --ticker TICKER --qty QTY --price PRICE`
- `/remove --type TYPE --ticker TICKER --qty QTY --price PRICE`
- `/set --ticker TICKER --price PRICE`
- `/setmany --file FILEPATH`
- `/value`
- `/help`
- `/exit`
