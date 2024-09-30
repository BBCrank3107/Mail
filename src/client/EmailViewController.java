package client;

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

    private DatagramSocket clientSocket;
    private InetAddress serverAddress; // Giữ nguyên biến này để có thể thiết lập lại
    private final int SERVER_PORT = 9876;

    public void initialize() throws Exception {
        clientSocket = new DatagramSocket();
        serverAddress = InetAddress.getByName("192.168.1.18"); // Thay đổi địa chỉ server ở đây
    }

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText("Logged in as: " + username);
    }

    private void loadEmails() throws IOException {
        String message = "GET_EMAILS " + username;
        byte[] sendData = message.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
        clientSocket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

        if ("NO_EMAILS".equals(response)) {
            showAlert(Alert.AlertType.INFORMATION, "Inbox", "No emails found.");
        } else {
            String[] emails = response.split(" ");
            emailListView.getItems().addAll(emails);
        }
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

        sendEmailToServer(username, address, title, content);
    }
    
    private void sendEmailToServer(String username, String address, String title, String content) {
        try {
            DatagramSocket socket = new DatagramSocket();
            String message = username + ";" + address + ";" + title + ";" + content;
            byte[] buffer = message.getBytes();

            // Cập nhật địa chỉ server nếu cần
            InetAddress serverAddress = InetAddress.getByName("192.168.1.18"); // Địa chỉ server
            int port = 12345; // Cổng server

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);
            socket.send(packet);
            socket.close();

            System.out.println("Email sent successfully.");
        } catch (Exception e) {
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
