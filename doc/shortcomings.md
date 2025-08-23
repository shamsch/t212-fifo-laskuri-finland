# Known Shortcomings To Be Fixed

- [x] **CSV Parsing**  
  **Problem**: Using destructuring based on position to get the value out of CSV row   
  **Reason**: This is a very weak, flimsy approach. Also weeds out fields that might be of use. 
  **Solution**: Create a map of key value for every field available in the CSV

- [ ] **Transaction Types Incomplete**  
  **Problem**: Only handles "Market buy/sell"  
  **Reason**: Missing others like "Limit buy" "Stop Limit Sell" etc. Check samples for better idea. 
  **Solution**: Handle all buy/sell order types

- [ ] **All Fees & Costs Not Added to Cost Basis**  
  **Problem**: Code ignores all additional costs beyond share price  
  **Reason**: All fees/taxes must be added to cost basis for accurate calculations  
  **Solution**: Add all applicable fees, duties, and taxes to Total for true cost basis

- [ ] **Code Organization**  
  **Problem**: Everything in single core.clj file, getting unwieldy  
  **Reason**: Hard to maintain, test, and understand as features grow  
  **Solution**: Split into logical namespaces: csv parsing, FIFO logic, tax calculations, reporting

- [ ] **Testing Missing**  
  **Problem**: No unit tests for core FIFO logic, tax calculations, or CSV parsing  
  **Reason**: Need confidence that calculations are correct, especially for financial data  
  **Solution**: Add comprehensive test suite covering edge cases, partial sales, tax brackets

- [ ] **Data Validation & Date Handling**  
  **Problem**: No data validation, string dates with no timezone awareness  
  **Reason**: Erroneous data could cause crashes, dates are critical for accurate calculations  
  **Solution**: Add input validation and parse dates to proper datetime objects

- [ ] **Dividend Income & Withholding Tax Missing**  
  **Problem**: Dividends not processed at all, withholding tax not accounted for  
  **Reason**: Dividend income is taxable (flat 30/34% rate), withholding tax needs to be deducted from dividend income  
  **Solution**: Parse dividend transactions, calculate net dividend income after withholding tax, include in tax summary

- [ ] **Multi-Year Processing**  
  **Problem**: Single file processing only  
  **Reason**: Real portfolios span multiple years  
  **Solution**: State persistence across files

- [ ] **Deemed Acquisition Cost Option Missing**  
  **Problem**: Only supports actual cost basis calculation, not deemed acquisition cost  
  **Reason**: Finnish tax law allows choosing 20% or 40% of sale price as cost basis (depending on holding period)  
  **Solution**: Add option to calculate using deemed acquisition cost instead of FIFO when it's more beneficial

- [ ] **AWS Lambda Deployment**  
  **Problem**: Currently CLI-only tool, not web accessible  
  **Reason**: As per intro.md, should be AWS Lambda with frontend for easy access  
  **Solution**: Package as Lambda function, add API Gateway, create simple web frontend for CSV upload