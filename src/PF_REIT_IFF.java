import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PF_REIT_IFF {

    ArrayList<String> PF_REIT_List = new ArrayList<>();
    ArrayList<String> IFF_List = new ArrayList<>();
    ArrayList<String> lessOneYear = new ArrayList<>();
    ArrayList<String> ETF_List = new ArrayList<>();

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks classed as IFF.
     **/
    public void DataCollection() throws IOException {
        String excelFilePath = "List of stock < 1Y_PF&REIT_IFF-3.xlsx";
        FileInputStream inputStream = new FileInputStream(excelFilePath);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 3; i <= sheet.getLastRowNum(); i++) {
            String stock;
            try {
                stock = sheet.getRow(i).getCell(2).getStringCellValue();
            } catch (IllegalStateException e1) {
                stock = "TRUE";
            }
            String list = sheet.getRow(i).getCell(3).getStringCellValue();
            if (Objects.equals(list, "EFTs")){
                ETF_List.add(stock);
            } else if (Objects.equals(list, "IFF")){
                IFF_List.add(stock);
            } else if (Objects.equals(list, "PF&REIT")){
                PF_REIT_List.add(stock);
            } else if (Objects.equals(list, "< 1 year")){
                lessOneYear.add(stock);
            }
        }
    }

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks classed as PF or REIT.
     *
     * @return PF_REIT, a list of all stocks that are classed as PF or REIT
     */
    public ArrayList<String> PF_REIT(){
        return PF_REIT_List;
    }

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks classed as IFF.
     *
     * @return IFF_List, a list of all stocks that are classed as IFF
     */
    public ArrayList<String> IFF(){
        return IFF_List;
    }

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks classed as EFT
     *
     * @return ETF_List, a list of all stocks that are classed as EFT
     */
    public ArrayList<String> EFT(){
        return ETF_List;
    }

    /**
     * Reads in the data stored in the given Excel file and returns a list of all stocks that have been in the market
     * for less than one year.
     *
     * @return lessOneYear, a list of all stocks that have been in the market for less than one year.
     */
    public ArrayList<String> lessOneYear(){
        return lessOneYear;
    }
}
