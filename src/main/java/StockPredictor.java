/**
 * Created by Liam on 29/04/2017.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.learning.SupervisedLearning;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

public class StockPredictor {

    static int[] LAYERS = new int[] {28,15,5,1};
    static Calendar START_DATE = Calendar.getInstance();
    static ArrayList<SentimentAnalysisEntry> semanticInputs;
    static DBInterface DB;

    public static void  main(String[] args){

        try {
            DB = new DBInterface();
            semanticInputs = new ArrayList(DB.readSentimentTable());
        }catch (java.sql.SQLException e){
            e.printStackTrace();
            DB = null;
        }

        predictStock("AAPL", "AAPL");
        predictStock("GOOGL", "GOOGL");
        predictStock("^FTSE", "FTSE100");

    }

    public static void predictStock(String yahooSymbol, String dbSymbol) {

        Date today = START_DATE.getTime();
        String tooday = 1900 + today.getYear() + "-" + today.getMonth() + "-" + today.getDate() + 1;
        START_DATE.add(START_DATE.YEAR, -1);
        START_DATE.add(START_DATE.DATE, -1);
        ArrayList<HistoricalQuote> history = new ArrayList<HistoricalQuote>();
        try {
            Stock stk = YahooFinance.get(yahooSymbol, START_DATE, Interval.DAILY);
            history = new ArrayList(stk.getHistory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultiLayerPerceptron nn = new MultiLayerPerceptron(TransferFunctionType.LINEAR, LAYERS);

        System.out.println("FETCHING DATA");

        ArrayList<Double> openingQuotes = new ArrayList<Double>();
        ArrayList<Double> closingQuotes = new ArrayList<Double>();
        ArrayList<Date> dates = new ArrayList<Date>();

        for (HistoricalQuote q : history) {
            openingQuotes.add(q.getOpen().doubleValue() / 10000);
            //closingQuotes.add(q.getOpen().doubleValue()/10000);
            closingQuotes.add(q.getClose().doubleValue() / 10000);
            dates.add(q.getDate().getTime());
        }

        DataSet data = generateDataSet(closingQuotes, dates, dbSymbol);

        multipleTests(10, data, nn);

        Double prediction = 0d;

       // postPredictions(dbSymbol, prediction, tooday);

    }

    public static void multipleTests(int reps, DataSet data, MultiLayerPerceptron nn){

        System.out.println("TESTING...");

        Double avg = 0d;
        for (int t = 0; t < reps; t++) {
            double res = trainAndTest(nn, data);
            if (!Double.isNaN(res))
                avg += res;
            else
                t--;
            nn = new MultiLayerPerceptron(TransferFunctionType.LINEAR, LAYERS);
        }
        System.out.println("Acc: "+avg/10);
    }

    public static DataSet generateDataSet(ArrayList<Double> quotes, ArrayList<Date> dates, String dbSymbol){

        int daysConsidered = LAYERS[0] - 10;
        DataSet data = new DataSet(LAYERS[0], 1);

//        Double lastPrice = closingQuotes.remove(0);
//        Double temp = 0.0;
//        for (int d = 0; d < closingQuotes.size(); d++) {
//            temp = lastPrice;
//            lastPrice = closingQuotes.get(d);
//            closingQuotes.set(d, lastPrice-temp);
//        }
        for (int i = 0; i + daysConsidered + 1 < quotes.size(); i++) {
            ArrayList<Double> inputs = new ArrayList<Double>(quotes.subList(i, i + daysConsidered));
            ArrayList<Double> target = new ArrayList<Double>(quotes.subList(i + daysConsidered + 1, i + daysConsidered + 2));
            inputs.addAll(getSemanticInputs(dates.get(i), dbSymbol));
            data.addRow(new DataSetRow(inputs, target));
        }

        return data;
    }

    public static double trainAndTest(NeuralNetwork net, DataSet data){

        DataSet[] sets = data.createTrainingAndTestSubsets(80, 20);

        prepAndTrain(net, sets[0]);

        //System.out.println("TESTING...");

        Double totalError = 0d;
        int count = 0;

        for (DataSetRow row : sets[1]){
            count++;
            net.setInput(row.getInput());
            net.calculate();
            //System.out.println((net.getOutput()[0]*10000)+" - "+(row.getDesiredOutput()[0]*10000)+" = "+(net.getOutput()[0]-row.getDesiredOutput()[0])*10000);
            totalError += Math.abs(net.getOutput()[0]-row.getDesiredOutput()[0])*10000;
        }
        //System.out.println("Average Error: "+(totalError/count));
        return totalError/count;

    }

    public static void prepAndTrain(NeuralNetwork net, DataSet data){

        SupervisedLearning lr = (SupervisedLearning) net.getLearningRule();
        lr.setLearningRate(0.000001);
        lr.setMaxIterations(10000);
        net.setLearningRule(lr);
        //System.out.println("TRAINING...");
        net.learn(data);

        //System.out.println("DONE");

    }

    public static ArrayList<Double> getSemanticInputs(Date date, String symbol){

        Double[] twitterHistogram ={0d,0d,0d,0d,0d};
        Double[] articleHistogram ={0d,0d,0d,0d,0d} ;

        for (SentimentAnalysisEntry ro : semanticInputs){
            if (ro.date.equals(date) && ro.stock.equals(symbol)){
                if (ro.twitterMood != -1)
                    twitterHistogram[(int) ro.twitterMood] += 1;
                if (ro.articleMood != -1)
                    articleHistogram[(int) ro.articleMood] += 1;
            }
        }

        ArrayList<Double> concat = new ArrayList<Double>(Arrays.asList(twitterHistogram));
        concat.addAll(new ArrayList<Double>(Arrays.asList(articleHistogram)));
//        System.out.println(date);
//        for (Double dub : twitterHistogram)
//            System.out.print(dub+", ");
//        for (Double dub2 : articleHistogram)
//            System.out.print(dub2+", ");
//        System.out.println();
//        System.out.println("---------------------");
        return concat;

    }

    public static void postPredictions(String dbSymbol, double prediction, String date){

        float converted = (float) prediction;
        //DB.addPredictionEntry(dbSymbol, converted, date);

    }

}