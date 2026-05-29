package com.aimsfx.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * OrderManagementView - opens Product Manager order review screen as a dialog stage.
 */
public class OrderManagementView {

    public void show(Stage owner) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aimsfx/order-management-view.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Order Management");
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setScene(new Scene(root, 1600, 900));
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.showAndWait();
    }
}