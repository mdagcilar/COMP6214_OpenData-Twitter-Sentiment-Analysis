
/**
 * First we will load all of this project's JavaScript dependencies which
 * includes Vue and other libraries. It is a great starting point when
 * building robust, powerful web applications using Vue and Laravel.
 */

require('./bootstrap');


$(function() {
    var lineVisible = true;
    $('.toggle-chart').click(function() {
        if(lineVisible) {
            $('#line-section').hide();
            $('#candle-section').show();

            drawCandlestickChart();
        } else {
            $('#candle-section').hide();
            $('#line-section').show();

            drawStockChart();
        }
        lineVisible = !lineVisible;
    });
    var sidebarVisible = false;
    var toggleSidebar = function() {
        if(sidebarVisible) {
            $('.stock-sidebar').fadeOut();
            $('.overlay').fadeOut();
        } else {
            $('.overlay').fadeIn();
            $('.stock-sidebar').fadeIn();
        }
        sidebarVisible = !sidebarVisible;
    };
    $('.overlay').click(toggleSidebar);
    $('.toggle-sidebar').click(toggleSidebar);
});
