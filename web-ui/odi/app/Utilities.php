<?php

namespace App;
use \Twitter;
use GuzzleHttp\Client;

class Utilities {

    //Used to convert a tweet text in to relevant links
    public static function linkifyTweet($tweet) {

      //Convert urls to <a> links
      $tweet = preg_replace("/([\w]+\:\/\/[\w-?&;#~=\.\/\@]+[\w\/])/", "<a target=\"_blank\" href=\"$1\">$1</a>", $tweet);

      //Convert hashtags to twitter searches in <a> links
      $tweet = preg_replace("/#([A-Za-z0-9\/\._]*)/", "<a target=\"_new\" href=\"http://twitter.com/hashtag/$1\">#$1</a>", $tweet);

      //Convert attags to twitter profiles in <a> links
      $tweet = preg_replace("/@([A-Za-z0-9\/\._]*)/", "<a href=\"http://www.twitter.com/$1\">@$1</a>", $tweet);

      //Convert cashtags
      $tweet = preg_replace("/\\\$([A-Za-z0-9\/\._]*)/", "<a target=\"_new\" href=\"https://twitter.com/search?q=\\\$$1\">\\\$$1</a>", $tweet);

      return $tweet;

    }

    //Takes an ID and returns raw Tweet data
    public static function getRawTweet($id) {
            try {
                $raw = Twitter::getTweet($id);
                $raw->tweetloc = Utilities::getCountry($raw->user->location);
                $tweet = json_encode($raw);
                return $raw;
            } catch(\Exception $e) {
                return null;
            }
    }

    /**
     *  Function to handle caching of Tweets. Pass ID, and if it exists
     *  in the database then fetch the cached data. If not use Twitter API.
     */
    public static function getTweetID($id) {

        $tweets = TwitCache::where('id', $id);
        if($tweets->count() == 0) {
            try {
                $raw = Twitter::getTweet($id);
                // Store country with the tweet
                $raw->tweetloc = Utilities::getCountry($raw->user->location);
                $tweet = json_encode($raw);
                // Cache in our database
                TwitCache::create([
                        'id' => $id,
                        'response' => $tweet
                    ]);
                return $raw;
            } catch(\Exception $e) {
                // As PHP is awful with getting big ints from database we need to do this to cure inprecision :(
                $twt = Utilities::getRawTweet($id + 1);
                if($twt == null) {
                    $twt = Utilities::getRawTweet($id + 2);
                }
                if($twt == null) {
                    $twt = Utilities::getRawTweet($id + 3);
                }
                if($twt == null) {
                    $twt = Utilities::getRawTweet($id + 4);
                }
                if($twt == null) {
                    $twt = Utilities::getRawTweet($id + 5);
                }
                if($twt == null) {
                    $twt = Utilities::getRawTweet($id + 6);
                }
                if($twt == null) {
                    TwitCache::create([
                            'id' => $id,
                            'response' => null,
                        ]);
                    return null;
                } else {
                    $twt->tweetloc = Utilities::getCountry($twt->user->location);
                    $tweet = json_encode($twt);
                    TwitCache::create([
                            'id' => $id,
                            'response' => $tweet
                        ]);
                    return $twt;
                }
            }
        } else {
            // If we found the tweet in database, return the response
            $response = $tweets->first()->response;
            if($response == null) return null;
            return json_decode($tweets->first()->response);
        }
    }

    // Take a location from a tweet and use googles geocoding API to get the country
    // Return NULL if unsuccessful
    public static function getCountry($tweetloc) {
            $location = preg_replace("/\W|,/", '', $tweetloc);
            if($location != "") {
                //https://maps.googleapis.com/maps/api/geocode/json?address=ll&key=AIzaSyDt3VmAv1M6n3K8w5YD0JNts26LEvjM9TQ
                $client = new Client();
                $res = $client->get('https://maps.googleapis.com/maps/api/geocode/json?address='.$location.'&key=AIzaSyB2epDoiKGhLfvADseJb3fZIFfcJfQG2RE');
                $location = json_decode($res->getBody());
                if(count($location->results) > 0) {
                    foreach($location->results[0]->address_components as $component) {
                        if($component->types[0] == 'country') {
                                return $component->long_name;
                        }
                    }
                }
            }

            return null;
    }
}