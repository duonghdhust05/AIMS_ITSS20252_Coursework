package com.aimsfx.view;

import com.aimsfx.utils.UIUtils;

/**
 * Base view class for displaying messages to user
 */
public class BaseView {

    /**
     * Display error message to user
     * 
     * @param message Error message to display
     */
    public void displayError(String message) {
        UIUtils.showError("Error", message);
    }

    /**
     * Display information message to user
     * 
     * @param message Information message to display
     */
    public void displayInfo(String message) {
        UIUtils.showAlert("Information", message);
    }

    /**
     * Display success message to user
     * 
     * @param message Success message to display
     */
    public void displaySuccess(String message) {
        UIUtils.showAlert("Success", message);
    }

    /**
     * Display warning message to user
     * 
     * @param message Warning message to display
     */
    public void displayWarning(String message) {
        UIUtils.showWarning("Warning", message);
    }
}
