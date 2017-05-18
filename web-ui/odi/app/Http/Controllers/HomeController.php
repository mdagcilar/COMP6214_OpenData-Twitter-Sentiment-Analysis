<?php

namespace App\Http\Controllers;
use \Twitter;

use Illuminate\Http\Request;
use GuzzleHttp\Client;
use DirkOlbrich\YahooFinanceQuery\YahooFinanceQuery;
use App\SentimentAnalysis;
use App\WordCount;
use App\Utilities;

class HomeController extends Controller
{
    // getIndex, default stock is FTSE
    public function getIndex($stock = 'ftse100') {

        //Tweet count, if request contains c then use this as tweet count
        $tc = 100;
        if(\Request::has('c') && is_int((int)\Request::get('c'))) {
            $tc = (int) \Request::get('c');
        }

        //Temporary fix as GM is stored in database as General Motors
        if($stock == 'gm') $stock = 'general motors';

        //Collect all Sentiment Analysis Tweets for certain stock
        $sa = SentimentAnalysis::where('Stock', $stock)->orderBy('Date', 'desc')->take($tc)->get();


        if($stock == 'general motors') $stock = 'gm';

        //Populate the tweets array with tweets and their sentiments
        $tweets = array();
        foreach($sa as $sentiment) {
            \Log::info(sprintf('%d', $sentiment->Tweet_ID));
            try {
                //Uses Utilities class to handle caching
                $tweet = Utilities::getTweetID(sprintf('%d', $sentiment->Tweet_ID));

                if($tweet != null && $tweet->lang == 'en') {
                    $tweet->sentiment = $sentiment;
                    $tweets[] = $tweet;
                } else {
                }
            } catch(\Exception $e) {
                dd($e);
            }
        }

        // Set up Yahoo Finance Query to fetch past 3 months history of selected stock
        $query = new YahooFinanceQuery();
        $startDate = date('Y-m-d', strtotime("-3 months ", time()));
        $endDate = date('Y-m-d');
        $param = 'd';
        $ystock = "^FTSE";
        if($stock != "ftse100") {
            $ystock = $stock;
        }
        $data = $query->historicalQuote($ystock, $startDate, $endDate, $param)->get();

        //Reverse so newest is last
        $data = array_reverse($data);
        $countries = array();

        //For every tweet, add whether it's neg/post/neutral to associated country
        foreach($tweets as $tweet) {
            if($tweet->tweetloc != "" && $tweet->tweetloc != null) {
                if(!array_key_exists($tweet->tweetloc, $countries)) {
                    $countries[$tweet->tweetloc] = new \stdClass();
                    $countries[$tweet->tweetloc]->negative = 0;
                    $countries[$tweet->tweetloc]->positive = 0;
                    $countries[$tweet->tweetloc]->neutral = 0;
                    $countries[$tweet->tweetloc]->total = 0;
                }

                $countries[$tweet->tweetloc]->total++;

                if($tweet->sentiment->Twitter_Mood < 2) {
                    $countries[$tweet->tweetloc]->negative++;
                } else if($tweet->sentiment->Twitter_Mood > 2) {
                    $countries[$tweet->tweetloc]->positive++;
                } else {
                    $countries[$tweet->tweetloc]->neutral++;
                }
            }
        }

        //Fetch all wordcounts for the certain stock
        $wc = WordCount::where('Stock', $stock)->orderBy('Count', 'desc')->take(20)->get();

        //Return out index view with all variables
        return view('index')
            ->withTweets($tweets)
            ->withStocks($data)
            ->withCountries($countries)
            ->with('stockName', $stock)
            ->with('tc', $tc)
            ->with('wordcount', $wc);
    }
}
