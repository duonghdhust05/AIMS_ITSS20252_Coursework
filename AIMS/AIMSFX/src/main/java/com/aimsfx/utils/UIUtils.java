package com.aimsfx.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class UIUtils {

    /**
     * Format price with thousand separators (Vietnamese locale)
     */
    public static String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.of("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price);
    }

    /**
     * Show information alert dialog
     */
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show warning alert
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Non-blocking
    }

    /**
     * Show confirmation dialog
     * @return true if user confirmed
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Helper to load a scene and set it to a stage
     */
    public static void navigate(Stage currentStage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource(fxmlPath));
            Parent view = loader.load();
            if (currentStage.getScene() != null) {
                currentStage.getScene().setRoot(view);
            } else {
                currentStage.setScene(new Scene(view));
            }
            currentStage.setTitle(title);
        } catch (IOException e) {
            showError("Navigation Error", "Could not load " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
