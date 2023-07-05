import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    }

    public void dataCollectionHelper(String excelFilePath, String stock) throws IOException {
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(2);
    }
    //data collection ICR level
        //add to a hashmap stockICR mapping list containing each years ICR


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
                if(stockNetProfit.get(stock).get(i) < 0){
                    counter+=1;
                } else {
                    counter = 0;
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
                if(stockICR.get(stock).get(i) < 1){
                    counter+=1;
                } else {
                    counter = 0;
                }
            }
            if(counter >= 4){
                failed.add(stock);
            }
        }
        return failed;
    }
}
