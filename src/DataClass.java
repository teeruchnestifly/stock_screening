import java.io.IOException;
import java.util.ArrayList;

public class DataClass {

    public static void main(String[] args) throws IOException {
        Volatility vol = new Volatility();
        vol.dataCollection();
        vol.numDaysForward(9);

        //Call for LTV60 Simulation
        vol.LTVVal(0.6);
        vol.count();
        vol.probability();
        ArrayList<String> SET60 = vol.volatilityPassed();
        System.out.println("Stocks classed as SET60: " + SET60);
        System.out.println(SET60.size());

        //Call for LTV50 Simulation
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
        ArrayList<String> SET40 = new ArrayList<>();
        for (String stock : vol.volatilityPassed()) {
            if (!SET50.contains(stock) && !SET60.contains(stock)) {
                SET40.add(stock);
            }
        }
        System.out.println("Stocks classed as SET40: " + SET40);
        System.out.println(SET40.size());

        // Displays failed stocks
        System.out.println("Stocks that failed all tests: " + vol.volatilityFailed());
        System.out.println(vol.volatilityFailed().size());
    }
}