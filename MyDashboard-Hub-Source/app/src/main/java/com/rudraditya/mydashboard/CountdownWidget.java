package com.rudraditya.mydashboard;
public class CountdownWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_countdown; }
    @Override String kind() { return "countdown"; }
}
