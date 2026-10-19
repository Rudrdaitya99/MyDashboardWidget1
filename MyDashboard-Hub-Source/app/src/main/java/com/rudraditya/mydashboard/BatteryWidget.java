package com.rudraditya.mydashboard;
public class BatteryWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_battery; }
    @Override String kind() { return "battery"; }
}
