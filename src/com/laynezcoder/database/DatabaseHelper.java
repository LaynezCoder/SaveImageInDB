package com.laynezcoder.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 *
 * @author LaynezCoder
 */
public class DatabaseHelper {

    public static boolean insertNewImage(File selectedImage) {
        try {
            FileInputStream image = new FileInputStream(selectedImage);
            String sql = "INSERT INTO Images (nameImage, image) VALUES (?, ?)";
            PreparedStatement stmt = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            stmt.setString(1, selectedImage.getName());
            stmt.setBlob(2, image);
            stmt.execute();
            return true;
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean updateImage(int id, File imageSelected) {
        try {
            FileInputStream image = new FileInputStream(imageSelected);
            String sql = "UPDATE Images SET nameImage = ?, image = ? WHERE id = ?";
            PreparedStatement preparedStatement = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            preparedStatement.setString(1, imageSelected.getName());
            preparedStatement.setBlob(2, image);
            preparedStatement.setInt(3, id);
            preparedStatement.execute();
            return true;
        } catch (SQLException | FileNotFoundException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean deleteImage(int id) {
        try {
            String sql = "DELETE FROM Images WHERE id = ?";
            PreparedStatement preparedStatement = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static Image getImage(int id) {
        Image image = null;
        try {
            String sql = "SELECT image FROM Images WHERE id = ?";
            PreparedStatement preparedStatement = DatabaseConnection.getInstance().getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                InputStream imageFile = rs.getBinaryStream("image");
                image = new Image(imageFile);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }
}
