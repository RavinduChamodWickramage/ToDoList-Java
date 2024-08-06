package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class RegisterController {
    public TextField txtFirstName;
    public TextField txtLastName;
    public TextField txtEmail;
    public PasswordField pwdPassword;
    public PasswordField pwdConfirmPassword;
    public TextField txtPassword;
    public TextField txtConfirmPassword;
    public Button btnShowPassword;
    public Button btnShowConfirmPassword;
    public Label lblInvalidEmail;
    public Label lblMismatchPassword1;
    public Label lblMismatchPassword2;
    public Label lblUserID;

    public void initialize() {
        Connection connection = DBConnection.getInstance().getConnection();

        autoGenerateID();
        initializeFields();
        addListeners();
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

    public void btnRegisterOnAction(ActionEvent actionEvent) throws IOException {
        if (isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        if (!isValidEmail()) {
            txtEmail.setStyle("-fx-border-color: red;");
            txtEmail.requestFocus();
            lblInvalidEmail.setVisible(true);
            return;
        }

        if (!isMatchPasswords()) {
            pwdConfirmPassword.setStyle("-fx-border-color: red;");
            pwdConfirmPassword.requestFocus();
            lblMismatchPassword1.setVisible(true);
            lblMismatchPassword2.setVisible(true);
            return;
        }

        register();

        Stage stage = new Stage();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/LoginForm.fxml"))));
        stage.setTitle("Login Form");
        stage.centerOnScreen();
        stage.show();

        Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        disposeStage.close();

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

    public void autoGenerateID() {
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT id FROM user ORDER BY id DESC LIMIT 1");

            boolean isExist = resultSet.next();

            if(isExist){
                String lastId = resultSet.getString("id");
                int noOfUsers = Integer.parseInt(lastId.substring(1));
                noOfUsers++;
                String newId = String.format("U%03d", noOfUsers);
                lblUserID.setText(newId);
            }else{
                lblUserID.setText("U001");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {
        Connection connection = DBConnection.getInstance().getConnection();

        String id = lblUserID.getText();
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String email = txtEmail.getText();
        String password = pwdPassword.getText();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO user (id, firstName, lastName, email, password) VALUES (?, ?, ?, ?, ?)");

            preparedStatement.setObject(1, id);
            preparedStatement.setObject(2,firstName);
            preparedStatement.setObject(3,lastName);
            preparedStatement.setObject(4,email);
            preparedStatement.setObject(5,password);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected != 0) {
                clearForm();
                showAlert("Success", "User registered successfully.");
            } else {
                showAlert("Error", "User registration failed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
