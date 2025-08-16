# Introduction to t212-fifo-laskuri-finland

## The Problem

If you're a Finnish investor using Trading 212, you face a challenge every tax season: figuring out how much capital gains tax you owe to Verohallinto (Finnish Tax Administration). Trading 212 doesn't calculate or report your Finnish tax obligations, leaving you to sort it out yourself.

Finnish tax law requires you to use the FIFO method (First In, First Out) when calculating gains. This means when you sell shares, you must consider the oldest purchases as being sold first - regardless of which ones you actually intended to sell. With complex trading histories, this becomes a nightmare to calculate manually.

## The Solution

This tool (which will eventually become a simple webpage) reads your Trading 212 CSV exports and automatically calculates your capital gains using the FIFO method that Verohallinto requires.

## Getting Your Trading 212 CSV Export

1. Log in to Trading 212 → Menu → History
2. Click the export/download icon (top right)
3. Select time frame (max 1 year per report) and data types (Orders, Dividends, Transactions)
4. Export CSV

**Important**: You need complete year-by-year reports from when you first started trading. FIFO requires tracking oldest purchases first - missing years = wrong calculations.

## How Finnish Capital Gains Tax Works (The Basics)

When you sell shares at a profit in Finland, you pay capital gains tax:
- **30%** on gains up to €30,000 per year
- **34%** on gains above €30,000

You can deduct:
- The original purchase price
- Trading fees and expenses

Or use the "deemed acquisition cost" (20% or 40% of sale price, depending on how long you held the shares)

Support for "deemed acquisition cost" calculations will be added down the line.

You **must** use FIFO - oldest shares sold first. That's where this tool comes in.

## Who This Is For

- Finnish tax residents who trade on Trading 212
- Residents of other countries where FIFO method is used for calculation (although the application is not optimized for it)

## Technical Notes

Built with Clojure, designed to eventually run as an AWS Lambda function with a frontend webpage for easy access.

---

**Disclaimer**: This tool is for informational purposes only. Always double-check your calculations and consult a tax professional when in doubt.

