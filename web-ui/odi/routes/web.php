<?php

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

Route::get('/{stock}', 'HomeController@getIndex');
Route::get('/', 'HomeController@getIndex');

Route::get('test', function() {
    dd(App\SentimentAnalysis::all());
    dd(Twitter::getSearch(array('q' => '#ftse'))->statuses);
});

Route::get('test2', function() {
    $quandl = new App\Quandl(env('QUANDL_KEY'), 'object');
    //dd($quandl->getSearch("ftse"));
    $data = $quandl->getSymbol("LSE/SUK2");
    dd($data);
});