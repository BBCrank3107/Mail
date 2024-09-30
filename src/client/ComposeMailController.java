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
    private Label usernameLabel; // Label để hiển thị username
    @FXML
    private TextField addressField; // TextField để nhập địa chỉ người nhận
    @FXML
    private TextField titleField; // TextField để nhập tiêu đề email
    @FXML
    private TextArea contentField; // TextArea để nhập nội dung email

    private String username; // Biến để lưu username
    private final int SERVER_PORT = 12345; // Port của server

    // Thiết lập username
    public void setUsername(String username) {
        this.username = username; // Gán giá trị username cho biến instance
        usernameLabel.setText("Logged in as: " + username);
    }

    // Xử lý sự kiện khi người dùng nhấn nút "Send"
    @FXML
    private void handleSend() {
        String address = addressField.getText();
        String title = titleField.getText();
        String content = contentField.getText();

        if (!address.isEmpty() && !title.isEmpty() && !content.isEmpty()) {
            // Gửi email đến server
            sendEmailToServer(username, address, title, content);
            
            // Lưu email thành file trong thư mục người dùng
            saveEmailToFile(username, address, title, content);
            
            // Đóng cửa sổ Compose sau khi gửi thành công
            Stage stage = (Stage) addressField.getScene().getWindow();
            stage.close();
        } else {
            // Thông báo nếu thông tin không đầy đủ
            System.out.println("Please fill in all fields.");
        }
    }

    // Gửi email đến server
    private void sendEmailToServer(String username, String address, String title, String content) {
        try {
            DatagramSocket socket = new DatagramSocket();
            
            // Tạo thông điệp với định dạng SEND_EMAIL
            String message = "SEND_EMAIL " + username + ";" + address + ";" + title + ";" + content;
            byte[] buffer = message.getBytes();

            // Gửi gói dữ liệu đến server
            InetAddress serverAddress = InetAddress.getByName("192.168.1.18"); // Địa chỉ IP của server
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, SERVER_PORT);
            socket.send(packet);
            socket.close();

            System.out.println("Email sent successfully to " + address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lưu email thành file trong thư mục của người dùng
    private void saveEmailToFile(String username, String address, String title, String content) {
        // Đường dẫn lưu email vào thư mục người dùng
        File userDir = new File("mails/" + address); // Sử dụng username để lưu email
        if (!userDir.exists()) {
            userDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
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
