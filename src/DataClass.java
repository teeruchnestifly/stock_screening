import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class DataClass {

    public static HashMap<String, Double> stockFSPortion = new HashMap<>();
    public static Integer simulatedTenor = 9;

    public static void main(String[] args) throws IOException {

        Volatility vol = new Volatility();
        Liquidity liq = new Liquidity();
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

        PF_REIT_IFF pf = new PF_REIT_IFF();



        ArrayList<String> MAX60 = new ArrayList<>();
        ArrayList<String> MAX50 = new ArrayList<>();
        ArrayList<String> MAX40 = new ArrayList<>();
        ArrayList<String> removedStocks = new ArrayList<>();

        for (String stock : SET60) {
            if ((!pf.lessOneYearCollection().contains(stock)) & !(pf.PF_REITCollection().contains(stock)) &
                    !(pf.IFFCollection().contains(stock))) {
                MAX60.add(stock);
            stockFSPortion.put(stock, 0.375);
            } else {
                removedStocks.add(stock);
            }
        }
        for (String stock : SET50) {
            if ((!pf.lessOneYearCollection().contains(stock)) & !(pf.PF_REITCollection().contains(stock)) &
                    !(pf.IFFCollection().contains(stock))) {
                MAX50.add(stock);
            stockFSPortion.put(stock, 0.5);
            } else {
                removedStocks.add(stock);
            }
        }
        for (String stock : SET40) {
            if ((!pf.lessOneYearCollection().contains(stock)) & !(pf.PF_REITCollection().contains(stock)) &
                    !(pf.IFFCollection().contains(stock))) {
                MAX40.add(stock);
            stockFSPortion.put(stock, 0.5833);
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


        System.out.println();
        System.out.println("Result of Liquidity test");
        System.out.println();
        liq.dataCollection();
        liq.calcAverages();
        System.out.println("Stocks that passed liquidity test: " + liq.liqPassed(stockFSPortion));
        System.out.println("Stocks that failed liquidity test: " + liq.liqFailed(stockFSPortion));

    }
}