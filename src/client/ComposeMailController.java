package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ComposeMailController {
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentField;

    private String username;
    private final int SERVER_PORT = 12345;

    // Thiết lập username
    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText("Logged in as: " + username);
    }
    
    @FXML
    private void handleSend() {
        String address = addressField.getText();
        String title = titleField.getText();
        String content = contentField.getText();

        if (!address.isEmpty() && !title.isEmpty() && !content.isEmpty()) {
            sendEmailToServer(username, address, title, content);
            
            saveEmailToFile(username, address, title, content);

            Stage stage = (Stage) addressField.getScene().getWindow();
            stage.close();
        } else {
            System.out.println("Please fill in all fields.");
        }
    }

    private void sendEmailToServer(String username, String address, String title, String content) {
        try {
            DatagramSocket socket = new DatagramSocket();
            
            String message = "SEND_EMAIL " + username + ";" + address + ";" + title + ";" + content;
            byte[] buffer = message.getBytes();

            InetAddress serverAddress = InetAddress.getByName("192.168.1.18");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
            socket.send(packet);
            socket.close();

            System.out.println("Email sent successfully to " + address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveEmailToFile(String username, String address, String title, String content) {
        File userDir = new File("mails/" + address);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        File emailFile = new File(userDir, title + ".txt");
        try (FileWriter writer = new FileWriter(emailFile)) {
            writer.write("From: " + username + "\n");
            writer.write("To: " + address + "\n");
            writer.write("Title: " + title + "\n");
            writer.write("Content: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
