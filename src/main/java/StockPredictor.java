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

    static int[] LAYERS = new int[] {50,25,1};
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
        predictStock("^FTSE", "FTSE100");


    }

    public static void predictStock(String yahooSymbol, String dbSymbol){
        START_DATE.add(START_DATE.YEAR, -1);
        ArrayList<HistoricalQuote> history = new ArrayList<HistoricalQuote>();
        try {
            Stock stk = YahooFinance.get(yahooSymbol, START_DATE, Interval.DAILY);
            history = new ArrayList(stk.getHistory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultiLayerPerceptron nn = new MultiLayerPerceptron(TransferFunctionType.LINEAR, LAYERS);

        System.out.println("FETCHING DATA");

        int daysConsidered = LAYERS[0]-8;
        ArrayList<Double> openingQuotes = new ArrayList<Double>();
        ArrayList<Double> closingQuotes = new ArrayList<Double>();
        ArrayList<Date> dates = new ArrayList<Date>();

        for (HistoricalQuote q : history){
            openingQuotes.add(q.getOpen().doubleValue()/10000);
            closingQuotes.add(q.getClose().doubleValue()/10000);
            dates.add(q.getDate().getTime());
        }

        DataSet data = new DataSet(LAYERS[0], 1);

        for (int i = 0; i+daysConsidered+1 < closingQuotes.size(); i++){
            ArrayList<Double> inputs = new ArrayList<Double>(closingQuotes.subList(i, i+daysConsidered));
            ArrayList<Double> target = new ArrayList<Double>(closingQuotes.subList(i+daysConsidered+1, i+daysConsidered+2));
            inputs.addAll(getSemanticInputs(dates.get(i), dbSymbol));
            data.addRow(new DataSetRow(inputs, target));
        }

        //TESTING

        for(int t = 0; t <10; t++){
            trainAndTest(nn, data);
            nn = new MultiLayerPerceptron(TransferFunctionType.LINEAR, LAYERS);
        }

        postPredictions();
    }

    public static void trainAndTest(NeuralNetwork net, DataSet data){

        DataSet[] sets = data.createTrainingAndTestSubsets(80, 20);

        prepAndTrain(net, sets[0]);

        System.out.println("TESTING...");

        Double totalError = 0d;
        int count = 0;

        for (DataSetRow row : sets[1]){
            count++;
            net.setInput(row.getInput());
            net.calculate();
            //System.out.println((net.getOutput()[0]*10000)+" - "+(row.getDesiredOutput()[0]*10000)+" = "+(net.getOutput()[0]-row.getDesiredOutput()[0])*10000);
            totalError += Math.abs(net.getOutput()[0]-row.getDesiredOutput()[0])*10000;
        }
        System.out.println("Average Error: "+(totalError/count));

    }

    public static void prepAndTrain(NeuralNetwork net, DataSet data){

        SupervisedLearning lr = (SupervisedLearning) net.getLearningRule();
        lr.setLearningRate(0.0001);
        lr.setMaxIterations(10000);
        net.setLearningRule(lr);
        System.out.println("TRAINING...");
        net.learn(data);

        System.out.println("DONE");
    }

    public static ArrayList<Double> getSemanticInputs(Date date, String symbol){

        Double[] twitterHistogram ={0d,0d,0d,0d};
        Double[] articleHistogram ={0d,0d,0d,0d} ;

        for (SentimentAnalysisEntry ro : semanticInputs){
            if (ro.date.equals(date) && ro.stock.equals(symbol)){
                twitterHistogram[(int) ro.twitterMood] += 1;
                articleHistogram[(int) ro.articleMood] += 1;
            }
        }

        ArrayList<Double> concat = new ArrayList<Double>(Arrays.asList(twitterHistogram));
        concat.addAll(new ArrayList<Double>(Arrays.asList(articleHistogram)));

        return concat;

    }

    public static void postPredictions(){

        //Post Predictions to db

    }

}