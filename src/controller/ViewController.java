package controller;

import db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

import static controller.LoginController.userFullName;
import static controller.LoginController.userID;

public class ViewController {
    public Label lblWelcome;
    public Label lblUser;
    public TextField txtSelectedToDO;
    public TextField txtAddToDo;
    public TableView<ObservableList<String>> tblToDo;
    public TableColumn<ObservableList<String>, String> colId;
    public TableColumn<ObservableList<String>, String> colToDo;
    public Pane paneAddTask;

    public void initialize() {
        Connection connection = DBConnection.getInstance().getConnection();
        setUsernameAndId();
        operateAddTask();
        setupTableColumns();
        populateTable();
        addTableSelectionListener();
    }

    private void setUsernameAndId() {
        lblWelcome.setText("Welcome " + userFullName + " to TO-DO LIST");
        lblUser.setText(userID);
    }

    private void operateAddTask() {
        paneAddTask.setVisible(false);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        colToDo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
    }

    private void addTableSelectionListener() {
        tblToDo.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() > 1) {
                ObservableList<String> selectedItem = tblToDo.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    txtSelectedToDO.setText(selectedItem.get(1));
                }
            }
        });
    }

    public void btnUpdateOnAction(ActionEvent actionEvent) {
        Connection connection = DBConnection.getInstance().getConnection();

        ObservableList<String> selectedItem = tblToDo.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No to-do item selected for update.");
            return;
        }

        String id = selectedItem.get(0);
        String newDescription = txtSelectedToDO.getText();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to update..?", ButtonType.YES,ButtonType.NO);

        Optional<ButtonType> buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)){
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE todo SET description = ? WHERE id = ?");
                preparedStatement.setString(1, newDescription);
                preparedStatement.setString(2, id);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected != 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Todo item updated successfully!");
                    populateTable();
                    txtSelectedToDO.setText("");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to update todo item.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
        Connection connection = DBConnection.getInstance().getConnection();

        ObservableList<String> selectedItem = tblToDo.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "No to-do item selected for deletion.");
            return;
        }

        String id = selectedItem.get(0);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to update..?", ButtonType.YES,ButtonType.NO);

        Optional<ButtonType> buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)){
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM todo WHERE id = ?");
                preparedStatement.setString(1, id);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected != 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Todo item deleted successfully!");
                    populateTable();
                    txtSelectedToDO.setText("");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Failed to delete todo item.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void btnAddNewOnAction(ActionEvent actionEvent) {
        paneAddTask.setVisible(true);
    }

    public void btnAddToListOnAction(ActionEvent actionEvent) {
        Connection connection = DBConnection.getInstance().getConnection();

        String id = autoGenerateID();
        String userId = userID;
        String description = txtAddToDo.getText();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO todo (id, userId, description) VALUES (?, ?, ?)");

            preparedStatement.setObject(1, id);
            preparedStatement.setObject(2,userId);
            preparedStatement.setObject(3,description);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected != 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Todo item added successfully!");
                paneAddTask.setVisible(false);
                populateTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Failed", "Failed to add todo item.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnLogOutOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to logout..?", ButtonType.YES,ButtonType.NO);

        Optional<ButtonType> buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)){
            try {
                Stage stage = new Stage();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("../view/LoginForm.fxml"))));
                stage.setTitle("Login Form");
                stage.centerOnScreen();
                stage.show();

                Stage disposeStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                disposeStage.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public String autoGenerateID() {
        Connection connection = DBConnection.getInstance().getConnection();

        String newTodoId = null;

        try {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT id FROM todo ORDER BY id DESC LIMIT 1");

            boolean isExist = resultSet.next();

            if(isExist){
                String lastId = resultSet.getString("id");
                int noOfTodos = Integer.parseInt(lastId.substring(2));
                noOfTodos++;
                newTodoId = String.format("TD%03d", noOfTodos);
            }else{
                newTodoId = "TD001";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return newTodoId;
    }

    public void populateTable() {
        Connection connection = DBConnection.getInstance().getConnection();
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, description FROM todo WHERE userId = ?");
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(resultSet.getString("id"));
                row.add(resultSet.getString("description"));
                data.add(row);
            }

            tblToDo.setItems(data);

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
