package com.laynezcoder.controller;

import com.laynezcoder.database.DatabaseConnection;
import com.laynezcoder.database.DatabaseHelper;
import java.io.File;
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
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author LaynezCoder
 */
public class SaveImageController implements Initializable {

    private static final long LIMIT = 419430;

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

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(tile);
    }

    @FXML
    private void handleAddNewImage() {
        File imageSelected = getFileSelected();
        if (imageSelected != null) {
            long imgLength = imageSelected.length();
            if (imgLength > LIMIT) {
                showAlert(Alert.AlertType.ERROR, "Oops.", "This image exceeds the weight limit to save. "
                        + "Select another image.\n" + imgLength + " bytes > " + LIMIT + " bytes");
            } else {
                boolean result = DatabaseHelper.insertNewImage(imageSelected);
                if (result) {
                    showAlert(Alert.AlertType.INFORMATION, "Success, nice job.", "The file was successfully added.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Oops.", "Connection error to Mysql. Please check your connection.");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Oops.", "No file has been selected.");
        }
    }

    @FXML
    private void handleShowImages() {
        loadImages();
    }

    private File getFileSelected() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        fileChooser.setTitle("Select an image");

        File selectedImage = fileChooser.showOpenDialog(getStage());
        return selectedImage;
    }

    private Stage getStage() {
        return (Stage) btnOpenFileExplorer.getScene().getWindow();
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

    private void actionToUpdateImage(int id) {
        File imageSelected = getFileSelected();
        if (imageSelected != null) {
            long imgLength = imageSelected.length();
            if (imgLength > LIMIT) {
                showAlert(Alert.AlertType.ERROR, "Oops.", "This image exceeds the weight limit to save. "
                        + "Select another image.\n" + imgLength + " bytes > " + LIMIT + " bytes");
            } else {
                boolean result = DatabaseHelper.updateImage(id, imageSelected);
                if (result) {
                    loadImages();
                    showAlert(Alert.AlertType.INFORMATION, "Success, nice job.", "The file was successfully updated.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Oops.", "Connection error to Mysql. Please check your connection.");
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Oops.", "No file has been selected.");
        }
    }

    private void createTile(Image image, int id, String name) {
        ImageView iv = new ImageView(image);
        iv.setFitWidth(120);
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

        MenuItem delete = new MenuItem("Delete");
        MenuItem update = new MenuItem("Update");

        ContextMenu menu = new ContextMenu(delete, update);
        menu.setAutoHide(true);
        menu.setAutoFix(true);
        menu.setConsumeAutoHidingEvents(true);
        menu.setHideOnEscape(true);

        delete.setOnAction(ev -> {
            boolean result = DatabaseHelper.deleteImage(id);
            if (result) {
                loadImages();
                showAlert(Alert.AlertType.INFORMATION, "Success, nice job.", "The file was successfully removed.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Oops.", "Connection error to Mysql. Please check your connection.");
            }
        });

        update.setOnAction(ev -> {
            actionToUpdateImage(id);
        });

        iv.setOnContextMenuRequested(ev -> {
            menu.show(iv, ev.getScreenX(), ev.getScreenY());
        });

        iv.setOnMouseClicked(ev -> {
            if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                Image img = DatabaseHelper.getImage(id);
                Double height = img.getHeight();
                Double witdh = img.getWidth();

                Stage stage = new Stage();
                stage.setTitle(name);

                ImageView imageView = new ImageView(img);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(imageView);

                if (height > 1000 && witdh > 600) {
                    imageView.setFitHeight(height / 2);
                    imageView.setFitWidth(witdh / 2);

                    Scene scene = new Scene(anchorPane);
                    stage.setScene(scene);
                    stage.setHeight(imageView.getFitHeight());
                    stage.setWidth(imageView.getFitWidth());
                } else {
                    Scene scene = new Scene(anchorPane);
                    stage.setScene(scene);
                    stage.setWidth(witdh);
                    stage.setHeight(height);
                }
                stage.show();
            }
        });

        tile.getChildren().addAll(root);
    }
}
