import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Fundamental {

    /**
     * A hashmap mapping all stocks to an arraylist storing their annual net profit.
     */
    private final HashMap<String, ArrayList<Double>> stockNetProfit = new HashMap<>();
    /**
     * A hashmap mapping all stocks to an arraylist storing their annual ICR.
     */
    private final HashMap<String, ArrayList<Double>> stockICR = new HashMap<>();
    private final HashMap<String, String> stockProfitResults = new HashMap<>();
    private final HashMap<String, String> stockICRResults = new HashMap<>();
    private final HashMap<String, String> stockFinalResults = new HashMap<>();
    private final HashMap<String, String> stockMarket = new HashMap<>();


    /**
     * Reads in the data stored in the most recent Excel file and creates a hashmap stockNetProfit containing every
     * stock mapped to an array list of its profit levels.
     * Updates the stockNetProfit hashmap, storing the profit for every stock.
     */
    public void dataCollectionNetProfit() throws IOException {
        String excelFilePath = "2022 Net Profit.xlsx"; /** CHANGE TO NAME OF MOST RECENT PROFIT DATA FILE FOR CURRENT TEST **/
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for (int r = 13; r < sheet.getLastRowNum(); r++) {
            String stock = sheet.getRow(r).getCell(0).getStringCellValue();
            String market = sheet.getRow(r).getCell(1).getStringCellValue();
            stockMarket.put(stock, market);
            ArrayList<Double> netProfit = new ArrayList<>();
            stockNetProfit.put(stock, netProfit);
            Double profit = sheet.getRow(r).getCell(4).getNumericCellValue();
            stockNetProfit.get(stock).add(profit);
        }
        dataCollectionHelper("2021 Net Profit.xlsx", stockNetProfit); /** CHANGE TO NAME OF PROFIT DATA FILE FOR CURRENT TEST IN DESCENDING ORDER**/
        dataCollectionHelper("2020 Net Profit.xlsx", stockNetProfit); /** CHANGE NAME **/
        dataCollectionHelper("2019 Net Profit.xlsx", stockNetProfit); /** CHANGE NAME **/
        dataCollectionHelper("2018 Net Profit.xlsx", stockNetProfit); /** CHANGE NAME **/
        for (String stock : stockNetProfit.keySet()) {
            if (stockNetProfit.get(stock).size() != 5) {
                for (int i = stockNetProfit.get(stock).size(); i < 5; i++) {
                    stockNetProfit.get(stock).add(null);
                }
            }
            Collections.reverse(stockNetProfit.get(stock));
        }
    }


    /**
     * Reads in the data stored in the most recent Excel file and creates a hashmap stockICR containing every stock
     * mapped to an array list of its ICR levels.
     * Updates the stockICR hashmap, storing the ICR for every stock.
     */
    public void dataCollectionICR() throws IOException {
        String excelFilePath = "2022 ICR.xlsx"; /** CHANGE TO NAME OF MOST RECENT ICR DATA FILE FOR CURRENT TEST **/
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for (int r = 13; r < sheet.getLastRowNum(); r++) {
            String stock = sheet.getRow(r).getCell(0).getStringCellValue();
            ArrayList<Double> ICR = new ArrayList<>();
            stockICR.put(stock, ICR);
            Double icr = sheet.getRow(r).getCell(4).getNumericCellValue();
            stockICR.get(stock).add(icr);
        }
        dataCollectionHelper("2021 ICR.xlsx", stockICR); /** CHANGE TO NAME OF ICR DATA FILE FOR CURRENT TEST IN DESCENDING ORDER**/
        dataCollectionHelper("2020 ICR.xlsx", stockICR); /** CHANGE NAME **/
        dataCollectionHelper("2019 ICR.xlsx", stockICR); /** CHANGE NAME **/
        dataCollectionHelper("2018 ICR.xlsx", stockICR); /** CHANGE NAME **/
        for (String stock : stockICR.keySet()) {
            if (stockICR.get(stock).size() != 5) {
                for (int i = stockICR.get(stock).size(); i < 5; i++) {
                    stockICR.get(stock).add(null);
                }
            }
            Collections.reverse(stockICR.get(stock));
        }
    }


    /**
     * A helper function which is used to read through and store data from the eight other files with data for net profit
     * and ICR.
     *
     * @param excelFilePath the name of the file to be read
     * @param stockData the hashmap being added to (either stockNetProfit or stockICR)
     *
     */
    public void dataCollectionHelper(String excelFilePath, HashMap<String, ArrayList<Double>> stockData)
            throws IOException {
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for (int r = 13; r < sheet.getLastRowNum(); r++) {
            String stock = sheet.getRow(r).getCell(0).getStringCellValue();
            Double profit = sheet.getRow(r).getCell(4).getNumericCellValue();
            try {
                stockData.get(stock).add(profit);
            } catch (NullPointerException e1) {
                ArrayList<Double> netProfit = new ArrayList<>();
                stockData.put(stock, netProfit);
                stockData.get(stock).add(profit);
                String market = sheet.getRow(r).getCell(1).getStringCellValue();
                stockMarket.put(stock, market);
            }
        }
    }


    /**
     * A function that will perform the first fundamental check by looping through the net profit levels for each stock
     * over the course of 5 years. If there are 4 consecutive years of losses, the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> fundamentalCheckOne() {
        ArrayList<String> failed = new ArrayList<>();
        for (String stock : stockNetProfit.keySet()) {
            int counter = 0;
            for (int i = 0; i < stockNetProfit.get(stock).size(); i++){
                if(stockNetProfit.get(stock).get(i) == null || stockNetProfit.get(stock).get(i) >= 0){
                    counter = 0;
                } else {
                    counter += 1;
                }
            }
            if(counter >= 4){
                failed.add(stock);
                stockProfitResults.put(stock, "Fail");
            } else {
                stockProfitResults.put(stock, "Pass");
            }
        }
        return failed;
    }


    /**
     * A function that will perform the second fundamental check by looping through the ICR levels for each stock
     * over the course of 5 years. If there are 4 consecutive years when ICR is less than 1, the stock fails.
     *
     * @return failed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> fundamentalCheckTwo() {
        ArrayList<String> failed = new ArrayList<>();
        for (String stock : stockICR.keySet()) {
            int counter = 0;
            for (int i = 0; i < stockICR.get(stock).size(); i++){
                if(stockICR.get(stock).get(i) == null || stockICR.get(stock).get(i) >= 1){
                    counter = 0;
                } else {
                    counter += 1;
                }
            }
            if(counter >= 4){
                failed.add(stock);
                stockICRResults.put(stock, "Fail");
            } else {
                stockICRResults.put(stock, "Pass");
            }
        }
        return failed;
    }


    /**
     * A function that will check to see which stocks passed the fundamental test.
     *
     * @return fundamentalPassed an arraylist of the stocks that passed the test.
     */
    public ArrayList<String> fundamentalPassed(){
        ArrayList<String> fundamentalPassed = new ArrayList<>();
        ArrayList<String> failed = fundamentalFailed();
        for (String stock : stockNetProfit.keySet()) {
            if (!failed.contains(stock)){
                fundamentalPassed.add(stock);
                stockFinalResults.put(stock, "Pass");
            }
        }
        return fundamentalPassed;
    }


    /**
     * A function that will check to see which stocks failed the fundamental test.
     *
     * @return fundamentalFailed an arraylist of the stocks that failed the test.
     */
    public ArrayList<String> fundamentalFailed(){
        ArrayList<String> fundamentalFailed = new ArrayList<>();
        for (String stock : stockNetProfit.keySet()) {
            if (fundamentalCheckOne().contains(stock) || fundamentalCheckTwo().contains(stock)){
                fundamentalFailed.add(stock);
                stockFinalResults.put(stock, "Fail");
            }
        }
        return fundamentalFailed;
    }

    /**
     * A function that returns the stockProfitResults hashmap to be used in the writing of the summary file.
     */
    public HashMap<String, String> getStockProfitResults(){
        return stockProfitResults;
    }

    /**
     * A function that returns the stockICRResult hashmap to be used in the writing of the summary file.
     */
    public HashMap<String, String> getStockICRResults(){
        return stockICRResults;
    }

    /**
     * A function that will read in the fundamental result template and write and output a new file.
     *
     * Outputs an updated file containing the data used in the fundamental test.
     */
    public void writeFundamentalResult() throws IOException {
        String excelFilePath = "Result Template of Fundamental Test.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet profit = workbook.getSheetAt(1);
        helperResult(profit, stockNetProfit, stockProfitResults);
        Sheet ICR = workbook.getSheetAt(2);
        helperResult(ICR, stockICR, stockICRResults);
        Sheet Summary = workbook.getSheetAt(0);
        helperSummary(Summary);
        inputStream.close();
        FileOutputStream outputStream = new FileOutputStream("Fundamental_Test_Result_June2023.xls");
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    /**
     * A helper function that updates the summary sheet of the fundamental result file.
     */
    public void helperSummary(Sheet sheet){
        int rowCount = 2;
        for (String stock : stockMarket.keySet()) {
            Row row = sheet.createRow(rowCount);
            Cell numStock = row.createCell(0);
            numStock.setCellValue(rowCount - 1);
            Cell stockCell = row.createCell(1);
            stockCell.setCellValue(stock);
            Cell marketCell = row.createCell(2);
            marketCell.setCellValue(stockMarket.get(stock));
            Cell profitTest = row.createCell(3);
            try{
                profitTest.setCellValue(stockProfitResults.get(stock));
            } catch (NullPointerException e2) {
                profitTest.setCellValue("XXX");
            }
            Cell ICRTest = row.createCell(4);
            try {
                if (stockICRResults.get(stock) == null){
                    ICRTest.setCellValue("XXX");
                } else {
                    ICRTest.setCellValue(stockICRResults.get(stock));
                }
            } catch (NullPointerException e3){
                ICRTest.setCellValue("XXX");
            }
            Cell overallResult = row.createCell(5);
            overallResult.setCellValue(stockFinalResults.get(stock));
            rowCount ++;
        }
    }


    /**
     * A helper function that updates the result sheet of the fundamental result file.
     */
    public void helperResult(Sheet sheet, HashMap<String, ArrayList<Double>> resultList,
                             HashMap<String, String> finalResultList){
        int rowCount = 1;
        for (String stock : resultList.keySet()) {
            Row row = sheet.createRow(rowCount);
            Cell numStock = row.createCell(0);
            numStock.setCellValue(rowCount);
            Cell stockCell = row.createCell(1);
            stockCell.setCellValue(stock);
            Cell marketCell = row.createCell(2);
            marketCell.setCellValue(stockMarket.get(stock));
            Cell yOne = row.createCell(3);
            try {
                if (resultList.get(stock).get(0) == null){
                    yOne.setCellValue("XXX");
                } else {
                    yOne.setCellValue(resultList.get(stock).get(0));
                }
            } catch (NullPointerException e1) {
                yOne.setCellValue("XXX");
            }
            Cell yTwo = row.createCell(4);
            try {
                if (resultList.get(stock).get(1) == null) {
                    yTwo.setCellValue("XXX");
                } else {
                    yTwo.setCellValue(resultList.get(stock).get(1));
                }
            } catch (NullPointerException e1) {
            yOne.setCellValue("XXX");
            }
            Cell yThree = row.createCell(5);
            try {
                if (resultList.get(stock).get(2) == null) {
                    yThree.setCellValue("XXX");
                } else {
                    yThree.setCellValue(resultList.get(stock).get(2));
                }
            } catch (NullPointerException e1) {
                yOne.setCellValue("XXX");
            }
            Cell yFour = row.createCell(6);
            try {
                if (resultList.get(stock).get(3) == null) {
                    yFour.setCellValue("XXX");
                } else {
                    yFour.setCellValue(resultList.get(stock).get(3));
                }
            } catch (NullPointerException e1) {
                yOne.setCellValue("XXX");
            }
            Cell yFive = row.createCell(7);
            try {
                if (resultList.get(stock).get(4) == null) {
                    yFive.setCellValue("XXX");
                } else {
                    yFive.setCellValue(resultList.get(stock).get(4));
                }
            } catch (NullPointerException e1) {
                yOne.setCellValue("XXX");
            }
            Cell resultCell = row.createCell(8);
            resultCell.setCellValue(finalResultList.get(stock));
            rowCount += 1;
        }
    }
}
