package com.aimsfx.view.ProductView;

import com.aimsfx.controller.ProductManagerController.ProductController;
import com.aimsfx.model.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDateTime;

/**
 * ProductHistoryView - Displays all historical versions of a product
 * 
 * DESIGN PATTERN: Dialog Pattern
 * PURPOSE: Shows temporal history of product changes in a modal dialog
 * 
 * FEATURES:
 * - Shows all versions of a product ordered by date (newest first)
 * - Highlights current version vs expired versions
 * - Displays key fields: ID, Title, Price, Status, Created Date, Expired Date
 * - Read-only view (no editing)
 */
public class ProductHistoryView {

    private final ProductController controller;
    private Stage dialogStage;
    @FXML
    private TableView<Product> historyTable;
    @FXML
    private Button closeButton;

    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProductHistoryView() {
        this.controller = ProductController.getInstance();
    }

    public void show(Long productId, Stage ownerStage) {
        dialogStage = new Stage();
        com.aimsfx.utils.UIUtils.applyAppIcon(dialogStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(ownerStage);
        dialogStage.setTitle("Product History - ID: " + productId);

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/aimsfx/product-history-view.fxml"));
            loader.setController(this);
            VBox root = loader.load();

            setupTableColumns();

            ObservableList<Product> history = controller.getProductHistory(productId);
            historyTable.setItems(history);

            historyTable.setRowFactory(tv -> new TableRow<Product>() {
                @Override
                protected void updateItem(Product product, boolean empty) {
                    super.updateItem(product, empty);
                    if (product == null || empty) {
                        setStyle("");
                    } else if (product.getIsCurrent() != null && product.getIsCurrent()) {
                        setStyle("-fx-background-color: #d4edda;");
                    } else {
                        setStyle("-fx-background-color: #f8f9fa;");
                    }
                }
            });

            Scene scene = new Scene(root, 1000, 600);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    private void setupTableColumns() {
        TableView<Product> table = historyTable;
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Version Status Column
        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            Boolean isCurrent = cellData.getValue().getIsCurrent();
            String status = (isCurrent != null && isCurrent) ? "CURRENT" : "EXPIRED";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusCol.setMaxWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");

        // Product ID Column
        TableColumn<Product, Long> idCol = new TableColumn<>("Version ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setMaxWidth(100);

        // Barcode Column
        TableColumn<Product, String> barcodeCol = new TableColumn<>("Barcode");
        barcodeCol.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        barcodeCol.setMaxWidth(120);

        // Product Type Column
        TableColumn<Product, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getClass().getSimpleName().toUpperCase();
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        typeCol.setMaxWidth(80);

        // Title Column
        TableColumn<Product, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setMinWidth(150);

        // Category Column
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setMaxWidth(120);

        // Original Price Column
        TableColumn<Product, Double> origPriceCol = new TableColumn<>("Original Price");
        origPriceCol.setCellValueFactory(new PropertyValueFactory<>("originalPrice"));
        origPriceCol.setMaxWidth(120);
        origPriceCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", price));
                }
            }
        });

        // Current Price Column
        TableColumn<Product, Double> priceCol = new TableColumn<>("Current Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        priceCol.setMaxWidth(120);
        priceCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", price));
                }
            }
        });

        // Stock Column (inventory)
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setMaxWidth(80);

        // Status Column
        TableColumn<Product, String> productStatusCol = new TableColumn<>("Product Status");
        productStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        productStatusCol.setMaxWidth(120);

        // Description Column
        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setMinWidth(200);

        // Weight Column
        TableColumn<Product, Double> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setMaxWidth(80);

        // Dimensions Column
        TableColumn<Product, String> dimensionsCol = new TableColumn<>("Dimensions");
        dimensionsCol.setCellValueFactory(new PropertyValueFactory<>("dimensions"));
        dimensionsCol.setMaxWidth(120);

        // VAT Rate Column
        TableColumn<Product, Double> vatCol = new TableColumn<>("VAT Rate");
        vatCol.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        vatCol.setMaxWidth(80);
        vatCol.setCellFactory(col -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double vat, boolean empty) {
                super.updateItem(vat, empty);
                if (empty || vat == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f%%", vat * 100));
                }
            }
        });

        // Created At Column
        TableColumn<Product, LocalDateTime> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdCol.setMinWidth(150);
        createdCol.setCellFactory(col -> new TableCell<Product, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(dateTime.format(DATE_FORMATTER));
                }
            }
        });

        // Updated At Column
        TableColumn<Product, LocalDateTime> updatedCol = new TableColumn<>("Updated At");
        updatedCol.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        updatedCol.setMinWidth(150);
        updatedCol.setCellFactory(col -> new TableCell<Product, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(dateTime.format(DATE_FORMATTER));
                }
            }
        });

        // Expired Date Column
        TableColumn<Product, LocalDateTime> expiredCol = new TableColumn<>("Expired At");
        expiredCol.setCellValueFactory(new PropertyValueFactory<>("expiredDate"));
        expiredCol.setMinWidth(150);
        expiredCol.setCellFactory(col -> new TableCell<Product, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText("-");
                    setStyle("-fx-text-fill: #999999;");
                } else {
                    setText(dateTime.format(DATE_FORMATTER));
                    setStyle("");
                }
            }
        });

        // Type-specific details column
        TableColumn<Product, String> detailsCol = new TableColumn<>("Type-Specific Details");
        detailsCol.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            StringBuilder details = new StringBuilder();
            if (product instanceof Book) {
                Book book = (Book) product;
                details.append("Author: ").append(book.getAuthor())
                        .append(" | Publisher: ").append(book.getPublisher())
                        .append(" | Pub.Date: ").append(book.getPublicationDate())
                        .append(" | Pages: ").append(book.getPages())
                        .append(" | Language: ").append(book.getLanguage())
                        .append(" | Cover: ").append(book.getCoverType())
                        .append(" | Genre: ").append(book.getGenre());
            } else if (product instanceof CD) {
                CD cd = (CD) product;
                details.append("Artist: ").append(cd.getArtist())
                        .append(" | Label: ").append(cd.getRecordLabel())
                        .append(" | Genre: ").append(cd.getGenre())
                        .append(" | Tracks: ").append(cd.getTrackCount())
                        .append(" | Release: ").append(cd.getReleaseDate());
            } else if (product instanceof DVD) {
                DVD dvd = (DVD) product;
                details.append("Director: ").append(dvd.getDirector())
                        .append(" | Studio: ").append(dvd.getStudio())
                        .append(" | Subtitle: ").append(dvd.getSubtitle())
                        .append(" | Disc: ").append(dvd.getDiscType())
                        .append(" | Duration: ").append(dvd.getDuration())
                        .append(" | Genre: ").append(dvd.getGenre())
                        .append(" | Release: ").append(dvd.getReleaseDate());
            } else if (product instanceof Newspaper) {
                Newspaper newspaper = (Newspaper) product;
                details.append("ISSN: ").append(newspaper.getIssn())
                        .append(" | Freq: ").append(newspaper.getFrequency())
                        .append(" | Editor: ").append(newspaper.getEditorInChief())
                        .append(" | Publisher: ").append(newspaper.getPublisher())
                        .append(" | Pub.Date: ").append(newspaper.getPublicationDate())
                        .append(" | Language: ").append(newspaper.getLanguage())
                        .append(" | Sections: ").append(newspaper.getSection());
            }
            return new javafx.beans.property.SimpleStringProperty(details.toString());
        });
        detailsCol.setMinWidth(300);

        table.getColumns().addAll(
                statusCol, idCol, barcodeCol, typeCol, titleCol, categoryCol,
                origPriceCol, priceCol, stockCol, productStatusCol,
                descCol, weightCol, dimensionsCol, vatCol,
                createdCol, updatedCol, expiredCol, detailsCol);
    }
}
