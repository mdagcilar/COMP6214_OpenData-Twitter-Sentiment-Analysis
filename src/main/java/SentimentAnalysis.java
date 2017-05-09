import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SentimentAnalysis {

    public static void generateSemanticData(String symbol, Date start, Date end){

        ArrayList<String> dateStrings = new ArrayList<String>();
        Calendar cal = Calendar.getInstance();
        Date current = start;
        cal.setTime(current);
        TweetLoader ld = new TweetLoader();

        while(current.before(end)){
            cal.add(cal.DATE, 1);
            current = cal.getTime();
            String month;
            String day;
            if (current.getMonth() < 9){
                month = "0"+(current.getMonth()+1);
            }else{
                month = Integer.toString((current.getMonth()+1));
            }
            if (current.getDate() < 10){
                day = "0"+current.getDate();
            }else{
                day = Integer.toString(current.getDate());
            }
            dateStrings.add(1900+current.getYear()+"-"+month+"-"+day);
        }

        for (int j = 0; j < dateStrings.size()-1; j++) {
            System.out.println(dateStrings.get(j));
            System.out.println("Loading tweets from dates " + dateStrings.get(i) + " to " + dateStrings.get(i + 1));
            ld.generateTweets(symbol, date.get(i), date.get(i + 1), 50);
        }
    }

    public static void main(String[] args) {

        generateSemanticData("foo", new Date(117, 0, 1), new Date(117, 5, 9));

    }

    public static void oldMain(){
        ArrayList<String> date = new ArrayList<String>();

        //feb
//        date.add("2017-02-01");
//        date.add("2017-02-02");
//        date.add("2017-02-03");
//        date.add("2017-02-04");
//        date.add("2017-02-05");
//        date.add("2017-02-06");
//        date.add("2017-02-07");
//        date.add("2017-02-08");
//        date.add("2017-02-09");
//        date.add("2017-02-10");
//        date.add("2017-02-11");
//        date.add("2017-02-12");
//        date.add("2017-02-13");
//        date.add("2017-02-14");
//        date.add("2017-02-15");
//        date.add("2017-02-16");
//        date.add("2017-02-17");
//        date.add("2017-02-18");
//        date.add("2017-02-19");
//        date.add("2017-02-20");
//        date.add("2017-02-21");
//        date.add("2017-02-22");
//        date.add("2017-02-23");
//        date.add("2017-02-24");
//        date.add("2017-02-25");
//        date.add("2017-02-26");
//        date.add("2017-02-27");
//        date.add("2017-02-28");
//
//
//        //march
//        date.add("2017-03-01");
//        date.add("2017-03-02");
//        date.add("2017-03-03");
//        date.add("2017-03-04");
//        date.add("2017-03-05");
//        date.add("2017-03-06");
//        date.add("2017-03-07");
//        date.add("2017-03-08");
//        date.add("2017-03-09");
//        date.add("2017-03-10");
//        date.add("2017-03-11");
//        date.add("2017-03-12");
//        date.add("2017-03-13");
//        date.add("2017-03-14");
//        date.add("2017-03-15");
//        date.add("2017-03-16");
//        date.add("2017-03-17");
//        date.add("2017-03-18");
//        date.add("2017-03-19");
//        date.add("2017-03-20");
//        date.add("2017-03-21");
//        date.add("2017-03-22");
//        date.add("2017-03-23");
//        date.add("2017-03-24");
//        date.add("2017-03-25");
//        date.add("2017-03-26");
//        date.add("2017-03-27");
//        date.add("2017-03-28");
//        date.add("2017-03-29");
//        date.add("2017-03-30");
//        date.add("2017-03-31");
//
//        //April
//        date.add("2017-04-01");
//        date.add("2017-04-02");
//        date.add("2017-04-03");
//        date.add("2017-04-04");
//        date.add("2017-04-05");
//        date.add("2017-04-06");
//        date.add("2017-04-07");
//        date.add("2017-04-08");
//        date.add("2017-04-09");
//        date.add("2017-04-10");
//        date.add("2017-04-11");
//        date.add("2017-04-12");
//        date.add("2017-04-13");
//        date.add("2017-04-14");
//        date.add("2017-04-15");
//        date.add("2017-04-16");
//        date.add("2017-04-17");
//        date.add("2017-04-18");
//        date.add("2017-04-19");
//        date.add("2017-04-20");
//        date.add("2017-04-21");
        date.add("2017-04-22");
        date.add("2017-04-23");
        date.add("2017-04-24");
        date.add("2017-04-25");
        date.add("2017-04-26");
        date.add("2017-04-27");
        date.add("2017-04-28");
        date.add("2017-04-29");
        date.add("2017-04-30");

        //May
        date.add("2017-05-01");
        date.add("2017-05-02");
        date.add("2017-05-03");
        date.add("2017-05-04");
        date.add("2017-05-05");
        date.add("2017-05-06");
        date.add("2017-05-07");

        TweetLoader load = new TweetLoader();

        for(int i =0; i< date.size() -1; i++){
            System.out.println("*******Loading tweets from dates " + date.get(i) + " to " + date.get(i+1));
            load.generateTweets("GOOGL", date.get(i), date.get(i+1), 50);
        }
    }
}
