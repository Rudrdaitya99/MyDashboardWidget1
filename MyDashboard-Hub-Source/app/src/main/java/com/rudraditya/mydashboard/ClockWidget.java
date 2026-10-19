package com.rudraditya.mydashboard;
public class ClockWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_clock; }
    @Override String kind() { return "clock"; }
}
