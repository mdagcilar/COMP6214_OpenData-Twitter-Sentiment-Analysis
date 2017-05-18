<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class TwitCache extends Model
{
    protected $connection = 'mysql';

    protected $table = 'cache';
    /**
     * Indicates if the model should be timestamped.
     *
     * @var bool
     */
    public $timestamps = false;


    protected $fillable = array('id', 'response');
}
