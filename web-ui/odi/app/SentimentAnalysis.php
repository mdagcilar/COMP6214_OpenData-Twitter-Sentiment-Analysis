<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class SentimentAnalysis extends Model
{
        /**
     * primaryKey 
     * 
     * @var integer
     * @access protected
     */
    protected $primaryKey = null;

    /**
     * Indicates if the IDs are auto-incrementing.
     *
     * @var bool
     */
    public $incrementing = false;


    protected $table = 'Sentiment_Analysis';
    /**
     * Indicates if the model should be timestamped.
     *
     * @var bool
     */
    public $timestamps = false;

    protected $casts = [
        'Tweet_ID' => 'integer'
    ];

    public function getIDAttribute() {
        return sprintf('%u', $this->Tweet_ID);
    }
}
