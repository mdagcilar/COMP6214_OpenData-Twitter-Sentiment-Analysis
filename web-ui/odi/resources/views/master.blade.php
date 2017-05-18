<!doctype html>
<html lang="{{ config('app.locale') }}">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="csrf-token" content="{{ csrf_token() }}">

        <title>Bull&amp;Bird</title>

        <link rel="stylesheet" href="{{ mix('/css/app.css') }}">
        @yield('head')
    </head>
    <body>

        @include('partials.sidebar')

        @include('partials.nav')
        
        @yield('body')

        <script>
                window.Laravel = <?php echo json_encode([
                    'csrfToken' => csrf_token(),
                ]); ?>
        </script>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js" integrity="sha256-hwg4gsxgFZhOsEEamdOYGBf13FyQuiTwlAQgxVSNgt4=" crossorigin="anonymous"></script>

        <script src="https://d3js.org/d3.v3.min.js"></script>
        
        <script src="/js/app.js"></script>
        <script src="/js/d3.js"></script>

        @yield('scripts')
    
    </body>
</html>
