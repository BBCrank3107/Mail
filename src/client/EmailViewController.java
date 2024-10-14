package client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.*;
import java.net.*;

public class EmailViewController {
    @FXML
    private ListView<String> emailListView;
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;
    @FXML
    private Button sendButton;

    private String username;
    
    @FXML
    private TextArea emailContentArea;

    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private final int SERVER_PORT = 12345;

    public void initialize() throws Exception {
        clientSocket = new DatagramSocket();
        serverAddress = InetAddress.getByName("192.168.1.18");
    }

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText("Logged in as: " + username);
        loadEmailList();
    }
    
    private void loadEmailList() {
        try {
            String request = "GET_EMAILS " + username;
            byte[] sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            updateEmailList(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateEmailList(String emailList) {
        ObservableList<String> items = FXCollections.observableArrayList();

        for (String email : emailList.split(" ")) {
            String emailName = email.endsWith(".txt") ? email.substring(0, email.length() - 4) : email;
            items.add(emailName);
        }

        emailListView.setItems(items);
    }

    @FXML
    private void handleComposeEmail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ComposeMail.fxml"));
            Parent root = loader.load();

            ComposeMailController composeMailController = loader.getController();
            composeMailController.setUsername(username);

            Stage stage = new Stage();
            stage.setTitle("Compose Mail");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSend() {
        String address = addressField.getText();
        String title = titleField.getText();
        String content = contentArea.getText();

        if (address.isEmpty() || title.isEmpty() || content.isEmpty()) {
            System.out.println("Please fill in all fields.");
            return;
        }
    }
    
    @FXML
    private void handleSeenEmail() {
        String selectedEmail = emailListView.getSelectionModel().getSelectedItem();

        if (selectedEmail == null) {
            showAlert(Alert.AlertType.WARNING, "No Email Selected", "Please select an email to view.");
            return;
        }

        try {
            String request = "READ_EMAIL " + username + " " + selectedEmail;
            byte[] sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

            emailContentArea.setText(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
