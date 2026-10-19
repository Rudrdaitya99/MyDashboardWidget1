package com.rudraditya.mydashboard;
public class DashboardWidget extends BaseWidget {
    @Override int layoutId() { return R.layout.widget_dashboard; }
    @Override String kind() { return "dashboard"; }
}
