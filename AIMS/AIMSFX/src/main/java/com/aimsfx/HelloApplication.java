package com.aimsfx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.aimsfx.utils.DatabaseManager;
import java.io.IOException;

import javafx.application.Application;
import java.util.TimeZone;

public class HelloApplication extends Application {
    
    @Override
    public void init() throws Exception {
        // Set timezone before initializing database connection
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        
        // Initialize database connection pool
        System.out.println("Initializing database connection pool...");
        DatabaseManager.initialize();
        super.init();
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("homepage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("AIMS - Product Management System");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        
        // Print database connection pool status for debugging
        System.out.println("\n" + DatabaseManager.getStatus());
    }

    public static void main(String[] args) {
        launch();
    }
}