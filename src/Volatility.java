import java.io.FileInputStream;
import java.io.IOException;
import java.time.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Volatility {

    /**
     * A list containing all trading days in the 5 years.
     */
    private final ArrayList<Date> dates = new ArrayList<>();

    /**
     * A hashmap mapping each stock to an arrayList of their closing prices for each day in the 5 years.
     */
    private final HashMap<String, ArrayList<Double>> stockClosingPrices = new HashMap<>();

    /**
     * A hashmap mapping each stock to an arrayList of the 75% LTV of each closing price for each day in the 5 years.
     */
    private final HashMap<String, ArrayList<Double>> stock75LTV = new HashMap<>();

    /**
     * A hashmap mapping each stock to an arrayList of the 100% LTV of each closing price for each day in the 5 years.
     */
    private final HashMap<String, ArrayList<Double>> stock100LTV = new HashMap<>();

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

    /**
     * A hashmap mapping each stock to an arrayList of the probability that the stock will fall to 100% of its LTV value
     * during the simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Double>> prob100 = new HashMap<>();

    /**
     * A hashmap mapping each stock to an arrayList storing the number of days it took for the stock to go from 75% of
     * its LTV to 100% of its LTV during the simulation length, for every closing price in the 5-year period.
     */
    private final HashMap<String, ArrayList<Integer>> difference75to100 = new HashMap<>();

    /**
     * A hashmap mapping each stock to a double representing the weighted average probability of the stock reaching 75%
     * of its LTV over the course of the 5 years.
     */
    private HashMap<String, Double> weightedAverage = new HashMap<>();


    /**
     * Reads in the data stored in the given master data Excel file and creates a hashmap stockClosingPrice containing
     * every stock mapped to an array list of its closing prices and creates an arraylist dates containing every date
     * in the 5-year period.
     *
     * Updates the stockClosingPrices hashmap, storing the closing prices for every stock and the dates arraylist,
     * storing every date from the Excel file
     *
     */
    public void dataCollection() throws IOException {
        String excelFilePath = "Data File.xlsx";
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
            ArrayList<Double> closingPrices = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                double value;
                // Try catch clause needed because N/A is read as a string and therefore getNumericCellValue causes an
                // error, N/A is stored as the int 99999 instead
                try {
                    value = sheet.getRow(r).getCell(c).getNumericCellValue();
                    closingPrices.add(value);
                } catch (Exception e2) {
                    value = 99999;
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
        Integer reportIndexSubmonths = helperDate(simulationLength, reportDate);
        // Loops through every date up until reportIndexSubMonths which is the index of the date
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

        int finalMonths = rowCount.get(reportIndexSubmonths - 1);
        for (int k = 0; k <= finalMonths; k++) {
            rowCount.add(finalMonths - k);
        }
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
     *
     * Updates the hashmaps stock75LTV and stock100LTV which contain the values of 75% and 100% LTV for each closing
     * price respectively .
     */
    public void LTVVal(Double LTV) {
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Double> result75 = calcLTV(stock, 0.75, LTV);
            ArrayList<Double> result100 = calcLTV(stock, 1.00, LTV);
            stock75LTV.put(stock, result75);
            stock100LTV.put(stock, result100);
        }
    }

    /**
     * A helper function that will loop through a given list of closing prices and calculate a given LTV percentage.
     *
     * @param LTV the specified LTV value for the simulation
     * @param stock the current stock in the iteration
     * @param percent the specified percentage level e.g. 75 or 100
     *
     * @return LTVvals an arraylist storing either 75% or 100% of the LTV value for each closing price of the given
     * stock.
     */
    public ArrayList<Double> calcLTV(String stock, double percent, double LTV) {
        ArrayList<Double> LTVvals = new ArrayList<>();
        for (Double closePrice : stockClosingPrices.get(stock)) {
//            if (closePrice == 99999 || closePrice == 0) {
            //Check to see if the price is equal to 99999 which represents null or no data
            if (closePrice == 99999) {
                LTVvals.add(99999.99);
            } else {
                // Add try catch error here + elsewhere in code
                double notRounded = (closePrice * LTV) / percent;
                double rounded = Math.round(notRounded * 100.0) / 100.0;
                LTVvals.add(rounded);
            }
        }
        return LTVvals;
    }


    /**
     * A function that will calculate and store the number of times the closing price reached 75 or 100% of its LTV
     * during the simulation length, for every stock.
     *
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
                    numDays.add(99999);
                } else {
                    numDays.add(dates.indexOf(first100Day.get(i)) - dates.indexOf(first75Day.get(i)));
                }
            }
            difference75to100.put(stock, numDays);
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
    public ArrayList<Integer> calcCount(String stock, ArrayList<Double> percentLTV, ArrayList<Date> firstDay) {
        ArrayList<Double> closePrices = stockClosingPrices.get(stock);
        ArrayList<Integer> countList = new ArrayList<>();
        for (int i = 0; i < closePrices.size(); i++) {
            int count = 0;
            firstDay.add(null);
            for (int k = i; k < i + rowCount.get(i); k++) {
//                if ((closePrices.get(k) != 99999 && closePrices.get(k) != 0) && (percentLTV.get(i) != 99999.99)) {
                // A check to avoid including days which do not have any data into the count
                if ((closePrices.get(k) != 99999) && (percentLTV.get(i) != 99999.99)) {
                    if (closePrices.get(k) <= percentLTV.get(i)) {
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
     *
     * Updates the hashmaps prob75 and prob100 which contain the probability that the stock will fall to 75% of its LTV
     * value during the simulation length.
     */
    public void probability() {
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Double> result75 = calcProb(count75.get(stock), stock);
            ArrayList<Double> result100 = calcProb(count100.get(stock), stock);
            prob75.put(stock, result75);
            prob100.put(stock, result100);
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
        for (int i = 0; i < count.size() - 1; i++) {
            // A check to deal with days that have no data
            if (stockClosingPrices.get(stock).get(i) == 99999){
                probList.add(99999.99);
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
    public ArrayList<String> volCheckOne(){
        ArrayList<String> failed = new ArrayList<>();
        // The calculation of the EWMA table, used to weight the averages properly
        ArrayList<Double> EWMA = new ArrayList<>();
        EWMACalc(EWMA);
        Collections.reverse(EWMA);

        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Double> result = calcAverages(stock);
            Double sum = 0.0;
            for (int i = 0; i < result.size(); i++){
                if (result.get(i) != null){
                    sum += EWMA.get(i) * result.get(i);
                }
            }
            Double sumEWMA = 0.0;
            for (int j = 0; j < result.size(); j++){
                if (result.get(j) != null){
                    sumEWMA += EWMA.get(j);
                }
            }
            if (sum / sumEWMA > 0.25){
                failed.add(stock);
            }
            weightedAverage.put(stock, sum/sumEWMA);
        }
//        System.out.println(weightedAverage);
        return failed;
    }

    private static void EWMACalc(ArrayList<Double> EWMA) { //make all numbers constants
        EWMA.add((0.06)/(1-(Math.pow(0.94, 20))));
        for (int i = 1; i < 20; i++){
            EWMA.add(EWMA.get(i-1)*0.94);
        }
    }


    /**
     * A function that will calculate and store the averaged probability values of a given stock. In order to do this,
     * the function must calculate 3-month intervals starting from the month 3-months prior to the reporting date. These
     * 20 periods will store the averaged probabilities.
     *
     * @return result an arraylist storing the averaged probabilities.
     */
    public ArrayList<Double> calcAverages(String stock){
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
        for (int j = 1; j < indexOfMonthPeriods.size(); j++) {
            int count = 0;
            int numDays = 0;
            int noDataCount = 0;
            for (int i = indexOfMonthPeriods.get(j - 1) + 1; i <= indexOfMonthPeriods.get(j); i++) {
                // A check to see if there is no data for the current day
                if ((prob75.get(stock).get(i) == 99999.99)) {
                    noDataCount++;
                }
                if ((prob75.get(stock).get(i) != 99999.99) && (prob75.get(stock).get(i) > 0)) {
                    count++;
                }
                numDays++;
            }
            // If noDataCount is equal to numDays, it means there is no data for the entire 3-month period and null is
            // stored instead of a value
            if (noDataCount == numDays) {
                result.add(null);
            } else {
//                System.out.println(count);
//                System.out.println(numDays);
                result.add((count / (double) numDays));
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
        // Will first calculate the index of the date 3 years prior to the reporting date
        Date reportDate = dates.get(dates.size() - 1);
        LocalDate reportLD = LocalDate.ofInstant(reportDate.toInstant(), ZoneId.systemDefault());
        LocalDate reportLDSubYears = reportLD.minusYears(3);
        Date reportDateSubYears = Date.from(reportLDSubYears.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int reportIndexSubYears = dates.indexOf(reportDateSubYears);
        while (reportIndexSubYears == -1) {
            reportLDSubYears = reportLDSubYears.minusDays(1);
            reportDateSubYears = Date.from(reportLDSubYears.atStartOfDay(ZoneId.systemDefault()).toInstant());
            reportIndexSubYears = dates.indexOf(reportDateSubYears);
        }
        for (String stock : stockClosingPrices.keySet()) {
            ArrayList<Integer> stockDifference = difference75to100.get(stock);
            int minDifference = stockDifference.get(reportIndexSubYears);
            for (int i = reportIndexSubYears + 1; i < dates.size(); i++) {
                if (stockDifference.get(i) < minDifference) {
                    minDifference = stockDifference.get(i);
                }
            }
            if (minDifference < 5) {
                failed.add(stock);
            }
        }
        return failed;
    }


    /**
     * A function that will check to see which stocks failed the volatility test.
     *
     * @return volatilityFailed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> volatilityFailed(){
        ArrayList<String> volatilityFailed = new ArrayList<>();
        for (String stock : stockClosingPrices.keySet()) {
            if (volCheckOne().contains(stock) || volCheckTwo().contains(stock)){
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
            }
        }
        return volatilityPassed;
    }
}