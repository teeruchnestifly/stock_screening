import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Volatility{

    /**
     * A list containing all trading days in the 5 years.
     */
    private final ArrayList<Date> dates = new ArrayList<>();

    /**
     * A hashmap mapping each stock to an arrayList of their closing prices for each day in the 5 years.
     */
    private final HashMap<String, ArrayList<BigDecimal>> stockClosingPrices = new HashMap<>();

    /**
     * A hashmap mapping each stock to an arrayList of the 75% LTV of each closing price for each day in the 5 years.
     */
    private final HashMap<String, ArrayList<BigDecimal>> stock75LTV = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arrayList of the 100% LTV of each closing price for each day in the 5 years.
     */
    private final HashMap<String, ArrayList<BigDecimal>> stock100LTV = new HashMap<>();
    /**
     * An arraylist containing the number of trading days needed for the set simulation length for each date.
     */
    private final ArrayList<Integer> rowCount = new ArrayList<>();
    /**
     * A hashmap mapping each stock to an arrayList of the number of times the stock reached 75% of its LTV during the
     * simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Integer>> count75 = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arrayList of the number of times the stock reached 100% of its LTV during the
     * simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Integer>> count100 = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arrayList of the probability that the stock will fall to 75% of its LTV value
     * during the simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Double>> prob75 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> prob75M60 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> prob75M50 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> prob75M40 = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arrayList of the probability that the stock will fall to 100% of its LTV value
     * during the simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Double>> prob100 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> prob100M60 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> prob100M50 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> prob100M40 = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arrayList storing the number of days it took for the stock to go from 75% of
     * its LTV to 100% of its LTV during the simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Integer>> difference75to100 = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> difference75to100M60 = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> difference75to100M50 = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> difference75to100M40 = new HashMap<>();
    /**
     * A hashmap mapping each stock to a double representing the weighted average probability of the stock reaching 75%
     * of its LTV over the course of the 5 years.
     */
    private final HashMap<String, Double> weightedAverage = new HashMap<>();
    private final HashMap<String, Double> weightedAverageM60 = new HashMap<>();
    private final HashMap<String, Double> weightedAverageM50 = new HashMap<>();
    private final HashMap<String, Double> weightedAverageM40 = new HashMap<>();
    /**
     * A hashmap mapping each stock to a double representing the normal average probability of the stock reaching 75%
     * of its LTV over the course of the 5 years.
     */
    private final HashMap<String, Double> normalAverage = new HashMap<>();
    private final HashMap<String, Double> normalAverageM60 = new HashMap<>();
    private final HashMap<String, Double> normalAverageM50 = new HashMap<>();
    private final HashMap<String, Double> normalAverageM40 = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arraylist of doubles representing the average probability of the stock
     * reaching 75% of its LTV for each of the 20 periods in the 5 years.
     */
    private final HashMap<String, ArrayList<Double>> Average = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> AverageM60 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> AverageM50 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> AverageM40 = new HashMap<>();
    /**
     * A hashmap mapping each stock to a double representing the weighted average probability of the stock reaching 100%
     * of its LTV over the course of the 5 years.
     */
    private final HashMap<String, Double> weightedAverage100 = new HashMap<>();
    private final HashMap<String, Double> weightedAverageM60p100 = new HashMap<>();
    private final HashMap<String, Double> weightedAverageM50p100 = new HashMap<>();
    private final HashMap<String, Double> weightedAverageM40p100 = new HashMap<>();
    /**
     * A hashmap mapping each stock to a double representing the normal average probability of the stock reaching 100%
     * of its LTV over the course of the 5 years.
     */
    private final HashMap<String, Double> normalAverage100 = new HashMap<>();
    private final HashMap<String, Double> normalAverageM60p100 = new HashMap<>();
    private final HashMap<String, Double> normalAverageM50p100 = new HashMap<>();
    private final HashMap<String, Double> normalAverageM40p100 = new HashMap<>();
    /**
     * A hashmap mapping each stock to an arraylist of doubles representing the average probability of the stock
     * reaching 100% of its LTV for each of the 20 periods in the 5 years.
     */
    private final HashMap<String, ArrayList<Double>> Average100 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> AverageM60p100 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> AverageM50p100 = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> AverageM40p100 = new HashMap<>();
    /** A hashmap mapping each stock to an array list of integers representing the minimum difference in days between a
     * closing price reaching 75% to 100% of its LTV for every period in the 5 years.
     */
    private final HashMap<String, ArrayList<Integer>> minDaysAvgM60 = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> minDaysAvgM50 = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> minDaysAvgM40 = new HashMap<>();
    /** A hashmap mapping each stock to an integer representing the minimum difference in days between a closing price
     * reaching 75% to 100% of its LTV for the last 3 years.
     */
    private final HashMap<String, Integer> minCount3y = new HashMap<>();
    private final HashMap<String, Integer> minCount3yM60 = new HashMap<>();
    private final HashMap<String, Integer> minCount3yM50 = new HashMap<>();
    private final HashMap<String, Integer> minCount3yM40 = new HashMap<>();
    /** A hashmap mapping each stock to an integer representing the minimum difference in days between a closing price
     * reaching 75% to 100% of its LTV for the last 5 years.
     */
    private final HashMap<String, Integer> minCount5yM60 = new HashMap<>();
    private final HashMap<String, Integer> minCount5yM50 = new HashMap<>();
    private final HashMap<String, Integer> minCount5yM40 = new HashMap<>();
    /** The current ltv rating */
    private Double ltv;
    /** The list of stocks that have passed each of the rounds of testing*/
    private final ArrayList<String> pass60 = new ArrayList<>();
    private final ArrayList<String> pass50 = new ArrayList<>();
    private final ArrayList<String> pass40 = new ArrayList<>();
    /** The max rating for each of the stocks */
    private final ArrayList<String> MAX50 = new ArrayList<>();
    private final ArrayList<String> MAX40 = new ArrayList<>();
    private final ArrayList<String> failed = new ArrayList<>();
    /**
     * Constant for when the Excel spreadsheet has NA for the closing price.
     */
    private static final Integer noData = 99999;
    /**
     * Constant for when the Excel spreadsheet has NA for the closing price but in a double format.
     */
    private static final double noDataDouble = 99999.99;
    /**
     * Constant for the number of periods of months in the 5-year simulation.
     */
    private static final Integer numberOfPeriods = 20;
    /**
     * Constant used in the EWMA calculation.
     */
    private static final double lambda = 0.94;
    /**
     * Constant used to determine the failing condition of the first check.
     */
    private static final double checkOneFailCondition = 0.25;
    /**
     * Constant used to determine the failing condition of the second check.
     */
    private static final Integer checkTwoFailCondition = 5;



    /**
     * Reads in the data stored in the given master data Excel file and creates a hashmap stockClosingPrice containing
     * every stock mapped to an array list of its closing prices and creates an arraylist dates containing every date
     * in the 5-year period.
     * Updates the stockClosingPrices hashmap, storing the closing prices for every stock and the dates arraylist,
     * storing every date from the Excel file
     */
    public void dataCollection() throws IOException {
        String excelFilePath = "rawData.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int numColumns = sheet.getRow(0).getLastCellNum();
        // Loops through every column from 1 onwards, and then loops through every row in each column to populate the
        // hashmap of stocks and their closing prices
        for (int c = 1; c < numColumns; c++) {
            String stock;
            // Try catch clause needed because the stock TRUE is read as a boolean and therefore getStringCellValue
            // causes an error
            try {
                stock = sheet.getRow(0).getCell(c).getStringCellValue();
            } catch (IllegalStateException e1) {
                stock = "TRUE";
            }
            ArrayList<BigDecimal> closingPrices = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                BigDecimal value;
                // Try catch clause needed because N/A is read as a string and therefore getNumericCellValue causes an
                // error, N/A is stored as the int 99999 instead
                try {
                    value = BigDecimal.valueOf(sheet.getRow(r).getCell(c).getNumericCellValue());
                    closingPrices.add(value);
                } catch (Exception e2) {
                    value = BigDecimal.valueOf(noData);
                    closingPrices.add(value);
                }
            }
            stockClosingPrices.put(stock, closingPrices);
        }

        //Loops through the first column of the Excel sheet and adds the dates to an array list dates
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Date currentDate = sheet.getRow(i).getCell(0).getDateCellValue();
            dates.add(currentDate);
        }
    }


    /**
     * A function that will loop through every date in the arrayList dates and calculate the number of trading days in
     * a given simulation length e.g. 9 months.
     *
     * @param simulationLength the number of months for the simulation period.
     *
     * Updates the arrayList rowCount which stores the number of trading days in the simulation length for each day.
     */
    public void numDaysForward(Integer simulationLength) {
        // Will first calculate the index of the date corresponding to the number of months in the simulation length
        // before the reporting date
        Date reportDate = dates.get(dates.size() - 1);  // Report date is the most recent date in the list
        int reportIndexSubmonths = helperDate(simulationLength, reportDate) - 1;
        // Loops through every date up until reportIndexSubMonths which is the index of the date
        do {
            rowCount.clear();
            reportIndexSubmonths += 1;
            for (int i = 0; i <= reportIndexSubmonths; i++) {
                Date date = dates.get(i);
                LocalDate LD = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
                LocalDate futureLD = LD.plusMonths(simulationLength);
                if (futureLD.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    futureLD = futureLD.minusDays(1);
                }
                if (futureLD.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    futureLD = futureLD.minusDays(2);
                }
                Date futureDate = Date.from(futureLD.atStartOfDay(ZoneId.systemDefault()).toInstant());
                while (!dates.contains(futureDate)) {
                    futureLD = futureLD.minusDays(1);
                    futureDate = Date.from(futureLD.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
                rowCount.add(dates.indexOf(futureDate) - dates.indexOf(date));
            }
            for (int i = reportIndexSubmonths; i < dates.size() - 1; i++) {
                rowCount.add(rowCount.get(i) - 1);
            }
        } while (rowCount.get(rowCount.size() - 1) == -1);
    }


    /**
     * A helper function which is used to calculate and return an index of a date a given number of months prior to
     * the input date.
     *
     * @param months the number of months for the simulation period
     * @param reportDate the final date in the arrayList dates
     *
     * @return reportIndexSubMonths the index of the date (in the arrayList dates) the set amount of months prior to
     * the reporting date.
     */
    public Integer helperDate(Integer months, Date reportDate){
        LocalDate reportLD = LocalDate.ofInstant(reportDate.toInstant(), ZoneId.systemDefault());
        LocalDate reportLDSubMonths = reportLD.minusMonths(months);
        Date reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int reportIndexSubMonths = dates.indexOf(reportDateSubMonths);
        // If the calculated date is not a business day, will subtract one day at a time until we reach a business day
        while (reportIndexSubMonths == -1) {
            reportLDSubMonths = reportLDSubMonths.minusDays(1);
            reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
            reportIndexSubMonths = dates.indexOf(reportDateSubMonths);
        }
        return reportIndexSubMonths;
    }


    /**
     * A function that will calculate and store the 75% and 100% LTV of each closing price for each day in the 5 year
     * period.
     *
     * @param LTV the specified LTV value for the simulation
     * Updates the hashmaps stock75LTV and stock100LTV which contain the values of 75% and 100% LTV for each closing
     * price respectively .
     */
    public void LTVVal(Double LTV) {
        ltv = LTV;
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<BigDecimal> result75 = calcLTV(stock, 0.75, LTV);
            ArrayList<BigDecimal> result100 = calcLTV(stock, 1.00, LTV);
            stock75LTV.put(stock, result75);
            stock100LTV.put(stock, result100);
        }
    }

    /**
     * A helper function that will loop through a given list of closing prices and calculate a given LTV percentage.
     *
     * @param LTV     the specified LTV value for the simulation
     * @param stock   the current stock in the iteration
     * @param percent the specified percentage level e.g. 75 or 100
     * @return LTVvals an arraylist storing either 75% or 100% of the LTV value for each closing price of the given
     * stock.
     */
    public ArrayList<BigDecimal> calcLTV(String stock, double percent, double LTV) {
        ArrayList<BigDecimal> LTVvals = new ArrayList<>();
        for (BigDecimal closePrice : stockClosingPrices.get(stock)) {
            //Check to see if the price is equal to 99999 which represents null or no data
            if (Objects.equals(closePrice, BigDecimal.valueOf(noData))) {
                LTVvals.add(BigDecimal.valueOf(noDataDouble));
            } else {
                BigDecimal bdec = closePrice;
                bdec = bdec.multiply(new BigDecimal(LTV));
                LTVvals.add(bdec.divide(new BigDecimal(percent), MathContext.DECIMAL64));
            }
        }
        return LTVvals;
    }


    /**
     * A function that will calculate and store the number of times the closing price reached 75 or 100% of its LTV
     * during the simulation length, for every stock.
     * Updates the hashmaps count75 and count100 which contain the number of times the closing price reached 75% and
     * 100% of its value. Also updates difference75to100 which contains the number of days it takes for the closing
     * price to drop from 75% to 100% of its value.
     */
    public void count() {
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Date> first75Day = new ArrayList<>();
            ArrayList<Date> first100Day = new ArrayList<>();
            ArrayList<Integer> numDays = new ArrayList<>();
            ArrayList<Integer> result75 = calcCount(stock, stock75LTV.get(stock), first75Day);
            ArrayList<Integer> result100 = calcCount(stock, stock100LTV.get(stock), first100Day);
            count75.put(stock, result75);
            count100.put(stock, result100);
            for (int i = 0; i < first75Day.size(); i++) {
                // A check to put in 99999 representing null in the case that the stock never reaches 100% of its LTV
                if (first100Day.get(i) == null) {
                    numDays.add(noData);
                } else {
                    numDays.add(dates.indexOf(first100Day.get(i)) - dates.indexOf(first75Day.get(i)));
                }
            }
            difference75to100.put(stock, numDays);
            if (ltv == 0.6){
                difference75to100M60.put(stock, numDays);
            } else if (ltv == 0.5){
                difference75to100M50.put(stock, numDays);
            } else if (ltv == 0.4){
                difference75to100M40.put(stock, numDays);
            }
        }
    }


    /**
     * A helper function that will loop through a given list of closing prices and calculate the number of times the
     * closing price reaches 75% or 100% of its LTV during the simulation length.
     *
     * @param percentLTV an arrayList of either 75% or 100% LTV of each closing price for each day in the 5 years.
     * @param stock the current stock in the iteration
     * @param firstDay an arraylist which stores the date when the closing price reaches 75% or 100% for the first time
     *
     * @return countList an arraylist storing the number of times the closing price reaches either 75% or 100% of its
     * value during the simulation length.
     */
    public ArrayList<Integer> calcCount(String stock, ArrayList<BigDecimal> percentLTV, ArrayList<Date> firstDay) {
        ArrayList<BigDecimal> closePrices = stockClosingPrices.get(stock);
        ArrayList<Integer> countList = new ArrayList<>();
        for (int i = 0; i < closePrices.size(); i++) {
            int count = 0;
            firstDay.add(null);
            for (int k = i; k < i + rowCount.get(i); k++) {
                // A check to avoid including days which do not have any data into the count
                if ((!Objects.equals(closePrices.get(k), BigDecimal.valueOf(noData))) &&
                        (!Objects.equals(percentLTV.get(i), BigDecimal.valueOf(noDataDouble)))) {
                    if (closePrices.get(k).compareTo(percentLTV.get(i)) <= 0) {
                        // A check to determine the date which is the first time the closing price reaches 75% or 100%
                        // of its value
                        if (count == 0) {
                            firstDay.remove(i);
                            firstDay.add(dates.get(k));
                        }
                        count += 1;
                    }
                }
            }
            countList.add(count);
        }
        return countList;
    }


    /**
     * A function that will calculate and store the probability of the stock's closing price reaching 75 or 100% LTV.
     * Updates the hashmaps prob75 and prob100 which contain the probability that the stock will fall to 75% of its LTV
     * value during the simulation length.
     */
    public void probability() {
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Double> result75 = calcProb(count75.get(stock), stock);
            ArrayList<Double> result100 = calcProb(count100.get(stock), stock);
            prob75.put(stock, result75);
            prob100.put(stock, result100);
            if (ltv == 0.6){
                prob75M60.put(stock, result75);
                prob100M60.put(stock, result100);
            } else if (ltv == 0.5){
                prob75M50.put(stock, result75);
                prob100M50.put(stock, result100);
            } else if (ltv == 0.4){
                prob75M40.put(stock, result75);
                prob100M40.put(stock, result100);
            }
        }
    }


    /**
     * A helper function that will calculate the probability of the closing price reaching 75% or 100% of its LTV during
     * the simulation length.
     *
     * @param count an arrayList containing the number of times the stock reached 75% or 100% of its LTV during the
     * simulation length.
     * @param stock the current stock in the iteration
     *
     * @return probList an arraylist storing the probability of each closing price reaching 75% or 100% of its LTV during
     * the simulation length.
     */
    public ArrayList<Double> calcProb(ArrayList<Integer> count, String stock) {
        ArrayList<Double> probList = new ArrayList<>();
        for (int i = 0; i < count.size(); i++) {
            // A check to deal with days that have no data
            if (Objects.equals(stockClosingPrices.get(stock).get(i), BigDecimal.valueOf(noData))){
                probList.add(noDataDouble);
            } else {
                probList.add(count.get(i) / (double) rowCount.get(i));
            }
        }
        return probList;
    }


    /**
     * A function that will perform the first volatility check by calculating the weighted average of the probabilities
     * of the stock reaching 75% of its LTV and comparing that value to 25%. If it is greater, the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> volCheckOne(HashMap<String, ArrayList<Double>> prob){
        ArrayList<String> failed = new ArrayList<>();
        // The calculation of the EWMA table, used to weight the averages properly
        ArrayList<Double> EWMA = new ArrayList<>();
        EWMACalc(EWMA);
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Double> result = calcAverages(stock, prob);
            Average.put(stock, result);
            if (ltv == 0.6){
                AverageM60.put(stock, result);
            } else if (ltv == 0.5){
                AverageM50.put(stock, result);
            } else if (ltv == 0.4){
                AverageM40.put(stock, result);
            }
            double sum = 0.0;
            double count = 0.0;
            for (Double aDouble : result) {
                if (aDouble != null) {
                    sum += aDouble;
                    count += 1;
                }
            }
            normalAverage.put(stock, sum/ count);
            if (ltv == 0.6){
                normalAverageM60.put(stock, sum/count);
            } else if (ltv == 0.5){
                normalAverageM50.put(stock, sum/count);
            } else if (ltv == 0.4){
                normalAverageM40.put(stock, sum/count);
            }
            sum = 0.0;
            for (int i = 0; i < result.size(); i++){
                if (result.get(i) != null){
                    sum += EWMA.get(i) * result.get(i);
                }
            }
            double sumEWMA = 0.0;
            for (int j = 0; j < result.size(); j++){
                if (result.get(j) != null){
                    sumEWMA += EWMA.get(j);
                }
            }
            if (Double.isNaN(sum / sumEWMA) || sum / sumEWMA > checkOneFailCondition ){
                failed.add(stock);
            }
            weightedAverage.put(stock, sum/sumEWMA);
            if (ltv == 0.6){
                weightedAverageM60.put(stock, sum/sumEWMA);
            } else if (ltv == 0.5){
                weightedAverageM50.put(stock, sum/sumEWMA);
            } else if (ltv == 0.4){
                weightedAverageM40.put(stock, sum/sumEWMA);
            }
        }
        return failed;
    }


    /**
     * A function that performs the EWMA calculation and adds the results to an arraylist to be used in calculating
     * the weighted average of probabilities.
     *
     */
    private static void EWMACalc(ArrayList<Double> EWMA) {
        EWMA.add((1-lambda)/(1-(Math.pow(lambda, numberOfPeriods))));
        for (int i = 1; i < numberOfPeriods; i++){
            EWMA.add(EWMA.get(i-1)*lambda);
        }
    }


    /**
     * A function that will calculate and store the averaged probability values of a given stock. In order to do this,
     * the function must calculate 3-month intervals starting from the month 3-months prior to the reporting date. These
     * 20 periods will store the averaged probabilities.
     *
     * @return result an arraylist storing the averaged probabilities.
     */
    public ArrayList<Double> calcAverages(String stock, HashMap<String, ArrayList<Double>> prob){
        ArrayList<Double> result = new ArrayList<>();
        // Will first calculate the last day of the month 3-months prior to the month of the reporting date e.g. April
        // to February
        Date reportDate = dates.get(dates.size() - 1);
        LocalDate reportLD = LocalDate.ofInstant(reportDate.toInstant(), ZoneId.systemDefault());
        LocalDate reportLDSubMonths = reportLD.minusMonths(2);
        Date reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int reportIndexSubmonths = dates.indexOf(reportDateSubMonths);
        while (reportLDSubMonths.getMonth() != reportLD.getMonth().minus(2)){
            reportLDSubMonths = reportLDSubMonths.plusDays(1);
        }
        reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        reportIndexSubmonths = dates.indexOf(reportDateSubMonths);
        while (reportIndexSubmonths == -1) {
            reportLDSubMonths = reportLDSubMonths.plusDays(1);
            reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
            reportIndexSubmonths = dates.indexOf(reportDateSubMonths);
        }

        // Will create an arraylist storing the indexes of the dates which will make up the periods for averaging the
        // data
        Date initialDate = dates.get(reportIndexSubmonths);
        ArrayList<Integer> indexOfMonthPeriods = new ArrayList<>();
        indexOfMonthPeriods.add(reportIndexSubmonths);
        for (int i = 0; i < 19; i++){
            indexOfMonthPeriods.add(calcDatePeriods(initialDate));
            initialDate = dates.get(calcDatePeriods(initialDate));
        }
        indexOfMonthPeriods.add(0);
        Collections.reverse(indexOfMonthPeriods);
        // Will loop through each 3-month period and calculate the average of the probabilities
        int count1 = 0;
        int numDays1 = 0;
        int noDataCount1 = 0;
        for ( int i = indexOfMonthPeriods.get(0); i <= indexOfMonthPeriods.get(1); i++){
            // A check to see if there is no data for the current day
            if ((prob.get(stock).get(i) == noDataDouble)) {
                noDataCount1++;
            }
            if ((prob.get(stock).get(i) != noDataDouble) && (prob.get(stock).get(i) > 0)) {
                count1++;
            }
            numDays1++;
        }
        if (noDataCount1 == numDays1) {
            result.add(null);
        } else {
            result.add((count1 / (double) numDays1));
        }
        for (int j = 2; j < indexOfMonthPeriods.size(); j++) {
            int count = 0;
            int numDays = 0;
            int noDataCount = 0;
            for (int i = indexOfMonthPeriods.get(j - 1) + 1; i <= indexOfMonthPeriods.get(j); i++) {
                // A check to see if there is no data for the current day
                if ((prob.get(stock).get(i) == noDataDouble)) {
                    noDataCount++;
                }
                if ((prob.get(stock).get(i) != noDataDouble) && (prob.get(stock).get(i) > 0)) {
                    count++;
                }
                numDays++;
            }
            // If noDataCount is equal to numDays, it means there is no data for the entire 3-month period and null is
            // stored instead of a value
            if (noDataCount == numDays) {
                result.add(null);
            } else {
                result.add((count / (double) (numDays - noDataCount)));
            }
        }
        return result;
    }


    /**
     * A helper function that will calculate the index of a date 3 months prior to given date initialDate.
     *
     * @param initialDate the date for which 3 months will be subtracted from
     *
     * @return initialDateSubMonths an integer representing the index of the date 3 months prior to the initialDate
     */
    public Integer calcDatePeriods(Date initialDate){
        LocalDate reportLD = LocalDate.ofInstant(initialDate.toInstant(), ZoneId.systemDefault());
        LocalDate reportLDSubMonths = reportLD.minusMonths(3);
        Date reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int initialDateSubMonths = dates.indexOf(reportDateSubMonths);
        while (reportLDSubMonths.getMonth() != reportLD.getMonth().minus(2)){
            reportLDSubMonths = reportLDSubMonths.plusDays(1);
        }
        reportLDSubMonths = reportLDSubMonths.minusDays(1);
        reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        initialDateSubMonths = dates.indexOf(reportDateSubMonths);
        while (initialDateSubMonths == -1) {
            reportLDSubMonths = reportLDSubMonths.minusDays(1);
            reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
            initialDateSubMonths = dates.indexOf(reportDateSubMonths);
        }
        return initialDateSubMonths;
    }


    /**
     * A function that will perform the second volatility check by looping through the number of days between the
     * closing price reaching 75% and 100% and checking if that value is less than 5. If so then the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> volCheckTwo() {
        ArrayList<String> failed = new ArrayList<>();
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Integer> result = calcMinValues(stock);
            if (ltv == 0.6){
                minDaysAvgM60.put(stock, result);
            } else if (ltv == 0.5){
                minDaysAvgM50.put(stock, result);
            } else if (ltv == 0.4){
                minDaysAvgM40.put(stock, result);
            }
            int minDays = 99999;
            for (Integer integer : result) {
                if (integer < minDays) {
                    minDays = integer;
                }
            }
            if (ltv == 0.6){
                minCount5yM60.put(stock, minDays);
            } else if (ltv == 0.5){
                minCount5yM50.put(stock, minDays);
            } else if (ltv == 0.4){
                minCount5yM40.put(stock, minDays);
            }
            minDays = 99999;
            for (int i = 8; i < result.size(); i++) {
                if(result.get(i) < minDays) {
                    minDays = result.get(i);
                }
            }
            if (minDays < checkTwoFailCondition) {
                failed.add(stock);
            }
            minCount3y.put(stock, minDays);
            if (ltv == 0.6){
                minCount3yM60.put(stock, minDays);
            } else if (ltv == 0.5){
                minCount3yM50.put(stock, minDays);
            } else if (ltv == 0.4){
                minCount3yM40.put(stock, minDays);
            }
        }
        return failed;
    }


    /**
     * A function that will perform the second volatility check by looping through the number of days between the
     * closing price reaching 75% and 100% and checking if that value is less than 5. If so then the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<Integer> calcMinValues(String stock) {
        ArrayList<Integer> result = new ArrayList<>();
        Date reportDate = dates.get(dates.size() - 1);
        LocalDate reportLD = LocalDate.ofInstant(reportDate.toInstant(), ZoneId.systemDefault());
        LocalDate reportLDSubMonths = reportLD.minusMonths(2);
        Date reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int reportIndexSubmonths = dates.indexOf(reportDateSubMonths);
        while (reportLDSubMonths.getMonth() != reportLD.getMonth().minus(2)) {
            reportLDSubMonths = reportLDSubMonths.plusDays(1);
        }
        reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
        reportIndexSubmonths = dates.indexOf(reportDateSubMonths);
        while (reportIndexSubmonths == -1) {
            reportLDSubMonths = reportLDSubMonths.plusDays(1);
            reportDateSubMonths = Date.from(reportLDSubMonths.atStartOfDay(ZoneId.systemDefault()).toInstant());
            reportIndexSubmonths = dates.indexOf(reportDateSubMonths);
        }
        Date initialDate = dates.get(reportIndexSubmonths);
        ArrayList<Integer> indexOfMonthPeriods = new ArrayList<>();
        indexOfMonthPeriods.add(reportIndexSubmonths);
        for (int i = 0; i < 19; i++) {
            indexOfMonthPeriods.add(calcDatePeriods(initialDate));
            initialDate = dates.get(calcDatePeriods(initialDate));
        }
        indexOfMonthPeriods.add(0);
        Collections.reverse(indexOfMonthPeriods);
        ArrayList<Integer> stockDifference = difference75to100.get(stock);
        int count1 = 0;
        int minVal1 = 999999;
        for (int i = indexOfMonthPeriods.get(0); i <= indexOfMonthPeriods.get(1); i++) {
            if (stockDifference.get(i) > 0 & !(Objects.equals(stockDifference.get(i), noData))) {
                count1++;
            }
            if (stockDifference.get(i) < minVal1) {
                minVal1 = stockDifference.get(i);
            }
        }
        if (count1 > 0) {
            result.add(minVal1);
        } else {
            result.add(noData);
        }
        for (int j = 2; j < indexOfMonthPeriods.size(); j++) {
            int count = 0;
            int minVal = 999999;
            for (int i = indexOfMonthPeriods.get(j - 1) + 1; i <= indexOfMonthPeriods.get(j); i++) {
                if (stockDifference.get(i) > 0 & !(Objects.equals(stockDifference.get(i), noData))) {
                    count++;
                }
                if (stockDifference.get(i) < minVal) {
                    minVal = stockDifference.get(i);
                }
            }
            if (count > 0) {
                result.add(minVal);
            } else {
                result.add(noData);
            }
        }
        return result;
    }


    /**
     * A function that will check to see which stocks failed the volatility test.
     *
     * @return volatilityFailed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> volatilityFailed(){
        ArrayList<String> volatilityFailed = new ArrayList<>();
        for (String stock : stockClosingPrices.keySet()) {
            if (volCheckOne(prob75).contains(stock) || volCheckTwo().contains(stock)){
                volatilityFailed.add(stock);
            }
        }
        return volatilityFailed;
    }


    /**
     * A function that will check to see which stocks passed the volatility test.
     *
     * @return volatilityPassed an arraylist of the stocks that passed the test
     */
    public ArrayList<String> volatilityPassed(){
        ArrayList<String> volatilityPassed = new ArrayList<>();
        ArrayList<String> failed = volatilityFailed();
        for (String stock : stockClosingPrices.keySet()) {
            if (!failed.contains(stock)){
                volatilityPassed.add(stock);
                if (ltv == 0.6){
                    pass60.add(stock);
                } else if (ltv == 0.5){
                    pass50.add(stock);
                } else if (ltv == 0.4){
                    pass40.add(stock);
                }
            }
        }
        calcAverages100(prob100);
        return volatilityPassed;
    }

    /**
     * A function that will read in the volatility result template and write and output a new file.
     *
     * Outputs an updated file containing the data used in the volatility test.
     *
     * **Currently cannot run on a large number of stocks due to an out of memory error. I had to rush the code for this
     * and it is not very efficient, could simplify by removing lots of the stored variables or switch to writing an
     * SXSSFWorkbook instead: https://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api may help.
     */
    public void writeVolatilityResult() throws IOException {
        String excelFilePath = "Result Template of Volatility Test.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
//        Workbook workbook = new XSSFWorkbook(inputStream);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
        CellStyle percentStyle = workbook.createCellStyle();
        percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.000%"));
        Sheet Analysis60 = workbook.getSheetAt(2);
            calcAnalysis(Analysis60, weightedAverageM60, AverageM60, normalAverageM60, minDaysAvgM60, minCount3yM60,
                    minCount5yM60, weightedAverageM60p100, AverageM60p100, normalAverageM60p100, percentStyle);
        Sheet Result60 = workbook.getSheetAt(5);
            calcResult(Result60, prob75M60, prob100M60, difference75to100M60, dates, cellStyle);
        Sheet Analysis50 = workbook.getSheetAt(3);
        calcAnalysis(Analysis50, weightedAverageM50, AverageM50, normalAverageM50, minDaysAvgM50, minCount3yM50,
                minCount5yM50, weightedAverageM50p100, AverageM50p100, normalAverageM50p100, percentStyle);
        Sheet Result50 = workbook.getSheetAt(6);
        calcResult(Result50, prob75M50, prob100M50, difference75to100M50, dates, cellStyle);
        Sheet Analysis40 = workbook.getSheetAt(4);
        calcAnalysis(Analysis40, weightedAverageM40, AverageM40, normalAverageM40, minDaysAvgM40, minCount3yM40,
                minCount5yM40, weightedAverageM40p100, AverageM40p100, normalAverageM40p100, percentStyle);
        Sheet Result40 = workbook.getSheetAt(7);
        calcResult(Result40, prob75M40, prob100M40, difference75to100M40, dates, cellStyle);
        Sheet summary = workbook.getSheetAt(0);
        calcSummary(summary);
        FileOutputStream outputStream = new FileOutputStream("Volatility_Test_Result_3.xls");
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }


    /**
     * A helper function that calculates the MAX rating for all stocks and assigns them into a list.
     */
    public void summaryHelper(){
        for (String stock : pass50) {
            if (!pass60.contains(stock)) {
                MAX50.add(stock);
            }
        }
        for (String stock : pass40) {
            if (!MAX50.contains(stock) && !pass60.contains(stock)) {
                MAX40.add(stock);
            }
        }
        System.out.println(MAX40);
        for (String stock : stockClosingPrices.keySet()) {
            if (!MAX50.contains(stock) && !pass60.contains(stock) && !MAX40.contains(stock)) {
                failed.add(stock);
            }
        }
    }

    /**
     * A helper function that updates the summary sheet of the volatility result file.
     */
    public void calcSummary(Sheet summary){
        summaryHelper();
        int rowCount = 1;
        for (String stock : stockClosingPrices.keySet()) {
            Row row = summary.createRow(rowCount);
            Cell numStock = row.createCell(0);
            numStock.setCellValue(rowCount);
            Cell stockCell = row.createCell(1);
            stockCell.setCellValue(stock);
            Cell testResult = row.createCell(2);
            if (failed.contains(stock)){
                testResult.setCellValue("Fail");
            } else {
                testResult.setCellValue("Pass");
            }
            Cell maxRating = row.createCell(3);
            if (pass60.contains(stock)){
                maxRating.setCellValue("MAX60");
            } else if (MAX50.contains(stock)){
                maxRating.setCellValue("MAX50");
            } else if (MAX40.contains(stock)){
                maxRating.setCellValue("MAX40");
            } else {
                maxRating.setCellValue("Fail");
            }
            Cell numStock2 = row.createCell(9);
            numStock2.setCellValue(rowCount);
            Cell stockCell2 = row.createCell(10);
            stockCell2.setCellValue(stock);
            Cell M60 = row.createCell(11);
            if (pass60.contains(stock)){
                M60.setCellValue("Pass");
            } else {
                M60.setCellValue("Fail");
            }
            Cell M50 = row.createCell(12);
            if (pass50.contains(stock)){
                M50.setCellValue("Pass");
            } else {
                M50.setCellValue("Fail");
            }
            Cell M40 = row.createCell(13);
            if (pass40.contains(stock)){
                M40.setCellValue("Pass");
            } else {
                M40.setCellValue("Fail");
            }
            rowCount++;
        }
        Cell Max60 = summary.getRow(1).createCell(6);
        Cell Heading1 = summary.getRow(1).createCell(5);
        Max60.setCellValue(pass60.size());
        Heading1.setCellValue("MAX60");
        Cell Max50 = summary.getRow(2).createCell(6);
        Cell Heading2 = summary.getRow(2).createCell(5);
        Max50.setCellValue(MAX50.size());
        Heading2.setCellValue("MAX50");
        Cell Max40 = summary.getRow(3).createCell(6);
        Cell Heading3 = summary.getRow(3).createCell(5);
        Max40.setCellValue(MAX40.size());
        Heading3.setCellValue("MAX40");
        Cell Fail = summary.getRow(4).createCell(6);
        Cell Heading4 = summary.getRow(4).createCell(5);
        Fail.setCellValue(failed.size());
        Heading4.setCellValue("Fail");
        Cell Total = summary.getRow(5).createCell(6);
        Cell Heading5 = summary.getRow(5).createCell(5);
        Total.setCellValue(stockClosingPrices.size());
        Heading5.setCellValue("Total");
    }

    /**
     * A helper function that updates the result sheets of the volatility result file.
     */
    public void calcResult(Sheet resultSheet, HashMap<String, ArrayList<Double>> prob75,
                           HashMap<String, ArrayList<Double>> prob100,
                           HashMap<String, ArrayList<Integer>> difference75to100, ArrayList<Date> dates,
                           CellStyle cellStyle){
        int rowCount = 2;
        for (Date date : dates) {
            Row row = resultSheet.createRow(rowCount);
            Cell day = row.createCell(0);
            day.setCellValue(date);
            day.setCellStyle(cellStyle);
            rowCount++;
        }
        Row stockRow = resultSheet.createRow(1);
        Cell stockHeading = resultSheet.getRow(1).createCell(0);
        stockHeading.setCellValue("Stock");
        int colCount = 1;
        for (String stock : stockClosingPrices.keySet()) {
            Cell stockCell = stockRow.createCell(colCount);
            stockCell.setCellValue(stock);
            for (int i = 2; i < rowCount; i++){
                Cell prob = resultSheet.getRow(i).createCell(colCount);
                if (prob75.get(stock).get(i - 2) == 99999.99){
                    prob.setCellValue("XXX");
                } else {
                    prob.setCellValue(prob75.get(stock).get(i - 2));
                }
            }
            colCount++;
        }
        colCount++;
        Cell heading = resultSheet.getRow(0).createCell(colCount);
        heading.setCellValue("Probability of reaching 100%LTV ");
        for (String stock : stockClosingPrices.keySet()) {
            Cell stockCell = stockRow.createCell(colCount);
            stockCell.setCellValue(stock);
            for (int i = 2; i < rowCount; i++){
                Cell prob = resultSheet.getRow(i).createCell(colCount);
                if (prob100.get(stock).get(i - 2) == 99999.99){
                    prob.setCellValue("XXX");
                } else {
                    prob.setCellValue(prob100.get(stock).get(i - 2));
                }
            }
            colCount++;
        }
        colCount++;
        Cell heading2 = resultSheet.getRow(0).createCell(colCount);
        heading2.setCellValue("(Trading) Days from 75% LTV to 100% LTV");
        for (String stock : stockClosingPrices.keySet()) {
            Cell stockCell = stockRow.createCell(colCount);
            stockCell.setCellValue(stock);
            for (int i = 2; i < rowCount; i++){
                Cell dif = resultSheet.getRow(i).createCell(colCount);
                if (difference75to100.get(stock).get(i - 2) == 99999){
                    dif.setCellValue("XXX");
                } else {
                    dif.setCellValue(difference75to100.get(stock).get(i - 2));
                }
            }
            colCount++;
        }
    }

    /**
     * A helper function that updates the analysis sheets of the volatility result file.
     */
    public void calcAnalysis(Sheet analysisSheet, HashMap<String, Double> weightedAverage,
                             HashMap<String, ArrayList<Double>> Average, HashMap<String, Double> normalAverage,
                             HashMap<String, ArrayList<Integer>> minDaysAvg, HashMap<String, Integer> minCount3y,
                             HashMap<String, Integer> minCount5y, HashMap<String, Double> weightedAverage100,
                             HashMap<String, ArrayList<Double>> Average100, HashMap<String, Double> normalAverage100,
                             CellStyle percentStyle){
        int rowCount = 3;
        for (String stock : stockClosingPrices.keySet()) {
            Row row = analysisSheet.createRow(rowCount);
            Cell numStock = row.createCell(0);
            numStock.setCellValue(rowCount - 2);
            Cell stockCell = row.createCell(1);
            stockCell.setCellValue(stock);
            for (int i=0; i < Average.get(stock).size(); i++){
                Cell avgVal = row.createCell(i + 2);
                try {
                    avgVal.setCellValue(Average.get(stock).get(i));
                    avgVal.setCellStyle(percentStyle);
                } catch (NullPointerException e1) {
                    avgVal.setCellValue("XXX");
                }
            }
            Cell avg = row.createCell(22);
            avg.setCellValue(normalAverage.get(stock) );
            avg.setCellStyle(percentStyle);
            Cell EWMAavg = row.createCell(23);
            EWMAavg.setCellValue(weightedAverage.get(stock));
            EWMAavg.setCellStyle(percentStyle);
            for (int i=0; i < Average100.get(stock).size(); i++){
                Cell avgVal = row.createCell(i + 24);
                try {
                    avgVal.setCellValue(Average100.get(stock).get(i));
                    avgVal.setCellStyle(percentStyle);
                } catch (NullPointerException e1) {
                    avgVal.setCellValue("XXX");
                }
            }
            Cell avg100 = row.createCell(44);
            avg100.setCellValue(normalAverage100.get(stock));
            avg100.setCellStyle(percentStyle);
            Cell EWMAavg100 = row.createCell(45);
            EWMAavg100.setCellValue(weightedAverage100.get(stock));
            EWMAavg100.setCellStyle(percentStyle);
            for (int i=0; i < minDaysAvg.get(stock).size(); i++){
                Cell avgVal = row.createCell(i + 46);
                try {
                    if (minDaysAvg.get(stock).get(i) == 99999){
                        avgVal.setCellValue("XXX");
                    } else {
                        avgVal.setCellValue(minDaysAvg.get(stock).get(i));
                    }
                } catch (NullPointerException e1) {
                    avgVal.setCellValue("XXX");
                }
            }
            Cell minCountTotal = row.createCell(66);
            if (minCount5y.get(stock) == 99999){
                minCountTotal.setCellValue("XXX");
            } else {
                minCountTotal.setCellValue(minCount5y.get(stock));
            }
            Cell minCount = row.createCell(67);
            if (minCount3y.get(stock) == 99999){
                minCount.setCellValue("XXX");
            } else {
                minCount.setCellValue(minCount3y.get(stock));
            }
            rowCount++;
        }
    }

    /**
     * A function that will perform the first volatility check but with 100% LTV instead of 75% in order to store the
     * data for the volatility result Excel file.
     */
    public void calcAverages100(HashMap<String, ArrayList<Double>> prob){
        // The calculation of the EWMA table, used to weight the averages properly
        ArrayList<Double> EWMA = new ArrayList<>();
        EWMACalc(EWMA);
        Collections.reverse(EWMA);
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Double> result = calcAverages(stock, prob);
            Average100.put(stock, result);
            if (ltv == 0.6){
                AverageM60p100.put(stock, result);
            } else if (ltv == 0.5){
                AverageM50p100.put(stock, result);
            } else if (ltv == 0.4){
                AverageM40p100.put(stock, result);
            }
            double sum = 0.0;
            double count = 0.0;
            for (Double aDouble : result) {
                if (aDouble != null) {
                    sum += aDouble;
                    count += 1;
                }
            }
            normalAverage100.put(stock, sum/ count);
            if (ltv == 0.6){
                normalAverageM60p100.put(stock, sum/count);
            } else if (ltv == 0.5){
                normalAverageM50p100.put(stock, sum/count);
            } else if (ltv == 0.4){
                normalAverageM40p100.put(stock, sum/count);
            }
            sum = 0.0;
            for (int i = 0; i < result.size(); i++){
                if (result.get(i) != null){
                    sum += EWMA.get(i) * result.get(i);
                }
            }
            double sumEWMA = 0.0;
            for (int j = 0; j < result.size(); j++){
                if (result.get(j) != null){
                    sumEWMA += EWMA.get(j);
                }
            }
            weightedAverage100.put(stock, sum/sumEWMA);
            if (ltv == 0.6){
                weightedAverageM60p100.put(stock, sum/sumEWMA);
            } else if (ltv == 0.5){
                weightedAverageM50p100.put(stock, sum/sumEWMA);
            } else if (ltv == 0.4){
                weightedAverageM40p100.put(stock, sum/sumEWMA);
            }
        }
    }
}