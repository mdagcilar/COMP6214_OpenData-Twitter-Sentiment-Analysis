<div class="overlay"></div>
<div class="stock-sidebar">
    <div class="sidebar-header toggle-sidebar">
        <span class="sidebar-title">Stocks</span>
    </div>

    <div class="sidebar-list">
        <ul>
            <a href="/ftse100{{ $tc != 100 ? "?c=".$tc : "" }}"><li>FTSE100</li></a>
            <a href="/googl{{ $tc != 100 ? "?c=".$tc : "" }}"><li>Google</li></a>
            <a href="/amzn{{ $tc != 100 ? "?c=".$tc : "" }}"><li>Amazon</li></a>
            <a href="/aapl{{ $tc != 100 ? "?c=".$tc : "" }}"><li>Apple</li></a>
            <a href="/tsla{{ $tc != 100 ? "?c=".$tc : "" }}"><li>Tesla</li></a>
            <a href="/fb{{ $tc != 100 ? "?c=".$tc : "" }}"><li>Facebook</li></a>
            <a href="/wmt{{ $tc != 100 ? "?c=".$tc : "" }}"><li>Walmart</li></a>
            <a href="/gm{{ $tc != 100 ? "?c=".$tc : "" }}"><li>General Motors</li></a>
        </ul>
    </div>
</div>