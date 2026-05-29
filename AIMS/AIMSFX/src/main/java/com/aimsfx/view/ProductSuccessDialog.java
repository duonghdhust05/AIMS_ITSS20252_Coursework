package com.aimsfx.view;

import com.aimsfx.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class ProductSuccessDialog implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label typeLabel;
    @FXML private Label categoryLabel;
    @FXML private Label priceLabel;
    @FXML private Label quantityLabel;
    @FXML private Label statusLabel;
    @FXML private VBox specificInfoContainer;
    @FXML private Button okButton;

    private Runnable onCloseAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize if needed
    }

    public void setOnCloseAction(Runnable callback) {
        this.onCloseAction = callback;
    }

    /**
     * Set product info without quantity (for database add operations)
     */
    public void setProductInfo(Product product) {
        setProductInfo(product, 1); // Default quantity is 1
    }

    /**
     * Set product info with specific quantity (for cart operations)
     */
    public void setProductInfo(Product product, int quantity) {
        titleLabel.setText(product.getTitle());
        categoryLabel.setText(product.getCategory() != null ? product.getCategory() : "N/A");
        priceLabel.setText("$" + String.format("%.2f", product.getOriginalPrice()));
        quantityLabel.setText(String.valueOf(quantity));
        statusLabel.setText(product.getStatus());

        // Set product type and specific information
        if (product instanceof Book) {
            typeLabel.setText("Book");
            Book book = (Book) product;
            addSpecificInfo("Author", book.getAuthor());
            addSpecificInfo("Publisher", book.getPublisher());
            addSpecificInfo("Publication Date", book.getPublicationDate());
            addSpecificInfo("Pages", String.valueOf(book.getPages()));
            addSpecificInfo("Language", book.getLanguage());
            addSpecificInfo("Cover Type", book.getCoverType());
            addSpecificInfo("Genre", book.getGenre());
        } else if (product instanceof CD) {
            typeLabel.setText("CD");
            CD cd = (CD) product;
            addSpecificInfo("Artist", cd.getArtist());
            addSpecificInfo("Record Label", cd.getRecordLabel());
            addSpecificInfo("Genre", cd.getGenre());
            addSpecificInfo("Track Count", String.valueOf(cd.getTrackCount()));
            if (cd.getReleaseDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                addSpecificInfo("Release Date", dateFormat.format(cd.getReleaseDate()));
            }
        } else if (product instanceof DVD) {
            typeLabel.setText("DVD");
            DVD dvd = (DVD) product;
            addSpecificInfo("Director", dvd.getDirector());
            addSpecificInfo("Studio", dvd.getStudio());
            addSpecificInfo("Subtitle", dvd.getSubtitle());
            addSpecificInfo("Disc Type", dvd.getDiscType());
            addSpecificInfo("Duration", dvd.getDuration() + " minutes");
            addSpecificInfo("Genre", dvd.getGenre());
            if (dvd.getReleaseDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                addSpecificInfo("Release Date", dateFormat.format(dvd.getReleaseDate()));
            }
        } else if (product instanceof Newspaper) {
            typeLabel.setText("Newspaper");
            Newspaper newspaper = (Newspaper) product;
            addSpecificInfo("ISSN", newspaper.getIssn());
            addSpecificInfo("Frequency", newspaper.getFrequency());
            addSpecificInfo("Editor-in-Chief", newspaper.getEditorInChief());
            addSpecificInfo("Publisher", newspaper.getPublisher());
            if (newspaper.getPublicationDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                addSpecificInfo("Publication Date", dateFormat.format(newspaper.getPublicationDate()));
            }
            addSpecificInfo("Language", newspaper.getLanguage());
            addSpecificInfo("Sections", newspaper.getSection());
        } else {
            typeLabel.setText("Unknown");
        }
    }

    private void addSpecificInfo(String label, String value) {
        if (value != null && !value.trim().isEmpty() && !value.equals("0")) {
            Label infoLabel = new Label(label + ": " + value);
            infoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
            specificInfoContainer.getChildren().add(infoLabel);
        }
    }

    @FXML
    private void handleOK() {
        if (onCloseAction != null) {
            onCloseAction.run();
        }
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
}