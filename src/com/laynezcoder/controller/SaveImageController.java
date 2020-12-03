package com.laynezcoder.controller;

import com.laynezcoder.database.DatabaseConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SaveImageController implements Initializable {

    @FXML
    private Button btnOpenFileExplorer;

    @FXML
    private ScrollPane scrollPane;

    private final TilePane tile = new TilePane();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tile.setPadding(new Insets(15, 15, 15, 15));
        tile.setHgap(15);
        tile.setVgap(15);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(tile);
    }

    @FXML
    private void handleOpenFileExplorer() {
        openFileExplorer();
    }

    @FXML
    private void handleShowImages() {
        loadImages();
    }

    private void openFileExplorer() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        File selectedImage = fileChooser.showOpenDialog(getStage());
        if (selectedImage != null) {
            boolean result = insertNewImage(selectedImage);
            if (result) {
                showAlert(Alert.AlertType.INFORMATION, "Success, nice job.", "The file was successfully added.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Oops.", "Connection error to Mysql. Please check your connection.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Oops.", "No file has been selected.");
        }
    }

    private Stage getStage() {
        return (Stage) btnOpenFileExplorer.getScene().getWindow();
    }

    private boolean insertNewImage(File selectedImage) {
        try {
            FileInputStream image = new FileInputStream(selectedImage);
            String sql = "INSERT INTO Images (nameImage, image) VALUES (?, ?)";
            PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            stmt.setString(1, selectedImage.getName());
            stmt.setBlob(2, image);
            stmt.execute();
            return true;
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(SaveImageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void loadImages() {
        tile.getChildren().clear();

        try {
            String sql = "SELECT * FROM Images";
            PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InputStream imageFile = rs.getBinaryStream("image");

                if (imageFile != null) {
                    Image image = new Image(imageFile, 200, 200, true, true);
                    createTile(image, rs.getInt("id"), rs.getString("nameImage"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SaveImageController.class.getName()).log(Level.SEVERE, null, ex);
            showAlert(Alert.AlertType.ERROR, "Oops.", "Connection error to Mysql. Please check your connection.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType.toString());
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean deleteImage(int id) {
        try {
            String sql = "DELETE FROM Images WHERE id = ?";
            PreparedStatement preparedStatement = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
            loadImages();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(SaveImageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void createTile(Image image, int id, String name) {
        ImageView iv = new ImageView(image);
        iv.setFitWidth(200);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: white");
        root.setPrefSize(iv.getFitWidth(), iv.getFitHeight());
        root.setPadding(new Insets(15, 15, 15, 15));
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);
        root.getChildren().addAll(iv, new Label(id + ". " + name));

        ContextMenu menu = new ContextMenu(new MenuItem("Delete"));
        menu.setAutoHide(true);
        menu.setAutoFix(true);
        menu.setConsumeAutoHidingEvents(true);
        menu.setHideOnEscape(true);

        menu.setOnAction(ev -> {
            boolean result = deleteImage(id);
            if (result) {
                showAlert(Alert.AlertType.INFORMATION, "Success, nice job.", "The file was successfully removed.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Oops.", "Connection error to Mysql. Please check your connection.");
            }
        });

        iv.setOnMouseClicked(ev -> {
            iv.setOnContextMenuRequested(e -> {
                menu.show(iv, ev.getScreenX(), ev.getScreenY());
            });
        });

        tile.getChildren().addAll(root);
    }
}
