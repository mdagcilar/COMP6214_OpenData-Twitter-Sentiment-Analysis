/**
 * Created by Liam on 29/04/2017.
 */
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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

//        DataSet test = new DataSet(3,1);
//        for (int t = 0; t < 10; t++) {
//            double[] array = new double[3];
//            array[0] = t;
//            array[1] = t;
//            array[2] = t;
//            double[] target = new double[1];
//            target[0] = t*t;
//            test.add(new DataSetRow(array, target));
//            System.out.println(array[0]+","+array[1]+","+array[2]+",   "+target[0]);
//        }
//        System.out.println("wew");
//        test = differentiateTargets(test);
//
//        for (DataSetRow ro : test){
//            System.out.println(ro.getInput()[0]+","+ro.getInput()[1]+","+ro.getInput()[2]+",   "+ro.getDesiredOutput()[0]);
//        }

        predictStock("XOM", "EXXON");
        predictStock("JPM");
        predictStock("AAPL");
        predictStock("GOOGL", "GOOG");
        predictStock("AMZN");
        predictStock("FB");
        predictStock("TSLA");
        predictStock("WMT");
        predictStock("GM", "General Motors");
        predictStock("^FTSE", "FTSE100");

    }

    //Wrapper function if Yahoo and Database use the same symbol

    public static void predictStock(String symbol){
        predictStock(symbol, symbol);
    }

    //Collects data, trains neural network and posts predictions for a given stock.

    public static void predictStock(String yahooSymbol, String dbSymbol) {

        Date today = START_DATE.getTime();
        String tooday = 1900 + today.getYear() + "-" + today.getMonth()+1 + "-" + today.getDate() + 1;
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
            openingQuotes.add(q.getOpen().doubleValue() / 100000);
            //closingQuotes.add(q.getOpen().doubleValue()/10000);
            closingQuotes.add(q.getClose().doubleValue() / 100000);
            dates.add(q.getDate().getTime());
            System.out.println(q.getDate().getTime());

        }
        Collections.reverse(closingQuotes);

        DataSet data = generateDataSet(closingQuotes, dates, dbSymbol);

        multipleTests(10, data, nn);

//        double[] inputs = new double[LAYERS[0]];
//        for (int k = 0; k > -18; k--)
//            inputs[-k] = closingQuotes.get((closingQuotes.size()+k)-1);
//        ArrayList<Double> semantix = getSemanticInputs(new Date(), dbSymbol);
//        for (int l = 18; l < LAYERS[0]; l++)
//            inputs[l] = semantix.get(l-18);
//
//        nn.setInput(inputs);
//        nn.calculate();
//
//        double prediction = nn.getOutput()[0]*10000;

        //postPrediction(dbSymbol, prediction, tooday);

    }


    //Makes the targets of a Dataset relative to the previous stock price.

    public static DataSet differentiateTargets(DataSet input){
        DataSet inputDash = new DataSet(input.getInputSize(), input.getOutputSize());
        double previousPrice = input.remove(0).getDesiredOutput()[0];

        for (DataSetRow inputRow : input){
            double[] target = new double[1];
            target[0] = inputRow.getDesiredOutput()[0]-previousPrice;
            inputDash.add(new DataSetRow(inputRow.getInput(), target));
            previousPrice = inputRow.getDesiredOutput()[0];
        }

        return inputDash;
    }

    //Prints the average directional and accuracy tests from a series of repeated tests.

    public static void multipleTests(int reps, DataSet data, MultiLayerPerceptron nn){

        System.out.println("TESTING...");

        Double avg = 0d;
        for (int t = 0; t < reps; t++) {
            System.out.print("-");
            double res = trainAndTest(nn, data);
            if (!Double.isNaN(res))
                avg += res;
            else
                t--;
            nn = new MultiLayerPerceptron(TransferFunctionType.LINEAR, LAYERS);
        }
        System.out.println();
        System.out.println("Average Error: "+avg/10);

        avg = 0d;
        for (int t = 0; t < reps; t++) {
            System.out.print("-");
            double res = trainAndTestDirection(nn, data);
            if (!Double.isNaN(res))
                avg += res;
            else
                t--;
            nn = new MultiLayerPerceptron(TransferFunctionType.LINEAR, LAYERS);
        }
        System.out.println();
        System.out.println("Directional Accuracy: "+avg*10);
    }

    //Turns an ArrayList of historical quotes and dates into a Dataset with attached semantic inputs.

    public static DataSet generateDataSet(ArrayList<Double> quotes, ArrayList<Date> dates, String dbSymbol){

        int daysConsidered = LAYERS[0] - 10;
        DataSet data = new DataSet(LAYERS[0], 1);

        for (int i = 0; i + daysConsidered + 1 < quotes.size(); i++) {
            ArrayList<Double> inputs = new ArrayList<Double>(quotes.subList(i, i + daysConsidered));
            ArrayList<Double> target = new ArrayList<Double>(quotes.subList(i + daysConsidered + 1, i + daysConsidered + 2));
            inputs.addAll(getSemanticInputs(dates.get(i), dbSymbol));
            data.addRow(new DataSetRow(inputs, target));
        }

        return data;
    }

    //Trains a neural network off of a data set and calculates the mean error of the net.

    public static double trainAndTest(NeuralNetwork net, DataSet data){

        DataSet[] sets = data.createTrainingAndTestSubsets(80, 20);

        prepAndTrain(net, sets[0]);

        Double totalError = 0d;
        int count = 0;

        for (DataSetRow row : sets[1]){
            count++;
            net.setInput(row.getInput());
            net.calculate();
            totalError += Math.abs(net.getOutput()[0]-row.getDesiredOutput()[0])*10000;
        }
        return totalError/count;

    }

    //Given a neural network and a dataset, trains it and tests the % accuracy of advising a long or short position.

    public static double trainAndTestDirection(NeuralNetwork net, DataSet data){

        DataSet[] sets = data.createTrainingAndTestSubsets(80, 20);

        prepAndTrain(net, sets[0]);

        double correct = 0;
        double count = 0;
        Double target = sets[0].getRowAt(sets[0].size()-1).getDesiredOutput()[0];

        for (DataSetRow row : sets[1]){
            count+=1;
            double[] ro = row.getInput();
            net.setInput(row.getInput());
            net.calculate();
            if((net.getOutput()[0]>row.getDesiredOutput()[0]) == (net.getOutput()[0]>target)){
                correct+=1;
            }
        }
        System.out.println(correct+"/"+count);
        return correct/count;

    }

    //Sets neural network learning rate and max iterations before training it on given dataset.

    public static void prepAndTrain(NeuralNetwork net, DataSet data){

        SupervisedLearning lr = (SupervisedLearning) net.getLearningRule();
        lr.setLearningRate(0.000001);
        lr.setMaxIterations(10000);
        net.setLearningRule(lr);
        net.learn(data);

    }

    //Returns a frequency histogram of semantic classifications for a given stock on a given date.

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

    //Posts a prediction to the database.

    public static void postPrediction(String dbSymbol, double prediction, String date){

        float converted = (float) prediction;
        try {
            DB.addPredictionEntry(dbSymbol, converted, date);
            System.out.println("POSTED: "+dbSymbol+", "+prediction+", "+date);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("FAILED TO POST: "+dbSymbol+", "+prediction+", "+date);
        }

    }

    //Clears the database predictions table.

    public static void nukePredictions(){
        try {
            DB.executeQuery("TRUNCATE TABLE Predictions");
            System.out.println("NUKED PREDICTIONS");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}