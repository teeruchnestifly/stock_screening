import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


public class DataClass {

    public static HashMap<String, Double> stockFSPortion = new HashMap<>();
    public static HashMap<String, Double> stockMaxLTV = new HashMap<>();
    public static Integer simulatedTenor = 9;
    static ArrayList<String> total = new ArrayList<>();
    static ArrayList<String> max60 = new ArrayList<>();
    static ArrayList<String> max50 = new ArrayList<>();
    static ArrayList<String> max40 = new ArrayList<>();
    static ArrayList<String> failedVol = new ArrayList<>();
    static ArrayList<String> failedLiq = new ArrayList<>();
    static ArrayList<String> failedFund = new ArrayList<>();
    static ArrayList<String> removedStocks = new ArrayList<>();
    static ArrayList<String> finalResult60 = new ArrayList<>();
    static ArrayList<String> finalResult50 = new ArrayList<>();
    static ArrayList<String> finalResult40 = new ArrayList<>();
    static HashMap<String, BigDecimal> maxCP = new HashMap<>();
    static HashMap<String, BigDecimal> maxCPRatio = new HashMap<>();
    static HashMap<String, String> profit = new HashMap<>();
    static HashMap<String, String> icr = new HashMap<>();
    static ArrayList<String> resultVol60 = new ArrayList<>();
    static ArrayList<String> resultVol50 = new ArrayList<>();
    static ArrayList<String> resultVol40 = new ArrayList<>();
    static ArrayList<String> resultVolFailed = new ArrayList<>();



    public static void main(String[] args) throws IOException {

        Volatility vol = new Volatility();
        Liquidity liq = new Liquidity();
        Fundamental fundamental = new Fundamental();
        vol.dataCollection();
        vol.numDaysForward(simulatedTenor);

//      Call for LTV60 Simulation
        vol.LTVVal(0.6);
        vol.count();
        vol.probability();
        ArrayList<String> SET60 = vol.volatilityPassed();
        max60 = SET60;

//      Call for LTV50 Simulation
        vol.LTVVal(0.5);
        vol.count();
        vol.probability();
        ArrayList<String> SET50 = new ArrayList<>();
        for (String stock : vol.volatilityPassed()) {
            if (!SET60.contains(stock)) {
                SET50.add(stock);
            }
        }
        max50 = SET50;

        //Call for LTV40 Simulation
        vol.LTVVal(0.4);
        vol.count();
        vol.probability();
        ArrayList<String> SET40 = new ArrayList<>();
        for (String stock : vol.volatilityPassed()) {
            if (!SET50.contains(stock) && !SET60.contains(stock)) {
                SET40.add(stock);
            }
        }
        max40 = SET40;
        failedVol = vol.volatilityFailed();
        vol.writeVolatilityResult();
        resultVol60 = vol.getResult60();
        resultVol50 = vol.getResult50();
        resultVol40 = vol.getResult40();
        resultVolFailed = vol.getResultFailed();


        // Addition of information for liquidity test
        for (String stock : SET60) {
            stockFSPortion.put(stock, 0.375);
            stockMaxLTV.put(stock, 0.6);
            total.add(stock);
        }
        for (String stock : SET50) {
            stockFSPortion.put(stock, 0.5);
            stockMaxLTV.put(stock, 0.5);
            total.add(stock);
        }
        for (String stock : SET40) {
            stockFSPortion.put(stock, 0.5833);
            stockMaxLTV.put(stock, 0.4);
            total.add(stock);
        }
        for (String stock : vol.volatilityFailed()) {
            stockFSPortion.put(stock, 0.5);
            stockMaxLTV.put(stock, 99.9);
            total.add(stock);
        }

        // Call for liquidity test
        liq.dataCollection();
        liq.calcAverages();
        liq.liqPassed(stockFSPortion, stockMaxLTV);
        ArrayList<String> liquidityFailed = liq.liqFailed(stockFSPortion, stockMaxLTV);
        failedLiq = liquidityFailed;

        //Removal of failed liquidity stocks
        ArrayList<String> MAX60Liq = SET60;
        for (String value : liquidityFailed) {
            MAX60Liq.remove(value);
        }
        ArrayList<String> MAX50Liq = SET50;
        for (String s : liquidityFailed) {
            MAX50Liq.remove(s);
        }
        ArrayList<String> MAX40Liq = SET40;
        for (String s : liquidityFailed) {
            MAX40Liq.remove(s);
        }
        liq.writeLiquidityResult();


        //Call for fundamental stocks
        fundamental.dataCollectionNetProfit();
        fundamental.dataCollectionICR();
        fundamental.fundamentalPassed();
        ArrayList<String> fundamentalFailed = fundamental.fundamentalFailed();
        failedFund = fundamentalFailed;
        fundamental.writeFundamentalResult();

        //Removal of failed fundamental stocks
        ArrayList<String> MAX60fund = MAX60Liq;
        for (String s : fundamentalFailed) {
            MAX60fund.remove(s);
        }
        ArrayList<String> MAX50fund = MAX50Liq;
        for (String s : fundamentalFailed) {
            MAX50fund.remove(s);
        }
        ArrayList<String> MAX40fund = MAX40Liq;
        for (String s : fundamentalFailed) {
            MAX40fund.remove(s);
        }

        //Call for exclusion list stocks
        PF_REIT_IFF pf = new PF_REIT_IFF();
        pf.DataCollection();

        //Removal of failed exclusion list stocks
        for (String stock : MAX60fund) {
            if ((!pf.IFF().contains(stock)) & !(pf.PF_REIT().contains(stock)) &
                    !(pf.lessOneYear().contains(stock)) & !(pf.EFT().contains(stock))) {
                finalResult60.add(stock);
            } else {
                removedStocks.add(stock);
            }
        }
        for (String stock : MAX50fund) {
            if ((!pf.IFF().contains(stock)) & !(pf.PF_REIT().contains(stock)) &
                    !(pf.lessOneYear().contains(stock)) & !(pf.EFT().contains(stock))) {
                finalResult50.add(stock);
            } else {
                removedStocks.add(stock);
            }
        }
        for (String stock : MAX40fund) {
            if ((!pf.IFF().contains(stock)) & !(pf.PF_REIT().contains(stock)) &
                    !(pf.lessOneYear().contains(stock)) & !(pf.EFT().contains(stock))) {
                finalResult40.add(stock);
            } else {
                removedStocks.add(stock);
            }
        }

        //Call for final summary
        maxCP = liq.returnMCP();
        maxCPRatio = liq.returnMCPratio();
        profit = fundamental.getStockProfitResults();
        icr = fundamental.getStockICRResults();
        DataClass dc = new DataClass();
        dc.writeFinalSummary();
    }


    /**
     * A function that will read in the summary template and write and output a new file.
     *
     * Outputs an updated file containing a summary of the data across all tests.
     */
    public void writeFinalSummary() throws IOException {
        PF_REIT_IFF pf = new PF_REIT_IFF();
        pf.DataCollection();
        Liquidity liq = new Liquidity();
        liq.dataCollection();
        String excelFilePath = "Result Template of Summary Stocklist.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.000%"));
        Sheet summary = workbook.getSheetAt(0);
        int rowCount = 2;
        for (String stock : total) {
            Row row = summary.createRow(rowCount);
            Cell numStock = row.createCell(0);
            numStock.setCellValue(rowCount - 1);
            Cell stockMarket = row.createCell(1);
            stockMarket.setCellValue(liq.getStockMarket().get(stock));
            Cell stockCell = row.createCell(2);
            stockCell.setCellValue(stock);
            Cell NTF = row.createCell(3);
            if (resultVol60.contains(stock)) {
                NTF.setCellValue("MAX60");
            } else if ((resultVol50.contains(stock))) {
                NTF.setCellValue("MAX50");
            } else if ((resultVol40.contains(stock))) {
                NTF.setCellValue("MAX40");
            } else {
                NTF.setCellValue("Fail");
            }
            Cell volResult = row.createCell(4);
            if (failedVol.contains(stock)) {
                volResult.setCellValue("Fail");
            } else {
                volResult.setCellValue("Pass");
            }
            Cell liqResult = row.createCell(5);
            if (failedLiq.contains(stock)) {
                liqResult.setCellValue("Fail");
            } else {
                liqResult.setCellValue("Pass");
            }
            Cell MCP = row.createCell(6);
            try {
                MCP.setCellValue(maxCP.get(stock).doubleValue());
            } catch (NullPointerException e1) {
                MCP.setCellValue("N/A");
            }
            Cell MCPratio = row.createCell(7);
            try {
                MCPratio.setCellValue(maxCPRatio.get(stock).doubleValue());
                MCPratio.setCellStyle(style);
            } catch (NullPointerException e1) {
                MCPratio.setCellValue("N/A");
            }
            Cell fundResult = row.createCell(8);
            if (failedFund.contains(stock)) {
                fundResult.setCellValue("Fail");
            } else {
                fundResult.setCellValue("Pass");
            }
            Cell netProfit = row.createCell(9);
            try {
                if (profit.get(stock) == null){
                    netProfit.setCellValue("N/A");
                } else {
                    netProfit.setCellValue(profit.get(stock));
                }
            } catch (NullPointerException e1) {
                netProfit.setCellValue("N/A");
            }
            Cell ICR = row.createCell(10);
            try {
                if (icr.get(stock) == null){
                    ICR.setCellValue("N/A");
                } else {
                    ICR.setCellValue(icr.get(stock));
                }
            } catch (NullPointerException e1) {
                ICR.setCellValue("N/A");
            }
            Cell exclusionList = row.createCell(11);
            if (pf.lessOneYear().contains(stock) || pf.PF_REIT().contains(stock) || pf.IFF().contains(stock) ||
                    pf.EFT().contains(stock)) {
                exclusionList.setCellValue("Fail");
            } else {
                exclusionList.setCellValue("Pass");
            }
            Cell lessOneYear = row.createCell(12);
            if (pf.lessOneYear().contains(stock)) {
                lessOneYear.setCellValue("YES");
            } else {
                lessOneYear.setCellValue("NO");
            }
            Cell PfReit = row.createCell(13);
            if (pf.PF_REIT().contains(stock)) {
                PfReit.setCellValue("YES");
            } else {
                PfReit.setCellValue("NO");
            }
            Cell IFF = row.createCell(14);
            if (pf.IFF().contains(stock)) {
                IFF.setCellValue("YES");
            } else {
                IFF.setCellValue("NO");
            }
            Cell ETF = row.createCell(15);
            if (pf.EFT().contains(stock)) {
                ETF.setCellValue("YES");
            } else {
                ETF.setCellValue("NO");
            }
            Cell allTestResult = row.createCell(16);
            if (finalResult60.contains(stock) || finalResult50.contains(stock) || finalResult40.contains(stock)) {
                allTestResult.setCellValue("Pass");
            } else {
                allTestResult.setCellValue("Fail");
            }
            rowCount++;
        }
        FileOutputStream outputStream = new FileOutputStream("Summary_Result_June_2023.xls");
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}