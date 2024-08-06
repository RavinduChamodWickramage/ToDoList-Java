package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    public TextField txtUsername;
    public PasswordField pwdPassword;
    public TextField txtPassword;
    public Button btnShow;

    public static String userFullName;
    public static String userID;

    public static String firstName;
    public static String lastName;
    public static String email;
    public static String oldPassword;

    public void initialize() {
        Connection connection = DBConnection.getInstance().getConnection();
        initializeFields();
    }

    private void initializeFields() {
        txtPassword.setVisible(false);

        txtPassword.managedProperty().bind(txtPassword.visibleProperty());

        txtPassword.textProperty().bindBidirectional(pwdPassword.textProperty());
    }

    public void btnRegisterOnAction(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/RegisterForm.fxml"))));
            stage.setTitle("Register Form");
            stage.centerOnScreen();
            stage.show();

            Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            disposeStage.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnLoginOnAction(ActionEvent actionEvent) {
        String userName = txtUsername.getText();
        String password = pwdPassword.getText();

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM user WHERE email = ? AND password = ?");

            preparedStatement.setObject(1,userName);
            preparedStatement.setObject(2,password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userFullName = resultSet.getString(2) + " " + resultSet.getString(3);
                userID = resultSet.getString(1);

                Stage stage = new Stage();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/ViewForm.fxml"))));
                stage.setTitle("TO-DO List");
                stage.centerOnScreen();
                stage.show();

                Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                disposeStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void btnShowOnAction(ActionEvent actionEvent) {
        if (pwdPassword.isVisible()) {
            pwdPassword.setVisible(false);
            txtPassword.setVisible(true);
            btnShow.setText("Hide");
        } else {
            pwdPassword.setVisible(true);
            txtPassword.setVisible(false);
            btnShow.setText("Show");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void btnUpdateAccountOnAction(ActionEvent actionEvent) {
        String userName = txtUsername.getText();
        String password = pwdPassword.getText();

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM user WHERE email = ? AND password = ?");

            preparedStatement.setObject(1,userName);
            preparedStatement.setObject(2,password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userID = resultSet.getString(1);
                firstName = resultSet.getString(2);
                lastName = resultSet.getString(3);
                email = resultSet.getString(4);
                oldPassword = resultSet.getString(5);

                Stage stage = new Stage();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/UserProfileForm.fxml"))));
                stage.setTitle("Update User Profile");
                stage.centerOnScreen();
                stage.show();

                Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                disposeStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
