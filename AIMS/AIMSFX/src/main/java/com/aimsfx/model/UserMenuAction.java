package com.aimsfx.model;

public enum UserMenuAction {
    CHANGE_PASSWORD("Change Password"),
    MANAGE_USERS("Manage Users"),
    MANAGE_ORDERS("Manage Orders"),
    LOGOUT("Logout");

    private final String label;

    UserMenuAction(String label) { this.label = label; }
    public String getLabel() { return label; }
}
