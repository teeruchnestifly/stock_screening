import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class DataClass {

    public static HashMap<String, Double> stockFSPortion = new HashMap<>();
    public static Integer simulatedTenor = 9;

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
        System.out.println("Volatility Test Results");
        System.out.println();

        System.out.println("Stocks classed as SET60: " + SET60);
        System.out.println(SET60.size());


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
        System.out.println("Stocks classed as SET50: " + SET50);
        System.out.println(SET50.size());

        //Call for LTV40 Simulation
        vol.LTVVal(0.4);
        vol.count();
        vol.probability();
        vol.volatilityPassed();
        ArrayList<String> SET40 = new ArrayList<>();
        for (String stock : vol.volatilityPassed()) {
            if (!SET50.contains(stock) && !SET60.contains(stock)) {
                SET40.add(stock);
            }
        }
        System.out.println("Stocks classed as SET40: " + SET40);
        System.out.println(SET40.size());

//      Displays failed stocks
        System.out.println("Stocks that failed all tests: " + vol.volatilityFailed());
        System.out.println(vol.volatilityFailed().size());

        for (String stock : SET60) {
            stockFSPortion.put(stock, 0.375);
        }
        for (String stock : SET50) {
            stockFSPortion.put(stock, 0.5);
        }
        for (String stock : SET40) {
            stockFSPortion.put(stock, 0.5833);
        }


        System.out.println();
        System.out.println("Result of Liquidity test");
        System.out.println();
        liq.dataCollection();
        liq.calcAverages();
        ArrayList<String> liquidityPassed = liq.liqPassed(stockFSPortion);
        ArrayList<String> liquidityFailed = liq.liqFailed(stockFSPortion);

        System.out.println("Stocks that passed liquidity test: " + liquidityPassed);
        System.out.println("Stocks that failed liquidity test: " + liquidityFailed);

        ArrayList<String> MAX60Liq = SET60;
        for (int i = 0; i < liquidityFailed.size(); i++){
            MAX60Liq.remove(liquidityFailed.get(i));
        }
        ArrayList<String> MAX50Liq = SET50;
        for (int i = 0; i < liquidityFailed.size(); i++){
            MAX50Liq.remove(liquidityFailed.get(i));
        }
        ArrayList<String> MAX40Liq = SET40;
        for (int i = 0; i < liquidityFailed.size(); i++){
            MAX40Liq.remove(liquidityFailed.get(i));
        }
        System.out.println(MAX60Liq);
        System.out.println(MAX60Liq.size());
        System.out.println(MAX50Liq);
        System.out.println(MAX50Liq.size());
        System.out.println(MAX40Liq);
        System.out.println(MAX40Liq.size());


        fundamental.dataCollectionNetProfit();
        fundamental.dataCollectionICR();
        ArrayList<String> fundamentalPassed = fundamental.fundamentalPassed();
        ArrayList<String> fundamentalFailed = fundamental.fundamentalFailed();

        System.out.println("Stocks that passed fundamental test: " + fundamentalPassed);
        System.out.println("Stocks that failed fundamental test: " + fundamentalFailed);

        ArrayList<String> MAX60fund = MAX60Liq;
        for (int i = 0; i < fundamentalFailed.size(); i++){
            MAX60fund.remove(fundamentalFailed.get(i));
        }
        ArrayList<String> MAX50fund = MAX50Liq;
        for (int i = 0; i < fundamentalFailed.size(); i++){
            MAX50fund.remove(fundamentalFailed.get(i));
        }
        ArrayList<String> MAX40fund = MAX40Liq;
        for (int i = 0; i < fundamentalFailed.size(); i++){
            MAX40fund.remove(fundamentalFailed.get(i));
        }

        System.out.println(MAX60fund);
        System.out.println(MAX60fund.size());
        System.out.println(MAX50fund);
        System.out.println(MAX50fund.size());
        System.out.println(MAX40fund);
        System.out.println(MAX40fund.size());

        PF_REIT_IFF pf = new PF_REIT_IFF();
        ArrayList<String> MAX60 = new ArrayList<>();
        ArrayList<String> MAX50 = new ArrayList<>();
        ArrayList<String> MAX40 = new ArrayList<>();
        ArrayList<String> removedStocks = new ArrayList<>();

        for (String stock : MAX60fund) {
            if ((!pf.lessOneYearCollection().contains(stock)) & !(pf.PF_REITCollection().contains(stock)) &
                    !(pf.IFFCollection().contains(stock))) {
                MAX60.add(stock);
            } else {
                removedStocks.add(stock);
            }
        }
        for (String stock : MAX50fund) {
            if ((!pf.lessOneYearCollection().contains(stock)) & !(pf.PF_REITCollection().contains(stock)) &
                    !(pf.IFFCollection().contains(stock))) {
                MAX50.add(stock);
            } else {
                removedStocks.add(stock);
            }
        }
        for (String stock : MAX40fund) {
            if ((!pf.lessOneYearCollection().contains(stock)) & !(pf.PF_REITCollection().contains(stock)) &
                    !(pf.IFFCollection().contains(stock))) {
                MAX40.add(stock);
            } else {
                removedStocks.add(stock);
            }
        }
        System.out.println();
        System.out.println("Removal of stocks which are PF&REIT / IFF / <1y");
        System.out.println();

        System.out.println("Stocks classed as MAX60: " + MAX60);
        System.out.println(MAX60.size());
        System.out.println("Stocks classed as MAX50: " + MAX50);
        System.out.println(MAX50.size());
        System.out.println("Stocks classed as MAX40: " + MAX40);
        System.out.println(MAX40.size());
        System.out.println("Stocks removed: " + removedStocks);
        System.out.println(removedStocks.size());

    }
}