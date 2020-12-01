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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
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
        scrollPane.setContent(tile);
    }

    @FXML
    private void handleOpenFileExplorer() {
        openFileExplorer();
    }

    @FXML
    private void handleShowImages() {
        showImages();
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
                System.out.println("Save Image");
            } else {
                System.err.println("FATAL ERROR");
            }
        } else {
            System.out.println("file is not valid!");
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

    private void showImages() {
        tile.getChildren().clear();

        try {
            String sql = "SELECT image FROM Images";
            PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                InputStream imageFile = rs.getBinaryStream(1);

                Image image = new Image(imageFile, 200, 200, true, true);

                ImageView iv;
                iv = new ImageView(image);
                iv.setFitWidth(200);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                iv.setCache(true);
                tile.getChildren().add(iv);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SaveImageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
