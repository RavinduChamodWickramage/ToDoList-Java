package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class UserProfileController {
    public TextField txtFirstName;
    public TextField txtLastName;
    public TextField txtEmail;
    public TextField txtPassword;
    public TextField txtConfirmPassword;
    public PasswordField pwdPassword;
    public PasswordField pwdConfirmPassword;
    public Button btnShowPassword;
    public Button btnShowConfirmPassword;
    public Label lblInvalidEmail;
    public Label lblMismatchPassword1;
    public Label lblMismatchPassword2;
    public Label lblUserID;

    public void initialize() {
        Connection connection = DBConnection.getInstance().getConnection();

        initializeFields();
        addListeners();
        setUserDetails();
    }

    private void initializeFields() {
        lblInvalidEmail.setVisible(false);
        lblMismatchPassword1.setVisible(false);
        lblMismatchPassword2.setVisible(false);

        txtPassword.setVisible(false);
        txtConfirmPassword.setVisible(false);

        txtPassword.managedProperty().bind(txtPassword.visibleProperty());
        txtConfirmPassword.managedProperty().bind(txtConfirmPassword.visibleProperty());

        txtPassword.textProperty().bindBidirectional(pwdPassword.textProperty());
        txtConfirmPassword.textProperty().bindBidirectional(pwdConfirmPassword.textProperty());
    }

    private void addListeners() {
        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidEmail()) {
                lblInvalidEmail.setVisible(false);
                txtEmail.setStyle(null);
            }
        });

        pwdPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isMatchPasswords()) {
                lblMismatchPassword1.setVisible(false);
                lblMismatchPassword2.setVisible(false);
                pwdConfirmPassword.setStyle(null);
            }
        });

        pwdConfirmPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isMatchPasswords()) {
                lblMismatchPassword1.setVisible(false);
                lblMismatchPassword2.setVisible(false);
                pwdConfirmPassword.setStyle(null);
            }
        });
    }

    public void setUserDetails() {
        lblUserID.setText(LoginController.userID);
        txtFirstName.setText(LoginController.firstName);
        txtLastName.setText(LoginController.lastName);
        txtEmail.setText(LoginController.email);
        pwdPassword.setText(LoginController.oldPassword);
    }

    public void btnUpdateOnAction(ActionEvent actionEvent) {
        if (isEmpty()) {
            showAlert("Warning", "Please fill all the fields.");
            return;
        }

        if (!isValidEmail()) {
            lblInvalidEmail.setVisible(true);
            txtEmail.setStyle("-fx-border-color: red");
            return;
        }

        if (!isMatchPasswords()) {
            lblMismatchPassword1.setVisible(true);
            lblMismatchPassword2.setVisible(true);
            pwdConfirmPassword.setStyle("-fx-border-color: red");
            return;
        }

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE user SET firstName = ?, lastName = ?, email = ?, password = ? WHERE id = ?"
            );
            preparedStatement.setString(1, txtFirstName.getText());
            preparedStatement.setString(2, txtLastName.getText());
            preparedStatement.setString(3, txtEmail.getText());
            preparedStatement.setString(4, pwdPassword.getText());
            preparedStatement.setString(5, lblUserID.getText());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected != 0) {
                showAlert("Success", "User details updated successfully!");

                Stage stage = new Stage();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/LoginForm.fxml"))));
                stage.setTitle("Login Form");
                stage.centerOnScreen();
                stage.show();

                Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                disposeStage.close();
            } else {
                showAlert("Failed", "Failed to update user details.");
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
        Connection connection = DBConnection.getInstance().getConnection();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure want to delete your account?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.get().equals(ButtonType.YES)) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM user WHERE id = ?");

                preparedStatement.setString(1, lblUserID.getText());

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected != 0) {
                    showAlert("Success", "User deleted successfully!");


                    Stage stage = new Stage();
                    stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/LoginForm.fxml"))));
                    stage.setTitle("Login Form");
                    stage.centerOnScreen();
                    stage.show();

                    Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    disposeStage.close();
                } else {
                    showAlert("Failed", "Failed to delete user.");
                }
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void btnShowPasswordOnAction(ActionEvent actionEvent) {
        if (pwdPassword.isVisible()) {
            pwdPassword.setVisible(false);
            txtPassword.setVisible(true);
            btnShowPassword.setText("Hide");
        } else {
            pwdPassword.setVisible(true);
            txtPassword.setVisible(false);
            btnShowPassword.setText("Show");
        }
    }

    public void btnShowConfirmPasswordOnAction(ActionEvent actionEvent) {
        if (pwdConfirmPassword.isVisible()) {
            pwdConfirmPassword.setVisible(false);
            txtConfirmPassword.setVisible(true);
            btnShowConfirmPassword.setText("Hide");
        } else {
            pwdConfirmPassword.setVisible(true);
            txtConfirmPassword.setVisible(false);
            btnShowConfirmPassword.setText("Show");
        }
    }

    private boolean isMatchPasswords(){
        return pwdPassword.getText().equals(pwdConfirmPassword.getText());
    }

    private boolean isValidEmail(){
        String email = txtEmail.getText();
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return email.matches(emailPattern);
    }

    private boolean isEmpty() {
        return txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || pwdPassword.getText().isEmpty() ||
                pwdConfirmPassword.getText().isEmpty();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm(){
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtEmail.setStyle(null);
        pwdConfirmPassword.setStyle(null);
        lblInvalidEmail.setVisible(false);
        lblMismatchPassword1.setVisible(false);
        lblMismatchPassword2.setVisible(false);
    }

}
