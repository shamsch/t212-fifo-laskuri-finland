# Known Shortcomings To Be Fixed

- [x] **CSV Parsing**  
  **Problem**: Using destructuring based on position to get the value out of CSV row   
  **Reason**: This is a very weak, flimsy approach. Also weeds out fields that might be of use. 
  **Solution**: Create a map of key value for every field available in the CSV

- [ ] **All Fees & Costs Not Added to Cost Basis**  
  **Problem**: Code ignores all additional costs beyond share price  
  **Reason**: All fees/taxes must be added to cost basis for accurate calculations  
  **Solution**: Add all applicable fees, duties, and taxes to Total for true cost basis

- [ ] **Transaction Types Incomplete**  
  **Problem**: Only handles "Market buy/sell"  
  **Reason**: Missing others like "Limit buy" "Stop Limit Sell" etc. Check samples for better idea. 
  **Solution**: Handle all buy/sell order types

- [ ] **Date Handling Broken**  
  **Problem**: String dates, no timezone awareness  
  **Reason**: Date is VERY important in calculations  
  **Solution**: Parse to proper datetime objects

- [ ] **Dividend Income Ignored**  
  **Problem**: Dividends not processed at all  
  **Reason**: Dividend is taxable so needs to be tracked separately as flat 30/34% tax applies
  **Solution**: TBD  

- [ ] **Withholding Tax Missing**  
  **Problem**: Withholding tax on dividend is not accounted for 
  **Reason**: Like the fees, duties, tax on buy/sell there, withholding tax needs to be deducted from Dividend Income 
  **Solution**: TBD  


- [ ] **Data Validation Missing**  
  **Problem**: Validate data all around 
  **Reason**: Erroneous data could cause the application to error out or produce wrong calculations  
  **Solution**: TBD  

- [ ] **Fractional Share Precision**  
  **Problem**: Basic double arithmetic loses precision  
  **Reason**: Rounding errors accumulate over many trades  
  **Solution**: Use BigDecimal for financial calculations

- [ ] **Finnish Tax Rules Missing**  
  **Problem**: Generic FIFO, not Finnish-compliant  
  **Reason**: Different tax brackets and deemed cost options  
  **Solution**: Apply tax only after total sale exceeds €1000 and apply 30% under €30k and 34% after. Deemed Acquisition as mentioned in `intro.md`

- [ ] **Multi-Year Processing**  
  **Problem**: Single file processing only  
  **Reason**: Real portfolios span multiple years  
  **Solution**: State persistence across files