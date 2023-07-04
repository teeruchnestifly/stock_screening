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
            } catch (IllegalStateException e1) {
                stock = "TRUE";
            }
            if (!stockList.contains(stock)) {
                stockList.add(stock);
            }
        }
        for (String stock : stockList) {
            ArrayList<BigDecimal> totalValue = new ArrayList<>();
            stockTotalValue.put(stock, totalValue);
            ArrayList<BigDecimal> marketCap = new ArrayList<>();
            stockMarketCap.put(stock, marketCap);
        }
        for (int r = 1; r < sheet.getLastRowNum(); r++) {
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
            } catch (NullPointerException e2){
                marketCap = null;
            }
            helper(stock, marketCap, stockMarketCap);
            Date currentDate = sheet.getRow(r).getCell(0).getDateCellValue();
            if (!dates.contains(currentDate)){
                dates.add(currentDate);
            }
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
     *
     * Updates stockTotalValueAveraged which maps each stock to a list of their averaged total values.
     */
    public void calcAverages() {
        ArrayList<Integer> monthIndexes = calcMonths();
        for (String stock : stockMarketCap.keySet()) {
            //STILL NEED TO FIND SOLUTION TO THIS CASE
            if (stockTotalValue.get(stock).size() == 262) {
                ArrayList<BigDecimal> averagedValues = new ArrayList<>();
                BigDecimal total1 = BigDecimal.valueOf(0);
                int numDays1 = 0;
                for (int i = monthIndexes.get(0) + 1; i <= monthIndexes.get(1); i++) {
                    total1 = total1.add(stockTotalValue.get(stock).get(i));
                    numDays1++;
                }
                averagedValues.add((total1.divide(BigDecimal.valueOf(numDays1), MathContext.DECIMAL32)));
                for (int j = 2; j < monthIndexes.size(); j++) {
                    BigDecimal total = BigDecimal.valueOf(0);
                    int numDays = 0;
                    for (int i = monthIndexes.get(j - 1) + 1; i <= monthIndexes.get(j); i++) {
                        total = total.add(stockTotalValue.get(stock).get(i));
                        numDays++;
                    }
                    averagedValues.add((total.divide(BigDecimal.valueOf(numDays), MathContext.DECIMAL32)));
                }
                stockTotalValueAveraged.put(stock, averagedValues);
            } else {
                stockTotalValueAveraged.put(stock, null);
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
    public ArrayList<String> liqCheckOne(HashMap<String, Double> stockFSPortion) {
        ArrayList<String> failed = new ArrayList<>();
        for (String stock : stockMarketCap.keySet()) {
            BigDecimal stockFS;
            if (stockFSPortion.get(stock) == null){
                stockFS = BigDecimal.valueOf(0.5);
            } else {
                stockFS = BigDecimal.valueOf(stockFSPortion.get(stock));
            }
            BigDecimal result = maxCollateralPledged(stock, stockFS);
            if ( result == null || result.compareTo(BigDecimal.valueOf(10000000)) < 0){
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
    public BigDecimal maxCollateralPledged(String stock, BigDecimal stockFS){
        BigDecimal sum = BigDecimal.valueOf(0);
        BigDecimal maxCP;
        if (stockTotalValueAveraged.get(stock) == null){
            return null;
        } else {
            for (BigDecimal value : stockTotalValueAveraged.get(stock)) {
                sum = sum.add(value);
            }
            BigDecimal avgTradingValue = sum.divide(BigDecimal.valueOf(12), MathContext.DECIMAL32);
            maxCP = avgTradingValue.multiply(FSDays).multiply(tradingValuePercent)
                    .divide(stockFS, MathContext.DECIMAL32);
            stockMaxCP.put(stock, maxCP);
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
                    if (result.compareTo(BigDecimal.valueOf(0.005)) > 0) {
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
    public ArrayList<String> liqPassed(HashMap<String, Double> stockFSPortion){
        ArrayList<String> liquidityPassed = new ArrayList<>();
        ArrayList<String> failed = liqFailed(stockFSPortion);
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
    public ArrayList<String> liqFailed(HashMap<String, Double> stockFSPortion){
        ArrayList<String> liquidityFailed = new ArrayList<>();
        for (String stock : stockMarketCap.keySet()) {
            if (liqCheckOne(stockFSPortion).contains(stock) || liqCheckTwo().contains(stock)){
                liquidityFailed.add(stock);
            }
        }
        return liquidityFailed;
    }
}
