@extends('master')

@section('head')
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<script type="text/javascript">
  // Load google charts, on load call our relevant drawCharts function.
  google.charts.load('current', {packages: ['corechart', 'line', 'geochart', 'bar']});
  google.charts.setOnLoadCallback(drawCharts);
  function drawCharts() {
      drawStockChart();
      @if(count($countries) > 0)
        drawRegionsMap();
        drawBarChart();
      @endif
  }

// Function to manage our candleStick chart. Data passed through Laravels Blade templating
function drawCandlestickChart() {
    var data = google.visualization.arrayToDataTable([
      @foreach($stocks as $stock)
      [new Date("{{ str_replace('-', ',', $stock['Date']) }}"), {{$stock['Low']}}, {{$stock['Open']}}, {{$stock['Close']}}, {{$stock['High']}}],
      @endforeach
      ], true);

    var options = {
      chart: {
          title: 'FTSE100',
          subtitle: 'Candlestick chart',
      },
      bar: { groupWidth: '80%' },
      candlestick: {
        fallingColor: { strokeWidth: 0, fill: '#a52714' }, // red
        risingColor: { strokeWidth: 0, fill: '#0f9d58' }   // green
    },
    vAxis: { gridlines: { count: 10 } },
    hAxis: { gridlines: { count: 8 } },
    chartArea: {'width': '82%', 'height': '80%'},
    legend:'none',
    theme: 'material',
      //colors:['#777']
  };

  var chart = new google.visualization.CandlestickChart(document.getElementById('candle-chart'));

  chart.draw(data, options);
}

// Handles our line graph for stock. Uses Laravels Blade templating for data.
function drawStockChart() {
  var data = new google.visualization.DataTable();
  data.addColumn('date', 'Date');
  data.addColumn('number', 'Closing Price');
  data.addColumn('number', 'Closing Price');
  data.addRows([
    @foreach($stocks as $key => $stock)
    @if($key == count($stocks) - 1)
    [new Date("{{ str_replace('-', ',', $stock['Date']) }}"), {{$stock['Close']}}, {{$stock['Close']}}],
    @else
    [new Date("{{ str_replace('-', ',', $stock['Date']) }}"), {{$stock['Close']}}, null],
    @endif
    @endforeach
        //PREDICTION ONE
        [new Date("{{ date("Y,m,d", strtotime("+1 day")) }}"), null, {{ $stocks[count($stocks) - 1]['Close'] }}],
        ], true);

  var options = {
    chart: {
      title: 'FTSE100',
      subtitle: 'Closing price over time',
    },
    pointSize: 4,
    hAxis: { gridlines: { count: 20 } },
    legend: {position: 'none'},  
    chartArea: {'width': '82%', 'height': '80%'},
    crosshair: {
       trigger: 'both'
   },
   vAxis: {format: 'decimal'},
   theme: 'material',
   series: {
      1: { color: '#2ecc71' }
  },
  explorer: {
      maxZoomOut:1,
      keepInBounds: true
  }
  };

  var chart = new google.visualization.LineChart(document.getElementById('stock-chart'));

  chart.draw(data, options);

}

// Handles our bar chart to manage each country's positive/negative/neutral tweets
function drawBarChart() {
    var data = google.visualization.arrayToDataTable([
      ['Country', '# Positive Tweets', '# Negative Tweets', '# Neutral Tweets'],
      @foreach($countries as $country => $value)
      ['{{ $country }}', {{ $value->positive }}, {{ $value->negative }}, {{ $value->neutral }}],
      @endforeach
      ]);

    var options = {
      chart: {
        title: 'Tweet Analytics',
    },
    bars: 'horizontal',
    legend: {position: 'none'},
          //isStacked: true,
          series: {
            0:{color:'#2ecc71'},
            1:{color:'#c0392b'},
            3:{color:'#ddd'},
        },
        hAxis: {
            direction: -1,
            slantedText:true,
            slantedTextAngle:45
        },
        explorer: {
            maxZoomOut:1,
            keepInBounds: true
        }
    };

    var chart = new google.charts.Bar(document.getElementById('bar-chart'));

    chart.draw(data, google.charts.Bar.convertOptions(options));
}

//Handles displaying number of tweets per region graph
function drawRegionsMap() {

    var data = google.visualization.arrayToDataTable([
      ['Country', 'Number of Tweets'],
      @foreach($countries as $country => $value)
      ['{{ $country }}', {{ $value->total }}],
      @endforeach
      ]);

    var options = {
        colorAxis: {colors: ['#D8E7D8', 'green']},
        theme: 'material',
    };

    var chart = new google.visualization.GeoChart(document.getElementById('map-chart'));

    chart.draw(data, options);
}
</script>

@endsection


@section('scripts')

<script>
    // Word Cloud script - uses d3 and d3.cloud extension to produce
    // based on frequency of words.
    var frequency_list = 
      [
        @foreach($wordcount as $wc)
          {"text": "{{ $wc->Word }}", "size": {{ $wc->Count }} },
        @endforeach
      ];

    var fill = d3.scale.category20();

    var layout = d3.layout.cloud()
        .size([$('#word-cloud').width() , $('#word-cloud').width() / 2])
        .words(frequency_list)
        .padding(5)
        .rotate(function() { return (~~(Math.random() * 2) * 90) - 0 })
        .font("Impact")
        .fontSize(function(d) { return d.size * ($('#word-cloud').width() / 1000)/3; })
        .on("end", draw);

    $(function() {
      layout.start();
    });
    function draw(words) {
      d3.select("#word-cloud").append("svg")
          .attr("width", layout.size()[0])
          .attr("height", layout.size()[1])
        .append("g")
          .attr("transform", "translate(" + layout.size()[0] / 2 + "," + layout.size()[1] / 2 + ")")
        .selectAll("text")
          .data(words)
        .enter().append("text")
          .style("font-size", function(d) { return d.size + "px"; })
          .style("font-family", "Impact")
          .style("fill", function(d, i) { return fill(i); })
          .attr("text-anchor", "middle")
          .attr("transform", function(d) {
            return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
          })
          .text(function(d) { return d.text; });
    }
</script>
@endsection

@section('body')
<div class="container-fluid">
  <div class="row">
      <div class="col-xs-12 col-sm-8 col-md-9 col-sm-push-4 col-md-push-3 col-xs-push-0 chart-scroll">
          <div id="line-section">
            <div class="row">
                <div class="col-xs-12 column-header">
                    <div class="pull-right">
                      <h2><i class="glyphicon glyphicon-stats pointer toggle-chart"></i></h2>
                    </div>
                    <h2>Line Chart</h2>
                </div>
            </div>
            <div class="row">
                <div id="stock-chart"></div>
            </div>
          </div>
          <div id="candle-section" style="display:none;">
            <div class="row">
                <div class="col-xs-12 column-header">
                    <div class="pull-right">
                      <h2><i class="glyphicon glyphicon-stats pointer toggle-chart"></i></h2>
                    </div>
                    <h2>Candlestick Chart</h2>
                </div>
            </div>
            <div class="row">
                <div id="candle-chart"></div>
            </div>
          </div>
          @if(count($countries) > 0)
          <div class="row">
              <div class="col-xs-12 column-header">
                  <h2>Location Analysis</h2>
              </div>
          </div>
          <div class="row">
              <div class="col-xs-12 col-sm-12 col-md-6">
                  <div class="row">
                      <div id="map-chart"></div>
                  </div>
              </div>
              <div class="col-xs-12 col-sm-12 col-md-6">
                  <div class="row">
                      <div id="bar-chart"></div>
                  </div>
              </div>
          </div>
          @endif


          @if(count($wordcount) > 0)
          <div class="row">
              <div class="col-xs-12 column-header">
                  <h2>Word Cloud</h2>
              </div>
          </div>
          <div class="row">
              <div class="col-xs-12">
                  <div id="word-cloud"></div>
              </div>
          </div>
          @endif

      </div>
      <div class="col-xs-12 col-sm-4 col-md-3 tweet-col col-sm-pull-8 col-md-pull-9 col-xs-pull-0" style="border-right:1px solid #cdcdcd;">
        <div class="row">
            <div class="col-xs-12 column-header">
                <h2>Tweets</h2>
            </div>
        </div>

        <div class="row tweet-scroll">
          @if(count($tweets) == 0)
            <div class="media tweet">
              <div class="media-body">
                Could not retrieve any processed tweets.
              </div>
            </div>
          @endif
          @foreach($tweets as $tweet)
            <div class="media tweet" style="border-right: 8px solid @if($tweet->sentiment->Twitter_Mood > 2) #2ecc71 @elseif($tweet->sentiment->Twitter_Mood < 2) #e74c3c @else none @endif ;">
              <div class="media-left media-middle">
                <a href="http://twitter.com/{{ $tweet->user->screen_name }}" target="_blank">
                  <img class="media-object" src="{{ $tweet->user->profile_image_url }}" alt="{{ $tweet->user->screen_name }}">
                </a>
              </div>
              <div class="media-body">
                <strong><a href="http://twitter.com/{{$tweet->user->screen_name}}">{{ "@".$tweet->user->screen_name}}</a></strong>: {!! App\Utilities::linkifyTweet($tweet->text) !!}
              </div>
              <div class="tweet-footer">
                  <a href="http://twitter.com/{{$tweet->user->screen_name}}/status/{{$tweet->id_str}}" target="_blank">{{ Twitter::ago($tweet->created_at) }}</a>
              </div>
          </div>
        @endforeach
      </div>
    </div>
  </div>
</div>
@endsection