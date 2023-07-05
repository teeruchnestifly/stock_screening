import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Fundamental {

    private final HashMap<String, ArrayList<Double>> stockNetProfit = new HashMap<>();
    private final HashMap<String, ArrayList<Double>> stockICR = new HashMap<>();


    //data collection net profit
        //add to a hashmap stockNetProfit mapping list containing each years profit

    //start at most recent date, loop through and add
    public void dataCollectionNetProfit() throws IOException {
        String excelFilePath = "2022 Net Profit.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for (int r = 13; r < sheet.getLastRowNum(); r++) {
            String stock = sheet.getRow(r).getCell(0).getStringCellValue();
            ArrayList<Double> netProfit = new ArrayList<>();
            stockNetProfit.put(stock, netProfit);
            Double profit = sheet.getRow(r).getCell(4).getNumericCellValue();
            stockNetProfit.get(stock).add(profit);
        }
        dataCollectionHelper("2021 Net Profit.xlsx", stockNetProfit);
        dataCollectionHelper("2020 Net Profit.xlsx", stockNetProfit);
        dataCollectionHelper("2019 Net Profit.xlsx", stockNetProfit);
        dataCollectionHelper("2018 Net Profit.xlsx", stockNetProfit);
        for (String stock : stockNetProfit.keySet()) {
            if (stockNetProfit.get(stock).size() != 5) {
                for (int i = stockNetProfit.get(stock).size(); i < 5; i++) {
                    stockNetProfit.get(stock).add(null);
                }
            }
            Collections.reverse(stockNetProfit.get(stock));
        }
//        System.out.println(stockNetProfit);
    }

    public void dataCollectionICR() throws IOException {
        String excelFilePath = "2022 ICR.xlsx";
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
        dataCollectionHelper("2021 ICR.xlsx", stockICR);
        dataCollectionHelper("2020 ICR.xlsx", stockICR);
        dataCollectionHelper("2019 ICR.xlsx", stockICR);
        dataCollectionHelper("2018 ICR.xlsx", stockICR);
        for (String stock : stockICR.keySet()) {
            if (stockICR.get(stock).size() != 5) {
                for (int i = stockICR.get(stock).size(); i < 5; i++) {
                    stockICR.get(stock).add(null);
                }
            }
            Collections.reverse(stockICR.get(stock));
        }
//        System.out.println(stockICR);
    }


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
            }
        }
        return failed;
    }

    /**
     * A function that will check to see which stocks passed the volatility test.
     *
     * @return liquidityPassed an arraylist of the stocks that passed the test
     */
    public ArrayList<String> fundamentalPassed(){
        ArrayList<String> fundamentalPassed = new ArrayList<>();
        ArrayList<String> failed = fundamentalFailed();
        for (String stock : stockNetProfit.keySet()) {
            if (!failed.contains(stock)){
                fundamentalPassed.add(stock);
            }
        }
        return fundamentalPassed;
    }


    /**
     * A function that will check to see which stocks failed the liquidity test.
     *
     * @return liquidityFailed an arraylist of the stocks that failed the test
     */
    public ArrayList<String> fundamentalFailed(){
        ArrayList<String> fundamentalFailed = new ArrayList<>();
        for (String stock : stockNetProfit.keySet()) {
            if (fundamentalCheckOne().contains(stock) || fundamentalCheckTwo().contains(stock)){
                fundamentalFailed.add(stock);
            }
        }
        return fundamentalFailed;
    }
}
