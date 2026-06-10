package com.aimsfx.view;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import java.net.URL;

/**
 * Demonstrates a WebView with a JSObject Bridge for Native Java <-> JS Communication.
 */
public class RichWebView {

    private final Stage parentStage;

    public RichWebView(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void show() {
        Stage stage = new Stage();
        com.aimsfx.utils.UIUtils.applyAppIcon(stage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Rich UI WebView Bridge");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Load the local HTML file
        URL url = getClass().getResource("/com/aimsfx/rich_ui.html");
        if (url != null) {
            engine.load(url.toExternalForm());
        } else {
            System.err.println("Could not find rich_ui.html");
        }

        // Establish the JSObject Bridge when the page successfully loads
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                // Inject the Java object as 'javaApp' into the JS window object
                window.setMember("javaApp", new JavaBridge(engine));
            }
        });

        BorderPane root = new BorderPane(webView);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Inner class representing the Java object exposed to JavaScript.
     * Methods must be public to be accessible from JS.
     */
    public class JavaBridge {
        private final WebEngine engine;

        public JavaBridge(WebEngine engine) {
            this.engine = engine;
        }

        // Called from JavaScript
        public void processData(String data) {
            System.out.println("[JavaBridge] Received data from JS: " + data);
            
            // Example of Java calling back into JavaScript
            String response = "Java acknowledged: " + data;
            // Execute on the JavaFX Application Thread automatically via executeScript
            engine.executeScript("receiveDataFromJava('" + response + "')");
        }
    }
}
