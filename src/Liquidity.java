import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Liquidity {

    /**
     * A list containing all the stocks in the Excel file.
     */
    private final ArrayList<String> stockList = new ArrayList<>();
    /**
     * A hashmap mapping all stocks to an arraylist containing their total value for each day in the last year.
     */
    private final HashMap<String, ArrayList<BigDecimal>> stockTotalValue = new HashMap<>();
    /**
     * A hashmap mapping all stocks to an arraylist containing a monthly average of their total value for the last year.
     */
    private final HashMap<String, ArrayList<BigDecimal>> stockTotalValueAveraged = new HashMap<>();
    /**
     * A hashmap mapping all stocks to an arraylist containing their market cap for each day in the last year.
     */
    private final HashMap<String, ArrayList<BigDecimal>> stockMarketCap = new HashMap<>();
    /**
     * A hashmap mapping all stocks to a big decimal representing their max collateral pledged.
     */
    private final HashMap<String, BigDecimal> stockMaxCP = new HashMap<>();
    /**
     * A hashmap mapping all stocks to a big decimal representing their max collateral pledged to Market Cap ratio.
     */
    private final HashMap<String, BigDecimal> stockMaxCPRatio = new HashMap<>();
    /**
     * A hashmap mapping all stocks to a big decimal representing their max loan.
     */
    private final HashMap<String, BigDecimal> stockMaxLoan = new HashMap<>();
    /**
     * A hashmap mapping all stocks to a big decimal representing their max loan to market cap ratio.
     */
    private final HashMap<String, BigDecimal> stockMaxLoandivMC = new HashMap<>();

    /**
     * A list containing all trading days in the last year.
     */
    private final ArrayList<Date> dates = new ArrayList<>();
    /**
     * A constant used in the calculation of max collateral pledged.
     */
    private static final BigDecimal FSDays = BigDecimal.valueOf(3);
    /**
     * A constant used in the calculation of max collateral pledged.
     */
    private static final BigDecimal tradingValuePercent = BigDecimal.valueOf(0.15);
    /**
     * Constant used to determine the failing condition of the first check.
     */
    private static final Integer checkOneFailCondition = 10000000;
    /**
     * Constant used to determine the failing condition of the second check.
     */
    private static final double checkTwoFailCondition = 0.005;


    /**
     * Reads in the data stored in the given master data Excel file. Updates the stockList arraylist: storing every
     * stock in the file, the stockTotalValue hashmap: mapping all stocks to an arraylist containing their total value
     * for each day in the last year, the stockMarketCap hashmap: mapping all stocks to an arraylist containing their
     * market cap for each day in the last year and the dates arraylist,storing every trading day in the year.
     */
    public void dataCollection() throws IOException {
        String excelFilePath = "Liquidity Test.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(2);
        for (int r = 1; r < sheet.getLastRowNum(); r++) {
            String stock;
            try {
                stock = sheet.getRow(r).getCell(1).getStringCellValue();
                if (!stockList.contains(stock)) {
                    stockList.add(stock);
                }
            } catch (IllegalStateException e1) {
                stock = "TRUE";
                if (!stockList.contains(stock)) {
                    stockList.add(stock);
                }
            }
            Date currentDate = sheet.getRow(r).getCell(0).getDateCellValue();
            if (!dates.contains(currentDate)) {
                dates.add(currentDate);
            }
        }
        for (String stock : stockList) {
            ArrayList<BigDecimal> totalValue = new ArrayList<>();
            stockTotalValue.put(stock, totalValue);
            ArrayList<BigDecimal> marketCap = new ArrayList<>();
            stockMarketCap.put(stock, marketCap);
        }
        for (int r = sheet.getLastRowNum(); r > 1; r--) {
            String stock;
            try {
                stock = sheet.getRow(r).getCell(3).getStringCellValue();
            } catch (IllegalStateException e1) {
                stock = "TRUE";
            }
            helper(stock, BigDecimal.valueOf(sheet.getRow(r).getCell(17).getNumericCellValue()), stockTotalValue);
            BigDecimal marketCap;
            try {
                marketCap = BigDecimal.valueOf(sheet.getRow(r).getCell(18).getNumericCellValue());
            } catch (NullPointerException e2) {
                marketCap = null;
            }
            helper(stock, marketCap, stockMarketCap);
        }
        for (String stock : stockTotalValue.keySet()) {
            if(stockTotalValue.get(stock).size() != dates.size()){
                for (int i = stockTotalValue.get(stock).size(); i < dates.size(); i++){
                    stockTotalValue.get(stock).add(null);
                    stockMarketCap.get(stock).add(null);
                }
            }
            Collections.reverse(stockTotalValue.get(stock));
            Collections.reverse(stockMarketCap.get(stock));
        }
    }


    /**
     * A helper function which is used to add additional values to a given hashmap while the Excel file is read through.
     *
     * @param stock the current stock
     * @param value the data being added to the hashmap
     * @param stockData the hashmap being added to (either stockMarketCap or stockTotalValue)
     *
     */
    public void helper(String stock, BigDecimal value, HashMap<String, ArrayList<BigDecimal>> stockData){
        try{
            ArrayList<BigDecimal> tempList = stockData.get(stock);
            tempList.add(value);
            stockData.put(stock, tempList);
        } catch (NullPointerException e3) {
            ArrayList<BigDecimal> newList = new ArrayList<>();
            stockData.put(stock, newList);
            ArrayList<BigDecimal> tempList = stockData.get(stock);
            tempList.add(value);
            stockData.put(stock, tempList);
        }
    }


    /**
     * A function that will calculate and store the averaged total values of a given stock. In order to do this,
     * the function must calculate one-month intervals. These 12 periods will store the averaged total values.
     * Updates stockTotalValueAveraged which maps each stock to a list of their averaged total values.
     */
    public void calcAverages() {
        ArrayList<Integer> monthIndexes = calcMonths();
        for (String stock : stockMarketCap.keySet()) {
            ArrayList<BigDecimal> averagedValues = new ArrayList<>();
            BigDecimal total1 = BigDecimal.valueOf(0);
            int numDays1 = 0;
            int noDataCount1 = 0;
            for (int i = monthIndexes.get(0) + 1; i <= monthIndexes.get(1); i++) {
                if (stockTotalValue.get(stock).get(i) == null) {
                    noDataCount1 += 1;
                } else {
                    total1 = total1.add(stockTotalValue.get(stock).get(i));
                }
                numDays1++;
            }
            if (noDataCount1 == numDays1) {
                averagedValues.add(null);
            } else {
                averagedValues.add((total1.divide(BigDecimal.valueOf(numDays1), MathContext.DECIMAL32)));
            }
            for (int j = 2; j < monthIndexes.size(); j++) {
                BigDecimal total = BigDecimal.valueOf(0);
                int numDays = 0;
                int noDataCount = 0;
                for (int i = monthIndexes.get(j - 1) + 1; i <= monthIndexes.get(j); i++) {
                    if (stockTotalValue.get(stock).get(i) == null) {
                        noDataCount += 1;
                    } else {
                        total = total.add(stockTotalValue.get(stock).get(i));
                    }
                    numDays++;
                }
                if (noDataCount == numDays) {
                    averagedValues.add(null);
                } else {
                    averagedValues.add((total.divide(BigDecimal.valueOf(numDays), MathContext.DECIMAL32)));
                }
                stockTotalValueAveraged.put(stock, averagedValues);
            }
        }
    }


    /**
     * A helper function that will calculate a list of indexes corresponding to the end of each month.
     *
     * @return indexOfMonthPeriod an arraylist of the indexes of the end of each month.
     */
    public ArrayList<Integer> calcMonths(){
        Date reportDate = dates.get(dates.size() - 1);
        ArrayList<Integer> indexOfMonthPeriods = new ArrayList<>();
        Date initialDate = reportDate;
        indexOfMonthPeriods.add(dates.indexOf(reportDate));
        for (int i = 0; i < 12; i++){
            indexOfMonthPeriods.add(calcDatePeriods(initialDate));
            initialDate = dates.get(calcDatePeriods(initialDate));
        }
        Collections.reverse(indexOfMonthPeriods);
        return indexOfMonthPeriods;
    }


    /**
     * A helper function that will calculate the index of a date 1 month prior to given date initialDate.
     *
     * @param initialDate the date for which 3 months will be subtracted from
     *
     * @return initialDateSubMonths an integer representing the index of the date 1 month prior to the initialDate
     */
    public Integer calcDatePeriods(Date initialDate){
        LocalDate reportLD = LocalDate.ofInstant(initialDate.toInstant(), ZoneId.systemDefault());
        LocalDate reportLDSubMonths = reportLD;
        while (reportLDSubMonths.getMonth() == reportLD.getMonth()){
            reportLDSubMonths = reportLDSubMonths.minusDays(1);
        }
        Date reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int initialDateSubMonths = dates.indexOf(reportDateSubMonths);
        while (initialDateSubMonths == -1) {
            reportLDSubMonths = reportLDSubMonths.minusDays(1);
            reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
            initialDateSubMonths = dates.indexOf(reportDateSubMonths);
        }
        return initialDateSubMonths;
    }


    /**
     * A function that will perform the first liquidity check by calculating the max collateral pledged for each stock
     * and comparing that value to 10 million baht. If it is less than 10 million, the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> liqCheckOne(HashMap<String, Double> stockFSPortion, HashMap<String, Double> stockMaxLTV) {
        ArrayList<String> failed = new ArrayList<>();
        for (String stock : stockMarketCap.keySet()) {
            BigDecimal stockFS;
            if (stockFSPortion.get(stock) == null){
                stockFS = BigDecimal.valueOf(0.5);
            } else {
                stockFS = BigDecimal.valueOf(stockFSPortion.get(stock));
            }
            BigDecimal stockMaxLtv;
            if (stockMaxLTV.get(stock) == null){
                stockMaxLtv = BigDecimal.valueOf(0.5);
            } else {
                stockMaxLtv = BigDecimal.valueOf(stockMaxLTV.get(stock));
            }
            BigDecimal result = maxCollateralPledged(stock, stockFS, stockMaxLtv);
            if ( result == null || result.compareTo(BigDecimal.valueOf(checkOneFailCondition)) < 0){
                failed.add(stock);
            }
        }
        return failed;
    }

    /**
     * A function that will calculate a given stock's maximum collateral pledged and return it.
     *
     * @param stock the current stock
     * @param stockFS the stock's FS portion
     *
     * @return maxCP, the stock's maximum collateral pledged.
     */
    public BigDecimal maxCollateralPledged(String stock, BigDecimal stockFS, BigDecimal stockMaxLtv) {
        BigDecimal sum = BigDecimal.valueOf(0);
        BigDecimal maxCP;
        int numMonths = 0;
        for (BigDecimal value : stockTotalValueAveraged.get(stock)) {
            if (value != null) {
                sum = sum.add(value);
                numMonths += 1;
            }
        }
        BigDecimal avgTradingValue = sum.divide(BigDecimal.valueOf(numMonths), MathContext.DECIMAL32);
        maxCP = avgTradingValue.multiply(FSDays).multiply(tradingValuePercent)
                .divide(stockFS, MathContext.DECIMAL32);
        stockMaxCP.put(stock, maxCP);
        stockMaxLoan.put(stock, maxCP.multiply(stockMaxLtv));
        if (stockMarketCap.get(stock).get(stockMarketCap.get(stock).size() - 1) == null ||
                stockMarketCap.get(stock).get(stockMarketCap.get(stock).size() - 1).equals(BigDecimal.valueOf(0)) ||
                stockMaxCP.get(stock) == null || stockMaxCP.get(stock).equals(BigDecimal.valueOf(0))) {
            stockMaxLoandivMC.put(stock, null);
        } else {
            try {
                stockMaxLoandivMC.put(stock, maxCP.multiply(stockMaxLtv).
                        divide(stockMarketCap.get(stock).get(stockMarketCap.get(stock).size() - 1), MathContext.DECIMAL32));
            } catch (ArithmeticException divUndefined) {
                stockMaxLoandivMC.put(stock, null);
            }
        }
        return maxCP;
    }


    /**
     * A function that will perform the second liquidity check by calculating the ratio of max collateral pledged
     * to market cap for each stock and comparing that value to 0.5%. If it is greater than 0.5%, the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> liqCheckTwo() {
        ArrayList<String> failed = new ArrayList<>();
        for (String stock : stockMarketCap.keySet()) {
            BigDecimal marketCap = stockMarketCap.get(stock).get(stockMarketCap.get(stock).size() - 1);
            if (marketCap == null || marketCap.equals(BigDecimal.valueOf(0)) || stockMaxCP.get(stock) == null
                    || stockMaxCP.get(stock).equals(BigDecimal.valueOf(0))) {
                failed.add(stock);
            } else {
                try {
                    BigDecimal result = (stockMaxCP.get(stock)).divide(marketCap, MathContext.DECIMAL32);
                    stockMaxCPRatio.put(stock, result);
                    if (result.compareTo(BigDecimal.valueOf(checkTwoFailCondition)) > 0) {
                        failed.add(stock);
                    }
                } catch (ArithmeticException e4) {
                    failed.add(stock);
                }
            }
        }
        return failed;
    }


    /**
     * A function that will check to see which stocks passed the volatility test.
     *
     * @return liquidityPassed an arraylist of the stocks that passed the test
     */
    public ArrayList<String> liqPassed(HashMap<String, Double> stockFSPortion, HashMap<String, Double> stockMaxLTV){
        ArrayList<String> liquidityPassed = new ArrayList<>();
        ArrayList<String> failed = liqFailed(stockFSPortion, stockMaxLTV);
        for (String stock : stockMarketCap.keySet()) {
            if (!failed.contains(stock)){
                liquidityPassed.add(stock);
            }
        }
        return liquidityPassed;
    }


    /**
     * A function that will check to see which stocks failed the liquidity test.
     *
     * @return liquidityFailed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> liqFailed(HashMap<String, Double> stockFSPortion, HashMap<String, Double> stockMaxLTV){
        ArrayList<String> liquidityFailed = new ArrayList<>();
        for (String stock : stockMarketCap.keySet()) {
            if (liqCheckOne(stockFSPortion, stockMaxLTV).contains(stock) || liqCheckTwo().contains(stock)){
                liquidityFailed.add(stock);
            }
        }
        return liquidityFailed;
    }
}
