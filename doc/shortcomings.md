# Known Shortcomings To Be Fixed

- [x] **CSV Parsing**  
  **Problem**: Using destructuring based on position to get the value out of CSV row   
  **Reason**: This is a very weak, flimsy approach. Also weeds out fields that might be of use. 
  **Solution**: Create a map of key value for every field available in the CSV

- [x] **Transaction Types Incomplete**  
  **Problem**: Only handles "Market buy/sell"  
  **Reason**: Missing others like "Limit buy" "Stop Limit Sell" etc. Check samples for better idea. 
  **Solution**: Handle all buy/sell order types

- [x] **All Fees & Costs Not Added to Cost Basis**  
  **Problem**: Code ignores all additional costs beyond share price  
  **Reason**: All fees/taxes must be added to cost basis for accurate calculations  
  **Solution**: Add all applicable fees, duties, and taxes to Total for true cost basis  
  *Resolved: Trading 212's Total field already includes all fees, duties, and currency conversion costs*

- [ ] **Code Organization**  
  **Problem**: Everything in single core.clj file, getting unwieldy  
  **Reason**: Hard to maintain, test, and understand as features grow  
  **Solution**: Split into logical namespaces: csv parsing, FIFO logic, tax calculations, reporting

- [ ] **Detailed Tax Reporting Fields Missing**  
  **Problem**: Only tracks basic gain/loss, missing required tax reporting details  
  **Reason**: Finnish tax authorities require detailed breakdown: Security Name, ISIN, Number Sold, Date of Sale, Selling Price, Sale Expenses, Date of Purchase, Acquisition Cost, Purchase Expenses, Calculation Method, Calculated Gain/Loss  
  **Solution**: Update Transaction/Sale records to capture all required fields, enhance print-summary to output proper tax report format

- [ ] **Configurable Field Aggregation with Currency Conversion**  
  **Problem**: Expense/fee calculations hardcode which CSV fields to sum instead of using configurable field groups  
  **Reason**: Computing total expenses requires summing multiple CSV fields with different currencies and applying exchange rates. Hardcoded field selection makes it impossible to adapt to CSV changes or user preferences without code modifications  
  **Solution**: Define field groupings as external constants that specify which fields to sum with their currencies, allowing parameterized field selection  
  ```clojure
  ;; Configurable field groups with currency conversion
  (def expense-field-groups
    {:purchase-costs [{:field :stamp-duty-reserve-tax :currency :currency-stamp-duty}
                      {:field :currency-conversion-fee :currency :currency-conversion-fee}]
     :sale-costs [{:field :transaction-fee :currency :currency-transaction-fee}]})
  
  ;; Usage: (calculate-total-amount csv-row :purchase-costs exchange-rates) -> EUR total
  ```

- [ ] **Testing Missing**  
  **Problem**: No unit tests for core FIFO logic, tax calculations, or CSV parsing  
  **Reason**: Need confidence that calculations are correct, especially for financial data  
  **Solution**: Add comprehensive test suite covering edge cases, partial sales, tax brackets

- [ ] **Data Validation**  
  **Problem**: No input validation or data integrity checks before processing  
  **Reason**: 1) String dates without timezone parsing can cause calculation errors 2) CSV completeness not verified (unmatched buys/sells) 3) Field type validation missing (e.g., ticker as string not number, ISIN format validation, numeric fields as numbers not text)  
  **Solution**: Add date parsing with timezone handling, CSV integrity validation (warn on orphaned sales), comprehensive field validation for all relevant CSV fields ensuring correct data types and formats

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