package com.aimsfx.view;

import com.aimsfx.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductUpdateSuccessDialog implements Initializable {

    @FXML private Label productTitleLabel;
    @FXML private Label productDetailsLabel;
    @FXML private Button closeButton;

    private Runnable onCloseAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // No initialization needed for this dialog
    }

    public void setProductInfo(Product product) {
        productTitleLabel.setText(product.getTitle());
        
        StringBuilder details = new StringBuilder();
        details.append("Type: ").append(product.getClass().getSimpleName()).append("\n");
        details.append("Barcode: ").append(product.getBarcode()).append("\n");
        details.append("Price: ").append(String.format("%.2f VND", product.getCurrentPrice())).append("\n");
        details.append("Status: ").append(product.getStatus()).append("\n\n");
        
        // Add type-specific information
        if (product instanceof Book) {
            Book book = (Book) product;
            details.append("Book Details:\n");
            if (book.getAuthor() != null) details.append("Author: ").append(book.getAuthor()).append("\n");
            if (book.getPublisher() != null) details.append("Publisher: ").append(book.getPublisher()).append("\n");
            if (book.getPublicationDate() != null) details.append("Publication Date: ").append(book.getPublicationDate()).append("\n");
            if (book.getPages() != null) details.append("Pages: ").append(book.getPages()).append("\n");
            if (book.getLanguage() != null) details.append("Language: ").append(book.getLanguage()).append("\n");
            if (book.getCoverType() != null) details.append("Cover Type: ").append(book.getCoverType()).append("\n");
            if (book.getGenre() != null) details.append("Genre: ").append(book.getGenre()).append("\n");
        } else if (product instanceof CD) {
            CD cd = (CD) product;
            details.append("CD Details:\n");
            if (cd.getArtist() != null) details.append("Artist: ").append(cd.getArtist()).append("\n");
            if (cd.getRecordLabel() != null) details.append("Record Label: ").append(cd.getRecordLabel()).append("\n");
            if (cd.getGenre() != null) details.append("Genre: ").append(cd.getGenre()).append("\n");
            if (cd.getTrackCount() != null) details.append("Track Count: ").append(cd.getTrackCount()).append("\n");
            if (cd.getReleaseDate() != null) details.append("Release Date: ").append(cd.getReleaseDate()).append("\n");
        } else if (product instanceof DVD) {
            DVD dvd = (DVD) product;
            details.append("DVD Details:\n");
            if (dvd.getDirector() != null) details.append("Director: ").append(dvd.getDirector()).append("\n");
            if (dvd.getStudio() != null) details.append("Studio: ").append(dvd.getStudio()).append("\n");
            if (dvd.getSubtitle() != null) details.append("Subtitle: ").append(dvd.getSubtitle()).append("\n");
            if (dvd.getDiscType() != null) details.append("Disc Type: ").append(dvd.getDiscType()).append("\n");
            if (dvd.getDuration() != null) details.append("Duration: ").append(dvd.getDuration()).append(" minutes\n");
            if (dvd.getGenre() != null) details.append("Genre: ").append(dvd.getGenre()).append("\n");
            if (dvd.getReleaseDate() != null) details.append("Release Date: ").append(dvd.getReleaseDate()).append("\n");
        } else if (product instanceof Newspaper) {
            Newspaper newspaper = (Newspaper) product;
            details.append("Newspaper Details:\n");
            if (newspaper.getIssn() != null) details.append("ISSN: ").append(newspaper.getIssn()).append("\n");
            if (newspaper.getFrequency() != null) details.append("Frequency: ").append(newspaper.getFrequency()).append("\n");
            if (newspaper.getEditorInChief() != null) details.append("Editor-in-Chief: ").append(newspaper.getEditorInChief()).append("\n");
            if (newspaper.getPublisher() != null) details.append("Publisher: ").append(newspaper.getPublisher()).append("\n");
            if (newspaper.getPublicationDate() != null) details.append("Publication Date: ").append(newspaper.getPublicationDate()).append("\n");
            if (newspaper.getLanguage() != null) details.append("Language: ").append(newspaper.getLanguage()).append("\n");
            if (newspaper.getSection() != null) details.append("Sections: ").append(newspaper.getSection()).append("\n");
        }
        
        productDetailsLabel.setText(details.toString());
    }

    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }

    @FXML
    private void handleClose() {
        if (onCloseAction != null) {
            onCloseAction.run();
        }
        
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}