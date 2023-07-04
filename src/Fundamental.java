import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class Fundamental {
    //data collection net profit
        //add to a hashmap stockNetProfit mapping list containing each years profit

    //data collection ICR level
        //add to a hashmap stockICR mapping list containing each years ICR


    /**
     * A function that will perform the first liquidity check by calculating the max collateral pledged for each stock
     * and comparing that value to 10 million baht. If it is less than 10 million, the stock fails.
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
     * A function that will perform the first liquidity check by calculating the max collateral pledged for each stock
     * and comparing that value to 10 million baht. If it is less than 10 million, the stock fails.
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
