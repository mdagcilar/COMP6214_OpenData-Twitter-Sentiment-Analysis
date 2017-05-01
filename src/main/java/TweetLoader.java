import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import twitter4j.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * TweetLoader.java
 */

public class TweetLoader {

    Twitter twitter = TwitterFactory.getSingleton();
    Set<Tweet> allTweets = new HashSet<Tweet>();
    static StanfordCoreNLP pipeline;
    ArrayList urlsPerTweet;
    ArrayList<Article> articles = null;
    //pattern to match urls contained within a tweet
    private final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public void generateTweets(String tweet) {
        Query query = new Query(tweet);
        query.setSince("2017-01-04");

        //How many tweets to retrieve in every call to Twitter. 100 is the maximum allowed in the API
        query.setCount(100);
        try {
            loadNextPage(query, tweet);
        } catch (TwitterException e) {
            System.out.println("Twitter query failed");
        }
    }

    public void loadNextPage(Query query, String tweet) throws TwitterException {
        long lowestTweetId = Long.MAX_VALUE;
        int tweetsPerPage = 0;
        do {
            this.init();
            QueryResult queryResult = twitter.search(query);
            tweetsPerPage = queryResult.getTweets().size();
            System.out.println("Please wait while I'm loading the tweets and analyzing their mood and articles..");
            for (Status tw : queryResult.getTweets()) {
                //Eliminating all Retweets as well as tweets in languages different than English
                if (tw.getRetweetCount() == 0 && tw.getLang().equals("en")) {
                    /*
                        * default article and tweet mood value = -1, later changed using setter methods
                        * stock name to be dynamically changed based on the user's stock name preference
                        * date to be changed to the actual tweet date
                     */
                    Tweet t = new Tweet("FTSE-TO BE CHANGED", tw.getId(), -1, -1, articles, "date TO BE CHANGED", tw.getText());
                    allTweets.add(t);
                    if (tw.getId() < lowestTweetId) {
                        lowestTweetId = tw.getId();
                        query.setMaxId(lowestTweetId);
                    }
                }
            }

        } while (tweetsPerPage != 0 && tweetsPerPage % 100 == 0);

        System.out.println("Total tweets found for: [" + tweet + "] -->" + allTweets.size());
        Iterator<Tweet> it = allTweets.iterator();
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            Tweet t = it.next();
            String str = t.getTweetText();
            //set the mood for every tweet
            t.setTweetMoodValue(this.analyseTweets(str));
            //fetch the articles(if multiple) per tweet
            ArrayList<String> urls = this.getUrls(str);
            //analyze the general mood for every article
            if(urls.size() >= 1) {
                this.articles = new ArrayList<Article>();
                for(int i=0; i<urls.size(); i++) {
                    //set the mood for the article
                    Article a = new Article(t.getTweetID(), urls.get(i),t.getTweetMoodValue());
                    t.setArticleMoodValue(this.analyseTweets(this.getArticleContent(urls.get(i))));
                    articles.add(a);
                }
                t.setRelatedArticles(articles);
            }

            if(t.getRelatedArticles() != null) {
                System.out.println("\n*******************"+ "\nGeneral mood of tweet " + counter + " : " + t.getTweetMoodValue() + ", tweetID: " + t.getTweetID()+ ", tweetMood: " + t.getTweetMoodValue() + ", number of articles: " + t.getRelatedArticles().size()  + ",: AND DATE TO BE CHANGED");
                System.out.println("The same tweet also has " + t.getRelatedArticles().size() + " relevant articles: ");
                for(int i=0; i<t.getRelatedArticles().size(); i++) {
                    System.out.println("Article " + i + " general mood: " + t.getRelatedArticles().get(i).getGeneralMood() + ", URL: " + t.getRelatedArticles().get(i).getArticleUrl());
                }
                System.out.println("*******************");
            } else {
                System.out.println("\n*******************"+ "\nGeneral mood of tweet " + counter + " : " + t.getTweetMoodValue() + ", tweetID: " + t.getTweetID()+ ", tweetMood: " + t.getTweetMoodValue() + ", number of articles: " + null + ", location: " + ",: AND DATE TO BE CHANGED");
            }

        }
    }


    public static void init() {
        pipeline = new StanfordCoreNLP("MyPropFile.properties");
    }

    public int analyseTweets(String tweet) {
        int mainSentiment = 0;
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(tweet);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        return mainSentiment;
    }


    public String getArticleContent(String urlString) {
        try {
            URL url = new URL(urlString);
            //avoid getting follow-up twitter links as we need article links only
            if (url.toString().contains("twitter")) {
                System.out.println("Disregarding twitter follow-up links.");
                //null would represent all the articles that ar of wrong format or whose output we won't be using
                return null;
            }

            String text = ArticleExtractor.INSTANCE.getText(url);
            return text;
        } catch (MalformedURLException m) {
            System.out.println("The url failed unexpectedly(CUSTOM ERROR MESSAGE)");
        } catch (IOException i) {
            System.out.println("Open connection failed unexpectedly(CUSTOM ERROR MESSAGE");
        } catch (BoilerpipeProcessingException b) {
            System.out.println("The header query failed unexpectedly(CUSTOM ERROR MESSAGE)");
        } catch (NoClassDefFoundError e) {
            System.out.println("Check Nekohtml library. (CUSTOM ERROR MESSAGE)");
        }
        System.out.println("Shouldnt be here");
        return null;
    }

    public ArrayList<String> getUrls(String str) {
        urlsPerTweet = new ArrayList<String>();
        Matcher matcher = urlPattern.matcher(str);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end(0);
            urlsPerTweet.add(str.substring(matchStart, matchEnd));
        }
        return urlsPerTweet;
    }
}
