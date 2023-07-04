import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PF_REIT_IFF {

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks classed as IFF.
     *
     * @return IFF_List, a list of all stocks that are classed as IFF
     */
    public ArrayList<String> IFFCollection() throws IOException {
        ArrayList<String> IFF_List = new ArrayList<>();
        String excelFilePath = "PF_REIT_IFF.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(2);
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            String stock = sheet.getRow(i).getCell(1).getStringCellValue();
            IFF_List.add(stock);
        }
        return IFF_List;
    }

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks classed as PF or REIT.
     *
     * @return PF_REIT, a list of all stocks that are classed as PF or REIT
     */
    public ArrayList<String> PF_REITCollection() throws IOException {
        ArrayList<String> PF_REIT_List = new ArrayList<>();
        String excelFilePath = "PF_REIT_IFF.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(1);
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            String stock = sheet.getRow(i).getCell(1).getStringCellValue();
            PF_REIT_List.add(stock);
        }
        return PF_REIT_List;
    }

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks that have been in the market
     * for less than one year.
     *
     * @return lessOneYear, a list of all stocks that have been in the market for less than one year.
     */
    public ArrayList<String> lessOneYearCollection() throws IOException {
        ArrayList<String> lessOneYear = new ArrayList<>();
        String excelFilePath = "PF_REIT_IFF.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            try {
                String stock = sheet.getRow(i).getCell(1).getStringCellValue();
                lessOneYear.add(stock);
            } catch (IllegalStateException e1) {
                String stock = "TRUE";
                lessOneYear.add(stock);
            }
        }
        return lessOneYear;
    }
}
