package com.example.sprintly_app_smd_finale;

import android.view.View;
import android.widget.TextView;

public class NavBarHelper {
    private final View calendarNav;
    private final View tasksNav;
    private final View homeNav;
    private final View profileNav;

    private final TextView calendarLabel;
    private final TextView tasksLabel;
    private final TextView homeLabel;
    private final TextView profileLabel;

    private final NavBarListener listener;

    public NavBarHelper(View root, NavBarListener listener) {
        this.listener = listener;

        calendarNav   = root.findViewById(R.id.calendarNavItem);
        tasksNav      = root.findViewById(R.id.tasksNavItem);
        homeNav       = root.findViewById(R.id.homeNavItem);
        profileNav    = root.findViewById(R.id.profileNavItem);

        calendarLabel = root.findViewById(R.id.calendarLabel);
        tasksLabel    = root.findViewById(R.id.tasksLabel);
        homeLabel     = root.findViewById(R.id.homeLabel);
        profileLabel  = root.findViewById(R.id.profileLabel);

        View.OnClickListener clicker = v -> {
            selectTab(v);
            if (v == calendarNav) {
                listener.onCalendarSelected();
            } else if (v == tasksNav) {
                listener.onTasksSelected();
            } else if (v == homeNav) {
                listener.onHomeSelected();
            } else if (v == profileNav) {
                listener.onProfileSelected();
            }
        };

        calendarNav.setOnClickListener(clicker);
        tasksNav   .setOnClickListener(clicker);
        homeNav    .setOnClickListener(clicker);
        profileNav .setOnClickListener(clicker);
    }

    private void clearAllLabels() {
        calendarLabel.setVisibility(View.GONE);
        tasksLabel   .setVisibility(View.GONE);
        homeLabel    .setVisibility(View.GONE);
        profileLabel .setVisibility(View.GONE);
    }

    public void selectTab(View selectedNav) {
        clearAllLabels();
        if (selectedNav == calendarNav) {
            calendarLabel.setVisibility(View.VISIBLE);
        } else if (selectedNav == tasksNav) {
            tasksLabel   .setVisibility(View.VISIBLE);
        } else if (selectedNav == homeNav) {
            homeLabel    .setVisibility(View.VISIBLE);
        } else if (selectedNav == profileNav) {
            profileLabel .setVisibility(View.VISIBLE);
        }
    }
}
